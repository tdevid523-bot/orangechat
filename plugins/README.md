# 橘瓣插件系统

## 简介

橘瓣插件系统允许用户通过安装 `.op` 格式的插件包来扩展 AI 的功能。插件使用 JavaScript 编写，通过 QuickJS 引擎在沙箱中执行。

## 插件包格式

一个插件包是一个 ZIP 压缩包，后缀为 `.op`，内部结构如下：

```
weather.op (ZIP)
├── manifest.json     # 插件元数据 + 工具声明（必需）
└── main.js           # 工具实现脚本（必需）
```

### manifest.json

```json
{
  "id": "com.orangechat.plugin.weather",
  "name": "天气查询",
  "description": "查询城市天气和天气预报",
  "version": "1.0.0",
  "author": "小橘",
  "icon": "☁️",
  "entry": "main.js",
  "tools": [
    {
      "name": "get_weather",
      "description": "查询指定城市的当前天气",
      "parameters": [
        {"name": "city", "type": "string", "required": true, "description": "城市名"}
      ]
    },
    {
      "name": "get_forecast", 
      "description": "查询指定城市未来几天的天气预报",
      "parameters": [
        {"name": "city", "type": "string", "required": true, "description": "城市名"},
        {"name": "days", "type": "integer", "required": false, "description": "预报天数"}
      ]
    }
  ]
}
```

### main.js

```javascript
async function get_weather(params) {
  const resp = await fetch("https://api.example.com/weather?city=" + params.city);
  const data = await resp.json();
  return { success: true, temperature: data.temp, weather: data.desc };
}

async function get_forecast(params) {
  const days = params.days || 3;
  const resp = await fetch("https://api.example.com/forecast?city=" + params.city + "&days=" + days);
  const data = await resp.json();
  return { success: true, forecast: data.list };
}

exports.get_weather = get_weather;
exports.get_forecast = get_forecast;
```

## 可用的 JavaScript API

### fetch(url, options)

发起 HTTP 请求。

```javascript
const response = await fetch("https://api.example.com/data", {
  method: "GET",
  headers: {
    "Content-Type": "application/json"
  }
});
const data = await response.json();
```

### console.log(...args)

输出日志到 Android Logcat。

```javascript
console.log("Debug message", someVariable);
```

## 插件索引

插件索引文件 `index.json` 放在独立的 GitHub 仓库中，橘瓣 App 会从该仓库获取可用插件列表。

```json
[
  {
    "id": "com.orangechat.plugin.weather",
    "name": "天气查询",
    "description": "查询城市天气和天气预报",
    "version": "1.0.0",
    "author": "小橘",
    "icon": "☁️",
    "downloadUrl": "https://raw.githubusercontent.com/xxx/orangechat-plugins/main/weather/weather.op",
    "size": 2048
  }
]
```

## 开发插件

1. 创建插件目录，包含 `manifest.json` 和 `main.js`
2. 使用 `zip` 命令打包：`zip -r weather.op manifest.json main.js`
3. 将插件包上传到 GitHub Release 或其他 CDN
4. 更新 `index.json` 添加插件信息

## 注意事项

- 插件在沙箱中执行，无法访问 Android 系统 API
- 网络请求通过 App 的 HTTP 客户端转发
- 插件工具名在全局命名空间中唯一，避免与其他插件冲突
- 建议插件 ID 使用反向域名格式，如 `com.yourname.plugin.xxx`