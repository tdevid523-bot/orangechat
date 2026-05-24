/**
 * 橘瓣AI插件 - 主入口
 * 
 * 通过橘瓣App的HTTP API实现QQ智能聊天
 */

import type { PluginModule, PluginContext, OB11Message } from './types/napcat';
import { EventType } from './types/napcat';
import { setConfig, configDefinition, JubanConfig } from './config';
import { handleMessage, onConfigChange } from './handlers/message-handler';

// 插件初始化
export const plugin_init: PluginModule['plugin_init'] = async (ctx: PluginContext) => {
    ctx.logger.log('🍊 橘瓣AI插件已初始化');
    ctx.logger.log(`配置项: ${Object.keys(configDefinition).join(', ')}`);
};

// 消息处理
export const plugin_onmessage: PluginModule['plugin_onmessage'] = async (
    ctx: PluginContext,
    event: OB11Message
) => {
    // 只处理消息事件
    if (event.post_type !== EventType.MESSAGE) {
        return;
    }

    try {
        await handleMessage(ctx, event);
    } catch (error) {
        ctx.logger.error('消息处理出错:', error);
    }
};

// 配置更新
export const plugin_onconfigchange: PluginModule['plugin_onconfigchange'] = async (
    ctx: PluginContext,
    config: Record<string, any>
) => {
    ctx.logger.log('🍊 配置已更新，重新加载...');
    
    // 更新配置
    setConfig(config as Partial<JubanConfig>);
    
    // 重新初始化服务
    onConfigChange();
    
    ctx.logger.log('✅ 配置重载完成');
};

// 插件卸载
export const plugin_cleanup: PluginModule['plugin_cleanup'] = (ctx: PluginContext) => {
    ctx.logger.log('🍊 橘瓣AI插件清理中...');
};

// 导出配置定义供NapCat使用
export { configDefinition };