var CITY_MAP = {
    "北京": "101010100", "上海": "101020100", "广州": "101280101",
    "深圳": "101280601", "福州": "101230101", "杭州": "101210101",
    "成都": "101270101", "武汉": "101200101", "南京": "101190101",
    "重庆": "101040100", "天津": "101030100", "西安": "101110101",
    "长沙": "101250101", "郑州": "101180101", "厦门": "101230201",
    "合肥": "101220101", "济南": "101120101", "沈阳": "101070101",
    "哈尔滨": "101050101", "长春": "101060101", "昆明": "101290101",
    "贵阳": "101260101", "南宁": "101300101", "海口": "101310101",
    "石家庄": "101090101", "太原": "101100101", "兰州": "101160101",
    "南昌": "101240101", "苏州": "101190401", "无锡": "101190301",
    "宁波": "101210401", "大连": "101070201", "青岛": "101120201",
    "东莞": "101281601", "佛山": "101280800", "珠海": "101280701",
    "泉州": "101230501", "烟台": "101120501", "温州": "101210701"
};

function getCityId(cityName) {
    for (var name in CITY_MAP) {
        if (cityName.indexOf(name) !== -1 || name.indexOf(cityName) !== -1) {
            return CITY_MAP[name];
        }
    }
    return null;
}

async function get_weather(params) {
    var city = params.city;
    var cityId = getCityId(city);
    if (!cityId) {
        return { success: false, error: "城市未找到，请输入更完整的城市名" };
    }
    try {
        var resp = await fetch("http://t.weather.sojson.com/api/weather/city/" + cityId);
        var data = await resp.json();
        if (data.status !== 200) {
            return { success: false, error: "查询失败: " + data.message };
        }
        var info = data.cityInfo;
        var d = data.data;
        var today = d.forecast[0];
        return {
            success: true,
            city: info.city,
            temperature: d.wendu + "°C",
            humidity: d.shidu,
            weather: today.type,
            wind: today.fx + " " + today.fl,
            high: today.high,
            low: today.low,
            quality: d.quality,
            sunrise: today.sunrise,
            sunset: today.sunset,
            ganmao: d.ganmao
        };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

async function get_forecast(params) {
    var city = params.city;
    var days = Math.min(params.days || 3, 5);
    var cityId = getCityId(city);
    if (!cityId) {
        return { success: false, error: "城市未找到，请输入更完整的城市名" };
    }
    try {
        var resp = await fetch("http://t.weather.sojson.com/api/weather/city/" + cityId);
        var data = await resp.json();
        if (data.status !== 200) {
            return { success: false, error: "查询失败: " + data.message };
        }
        var info = data.cityInfo;
        var forecast = [];
        for (var i = 0; i < days && i < data.data.forecast.length; i++) {
            var day = data.data.forecast[i];
            forecast.push({
                date: day.ymd,
                week: day.week,
                weather: day.type,
                high: day.high,
                low: day.low,
                wind: day.fx + " " + day.fl,
                sunrise: day.sunrise,
                sunset: day.sunset
            });
        }
        return { success: true, city: info.city, forecast: forecast };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

exports.get_weather = get_weather;
exports.get_forecast = get_forecast;
