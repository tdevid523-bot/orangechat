# 🍊 橘瓣AI聊天插件

通过橘瓣App的AI接口实现QQ智能聊天，支持完整记忆和工具调用。

## 功能特性

- 🤖 **AI对话** - 通过橘瓣App的HTTP API与AI聊天
- 💾 **用户记忆** - 每个QQ用户有独立的对话上下文
- 🔧 **快捷命令** - ping、/status、/help
- ⚡ **触发方式** - 支持前缀触发和@触发
- 🛡️ **黑白名单** - 支持群白名单和用户黑名单
- ⏱️ **冷却机制** - 防止刷屏
- 📤 **长消息分段** - 自动分段发送长回复

## 安装方法

### 1. 安装到NapCat

将插件文件夹复制到NapCat的插件目录：
```
napcat/plugins/napcat-plugin-juban-ai/
```

### 2. 构建插件

```bash
cd napcat-plugin-juban-ai
npm install
npm run build
```

### 3. 配置橘瓣App

在橘瓣App中：
1. 打开设置 → Web服务
2. 启用Web服务器
3. 设置端口（默认8080）
4. （可选）设置访问密码

## 配置说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| jubanApiUrl | string | http://192.168.1.100:8080 | 橘瓣App地址 |
| apiToken | string | "" | 橘瓣API Token（可选） |
| enabled | boolean | true | 是否启用 |
| triggerPrefix | string | "" | 唤醒前缀（如"/"） |
| atTrigger | boolean | true | @触发 |
| replyPrefix | string | "" | 回复前缀（如"🍊"） |
| replyQuote | boolean | true | 引用回复 |
| allowedGroups | string | "" | 白名单群号（逗号分隔） |
| blockedUsers | string | "" | 黑名单用户（逗号分隔） |
| timeout | number | 30 | 超时秒数 |
| cooldown | number | 3 | 冷却秒数 |

## 使用方法

### 基础对话

直接发送消息即可与AI对话：
```
你好，橘瓣！
```

### @触发

在群中@机器人：
```
@橘瓣 你好
```

### 快捷命令

- `ping` - 测试连接
- `/status` - 检查橘瓣连接状态
- `/help` - 显示帮助信息

## 项目结构

```
napcat-plugin-juban-ai/
├── src/
│   ├── index.ts              # 插件主入口
│   ├── config.ts             # 配置定义
│   ├── handlers/
│   │   └── message-handler.ts # 消息处理
│   ├── services/
│   │   └── juban-api.ts      # 橘瓣API通信
│   └── types/
│       └── napcat.d.ts       # NapCat类型定义
├── package.json
├── tsconfig.json
└── README.md
```

## 依赖

- NapCat >= 4.14.0
- 橘瓣App（已启动Web服务器）

## 许可证

MIT