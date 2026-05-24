// 天气查询插件 - 示例实现
// 注意：这是一个演示用的模拟实现，实际使用时需要接入真实的天气 API

async function get_weather(params) {
  const city = params.city;
  
  // 模拟 API 调用
  // 实际使用时，请替换为真实的天气 API，例如：
  // const resp = await fetch("https://api.example.com/weather?city=" + encodeURIComponent(city));
  // const data = await resp.json();
  
  // 模拟返回数据
  const mockData = {
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

async function get_forecast(params) {
  const city = params.city;
  const days = params.days || 3;
  
  // 模拟未来几天天气预报
  const forecast = [];
  const today = new Date();
  
  for (let i = 0; i < days; i++) {
    const date = new Date(today);
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