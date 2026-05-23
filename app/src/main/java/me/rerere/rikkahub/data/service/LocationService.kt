@file:Suppress("unused")

package me.rerere.rikkahub.data.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val city: String = "",
    val district: String = "",
    val street: String = "",
    val poiList: List<PoiInfo> = emptyList()
)

data class PoiInfo(
    val name: String,
    val address: String,
    val distance: Int,
    val type: String,
    val latitude: Double,
    val longitude: Double
)

class LocationService(
    private val context: Context,
    private val amapService: AmapService
) {
    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(amapApiKey: String): Result<LocationInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val location = getLastKnownLocation()
                ?: requestFreshLocation()

            if (location != null) {
                // GPS坐标(WGS84)需要先转换为高德坐标(GCJ02)才能正确逆地理编码
                val amapCoord = amapService.convertToAmapCoord(location.latitude, location.longitude)
                val lat = amapCoord?.first ?: location.latitude
                val lng = amapCoord?.second ?: location.longitude

                val address = amapService.reverseGeocode(lat, lng)
                if (!address.success) {
                    android.util.Log.w("LocationService", "Reverse geocode failed: ${address.error}")
                }
                LocationInfo(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address.formattedAddress ?: "",
                    city = address.city ?: address.province ?: "",
                    district = address.district ?: "",
                    street = buildString {
                        append(address.street ?: "")
                        if (!address.streetNumber.isNullOrBlank()) {
                            append(address.streetNumber)
                        }
                    }
                )
            } else {
                throw IllegalStateException("无法获取位置信息")
            }
        }
    }

    /**
     * 仅获取坐标，不需要高德API Key，不进行逆地理编码
     */
    @SuppressLint("MissingPermission")
    suspend fun getCoordinatesOnly(): Result<LocationInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val location = getLastKnownLocation()
                ?: requestFreshLocation()

            if (location != null) {
                LocationInfo(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
            } else {
                throw IllegalStateException("无法获取位置信息")
            }
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun exploreNearby(
        amapApiKey: String,
        keyword: String = "",
        radius: Int = 1000,
        type: String = ""
    ): Result<List<PoiInfo>> = withContext(Dispatchers.IO) {
        runCatching {
            val location = getLastKnownLocation()
                ?: requestFreshLocation()

            if (location == null) {
                throw IllegalStateException("无法获取位置信息，请先开启定位")
            }

            amapService.searchNearbyPoi(
                latitude = location.latitude,
                longitude = location.longitude,
                keyword = keyword,
                radius = radius,
                type = type
            ).map { poi ->
                PoiInfo(
                    name = poi.name,
                    address = poi.address,
                    distance = poi.distance,
                    type = poi.type,
                    latitude = poi.latitude,
                    longitude = poi.longitude
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(): Location? {
        val providers = locationManager.getProviders(true)
        return providers.mapNotNull { provider ->
            locationManager.getLastKnownLocation(provider)
        }.maxByOrNull { it.accuracy }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestFreshLocation(): Location? {
        return try {
            kotlinx.coroutines.withTimeoutOrNull(10_000L) {
                suspendCancellableCoroutine<Location?> { cont ->
                    val providers = locationManager.getProviders(true)
                    val bestProvider = providers.firstOrNull()
                        ?: locationManager.getProvider(LocationManager.GPS_PROVIDER)?.let { LocationManager.GPS_PROVIDER }
                        ?: locationManager.getProvider(LocationManager.NETWORK_PROVIDER)?.let { LocationManager.NETWORK_PROVIDER }
                        ?: return@suspendCancellableCoroutine cont.resume(null)

                    val listener = object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            locationManager.removeUpdates(this)
                            if (cont.isActive) cont.resume(location)
                        }

                        override fun onProviderDisabled(provider: String) {
                            locationManager.removeUpdates(this)
                            if (cont.isActive) cont.resume(null)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

                        override fun onProviderEnabled(provider: String) {}
                    }

                    cont.invokeOnCancellation {
                        locationManager.removeUpdates(listener)
                    }

                    locationManager.requestLocationUpdates(
                        bestProvider,
                        0L,
                        0f,
                        listener
                    )
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}