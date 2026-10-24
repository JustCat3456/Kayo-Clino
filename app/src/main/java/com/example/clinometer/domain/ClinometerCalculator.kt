package com.example.clinometer.domain

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
 * 地学用クリノメーターの幾何計算および表記フォーマット変換を行うクラス
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
   * @param declinationDeg 磁気偏角 (度: 東偏が正、西偏が負。日本付近は約 -7.5°)
   */
  fun computeFromRotationMatrix(
    rotationMatrix: FloatArray,
    mode: MeasureMode,
    declinationDeg: Float = 0f
  ): ClinometerReading {
    require(rotationMatrix.size >= 9) { "rotationMatrix must have at least 9 elements" }

    // 端末 Y 軸 (画面上方向・長辺) のワールド座標成分
    val yEast = rotationMatrix[1]
    val yNorth = rotationMatrix[4]
    val yUp = rotationMatrix[7]

    // 端末 Z 軸 (画面法線方向) のワールド座標成分
    val zEast = rotationMatrix[2]
    val zNorth = rotationMatrix[5]
    val zUp = rotationMatrix[8]

    // 端末 X 軸 (画面右方向) のワールド座標成分
    val xEast = rotationMatrix[0]
    val xNorth = rotationMatrix[3]
    val xUp = rotationMatrix[6]

    // 端末長辺の水平方位角 (0..360°)
    val rawDeviceAzimuth = if (abs(yEast) < 1e-4f && abs(yNorth) < 1e-4f) {
      // 端末がほぼ直立してY軸が鉛直に近い場合はZ軸から方位を補完
      atan2(zEast, zNorth) * RAD_TO_DEG
    } else {
      atan2(yEast, yNorth) * RAD_TO_DEG
    }
    val deviceAzimuth = normalizeDegrees(rawDeviceAzimuth + declinationDeg)

    // 水準器バブル用の傾き成分 (-1.0..1.0)
    // 端末を水平に置いたとき、高い側へ気泡が移動するように符号を設定
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

  /**
   * 面測定モード: 端末背面を地層面に密着させたときの法線ベクトルから算出
   */
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
    // オーバーハング等で画面が下向き (zUp < 0) の場合も上向き法線に揃えて地層面を評価
    val sign = if (zUp >= 0f) 1f else -1f
    val nx = zEast * sign
    val ny = zNorth * sign
    val nz = (zUp * sign).coerceIn(0f, 1f)

    // 最大傾斜角 Dip (0..90°)
    val dipAngle = (acos(nz) * RAD_TO_DEG).coerceIn(0f, 90f)

    // 水平に近い場合 (Dip < 0.5°) は端末の向きを基準にする
    val horizontalMag = sqrt(nx * nx + ny * ny)
    val dipDirection = if (horizontalMag < 1e-3f) {
      normalizeDegrees(deviceAzimuth + 90f)
    } else {
      val rawDipDir = atan2(nx, ny) * RAD_TO_DEG
      normalizeDegrees(rawDipDir + declinationDeg)
    }

    // 右手則 (Right-Hand Rule): 走向方向を向いたとき右側(+90°)が傾斜方向になる
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

  /**
   * 側面測定モード (伝統式): 端末の長辺 (Y軸) を地層面に当てて計測
   */
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
    // 長辺の水平面に対する傾き角 (0..90°)
    val dipAngle = (asin(abs(yUp).coerceIn(0f, 1f)) * RAD_TO_DEG).coerceIn(0f, 90f)

    // 長辺が向いている水平方位を走向とする
    val strikeAzimuth = deviceAzimuth

    // 長辺が下っている方向、または画面表側の向きから傾斜方位を決定
    val downSlopeX = if (yUp <= 0f) yEast else -yEast
    val downSlopeY = if (yUp <= 0f) yNorth else -yNorth
    val dipDirection = if (abs(yUp) > 0.05f) {
      normalizeDegrees(atan2(downSlopeX, downSlopeY) * RAD_TO_DEG + declinationDeg)
    } else {
      // 水平に当てて走向を測っているときは長辺と直交する方向
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

  /**
   * 角度を 0.0 <= deg < 360.0 に正規化する
   */
  fun normalizeDegrees(degrees: Float): Float {
    val mod = degrees % 360f
    return if (mod < 0f) mod + 360f else mod
  }

  /**
   * 日本式走向表記に変換する (例: "N 32° E", "N 15° W", "N-S", "E-W")
   */
  fun formatJapaneseStrike(strikeAzimuth: Float): String {
    // 走向は 180° 周期の直線なので 0..179° の範囲で北基準に変換
    val halfCircle = ((normalizeDegrees(strikeAzimuth).roundToInt()) % 180 + 180) % 180
    return when {
      halfCircle == 0 -> "N-S (N 0° E)"
      halfCircle == 90 -> "E-W (N 90° E)"
      halfCircle < 90 -> "N ${halfCircle}° E"
      else -> "N ${180 - halfCircle}° W"
    }
  }

  /**
   * 日本式傾斜表記に変換する (例: "42° SE", "0° (水平)", "90° (直立)")
   */
  fun formatJapaneseDip(dipAngle: Float, dipDirection: Float): String {
    val roundedDip = dipAngle.roundToInt().coerceIn(0, 90)
    return when (roundedDip) {
      0 -> "0° (水平)"
      90 -> "90° (直立)"
      else -> "${roundedDip}° ${toCardinal8(dipDirection)}"
    }
  }

  /**
   * 指定されたフォーマットに応じた走向テキストを返す
   */
  fun formatStrike(reading: ClinometerReading, format: NotationFormat): String {
    return when (format) {
      NotationFormat.JAPANESE -> formatJapaneseStrike(reading.strikeAzimuth)
      NotationFormat.RIGHT_HAND_RULE -> "%03d°".format(reading.strikeAzimuth.roundToInt() % 360)
      NotationFormat.DIP_DIRECTION -> "%03d° (${toCardinal8(reading.dipDirection)})".format(
        reading.dipDirection.roundToInt() % 360
      )
    }
  }

  /**
   * 指定されたフォーマットに応じた傾斜テキストを返す
   */
  fun formatDip(reading: ClinometerReading, format: NotationFormat): String {
    return when (format) {
      NotationFormat.JAPANESE -> formatJapaneseDip(reading.dipAngle, reading.dipDirection)
      NotationFormat.RIGHT_HAND_RULE -> "%02d° %s".format(
        reading.dipAngle.roundToInt().coerceIn(0, 90),
        toCardinal8(reading.dipDirection)
      )
      NotationFormat.DIP_DIRECTION -> "%02d°".format(
        reading.dipAngle.roundToInt().coerceIn(0, 90)
      )
    }
  }

  /**
   * 方位角 (0..360°) を8方位 ("N", "NE", "E", "SE", "S", "SW", "W", "NW") に変換する
   */
  fun toCardinal8(azimuth: Float): String {
    val dirs = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    val index = (((normalizeDegrees(azimuth) + 22.5f) / 45f).toInt()) % 8
    return dirs[index]
  }

  /**
   * 120Hz 描画向けに前回の計測値と今回の計測値をローパスフィルタで滑らかに補間する
   */
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

  /**
   * 0° と 360° の境界をまたいでも最短経路で補間する角度フィルタ
   */
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
