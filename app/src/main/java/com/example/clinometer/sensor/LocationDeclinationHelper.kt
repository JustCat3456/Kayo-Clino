package com.example.clinometer.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.GeomagneticField
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * GPS (LocationManager) と Android 標準の GeomagneticField (世界磁気モデル WMM) を用いて
 * 現在地の緯度・経度・高度から正確な磁気偏角を自動計算するヘルパークラス
 */
class LocationDeclinationHelper(private val context: Context) {

  data class DeclinationResult(
    val declinationDeg: Float,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Float
  )

  fun hasLocationPermission(): Boolean {
    val fine = ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
  }

  /**
   * 任意の緯度・経度・高度から世界磁気モデル (WMM) に基づく磁気偏角を算出する
   */
  fun computeDeclination(
    latitude: Double,
    longitude: Double,
    altitudeMeters: Float = 0f,
    timeMillis: Long = System.currentTimeMillis()
  ): Float {
    val geoField = GeomagneticField(
      latitude.toFloat(),
      longitude.toFloat(),
      altitudeMeters,
      timeMillis
    )
    return geoField.declination
  }

  /**
   * 端末の現在地を取得し、その地点の磁気偏角を計算して返す
   */
  @SuppressLint("MissingPermission")
  suspend fun fetchCurrentLocationAndDeclination(): DeclinationResult? {
    if (!hasLocationPermission()) return null

    val locationManager =
      context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

    // 1. まずキャッシュされた最新の位置情報を確認
    val providers = listOf(
      LocationManager.GPS_PROVIDER,
      LocationManager.NETWORK_PROVIDER,
      LocationManager.PASSIVE_PROVIDER
    )
    var bestLocation: Location? = null
    for (provider in providers) {
      val loc = runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull()
      if (loc != null) {
        if (bestLocation == null || loc.time > bestLocation.time) {
          bestLocation = loc
        }
      }
    }

    // 2. キャッシュがない場合は最大4秒間リアルタイム測位を試行
    if (bestLocation == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val activeProvider = providers.firstOrNull {
        runCatching { locationManager.isProviderEnabled(it) }.getOrDefault(false)
      }
      if (activeProvider != null) {
        bestLocation = withTimeoutOrNull(4000L) {
          suspendCancellableCoroutine { cont ->
            val signal = CancellationSignal()
            cont.invokeOnCancellation { signal.cancel() }
            runCatching {
              locationManager.getCurrentLocation(
                activeProvider,
                signal,
                ContextCompat.getMainExecutor(context)
              ) { location ->
                if (cont.isActive) cont.resume(location)
              }
            }.onFailure {
              if (cont.isActive) cont.resume(null)
            }
          }
        }
      }
    }

    val finalLoc = bestLocation ?: return null
    val alt = if (finalLoc.hasAltitude()) finalLoc.altitude.toFloat() else 0f
    val declination = computeDeclination(
      latitude = finalLoc.latitude,
      longitude = finalLoc.longitude,
      altitudeMeters = alt
    )

    return DeclinationResult(
      declinationDeg = declination,
      latitude = finalLoc.latitude,
      longitude = finalLoc.longitude,
      altitudeMeters = alt
    )
  }
}
