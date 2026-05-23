package me.rerere.rikkahub.data.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.data.ai.tools.SystemTools
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class SupabaseSyncData(
    val timestamp: String,
    val location: SupabaseLocationData? = null,
    val appUsage: List<SupabaseAppUsageData> = emptyList(),
    val notifications: List<SupabaseNotificationData> = emptyList(),
    val foregroundApp: String = "",
)

@Serializable
data class SupabaseLocationData(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val city: String = "",
    val district: String = "",
    val street: String = "",
)

@Serializable
data class SupabaseAppUsageData(
    val packageName: String,
    val appName: String,
    val totalTimeInForeground: Long,
    val lastTimeUsed: Long,
)

@Serializable
data class SupabaseNotificationData(
    val packageName: String,
    val appName: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val category: String? = null,
)

class SupabaseService(
    private val supabaseUrl: String,
    private val supabaseApiKey: String,
    private val tableName: String,
) {
    companion object {
        private const val TAG = "SupabaseService"
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun insertRow(data: SupabaseSyncData): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            if (supabaseUrl.isBlank() || supabaseApiKey.isBlank()) {
                throw IllegalArgumentException("Supabase URL and API Key must not be blank")
            }

            val baseUrl = supabaseUrl.trimEnd('/')
            val url = URL("$baseUrl/rest/v1/$tableName")

            val jsonObject = buildJsonObject(data)
            val jsonString = json.encodeToString(JsonObject.serializer(), jsonObject)

            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("apikey", supabaseApiKey)
                setRequestProperty("Authorization", "Bearer $supabaseApiKey")
                setRequestProperty("Prefer", "return=minimal")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(jsonString)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                throw Exception("Supabase API error ($responseCode): $errorBody")
            }

            Log.d(TAG, "Successfully inserted row into $tableName")
        }
    }

    private fun buildJsonObject(data: SupabaseSyncData): JsonObject {
        val map = mutableMapOf<String, JsonPrimitive>()
        map["timestamp"] = JsonPrimitive(data.timestamp)
        map["foreground_app"] = JsonPrimitive(data.foregroundApp)

        data.location?.let { loc ->
            map["location_latitude"] = JsonPrimitive(loc.latitude)
            map["location_longitude"] = JsonPrimitive(loc.longitude)
            map["location_address"] = JsonPrimitive(loc.address)
            map["location_city"] = JsonPrimitive(loc.city)
            map["location_district"] = JsonPrimitive(loc.district)
            map["location_street"] = JsonPrimitive(loc.street)
        }

        if (data.appUsage.isNotEmpty()) {
            val appUsageJson = json.encodeToString(
                kotlinx.serialization.serializer<List<SupabaseAppUsageData>>(),
                data.appUsage
            )
            map["app_usage"] = JsonPrimitive(appUsageJson)
        }

        if (data.notifications.isNotEmpty()) {
            val notificationsJson = json.encodeToString(
                kotlinx.serialization.serializer<List<SupabaseNotificationData>>(),
                data.notifications
            )
            map["notifications"] = JsonPrimitive(notificationsJson)
        }

        return JsonObject(map)
    }
}

suspend fun SupabaseService.collectAndUpload(context: Context): Result<Unit> {
    return runCatching {
        val settingsStore = org.koin.core.context.GlobalContext.get().get<SettingsStore>()
        val settings = settingsStore.settingsFlowRaw.first()

        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val timestamp = sdf.format(java.util.Date())

        // Collect location
        var locationData: SupabaseLocationData? = null
        try {
            if (settings.systemToolsSetting.locationAccess) {
                val amapApiKey = settings.systemToolsSetting.amapApiKey
                if (amapApiKey.isNotBlank()) {
                    // 有高德API Key，获取完整地址信息
                    val amapService = AmapService(amapApiKey)
                    val locationService = LocationService(context, amapService)
                    val locationResult = locationService.getCurrentLocation(amapApiKey)
                    if (locationResult.isSuccess) {
                        val loc = locationResult.getOrThrow()
                        android.util.Log.d("SupabaseService", "Location obtained: lat=${loc.latitude}, lng=${loc.longitude}, address=${loc.address}, city=${loc.city}, district=${loc.district}, street=${loc.street}")
                        locationData = SupabaseLocationData(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            address = loc.address,
                            city = loc.city,
                            district = loc.district,
                            street = loc.street
                        )
                    } else {
                        android.util.Log.e("SupabaseService", "Failed to get location with amap, falling back to coordinates only: ${locationResult.exceptionOrNull()?.message}")
                        // 高德失败，尝试只获取坐标
                        val locationService = LocationService(context, AmapService(""))
                        val coordResult = locationService.getCoordinatesOnly()
                        if (coordResult.isSuccess) {
                            val loc = coordResult.getOrThrow()
                            locationData = SupabaseLocationData(latitude = loc.latitude, longitude = loc.longitude)
                        }
                    }
                } else {
                    // 没有高德API Key，只获取坐标
                    val locationService = LocationService(context, AmapService(""))
                    val locationResult = locationService.getCoordinatesOnly()
                    if (locationResult.isSuccess) {
                        val loc = locationResult.getOrThrow()
                        android.util.Log.d("SupabaseService", "Coordinates only obtained: lat=${loc.latitude}, lng=${loc.longitude}")
                        locationData = SupabaseLocationData(latitude = loc.latitude, longitude = loc.longitude)
                    } else {
                        android.util.Log.e("SupabaseService", "Failed to get coordinates: ${locationResult.exceptionOrNull()?.message}")
                    }
                }
            } else {
                android.util.Log.w("SupabaseService", "Location access disabled, skipping location")
            }
        } catch (e: Exception) {
            android.util.Log.w("SupabaseService", "Failed to collect location", e)
        }

        // Collect app usage
        var appUsageData = emptyList<SupabaseAppUsageData>()
        try {
            if (settings.systemToolsSetting.appUsageAccess && SystemTools.hasAppUsagePermission(context)) {
                val appUsageService = AppUsageService(context)
                val usageResult = appUsageService.getTodayUsageStats()
                if (usageResult.isSuccess) {
                    appUsageData = usageResult.getOrThrow().take(10).map { info ->
                        SupabaseAppUsageData(
                            packageName = info.packageName,
                            appName = info.appName,
                            totalTimeInForeground = info.totalTimeInForeground,
                            lastTimeUsed = info.lastTimeUsed
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("SupabaseService", "Failed to collect app usage", e)
        }

        // Collect notifications
        var notificationData = emptyList<SupabaseNotificationData>()
        try {
            if (settings.systemToolsSetting.notificationAccess) {
                val recentNotifications = RikkaNotificationListenerService.recentNotifications.value
                notificationData = recentNotifications.take(20).map { notif ->
                    SupabaseNotificationData(
                        packageName = notif.packageName,
                        appName = notif.appName,
                        title = notif.title,
                        content = notif.content,
                        timestamp = notif.timestamp,
                        category = notif.category
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("SupabaseService", "Failed to collect notifications", e)
        }

        // Collect foreground app
        var foregroundApp = ""
        try {
            if (settings.systemToolsSetting.appUsageAccess && SystemTools.hasAppUsagePermission(context)) {
                val appUsageService = AppUsageService(context)
                val result = appUsageService.getForegroundApp()
                if (result.isSuccess) {
                    foregroundApp = result.getOrThrow()
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("SupabaseService", "Failed to get foreground app", e)
        }

        val syncData = SupabaseSyncData(
            timestamp = timestamp,
            location = locationData,
            appUsage = appUsageData,
            notifications = notificationData,
            foregroundApp = foregroundApp
        )

        insertRow(syncData).getOrThrow()
    }
}