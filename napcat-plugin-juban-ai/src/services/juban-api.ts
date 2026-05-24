/**
 * 橘瓣API通信服务
 */

import { getConfig } from '../config';

export interface ChatRequest {
    message: string;
    user_id?: string;
    user_name?: string;
    group_id?: string;
    group_name?: string;
    message_type?: 'group' | 'private';
}

export interface ChatSuccessResponse {
    success: true;
    reply: string;
    model: string;
}

export interface ChatErrorResponse {
    success: false;
    error: string;
}

export type ChatResponse = ChatSuccessResponse | ChatErrorResponse;

/**
 * 与橘瓣App API通信
 */
export class JubanApiService {
    private baseUrl: string;
    private apiToken: string;
    private timeout: number;

    constructor() {
        const config = getConfig();
        this.baseUrl = config.jubanApiUrl.replace(/\/$/, ''); // 移除末尾斜杠
        this.apiToken = config.apiToken;
        this.timeout = config.timeout * 1000; // 转换为毫秒
    }

    /**
     * 发送聊天消息并获取AI回复
     */
    async chat(request: ChatRequest): Promise<ChatResponse> {
        const url = `${this.baseUrl}/api/chat`;
        
        const controller = new AbortController();
        const timeoutId = setTimeout(() => controller.abort(), this.timeout);

        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(this.apiToken ? { 'Authorization': `Bearer ${this.apiToken}` } : {})
                },
                body: JSON.stringify(request),
                signal: controller.signal
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                if (response.status === 408 || response.status === 504) {
                    return {
                        success: false,
                        error: '🍊 AI思考太久了，请重试~'
                    };
                }
                
                const errorText = await response.text();
                return {
                    success: false,
                    error: `🍊 橘瓣API错误 (${response.status}): ${errorText}`
                };
            }

            const data = await response.json() as ChatResponse;
            return data;
        } catch (error) {
            clearTimeout(timeoutId);
            
            if (error instanceof Error) {
                if (error.name === 'AbortError') {
                    return {
                        success: false,
                        error: '🍊 AI思考太久了，请重试~'
                    };
                }
                
                if (error.message.includes('fetch failed') || error.message.includes('ECONNREFUSED')) {
                    return {
                        success: false,
                        error: '🍊 橘瓣离线中，请稍后再试~'
                    };
                }
                
                return {
                    success: false,
                    error: `🍊 请求失败: ${error.message}`
                };
            }
            
            return {
                success: false,
                error: '🍊 未知错误，请检查橘瓣App是否在线'
            };
        }
    }

    /**
     * 检查橘瓣API连接状态
     */
    async checkHealth(): Promise<{ ok: boolean; message: string }> {
        try {
            // 发送一个简单的测试消息
            const response = await fetch(`${this.baseUrl}/api/chat`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...(this.apiToken ? { 'Authorization': `Bearer ${this.apiToken}` } : {})
                },
                body: JSON.stringify({ message: 'ping' }),
                signal: AbortSignal.timeout(10000)
            });

            if (response.ok) {
                return { ok: true, message: '🍊 橘瓣在线，连接正常！' };
            } else {
                return { ok: false, message: `🍊 橘瓣响应异常 (${response.status})` };
            }
        } catch (error) {
            if (error instanceof Error) {
                return { ok: false, message: `🍊 无法连接橘瓣: ${error.message}` };
            }
            return { ok: false, message: '🍊 无法连接橘瓣，请检查网络' };
        }
    }
}

// 单例实例
export const jubanApi = new JubanApiService();

/**
 * 重新初始化API服务（配置变更后调用）
 */
export function reinitializeJubanApi(): void {
    Object.assign(jubanApi, new JubanApiService());
}