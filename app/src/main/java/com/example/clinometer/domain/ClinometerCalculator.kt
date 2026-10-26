package com.example.clinometer.domain

import com.example.clinometer.model.AppLanguage
import com.example.clinometer.model.ClinometerReading
import com.example.clinometer.model.MeasureMode
import com.example.clinometer.model.NotationFormat
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 地学用クリノメーターの幾何計算・カメラバンプ補正・表記フォーマット変換を行うクラス
 */
object ClinometerCalculator {

  private const val RAD_TO_DEG = (180.0 / Math.PI).toFloat()
  private const val DEG_TO_RAD = (Math.PI / 180.0).toFloat()

  /**
   * 3x3 回転行列 (row-major 9要素配列) から走向・傾斜を算出する
   *
   * ワールド座標系: X = 東(East), Y = 北(North), Z = 天頂(Up)
   * デバイス座標系: x = 画面右, y = 画面上(長辺), z = 画面手前(背面に対する法線)
   *
   * @param rotationMatrix 長さ9以上の回転行列配列
   * @param mode 測定モード (面測定 or 側面測定)
   * @param declinationDeg 磁気偏角 (度: 東偏が正、西偏が負)
   * @param pitchOffsetDeg カメラ出っ張りゼロ点補正用ピッチオフセット (度)
   * @param rollOffsetDeg カメラ出っ張りゼロ点補正用ロールオフセット (度)
   */
  fun computeFromRotationMatrix(
    rotationMatrix: FloatArray,
    mode: MeasureMode,
    declinationDeg: Float = 0f,
    pitchOffsetDeg: Float = 0f,
    rollOffsetDeg: Float = 0f
  ): ClinometerReading {
    require(rotationMatrix.size >= 9) { "rotationMatrix must have at least 9 elements" }

    // カメラ出っ張り補正 (デバイス座標系での法線・軸の微調整)
    val pRad = -pitchOffsetDeg * DEG_TO_RAD
    val rRad = -rollOffsetDeg * DEG_TO_RAD
    val cosP = cos(pRad)
    val sinP = sin(pRad)
    val cosR = cos(rRad)
    val sinR = sin(rRad)

    // 元の回転行列の列ベクトル (デバイス X, Y, Z 軸のワールド成分)
    val rawXEast = rotationMatrix[0]
    val rawXNorth = rotationMatrix[3]
    val rawXUp = rotationMatrix[6]

    val rawYEast = rotationMatrix[1]
    val rawYNorth = rotationMatrix[4]
    val rawYUp = rotationMatrix[7]

    val rawZEast = rotationMatrix[2]
    val rawZNorth = rotationMatrix[5]
    val rawZUp = rotationMatrix[8]

    // ロール (Y軸回り) および ピッチ (X軸回り) の微小オフセット回転を適用した補正後法線 Z' と Y'
    val zEast = rawZEast * (cosR * cosP) + rawXEast * sinR + rawYEast * (-cosR * sinP)
    val zNorth = rawZNorth * (cosR * cosP) + rawXNorth * sinR + rawYNorth * (-cosR * sinP)
    val zUp = rawZUp * (cosR * cosP) + rawXUp * sinR + rawYUp * (-cosR * sinP)

    val yEast = rawYEast * cosP + rawZEast * sinP
    val yNorth = rawYNorth * cosP + rawZNorth * sinP
    val yUp = rawYUp * cosP + rawZUp * sinP

    val xEast = rawXEast * cosR - rawZEast * sinR
    val xNorth = rawXNorth * cosR - rawZNorth * sinR
    val xUp = rawXUp * cosR - rawZUp * sinR

    // 端末長辺の水平方位角 (0..360°)
    val rawDeviceAzimuth = if (abs(yEast) < 1e-4f && abs(yNorth) < 1e-4f) {
      atan2(zEast, zNorth) * RAD_TO_DEG
    } else {
      atan2(yEast, yNorth) * RAD_TO_DEG
    }
    val deviceAzimuth = normalizeDegrees(rawDeviceAzimuth + declinationDeg)

    // 水準器バブル用の傾き成分 (-1.0..1.0)
    val tiltX = (-xUp).coerceIn(-1f, 1f)
    val tiltY = (-yUp).coerceIn(-1f, 1f)

    // ピッチ・ロール (度)
    val pitch = asin(yUp.coerceIn(-1f, 1f)) * RAD_TO_DEG
    val roll = atan2(-xUp, zUp) * RAD_TO_DEG

    return when (mode) {
      MeasureMode.SURFACE -> computeSurfaceMode(
        zEast = zEast,
        zNorth = zNorth,
        zUp = zUp,
        deviceAzimuth = deviceAzimuth,
        pitch = pitch,
        roll = roll,
        tiltX = tiltX,
        tiltY = tiltY,
        declinationDeg = declinationDeg
      )

      MeasureMode.EDGE -> computeEdgeMode(
        xEast = xEast,
        xNorth = xNorth,
        xUp = xUp,
        yEast = yEast,
        yNorth = yNorth,
        yUp = yUp,
        deviceAzimuth = deviceAzimuth,
        pitch = pitch,
        roll = roll,
        tiltX = tiltX,
        tiltY = tiltY,
        declinationDeg = declinationDeg
      )
    }
  }

  private fun computeSurfaceMode(
    zEast: Float,
    zNorth: Float,
    zUp: Float,
    deviceAzimuth: Float,
    pitch: Float,
    roll: Float,
    tiltX: Float,
    tiltY: Float,
    declinationDeg: Float
  ): ClinometerReading {
    val norm = sqrt(zEast * zEast + zNorth * zNorth + zUp * zUp).coerceAtLeast(1e-6f)
    val sign = if (zUp >= 0f) 1f else -1f
    val nx = (zEast / norm) * sign
    val ny = (zNorth / norm) * sign
    val nz = ((zUp / norm) * sign).coerceIn(0f, 1f)

    val dipAngle = (acos(nz) * RAD_TO_DEG).coerceIn(0f, 90f)

    val horizontalMag = sqrt(nx * nx + ny * ny)
    val dipDirection = if (horizontalMag < 1e-3f) {
      normalizeDegrees(deviceAzimuth + 90f)
    } else {
      val rawDipDir = atan2(nx, ny) * RAD_TO_DEG
      normalizeDegrees(rawDipDir + declinationDeg)
    }

    val strikeAzimuth = normalizeDegrees(dipDirection - 90f)

    return ClinometerReading(
      strikeAzimuth = strikeAzimuth,
      dipAngle = dipAngle,
      dipDirection = dipDirection,
      deviceAzimuth = deviceAzimuth,
      pitch = pitch,
      roll = roll,
      tiltX = tiltX,
      tiltY = tiltY
    )
  }

  private fun computeEdgeMode(
    xEast: Float,
    xNorth: Float,
    xUp: Float,
    yEast: Float,
    yNorth: Float,
    yUp: Float,
    deviceAzimuth: Float,
    pitch: Float,
    roll: Float,
    tiltX: Float,
    tiltY: Float,
    declinationDeg: Float
  ): ClinometerReading {
    val dipAngle = (asin(abs(yUp).coerceIn(0f, 1f)) * RAD_TO_DEG).coerceIn(0f, 90f)
    val strikeAzimuth = deviceAzimuth

    val downSlopeX = if (yUp <= 0f) yEast else -yEast
    val downSlopeY = if (yUp <= 0f) yNorth else -yNorth
    val dipDirection = if (abs(yUp) > 0.05f) {
      normalizeDegrees(atan2(downSlopeX, downSlopeY) * RAD_TO_DEG + declinationDeg)
    } else {
      val crossHoriz = atan2(xEast, xNorth) * RAD_TO_DEG
      normalizeDegrees(crossHoriz + declinationDeg)
    }

    return ClinometerReading(
      strikeAzimuth = strikeAzimuth,
      dipAngle = dipAngle,
      dipDirection = dipDirection,
      deviceAzimuth = deviceAzimuth,
      pitch = pitch,
      roll = roll,
      tiltX = tiltX,
      tiltY = tiltY
    )
  }

  fun normalizeDegrees(degrees: Float): Float {
    val mod = degrees % 360f
    return if (mod < 0f) mod + 360f else mod
  }

  fun formatJapaneseStrike(strikeAzimuth: Float): String {
    val halfCircle = ((normalizeDegrees(strikeAzimuth).roundToInt()) % 180 + 180) % 180
    return when {
      halfCircle == 0 -> "N-S (N 0° E)"
      halfCircle == 90 -> "E-W (N 90° E)"
      halfCircle < 90 -> "N ${halfCircle}° E"
      else -> "N ${180 - halfCircle}° W"
    }
  }

  fun formatJapaneseDip(
    dipAngle: Float,
    dipDirection: Float,
    language: AppLanguage = AppLanguage.JA
  ): String {
    val roundedDip = dipAngle.roundToInt().coerceIn(0, 90)
    return when (roundedDip) {
      0 -> when (language) {
        AppLanguage.JA, AppLanguage.ZH_TW, AppLanguage.ZH_CN -> "0° (水平)"
        AppLanguage.KO -> "0° (수평)"
        AppLanguage.ES -> "0° (Horiz)"
        AppLanguage.EN -> "0° (Flat)"
      }
      90 -> when (language) {
        AppLanguage.JA, AppLanguage.ZH_TW, AppLanguage.ZH_CN -> "90° (直立)"
        AppLanguage.KO -> "90° (수직)"
        AppLanguage.ES, AppLanguage.EN -> "90° (Vert)"
      }
      else -> "${roundedDip}° ${toCardinal8(dipDirection)}"
    }
  }

  fun formatStrike(reading: ClinometerReading, format: NotationFormat): String {
    return when (format) {
      NotationFormat.JAPANESE -> formatJapaneseStrike(reading.strikeAzimuth)
      NotationFormat.RIGHT_HAND_RULE -> "%03d°".format(reading.strikeAzimuth.roundToInt() % 360)
      NotationFormat.DIP_DIRECTION -> "%03d° (${toCardinal8(reading.dipDirection)})".format(
        reading.dipDirection.roundToInt() % 360
      )
    }
  }

  fun formatDip(
    reading: ClinometerReading,
    format: NotationFormat,
    language: AppLanguage = AppLanguage.JA
  ): String {
    return when (format) {
      NotationFormat.JAPANESE -> formatJapaneseDip(reading.dipAngle, reading.dipDirection, language)
      NotationFormat.RIGHT_HAND_RULE -> "%02d° %s".format(
        reading.dipAngle.roundToInt().coerceIn(0, 90),
        toCardinal8(reading.dipDirection)
      )
      NotationFormat.DIP_DIRECTION -> "%02d°".format(
        reading.dipAngle.roundToInt().coerceIn(0, 90)
      )
    }
  }

  fun toCardinal8(azimuth: Float): String {
    val dirs = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = (((normalizeDegrees(azimuth) + 22.5f) / 45f).toInt()) % 8
    return dirs[index]
  }

  /**
   * 音声読み上げ (TTS) 用のテキストを生成する (6言語対応)
   */
  fun buildSpeechText(reading: ClinometerReading, language: AppLanguage): String {
    val halfCircle = ((normalizeDegrees(reading.strikeAzimuth).roundToInt()) % 180 + 180) % 180
    val dip = reading.dipAngle.roundToInt().coerceIn(0, 90)
    val cardinal = toCardinal8(reading.dipDirection)

    return when (language) {
      AppLanguage.JA -> {
        val strikeSpeech = when {
          halfCircle == 0 -> "走向 南北"
          halfCircle == 90 -> "走向 東西"
          halfCircle < 90 -> "走向 北 ${halfCircle}度 東"
          else -> "走向 北 ${180 - halfCircle}度 西"
        }
        val cardinalJa = when (cardinal) {
          "N" -> "北"
          "NE" -> "北東"
          "E" -> "東"
          "SE" -> "南東"
          "S" -> "南"
          "SW" -> "南西"
          "W" -> "西"
          "NW" -> "北西"
          else -> cardinal
        }
        "$strikeSpeech、傾斜 ${dip}度 $cardinalJa"
      }

      AppLanguage.ZH_TW, AppLanguage.ZH_CN -> {
        val strikeSpeech = when {
          halfCircle == 0 -> "走向 南北"
          halfCircle == 90 -> "走向 東西"
          halfCircle < 90 -> "走向 北偏東 ${halfCircle}度"
          else -> "走向 北偏西 ${180 - halfCircle}度"
        }
        "$strikeSpeech，傾角 ${dip}度 $cardinal"
      }

      AppLanguage.KO -> {
        val strikeSpeech = when {
          halfCircle == 0 -> "주향 남북"
          halfCircle == 90 -> "주향 동서"
          halfCircle < 90 -> "주향 북 ${halfCircle}도 동"
          else -> "주향 북 ${180 - halfCircle}도 서"
        }
        "$strikeSpeech, 경사 ${dip}도 $cardinal"
      }

      AppLanguage.ES -> {
        val strikeSpeech = when {
          halfCircle == 0 -> "Rumbo Norte Sur"
          halfCircle == 90 -> "Rumbo Este Oeste"
          halfCircle < 90 -> "Rumbo Norte $halfCircle grados Este"
          else -> "Rumbo Norte ${180 - halfCircle} grados Oeste"
        }
        "$strikeSpeech, Buzamiento $dip grados $cardinal"
      }

      AppLanguage.EN -> {
        val strikeSpeech = when {
          halfCircle == 0 -> "Strike North South"
          halfCircle == 90 -> "Strike East West"
          halfCircle < 90 -> "Strike North $halfCircle degrees East"
          else -> "Strike North ${180 - halfCircle} degrees West"
        }
        "$strikeSpeech, Dip $dip degrees $cardinal"
      }
    }
  }

  fun smoothReading(
    previous: ClinometerReading,
    current: ClinometerReading,
    alpha: Float = 0.2f
  ): ClinometerReading {
    val clampedAlpha = alpha.coerceIn(0.01f, 1f)
    return ClinometerReading(
      strikeAzimuth = smoothAngle(previous.strikeAzimuth, current.strikeAzimuth, clampedAlpha),
      dipAngle = previous.dipAngle + (current.dipAngle - previous.dipAngle) * clampedAlpha,
      dipDirection = smoothAngle(previous.dipDirection, current.dipDirection, clampedAlpha),
      deviceAzimuth = smoothAngle(previous.deviceAzimuth, current.deviceAzimuth, clampedAlpha),
      pitch = previous.pitch + (current.pitch - previous.pitch) * clampedAlpha,
      roll = smoothAngleSigned(previous.roll, current.roll, clampedAlpha),
      tiltX = previous.tiltX + (current.tiltX - previous.tiltX) * clampedAlpha,
      tiltY = previous.tiltY + (current.tiltY - previous.tiltY) * clampedAlpha
    )
  }

  fun smoothAngle(prevDeg: Float, targetDeg: Float, alpha: Float): Float {
    val prevRad = prevDeg * DEG_TO_RAD
    val targetRad = targetDeg * DEG_TO_RAD
    val sinMix = (1f - alpha) * sin(prevRad) + alpha * sin(targetRad)
    val cosMix = (1f - alpha) * cos(prevRad) + alpha * cos(targetRad)
    return normalizeDegrees(atan2(sinMix, cosMix) * RAD_TO_DEG)
  }

  private fun smoothAngleSigned(prevDeg: Float, targetDeg: Float, alpha: Float): Float {
    var diff = targetDeg - prevDeg
    while (diff > 180f) diff -= 360f
    while (diff < -180f) diff += 360f
    return prevDeg + diff * alpha
  }
}
