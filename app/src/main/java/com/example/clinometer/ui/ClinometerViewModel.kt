package com.example.clinometer.ui

import android.app.Application
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinometer.domain.ClinometerCalculator
import com.example.clinometer.model.ClinometerReading
import com.example.clinometer.model.MeasureMode
import com.example.clinometer.model.MeasurementRecord
import com.example.clinometer.model.NotationFormat
import com.example.clinometer.sensor.ClinometerSensorManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 画面全体の UI 状態
 */
data class ClinometerUiState(
  val reading: ClinometerReading = ClinometerReading(
    strikeAzimuth = 35f,
    dipAngle = 42f,
    dipDirection = 125f,
    deviceAzimuth = 35f
  ),
  val measureMode: MeasureMode = MeasureMode.SURFACE,
  val notationFormat: NotationFormat = NotationFormat.JAPANESE,
  val isHeld: Boolean = false,
  val reverseEastWest: Boolean = true,
  val declinationDeg: Float = -5.5f, // 沖縄・南西諸島〜日本標準の初期偏角 (西偏)
  val sensorAccuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
  val isSimulationMode: Boolean = false,
  val records: List<MeasurementRecord> = emptyList()
)

class ClinometerViewModel(application: Application) : AndroidViewModel(application) {

  private val clinometerSensorManager = ClinometerSensorManager(application)

  private val _uiState = MutableStateFlow(
    ClinometerUiState(isSimulationMode = !clinometerSensorManager.hasSensors)
  )
  val uiState: StateFlow<ClinometerUiState> = _uiState.asStateFlow()

  private var lastRawMatrix: FloatArray? = null

  init {
    if (clinometerSensorManager.hasSensors) {
      viewModelScope.launch {
        clinometerSensorManager.rotationMatrixFlow().collect { (matrix, accuracy) ->
          lastRawMatrix = matrix
          val state = _uiState.value
          if (!state.isHeld && !state.isSimulationMode) {
            val rawReading = ClinometerCalculator.computeFromRotationMatrix(
              rotationMatrix = matrix,
              mode = state.measureMode,
              declinationDeg = state.declinationDeg
            )
            val smoothed = ClinometerCalculator.smoothReading(
              previous = state.reading,
              current = rawReading,
              alpha = 0.22f
            )
            _uiState.update {
              it.copy(
                reading = smoothed,
                sensorAccuracy = accuracy
              )
            }
          }
        }
      }
    }
  }

  /**
   * HOLD (計測値固定) の切り替え
   */
  fun toggleHold(): Boolean {
    val nextHeld = !_uiState.value.isHeld
    _uiState.update { it.copy(isHeld = nextHeld) }
    return nextHeld
  }

  /**
   * 測定モード (面測定 / 側面測定) の変更
   */
  fun setMeasureMode(mode: MeasureMode) {
    _uiState.update { it.copy(measureMode = mode) }
    recomputeFromLastMatrix()
  }

  /**
   * 表記フォーマット (日本式 / RHR / DipDir) の変更
   */
  fun setNotationFormat(format: NotationFormat) {
    _uiState.update { it.copy(notationFormat = format) }
  }

  /**
   * コンパス盤面の E/W 逆転 (日本式クリノメーター盤) の切り替え
   */
  fun toggleReverseEastWest() {
    _uiState.update { it.copy(reverseEastWest = !it.reverseEastWest) }
  }

  /**
   * 磁気偏角 (度) の更新
   */
  fun setDeclination(declinationDeg: Float) {
    _uiState.update { it.copy(declinationDeg = declinationDeg) }
    recomputeFromLastMatrix()
  }

  /**
   * シミュレーションモードの切り替え
   */
  fun setSimulationMode(enabled: Boolean) {
    _uiState.update { it.copy(isSimulationMode = enabled, isHeld = false) }
  }

  /**
   * シミュレーションモードでの手動パラメータ更新
   */
  fun updateSimulatedReading(
    strikeAzimuth: Float,
    dipAngle: Float,
    deviceAzimuth: Float
  ) {
    val normStrike = ClinometerCalculator.normalizeDegrees(strikeAzimuth)
    val clampedDip = dipAngle.coerceIn(0f, 90f)
    val dipDir = ClinometerCalculator.normalizeDegrees(normStrike + 90f)
    val dipRad = Math.toRadians(clampedDip.toDouble())
    val dirRad = Math.toRadians(dipDir.toDouble())
    val tiltMag = sin(dipRad).toFloat()

    _uiState.update {
      it.copy(
        reading = ClinometerReading(
          strikeAzimuth = normStrike,
          dipAngle = clampedDip,
          dipDirection = dipDir,
          deviceAzimuth = ClinometerCalculator.normalizeDegrees(deviceAzimuth),
          pitch = clampedDip * cos(dirRad).toFloat(),
          roll = clampedDip * sin(dirRad).toFloat(),
          tiltX = -sin(dirRad).toFloat() * tiltMag,
          tiltY = -cos(dirRad).toFloat() * tiltMag
        )
      )
    }
  }

  /**
   * 現在の計測値をログに保存する
   */
  fun saveCurrentReading(note: String = "") {
    val state = _uiState.value
    val reading = state.reading
    val record = MeasurementRecord(
      id = System.currentTimeMillis(),
      timestampMillis = System.currentTimeMillis(),
      strikeFormatted = ClinometerCalculator.formatJapaneseStrike(reading.strikeAzimuth),
      dipFormatted = ClinometerCalculator.formatJapaneseDip(reading.dipAngle, reading.dipDirection),
      strikeAzimuth = reading.strikeAzimuth,
      dipAngle = reading.dipAngle,
      dipDirection = reading.dipDirection,
      mode = state.measureMode,
      note = note.trim()
    )
    _uiState.update { it.copy(records = listOf(record) + it.records) }
  }

  /**
   * 保存済みレコードの削除
   */
  fun deleteRecord(id: Long) {
    _uiState.update { state ->
      state.copy(records = state.records.filterNot { it.id == id })
    }
  }

  /**
   * 保存済みレコードを CSV 文字列に変換する
   */
  fun exportRecordsCsv(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
    val sb = StringBuilder()
    sb.appendLine("日時,日本式走向,日本式傾斜,走向方位(RHR),傾斜角(Dip),傾斜方位(DipDir),測定モード,メモ")
    for (rec in _uiState.value.records) {
      val timeStr = dateFormat.format(Date(rec.timestampMillis))
      val safeNote = rec.note.replace(",", " ")
      sb.appendLine(
        "$timeStr,${rec.strikeFormatted},${rec.dipFormatted}," +
          "%.1f,%.1f,%.1f,%s,%s".format(
            Locale.US,
            rec.strikeAzimuth,
            rec.dipAngle,
            rec.dipDirection,
            rec.mode.label,
            safeNote
          )
      )
    }
    return sb.toString()
  }

  private fun recomputeFromLastMatrix() {
    val matrix = lastRawMatrix ?: return
    val state = _uiState.value
    if (!state.isSimulationMode) {
      val updated = ClinometerCalculator.computeFromRotationMatrix(
        rotationMatrix = matrix,
        mode = state.measureMode,
        declinationDeg = state.declinationDeg
      )
      _uiState.update { it.copy(reading = updated) }
    }
  }
}
