package me.rerere.rikkahub.data.ai.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import me.rerere.ai.core.InputSchema
import me.rerere.ai.core.Tool
import me.rerere.ai.ui.UIMessagePart
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.data.service.AmapService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun createExploreNearbyTool(context: Context, settings: Settings): Tool = Tool(
    name = "explore_nearby",
    description = "Explore nearby points of interest (POI) around the current location. Requires location permission and Amap API key. Returns restaurants, shops, attractions, etc.",
    parameters = {
        InputSchema.Obj(
            properties = buildJsonObject {
                putJsonObject("keyword") {
                    put("type", "string")
                    put("description", "Search keyword (e.g. restaurant, coffee, hospital, bank)")
                }
                putJsonObject("radius") {
                    put("type", "integer")
                    put("description", "Search radius in meters (default 1000, max 50000)")
                }
                putJsonObject("type") {
                    put("type", "string")
                    put("description", "POI type code (e.g. 050000 for restaurants, 060000 for shopping, 080000 for medical)")
                }
                putJsonObject("limit") {
                    put("type", "integer")
                    put("description", "Maximum number of results (default 10)")
                }
            }
        )
    },
    execute = { args ->
        val params = args.jsonObject
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        if (!hasLocationPermission(context)) {
            return@Tool listOf(UIMessagePart.Text(
                buildJsonObject { put("success", false); put("error", "Location permission not granted") }.toString()
            ))
        }

        val apiKey = settings.systemToolsSetting.amapApiKey
        if (apiKey.isBlank()) {
            return@Tool listOf(UIMessagePart.Text(
                buildJsonObject { put("success", false); put("error", "Amap API key not configured. Set it in System Tools settings.") }.toString()
            ))
        }

        try {
            val keyword = params["keyword"]?.jsonPrimitive?.content ?: ""
            val radius = params["radius"]?.jsonPrimitive?.intOrNull ?: 1000
            val type = params["type"]?.jsonPrimitive?.content ?: ""
            val limit = params["limit"]?.jsonPrimitive?.intOrNull ?: 10

            // Get current location
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            if (loc == null) {
                return@Tool listOf(UIMessagePart.Text(
                    buildJsonObject { put("success", false); put("error", "Unable to get current location") }.toString()
                ))
            }

            val amapService = AmapService(apiKey)
            val pois = runBlocking {
                amapService.searchNearbyPoi(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    keyword = keyword,
                    radius = radius.coerceIn(100, 50000),
                    type = type,
                    limit = limit
                )
            }

            if (pois.isEmpty()) {
                return@Tool listOf(UIMessagePart.Text(
                    buildJsonObject {
                        put("success", true)
                        put("count", 0)
                        put("message", "No nearby POI found for keyword: '$keyword'")
                    }.toString()
                ))
            }

            // Get current address
            val addressResult = runBlocking { amapService.getAddressFromGps(loc.latitude, loc.longitude) }

            val arr = buildJsonArray {
                pois.forEach { poi ->
                    add(buildJsonObject {
                        put("name", poi.name)
                        put("address", poi.address)
                        put("distance", poi.distance)
                        put("type", poi.type)
                        put("tel", poi.tel)
                    })
                }
            }

            listOf(UIMessagePart.Text(
                buildJsonObject {
                    put("success", true)
                    put("count", pois.size)
                    put("current_location", addressResult.formattedAddress ?: "${loc.latitude},${loc.longitude}")
                    put("search_radius", radius)
                    put("places", arr)
                }.toString()
            ))
        } catch (e: Exception) {
            listOf(UIMessagePart.Text(
                buildJsonObject { put("success", false); put("error", e.message ?: "Unknown error") }.toString()
            ))
        }
    }
)

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED