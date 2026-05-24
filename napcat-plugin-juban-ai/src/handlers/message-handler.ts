/**
 * 消息处理器
 */

import type { PluginContext, OB11Message, OB11PostSendMsg } from '../types/napcat';
import { EventType } from '../types/napcat';
import { getConfig, isGroupAllowed, isUserBlocked } from '../config';
import { jubanApi, reinitializeJubanApi } from '../services/juban-api';

// 冷却管理
const cooldownMap = new Map<string, number>();

/**
 * 检查冷却时间
 */
function checkCooldown(userId: string): boolean {
    const config = getConfig();
    if (config.cooldown <= 0) return true;

    const now = Date.now();
    const lastTime = cooldownMap.get(userId) || 0;
    const cooldownMs = config.cooldown * 1000;

    if (now - lastTime < cooldownMs) {
        return false;
    }

    cooldownMap.set(userId, now);
    return true;
}

/**
 * 清理过期的冷却记录（每10分钟清理一次）
 */
function cleanupCooldown(): void {
    const now = Date.now();
    const maxAge = 10 * 60 * 1000; // 10分钟

    for (const [userId, timestamp] of cooldownMap.entries()) {
        if (now - timestamp > maxAge) {
            cooldownMap.delete(userId);
        }
    }
}

// 每10分钟清理一次
setInterval(cleanupCooldown, 10 * 60 * 1000);

/**
 * 提取消息中的@信息
 */
function extractAtInfo(message: OB11Message): { isAtBot: boolean; cleanMessage: string } {
    const config = getConfig();
    let cleanMessage = message.raw_message || '';
    let isAtBot = false;

    // 检查消息中的@段
    if (message.message && Array.isArray(message.message)) {
        const segments = message.message;
        const atSegments = segments.filter(
            (seg: any) => seg.type === 'at'
        );

        // 检查是否@了机器人
        for (const seg of atSegments) {
            if (seg.data && String(seg.data.qq) === String(message.self_id)) {
                isAtBot = true;
                // 从消息中移除@部分
                cleanMessage = cleanMessage.replace(`[CQ:at,qq=${seg.data.qq}]`, '').trim();
            }
        }
    }

    return { isAtBot, cleanMessage };
}

/**
 * 检查是否触发唤醒前缀
 */
function checkTriggerPrefix(message: string): { triggered: boolean; cleanMessage: string } {
    const config = getConfig();
    const prefix = config.triggerPrefix;

    // 如果没有设置前缀，所有消息都触发
    if (!prefix) {
        return { triggered: true, cleanMessage: message };
    }

    // 检查前缀
    if (message.startsWith(prefix)) {
        return {
            triggered: true,
            cleanMessage: message.slice(prefix.length).trim()
        };
    }

    return { triggered: false, cleanMessage: message };
}

/**
 * 处理快捷命令
 */
async function handleCommand(
    ctx: PluginContext,
    message: string,
    event: OB11Message
): Promise<boolean> {
    const trimmedMessage = message.trim().toLowerCase();

    // ping命令
    if (trimmedMessage === 'ping') {
        await sendReply(ctx, event, 'pong 🍊', false);
        return true;
    }

    // 状态命令
    if (trimmedMessage === '/status') {
        const { jubanApi } = await import('../services/juban-api');
        const health = await jubanApi.checkHealth();
        await sendReply(ctx, event, health.message, false);
        return true;
    }

    // 帮助命令
    if (trimmedMessage === '/help') {
        const helpText = `🍊 橘瓣AI使用说明：

• 直接发送消息即可与AI对话
• @机器人 也可以触发回复
• 可用命令：
  - ping: 测试连接
  - /status: 检查橘瓣连接状态
  - /help: 显示本帮助

配置项：
• 唤醒前缀: ${getConfig().triggerPrefix || '无（所有消息）'}
• @触发: ${getConfig().atTrigger ? '开启' : '关闭'}
• 冷却时间: ${getConfig().cooldown}秒`;
        await sendReply(ctx, event, helpText, false);
        return true;
    }

    return false;
}

/**
 * 发送回复消息
 */
async function sendReply(
    ctx: PluginContext,
    event: OB11Message,
    message: string,
    quote: boolean = true
): Promise<void> {
    const config = getConfig();
    const replyPrefix = config.replyPrefix;

    // 添加回复前缀
    const finalMessage = replyPrefix ? `${replyPrefix} ${message}` : message;

    const isGroup = event.message_type === 'group';
    const params: OB11PostSendMsg = {
        message: finalMessage,
        message_type: event.message_type,
        ...(isGroup
            ? { group_id: String(event.group_id) }
            : { user_id: String(event.user_id) }
        )
    };

    // 添加引用回复
    if (quote && config.replyQuote && event.message_id) {
        params.message = `[CQ:reply,id=${event.message_id}]${params.message}`;
    }

    try {
        await ctx.actions.call(
            'send_msg',
            params,
            ctx.adapterName,
            ctx.pluginManager.config
        );
    } catch (error) {
        ctx.logger.error('发送消息失败:', error);
    }
}

/**
 * 主消息处理函数
 */
export async function handleMessage(
    ctx: PluginContext,
    event: OB11Message
): Promise<void> {
    const config = getConfig();

    // 检查插件是否启用
    if (!config.enabled) {
        return;
    }

    // 只处理消息事件
    if (event.post_type !== EventType.MESSAGE) {
        return;
    }

    // 过滤自身消息
    if (String(event.user_id) === String(event.self_id)) {
        return;
    }

    const userId = String(event.user_id);
    const groupId = event.group_id ? String(event.group_id) : undefined;

    // 检查黑名单
    if (isUserBlocked(userId)) {
        return;
    }

    // 检查群白名单
    if (groupId && !isGroupAllowed(groupId)) {
        return;
    }

    // 提取@信息和清理消息
    const { isAtBot, cleanMessage: atCleanedMessage } = extractAtInfo(event);

    // 检查是否@触发
    const shouldTriggerByAt = config.atTrigger && isAtBot;

    // 检查唤醒前缀
    const { triggered: shouldTriggerByPrefix, cleanMessage: prefixCleanedMessage } =
        checkTriggerPrefix(atCleanedMessage);

    // 如果没有触发条件，忽略消息
    if (!shouldTriggerByAt && !shouldTriggerByPrefix) {
        return;
    }

    // 使用清理后的消息
    const finalMessage = shouldTriggerByPrefix ? prefixCleanedMessage : atCleanedMessage;

    // 检查冷却
    if (!checkCooldown(userId)) {
        return; // 静默忽略冷却中的消息
    }

    // 处理快捷命令
    const isCommand = await handleCommand(ctx, finalMessage, event);
    if (isCommand) {
        return;
    }

    // 调用橘瓣AI
    try {
        const response = await jubanApi.chat({
            message: finalMessage,
            user_id: userId,
            user_name: event.sender?.nickname || event.sender?.card || '未知用户',
            group_id: groupId,
            group_name: groupId ? `群${groupId}` : undefined,
            message_type: event.message_type as 'group' | 'private'
        });

        if (response.success) {
            // 分段发送长消息
            const reply = response.reply;
            const maxLength = 3000; // QQ单条消息限制

            if (reply.length <= maxLength) {
                await sendReply(ctx, event, reply, true);
            } else {
                // 分段发送
                const segments = splitMessage(reply, maxLength);
                for (let i = 0; i < segments.length; i++) {
                    const segment = segments[i];
                    const prefix = i === 0 ? '' : `[续${i + 1}/${segments.length}] `;
                    await sendReply(ctx, event, prefix + segment, i === 0);
                }
            }
        } else {
            await sendReply(ctx, event, response.error, false);
        }
    } catch (error) {
        ctx.logger.error('调用橘瓣API失败:', error);
        await sendReply(ctx, event, '🍊 橘瓣离线中，请稍后再试~', false);
    }
}

/**
 * 分段消息（按句子分割，尽量保持完整语义）
 */
function splitMessage(message: string, maxLength: number): string[] {
    const segments: string[] = [];
    let currentSegment = '';

    // 按段落分割
    const paragraphs = message.split('\n');

    for (const paragraph of paragraphs) {
        // 如果当前段落本身就超过限制，需要进一步分割
        if (paragraph.length > maxLength) {
            // 先把之前的段落保存
            if (currentSegment.length > 0) {
                segments.push(currentSegment.trim());
                currentSegment = '';
            }

            // 按句子分割长段落
            const sentences = paragraph.split(/([。！？.!?])/);
            let tempSegment = '';

            for (let i = 0; i < sentences.length; i++) {
                const sentence = sentences[i];
                // 奇数索引是标点符号，需要加到前面的句子上
                if (i % 2 === 1 && tempSegment.length > 0) {
                    tempSegment += sentence;
                } else {
                    tempSegment += sentence;
                }

                if (tempSegment.length >= maxLength * 0.8) { // 达到80%就分割
                    segments.push(tempSegment.trim());
                    tempSegment = '';
                }
            }

            if (tempSegment.length > 0) {
                currentSegment = tempSegment;
            }
        } else {
            // 正常段落
            if (currentSegment.length + paragraph.length + 1 > maxLength) {
                segments.push(currentSegment.trim());
                currentSegment = paragraph;
            } else {
                currentSegment += (currentSegment.length > 0 ? '\n' : '') + paragraph;
            }
        }
    }

    // 添加最后一段
    if (currentSegment.length > 0) {
        segments.push(currentSegment.trim());
    }

    return segments;
}

/**
 * 配置变更时重新初始化
 */
export function onConfigChange(): void {
    reinitializeJubanApi();
}