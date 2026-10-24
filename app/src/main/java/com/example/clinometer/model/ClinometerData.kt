package com.example.clinometer.model

/**
 * クリノメーターの測定モード
 */
enum class MeasureMode(val label: String, val description: String) {
  SURFACE(
    label = "面測定 (背面密着)",
    description = "端末の背面を地層面に当てて走向と最大傾斜を同時計測します"
  ),
  EDGE(
    label = "側面測定 (伝統式)",
    description = "端末の長辺を地層面に当てて走向または傾斜角を計測します"
  )
}

/**
 * 走向・傾斜の表記フォーマット
 */
enum class NotationFormat(val label: String, val example: String) {
  JAPANESE(
    label = "日本式 (N30°E / 45°SE)",
    example = "N 30° E / 45° SE"
  ),
  RIGHT_HAND_RULE(
    label = "右手則 RHR (030 / 45)",
    example = "030° / 45°"
  ),
  DIP_DIRECTION(
    label = "Dip/DipDir (45 → 120°)",
    example = "45° → 120°"
  )
}

/**
 * リアルタイムのクリノメーター計測値
 *
 * @property strikeAzimuth 右手則に基づく走向方位角 (0.0..360.0)
 * @property dipAngle 最大傾斜角 (0.0..90.0)
 * @property dipDirection 最大傾斜方位角 (0.0..360.0)
 * @property deviceAzimuth 端末上端(Y軸)が向いている方位角 (0.0..360.0)
 * @property pitch 端末前後の傾き (-90.0..90.0)
 * @property roll 端末左右の傾き (-180.0..180.0)
 * @property tiltX 水準器バブル表示用のX軸傾き成分 (-1.0..1.0)
 * @property tiltY 水準器バブル表示用のY軸傾き成分 (-1.0..1.0)
 */
data class ClinometerReading(
  val strikeAzimuth: Float = 0f,
  val dipAngle: Float = 0f,
  val dipDirection: Float = 90f,
  val deviceAzimuth: Float = 0f,
  val pitch: Float = 0f,
  val roll: Float = 0f,
  val tiltX: Float = 0f,
  val tiltY: Float = 0f
)

/**
 * 保存された計測記録
 */
data class MeasurementRecord(
  val id: Long,
  val timestampMillis: Long,
  val strikeFormatted: String,
  val dipFormatted: String,
  val strikeAzimuth: Float,
  val dipAngle: Float,
  val dipDirection: Float,
  val mode: MeasureMode,
  val note: String = ""
)
