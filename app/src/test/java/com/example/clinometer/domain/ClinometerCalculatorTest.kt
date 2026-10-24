package com.example.clinometer.domain

import com.example.clinometer.model.MeasureMode
import kotlin.math.cos
import kotlin.math.sin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClinometerCalculatorTest {

  @Test
  fun horizontalPlane_returnsZeroDip() {
    // 単位行列: 端末が水平で上端が北を向いている状態
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
  fun dippingEast45Degrees_returnsNorthSouthStrikeAnd45DipEast() {
    // 東 (+X) 方向へ 45° 傾斜している面 (法線ベクトルが 東45°・天頂45°)
    val rad45 = Math.toRadians(45.0).toFloat()
    val c = cos(rad45)
    val s = sin(rad45)

    // Z軸 (第3列) = (sin45, 0, cos45), Y軸 (第2列) = (0, 1, 0)
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
    // 180° 周期の対称性確認 (210° -> N 30° E)
    assertEquals("N 30° E", ClinometerCalculator.formatJapaneseStrike(210f))
  }

  @Test
  fun formatJapaneseDip_formatsCorrectly() {
    assertEquals("0° (水平)", ClinometerCalculator.formatJapaneseDip(0f, 120f))
    assertEquals("42° SE", ClinometerCalculator.formatJapaneseDip(42f, 135f))
    assertEquals("90° (直立)", ClinometerCalculator.formatJapaneseDip(90f, 90f))
  }

  @Test
  fun smoothAngle_interpolatesAcrossZeroBoundary() {
    // 350° から 10° へ alpha=0.5 で補間すると 0° (または 360°) 付近になること
    val smoothed = ClinometerCalculator.smoothAngle(350f, 10f, 0.5f)
    assertTrue("Expected near 0 or 360, but was $smoothed", smoothed < 1f || smoothed > 359f)
  }
}
