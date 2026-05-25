async function get_weather(params) {
    var city = params.city;
    try {
        var resp = await fetch("https://wttr.in/" + encodeURIComponent(city) + "?format=j1&lang=zh");
        if (!resp.ok) return { success: false, error: "API请求失败: " + resp.status };
        var data = await resp.json();
        var current = data.current_condition[0];
        var desc = current.lang_zh && current.lang_zh[0] ? current.lang_zh[0].value : current.weatherDesc[0].value;
        return {
            success: true,
            city: city,
            temperature: current.temp_C + "°C",
            feelsLike: current.FeelsLikeC + "°C",
            weather: desc,
            humidity: current.humidity + "%",
            windSpeed: current.windspeedKmph + " km/h",
            windDir: current.winddir16Point,
            visibility: current.visibility + " km",
            uvIndex: current.uvIndex,
            updateTime: current.observation_time
        };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

async function get_forecast(params) {
    var city = params.city;
    var days = Math.min(params.days || 3, 3);
    try {
        var resp = await fetch("https://wttr.in/" + encodeURIComponent(city) + "?format=j1&lang=zh");
        if (!resp.ok) return { success: false, error: "API请求失败: " + resp.status };
        var data = await resp.json();
        var forecast = [];
        for (var i = 0; i < days; i++) {
            var day = data.weather[i];
            if (!day) break;
            var h = day.hourly[4] || day.hourly[0];
            var desc = h && h.lang_zh && h.lang_zh[0] ? h.lang_zh[0].value : (h ? h.weatherDesc[0].value : "");
            var avgHum = Math.round(day.hourly.reduce(function(s, x) { return s + parseInt(x.humidity || "0"); }, 0) / day.hourly.length);
            forecast.push({
                date: day.date,
                temperatureHigh: day.maxtempC + "°C",
                temperatureLow: day.mintempC + "°C",
                weather: desc,
                avgHumidity: avgHum + "%",
                sunrise: day.astronomy && day.astronomy[0] ? day.astronomy[0].sunrise : "",
                sunset: day.astronomy && day.astronomy[0] ? day.astronomy[0].sunset : ""
            });
        }
        return { success: true, city: city, forecast: forecast };
    } catch (e) {
        return { success: false, error: e.message };
    }
}

exports.get_weather = get_weather;
exports.get_forecast = get_forecast;
