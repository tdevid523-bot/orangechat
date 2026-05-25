async function get_weather(params) {
    var city = params.city;
    var key = "92fc69699a1b46cb9566395c3b777b3d";
    try {
        var geoResp = await fetch("https://geoapi.qweather.com/v2/city/lookup?location=" + encodeURIComponent(city) + "&key=" + key);
        var geoData = await geoResp.json();
        if (geoData.code !== "200" || !geoData.location || !geoData.location[0]) {
            return { success: false, error: "城市未找到: " + city };
        }
        var loc = geoData.location[0];
        var locId = loc.id;
        var cityName = loc.name;

        var resp = await fetch("https://devapi.qweather.com/v7/weather/now?location=" + locId + "&key=" + key);
        var data = await resp.json();
        if (data.code !== "200") {
            return { success: false, error: "天气查询失败: " + data.code };
        }
        var now = data.now;
        return {
            success: true,
            city: cityName,
            temperature: now.temp + "°C",
            feelsLike: now.feelsLike + "°C",
            weather: now.text,
            windDir: now.windDir,
            windScale: now.windScale + "级",
            humidity: now.humidity + "%",
            pressure: now.pressure + "hPa",
            visibility: now.vis + "km",
            updateTime: data.updateTime
        };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

async function get_forecast(params) {
    var city = params.city;
    var days = Math.min(params.days || 3, 3);
    var key = "92fc69699a1b46cb9566395c3b777b3d";
    try {
        var geoResp = await fetch("https://geoapi.qweather.com/v2/city/lookup?location=" + encodeURIComponent(city) + "&key=" + key);
        var geoData = await geoResp.json();
        if (geoData.code !== "200" || !geoData.location || !geoData.location[0]) {
            return { success: false, error: "城市未找到: " + city };
        }
        var loc = geoData.location[0];
        var locId = loc.id;
        var cityName = loc.name;

        var resp = await fetch("https://devapi.qweather.com/v7/weather/3d?location=" + locId + "&key=" + key);
        var data = await resp.json();
        if (data.code !== "200") {
            return { success: false, error: "预报查询失败: " + data.code };
        }
        var forecast = [];
        var daily = data.daily;
        for (var i = 0; i < days && i < daily.length; i++) {
            var d = daily[i];
            forecast.push({
                date: d.fxDate,
                temperatureHigh: d.tempMax + "°C",
                temperatureLow: d.tempMin + "°C",
                weatherDay: d.textDay,
                weatherNight: d.textNight,
                windDirDay: d.windDirDay,
                windScaleDay: d.windScaleDay + "级",
                humidity: d.humidity + "%",
                uvIndex: d.uvIndex,
                sunrise: d.sunrise,
                sunset: d.sunset
            });
        }
        return { success: true, city: cityName, forecast: forecast };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

exports.get_weather = get_weather;
exports.get_forecast = get_forecast;
