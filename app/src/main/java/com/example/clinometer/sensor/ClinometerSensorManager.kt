package com.example.clinometer.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * 端末の姿勢センサー (Rotation Vector または 加速度+地磁気) から
 * 3x3 回転行列をリアルタイムの Flow として提供するセンサークラス
 */
class ClinometerSensorManager(context: Context) {

  private val sensorManager =
    context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

  private val rotationVectorSensor: Sensor? =
    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

  private val accelerometerSensor: Sensor? =
    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

  private val magneticFieldSensor: Sensor? =
    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

  val hasSensors: Boolean
    get() = rotationVectorSensor != null ||
      (accelerometerSensor != null && magneticFieldSensor != null)

  /**
   * 3x3 回転行列 (FloatArray(9)) と磁気センサー精度 (Int) のペアを流す Flow
   * POCO F7 の 120Hz 表示に合わせ SENSOR_DELAY_GAME (約50Hz) で取得
   */
  fun rotationMatrixFlow(): Flow<Pair<FloatArray, Int>> = callbackFlow {
    val rotationMatrix = FloatArray(9)
    val gravity = FloatArray(3)
    val geomagnetic = FloatArray(3)
    var hasGravity = false
    var hasMagnetic = false
    var currentAccuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH

    val listener = object : SensorEventListener {
      override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
          Sensor.TYPE_ROTATION_VECTOR -> {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            trySend(rotationMatrix.copyOf() to currentAccuracy)
          }

          Sensor.TYPE_ACCELEROMETER -> {
            System.arraycopy(event.values, 0, gravity, 0, 3)
            hasGravity = true
            if (hasMagnetic) {
              if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                trySend(rotationMatrix.copyOf() to currentAccuracy)
              }
            }
          }

          Sensor.TYPE_MAGNETIC_FIELD -> {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            hasMagnetic = true
            if (hasGravity) {
              if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                trySend(rotationMatrix.copyOf() to currentAccuracy)
              }
            }
          }
        }
      }

      override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD ||
          sensor?.type == Sensor.TYPE_ROTATION_VECTOR
        ) {
          currentAccuracy = accuracy
        }
      }
    }

    if (rotationVectorSensor != null) {
      sensorManager.registerListener(
        listener,
        rotationVectorSensor,
        SensorManager.SENSOR_DELAY_GAME
      )
    } else {
      accelerometerSensor?.let {
        sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
      }
      magneticFieldSensor?.let {
        sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
      }
    }

    awaitClose {
      sensorManager.unregisterListener(listener)
    }
  }
}
