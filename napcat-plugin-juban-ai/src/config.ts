/**
 * 橘瓣AI插件配置
 */

export interface JubanConfig {
    /** 橘瓣App API地址 */
    jubanApiUrl: string;
    /** 橘瓣API Token（空=不验证） */
    apiToken: string;
    /** 是否启用 */
    enabled: boolean;
    /** 唤醒前缀 */
    triggerPrefix: string;
    /** @触发 */
    atTrigger: boolean;
    /** 回复前缀 */
    replyPrefix: string;
    /** 引用回复 */
    replyQuote: boolean;
    /** 白名单群号（逗号分隔） */
    allowedGroups: string;
    /** 黑名单用户（逗号分隔） */
    blockedUsers: string;
    /** 超时秒数 */
    timeout: number;
    /** 冷却秒数 */
    cooldown: number;
}

// 默认配置
export const defaultConfig: JubanConfig = {
    jubanApiUrl: 'http://192.168.1.100:8080',
    apiToken: '',
    enabled: true,
    triggerPrefix: '',
    atTrigger: true,
    replyPrefix: '',
    replyQuote: true,
    allowedGroups: '',
    blockedUsers: '',
    timeout: 30,
    cooldown: 3
};

// 配置项定义（用于NapCat配置界面）
export const configDefinition = {
    jubanApiUrl: {
        type: 'string',
        default: defaultConfig.jubanApiUrl,
        description: '橘瓣App API地址（包含端口）'
    },
    apiToken: {
        type: 'string',
        default: defaultConfig.apiToken,
        description: '橘瓣API Token（可选）'
    },
    enabled: {
        type: 'boolean',
        default: defaultConfig.enabled,
        description: '是否启用插件'
    },
    triggerPrefix: {
        type: 'string',
        default: defaultConfig.triggerPrefix,
        description: '唤醒前缀（如"/"或"!"，空=所有消息）'
    },
    atTrigger: {
        type: 'boolean',
        default: defaultConfig.atTrigger,
        description: '@机器人时触发'
    },
    replyPrefix: {
        type: 'string',
        default: defaultConfig.replyPrefix,
        description: '回复前缀（如"🍊"）'
    },
    replyQuote: {
        type: 'boolean',
        default: defaultConfig.replyQuote,
        description: '是否引用回复原消息'
    },
    allowedGroups: {
        type: 'string',
        default: defaultConfig.allowedGroups,
        description: '白名单群号（逗号分隔，空=所有群）'
    },
    blockedUsers: {
        type: 'string',
        default: defaultConfig.blockedUsers,
        description: '黑名单用户QQ号（逗号分隔）'
    },
    timeout: {
        type: 'number',
        default: defaultConfig.timeout,
        description: 'AI响应超时秒数'
    },
    cooldown: {
        type: 'number',
        default: defaultConfig.cooldown,
        description: '冷却秒数（防止刷屏）'
    }
};

// 全局配置引用（由插件初始化时设置）
let globalConfig: JubanConfig = { ...defaultConfig };

export function setConfig(config: Partial<JubanConfig>): void {
    globalConfig = { ...globalConfig, ...config };
}

export function getConfig(): JubanConfig {
    return globalConfig;
}

// 解析群号列表
export function getAllowedGroups(): string[] {
    return globalConfig.allowedGroups
        .split(',')
        .map(s => s.trim())
        .filter(s => s.length > 0);
}

// 解析黑名单用户
export function getBlockedUsers(): string[] {
    return globalConfig.blockedUsers
        .split(',')
        .map(s => s.trim())
        .filter(s => s.length > 0);
}

// 检查群号是否在白名单中
export function isGroupAllowed(groupId: string): boolean {
    const allowed = getAllowedGroups();
    if (allowed.length === 0) return true;
    return allowed.includes(groupId);
}

// 检查用户是否在黑名单中
export function isUserBlocked(userId: string): boolean {
    return getBlockedUsers().includes(userId);
}