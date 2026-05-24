/**
 * NapCat 类型声明文件
 * 由于napcat-types可能不存在，这里提供基本的类型定义
 */

// 插件上下文
export interface PluginContext {
    logger: {
        log: (...args: any[]) => void;
        info: (...args: any[]) => void;
        warn: (...args: any[]) => void;
        error: (...args: any[]) => void;
    };
    actions: {
        call: (action: string, params: any, adapterName: string, config: any) => Promise<any>;
    };
    adapterName: string;
    pluginManager: {
        config: any;
    };
}

// 插件模块接口
export interface PluginModule {
    plugin_init?: (ctx: PluginContext) => Promise<void>;
    plugin_onmessage?: (ctx: PluginContext, event: OB11Message) => Promise<void>;
    plugin_onconfigchange?: (ctx: PluginContext, config: Record<string, any>) => Promise<void>;
    plugin_cleanup?: (ctx: PluginContext) => void;
}

// OB11消息事件
export interface OB11Message {
    post_type: string;
    message_type: 'group' | 'private';
    sub_type?: string;
    message_id: number;
    user_id: number;
    group_id?: number;
    sender?: {
        user_id?: number;
        nickname?: string;
        card?: string;
        role?: string;
    };
    message?: any;
    raw_message?: string;
    self_id: number;
    time?: number;
}

// 发送消息参数
export interface OB11PostSendMsg {
    message: string;
    message_type: 'group' | 'private';
    user_id?: string;
    group_id?: string;
    auto_escape?: boolean;
}

// 事件类型
export enum EventType {
    MESSAGE = 'message',
    NOTICE = 'notice',
    REQUEST = 'request',
    META_EVENT = 'meta_event'
}