// 天气查询插件 - 示例实现
// 注意：QuickJS 不支持 async/await，所有函数必须是同步的

function get_weather(params) {
    var city = params.city;
    
    // 模拟返回数据（同步实现）
    var mockData = {
        success: true,
        city: city,
        temperature: Math.floor(Math.random() * 15) + 15, // 15-30 度
        weather: ["晴天", "多云", "阴天", "小雨"][Math.floor(Math.random() * 4)],
        humidity: Math.floor(Math.random() * 40) + 40, // 40-80%
        windSpeed: Math.floor(Math.random() * 10) + 1, // 1-10 km/h
        updateTime: new Date().toISOString()
    };
    
    return mockData;
}

function get_forecast(params) {
    var city = params.city;
    var days = params.days || 3;
    
    // 模拟未来几天天气预报
    var forecast = [];
    var today = new Date();
    
    for (var i = 0; i < days; i++) {
        var date = new Date(today);
        date.setDate(today.getDate() + i + 1);
        
        forecast.push({
            date: date.toISOString().split('T')[0],
            temperatureHigh: Math.floor(Math.random() * 10) + 20,
            temperatureLow: Math.floor(Math.random() * 10) + 10,
            weather: ["晴天", "多云", "阴天", "小雨", "雷阵雨"][Math.floor(Math.random() * 5)]
        });
    }
    
    return {
        success: true,
        city: city,
        forecast: forecast
    };
}

// 导出工具函数
exports.get_weather = get_weather;
exports.get_forecast = get_forecast;