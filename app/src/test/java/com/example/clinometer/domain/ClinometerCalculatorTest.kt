package com.example.clinometer.domain

import com.example.clinometer.model.AppLanguage
import com.example.clinometer.model.ClinometerReading
import com.example.clinometer.model.MeasureMode
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinometerCalculatorTest {

  @Test
  fun horizontalPlane_returnsZeroDip() {
    val identityMatrix = floatArrayOf(
      1f, 0f, 0f,
      0f, 1f, 0f,
      0f, 0f, 1f
    )

    val reading = ClinometerCalculator.computeFromRotationMatrix(
      rotationMatrix = identityMatrix,
      mode = MeasureMode.SURFACE,
      declinationDeg = 0f
    )

    assertEquals(0f, reading.dipAngle, 0.01f)
    assertEquals(0f, reading.deviceAzimuth, 0.01f)
  }

  @Test
  fun cameraBumpOffsetCalibration_cancelsSmallTiltToZeroDip() {
    // 背面カメラの出っ張りにより Y軸が 2.5° 浮いている状態を再現
    val tiltDeg = 2.5f
    val rad = Math.toRadians(tiltDeg.toDouble()).toFloat()
    val c = cos(rad)
    val s = sin(rad)

    val tiltedMatrix = floatArrayOf(
      1f, 0f, 0f,
      0f, c, -s,
      0f, s, c
    )

    // 補正なしの場合は約 2.5° の傾斜として検出される
    val uncalibrated = ClinometerCalculator.computeFromRotationMatrix(
      rotationMatrix = tiltedMatrix,
      mode = MeasureMode.SURFACE
    )
    assertEquals(2.5f, uncalibrated.dipAngle, 0.1f)

    // 検出された pitch を pitchOffsetDeg として与えると Dip が 0° に補正される
    val calibrated = ClinometerCalculator.computeFromRotationMatrix(
      rotationMatrix = tiltedMatrix,
      mode = MeasureMode.SURFACE,
      pitchOffsetDeg = uncalibrated.pitch,
      rollOffsetDeg = uncalibrated.roll
    )
    assertEquals(0f, calibrated.dipAngle, 0.05f)
  }

  @Test
  fun dippingEast45Degrees_returnsNorthSouthStrikeAnd45DipEast() {
    val rad45 = Math.toRadians(45.0).toFloat()
    val c = cos(rad45)
    val s = sin(rad45)

    val matrix = floatArrayOf(
      c, 0f, s,
      0f, 1f, 0f,
      -s, 0f, c
    )

    val reading = ClinometerCalculator.computeFromRotationMatrix(
      rotationMatrix = matrix,
      mode = MeasureMode.SURFACE,
      declinationDeg = 0f
    )

    assertEquals(45f, reading.dipAngle, 0.1f)
    assertEquals(90f, reading.dipDirection, 0.1f)
    assertEquals(0f, reading.strikeAzimuth, 0.1f)
  }

  @Test
  fun formatJapaneseStrike_formatsCorrectly() {
    assertEquals("N-S (N 0° E)", ClinometerCalculator.formatJapaneseStrike(0f))
    assertEquals("N 30° E", ClinometerCalculator.formatJapaneseStrike(30f))
    assertEquals("E-W (N 90° E)", ClinometerCalculator.formatJapaneseStrike(90f))
    assertEquals("N 40° W", ClinometerCalculator.formatJapaneseStrike(140f))
    assertEquals("N 30° E", ClinometerCalculator.formatJapaneseStrike(210f))
  }

  @Test
  fun formatJapaneseDip_supportsJapaneseAndEnglish() {
    assertEquals("0° (水平)", ClinometerCalculator.formatJapaneseDip(0f, 120f, AppLanguage.JA))
    assertEquals("0° (Flat)", ClinometerCalculator.formatJapaneseDip(0f, 120f, AppLanguage.EN))
    assertEquals("42° SE", ClinometerCalculator.formatJapaneseDip(42f, 135f, AppLanguage.JA))
    assertEquals("90° (直立)", ClinometerCalculator.formatJapaneseDip(90f, 90f, AppLanguage.JA))
    assertEquals("90° (Vert)", ClinometerCalculator.formatJapaneseDip(90f, 90f, AppLanguage.EN))
  }

  @Test
  fun buildSpeechText_generatesLocalizedSpeech() {
    val reading = ClinometerReading(
      strikeAzimuth = 35f,
      dipAngle = 42f,
      dipDirection = 125f
    )
    val jaSpeech = ClinometerCalculator.buildSpeechText(reading, AppLanguage.JA)
    val enSpeech = ClinometerCalculator.buildSpeechText(reading, AppLanguage.EN)
    val koSpeech = ClinometerCalculator.buildSpeechText(reading, AppLanguage.KO)
    val esSpeech = ClinometerCalculator.buildSpeechText(reading, AppLanguage.ES)

    assertEquals("走向 北 35度 東、傾斜 42度 南東", jaSpeech)
    assertEquals("Strike North 35 degrees East, Dip 42 degrees SE", enSpeech)
    assertEquals("주향 북 35도 동, 경사 42도 SE", koSpeech)
    assertEquals("Rumbo Norte 35 grados Este, Buzamiento 42 grados SE", esSpeech)
  }

  @Test
  fun i18nJsonFiles_existForAllLanguagesAndContainAllBaseKeys() {
    val assetsDir = File("src/main/assets/i18n").let {
      if (it.exists()) it else File("app/src/main/assets/i18n")
    }
    assertTrue("i18n directory must exist", assetsDir.exists())

    val enText = File(assetsDir, "en.json").readText(Charsets.UTF_8)
    val keyRegex = Regex(""""([a-zA-Z0-9_]+)"\s*:""")
    val baseKeys = keyRegex.findAll(enText).map { it.groupValues[1] }.toSet()
    assertTrue("en.json must have at least 60 keys", baseKeys.size >= 60)

    for (lang in AppLanguage.entries) {
      val langFile = File(assetsDir, "${lang.code}.json")
      assertTrue("Missing translation file for ${lang.code}", langFile.exists())
      val content = langFile.readText(Charsets.UTF_8)
      val keys = keyRegex.findAll(content).map { it.groupValues[1] }.toSet()
      val missing = baseKeys - keys
      assertTrue("Language ${lang.code} is missing keys: $missing", missing.isEmpty())
      assertFalse(
        "Language ${lang.code} must not contain 'POCO F7'",
        content.contains("POCO F7", ignoreCase = true)
      )
    }
  }

  @Test
  fun smoothAngle_interpolatesAcrossZeroBoundary() {
    val smoothed = ClinometerCalculator.smoothAngle(350f, 10f, 0.5f)
    assertTrue("Expected near 0 or 360, but was $smoothed", smoothed < 1f || smoothed > 359f)
  }
}
