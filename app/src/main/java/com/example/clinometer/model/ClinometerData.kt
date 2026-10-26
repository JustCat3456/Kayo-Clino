package com.example.clinometer.model

/**
 * アプリ内の表示言語
 *
 * 新しい言語を追加する場合は:
 * 1. `app/src/main/assets/i18n/<code.json>` を追加
 * 2. この Enum に 1 行追加するだけで UI の言語選択・翻訳読み込み・音声読み上げに自動反映されます。
 */
enum class AppLanguage(
  val code: String,
  val nativeName: String,
  val localeTag: String
) {
  JA("ja", "日本語", "ja-JP"),
  EN("en", "English", "en-US"),
  ZH_TW("zh-TW", "繁體中文", "zh-TW"),
  ZH_CN("zh-CN", "简体中文", "zh-CN"),
  KO("ko", "한국어", "ko-KR"),
  ES("es", "Español", "es-ES")
}

/**
 * クリノメーターの測定モード
 */
enum class MeasureMode {
  SURFACE,
  EDGE
}

/**
 * 走向・傾斜の表記フォーマット
 */
enum class NotationFormat(val example: String) {
  JAPANESE("N 30° E / 45° SE"),
  RIGHT_HAND_RULE("030° / 45°"),
  DIP_DIRECTION("45° → 120°")
}

/**
 * リアルタイムのクリノメーター計測値
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
 * 保存された計測記録 (デジタル野帳レコード)
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
  val note: String = "",
  val latitude: Double? = null,
  val longitude: Double? = null
)
