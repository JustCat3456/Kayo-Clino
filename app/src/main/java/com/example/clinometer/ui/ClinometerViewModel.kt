package com.example.clinometer.ui

import android.app.Application
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinometer.data.ClinometerStorage
import com.example.clinometer.domain.ClinometerCalculator
import com.example.clinometer.model.AppLanguage
import com.example.clinometer.model.ClinometerReading
import com.example.clinometer.model.MeasureMode
import com.example.clinometer.model.MeasurementRecord
import com.example.clinometer.model.NotationFormat
import com.example.clinometer.model.ThemeMode
import com.example.clinometer.sensor.ClinometerSensorManager
import com.example.clinometer.sensor.LocationDeclinationHelper
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
  val language: AppLanguage = AppLanguage.JA,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val measureMode: MeasureMode = MeasureMode.SURFACE,
  val notationFormat: NotationFormat = NotationFormat.JAPANESE,
  val isHeld: Boolean = false,
  val reverseEastWest: Boolean = true,
  val declinationDeg: Float = -5.5f,
  val pitchOffsetDeg: Float = 0f,
  val rollOffsetDeg: Float = 0f,
  val voiceReadoutEnabled: Boolean = false,
  val showOnboarding: Boolean = true,
  val isGpsLoading: Boolean = false,
  val lastLatitude: Double? = null,
  val lastLongitude: Double? = null,
  val sensorAccuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_HIGH,
  val isSimulationMode: Boolean = false,
  val records: List<MeasurementRecord> = emptyList()
) {
  val isLevelCalibrated: Boolean
    get() = kotlin.math.abs(pitchOffsetDeg) > 0.05f || kotlin.math.abs(rollOffsetDeg) > 0.05f
}

class ClinometerViewModel(application: Application) : AndroidViewModel(application) {

  private val storage = ClinometerStorage(application)
  private val clinometerSensorManager = ClinometerSensorManager(application)
  private val locationHelper = LocationDeclinationHelper(application)

  private val _uiState: MutableStateFlow<ClinometerUiState>
  val uiState: StateFlow<ClinometerUiState>

  private var lastRawMatrix: FloatArray? = null

  init {
    val saved = storage.loadSettings()
    val savedRecords = storage.loadRecords()

    _uiState = MutableStateFlow(
      ClinometerUiState(
        language = saved.language,
        themeMode = saved.themeMode,
        measureMode = saved.measureMode,
        notationFormat = saved.notationFormat,
        reverseEastWest = saved.reverseEastWest,
        declinationDeg = saved.declinationDeg,
        pitchOffsetDeg = saved.pitchOffsetDeg,
        rollOffsetDeg = saved.rollOffsetDeg,
        voiceReadoutEnabled = saved.voiceReadoutEnabled,
        showOnboarding = !saved.hasCompletedOnboarding,
        isSimulationMode = !clinometerSensorManager.hasSensors,
        records = savedRecords
      )
    )
    uiState = _uiState.asStateFlow()

    if (clinometerSensorManager.hasSensors) {
      viewModelScope.launch {
        clinometerSensorManager.rotationMatrixFlow().collect { (matrix, accuracy) ->
          lastRawMatrix = matrix
          val state = _uiState.value
          if (!state.isHeld && !state.isSimulationMode) {
            val rawReading = ClinometerCalculator.computeFromRotationMatrix(
              rotationMatrix = matrix,
              mode = state.measureMode,
              declinationDeg = state.declinationDeg,
              pitchOffsetDeg = state.pitchOffsetDeg,
              rollOffsetDeg = state.rollOffsetDeg
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

  fun toggleHold(): Boolean {
    val nextHeld = !_uiState.value.isHeld
    _uiState.update { it.copy(isHeld = nextHeld) }
    return nextHeld
  }

  fun setLanguage(language: AppLanguage) {
    storage.saveLanguage(language)
    _uiState.update { it.copy(language = language) }
  }

  fun setThemeMode(themeMode: ThemeMode) {
    storage.saveThemeMode(themeMode)
    _uiState.update { it.copy(themeMode = themeMode) }
  }

  fun setMeasureMode(mode: MeasureMode) {
    storage.saveMeasureMode(mode)
    _uiState.update { it.copy(measureMode = mode) }
    recomputeFromLastMatrix()
  }

  fun setNotationFormat(format: NotationFormat) {
    storage.saveNotationFormat(format)
    _uiState.update { it.copy(notationFormat = format) }
  }

  fun toggleReverseEastWest() {
    val next = !_uiState.value.reverseEastWest
    storage.saveReverseEastWest(next)
    _uiState.update { it.copy(reverseEastWest = next) }
  }

  fun setDeclination(declinationDeg: Float) {
    storage.saveDeclination(declinationDeg)
    _uiState.update { it.copy(declinationDeg = declinationDeg) }
    recomputeFromLastMatrix()
  }

  fun setVoiceReadout(enabled: Boolean) {
    storage.saveVoiceReadout(enabled)
    _uiState.update { it.copy(voiceReadoutEnabled = enabled) }
  }

  fun openOnboarding() {
    _uiState.update { it.copy(showOnboarding = true) }
  }

  fun completeOnboarding() {
    storage.saveOnboardingCompleted(true)
    _uiState.update { it.copy(showOnboarding = false) }
  }

  /**
   * カメラ出っ張り等の傾きを打ち消す水平ゼロ点キャリブレーション
   */
  fun calibrateFlatSurface() {
    val matrix = lastRawMatrix
    if (matrix != null) {
      val uncalibrated = ClinometerCalculator.computeFromRotationMatrix(
        rotationMatrix = matrix,
        mode = MeasureMode.SURFACE,
        declinationDeg = _uiState.value.declinationDeg,
        pitchOffsetDeg = 0f,
        rollOffsetDeg = 0f
      )
      val pOffset = uncalibrated.pitch.coerceIn(-15f, 15f)
      val rOffset = uncalibrated.roll.coerceIn(-15f, 15f)
      storage.saveLevelCalibration(pOffset, rOffset)
      _uiState.update {
        it.copy(pitchOffsetDeg = pOffset, rollOffsetDeg = rOffset)
      }
      recomputeFromLastMatrix()
    } else {
      storage.saveLevelCalibration(0f, 0f)
      _uiState.update {
        it.copy(
          pitchOffsetDeg = 0f,
          rollOffsetDeg = 0f,
          reading = it.reading.copy(dipAngle = 0f, tiltX = 0f, tiltY = 0f)
        )
      }
    }
  }

  fun resetLevelCalibration() {
    storage.saveLevelCalibration(0f, 0f)
    _uiState.update { it.copy(pitchOffsetDeg = 0f, rollOffsetDeg = 0f) }
    recomputeFromLastMatrix()
  }

  /**
   * GPS現在地を取得し、世界磁気モデル(WMM)から磁気偏角を自動計算して設定する
   */
  fun updateDeclinationFromGps(
    onResult: (LocationDeclinationHelper.DeclinationResult?) -> Unit
  ) {
    viewModelScope.launch {
      _uiState.update { it.copy(isGpsLoading = true) }
      val result = locationHelper.fetchCurrentLocationAndDeclination()
      if (result != null) {
        storage.saveDeclination(result.declinationDeg)
        _uiState.update {
          it.copy(
            declinationDeg = result.declinationDeg,
            lastLatitude = result.latitude,
            lastLongitude = result.longitude,
            isGpsLoading = false
          )
        }
        recomputeFromLastMatrix()
      } else {
        _uiState.update { it.copy(isGpsLoading = false) }
      }
      onResult(result)
    }
  }

  fun setSimulationMode(enabled: Boolean) {
    _uiState.update { it.copy(isSimulationMode = enabled, isHeld = false) }
  }

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

  fun saveCurrentReading(note: String = "") {
    val state = _uiState.value
    val reading = state.reading
    val record = MeasurementRecord(
      id = System.currentTimeMillis(),
      timestampMillis = System.currentTimeMillis(),
      strikeFormatted = ClinometerCalculator.formatJapaneseStrike(reading.strikeAzimuth),
      dipFormatted = ClinometerCalculator.formatJapaneseDip(
        reading.dipAngle,
        reading.dipDirection,
        state.language
      ),
      strikeAzimuth = reading.strikeAzimuth,
      dipAngle = reading.dipAngle,
      dipDirection = reading.dipDirection,
      mode = state.measureMode,
      note = note.trim(),
      latitude = state.lastLatitude,
      longitude = state.lastLongitude
    )
    val updatedList = listOf(record) + state.records
    storage.saveRecords(updatedList)
    _uiState.update { it.copy(records = updatedList) }
  }

  fun deleteRecord(id: Long) {
    val updatedList = _uiState.value.records.filterNot { it.id == id }
    storage.saveRecords(updatedList)
    _uiState.update { it.copy(records = updatedList) }
  }

  fun exportRecordsCsv(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
    val sb = StringBuilder()
    sb.appendLine("Timestamp,Strike(Quadrant),Dip,StrikeAzimuth(RHR),DipAngle,DipDirection,Mode,Lat,Lon,Note")
    for (rec in _uiState.value.records) {
      val timeStr = dateFormat.format(Date(rec.timestampMillis))
      val safeNote = rec.note.replace(",", " ")
      val latStr = rec.latitude?.let { "%.5f".format(Locale.US, it) } ?: ""
      val lonStr = rec.longitude?.let { "%.5f".format(Locale.US, it) } ?: ""
      sb.appendLine(
        "$timeStr,${rec.strikeFormatted},${rec.dipFormatted}," +
          "%.1f,%.1f,%.1f,%s,%s,%s,%s".format(
            Locale.US,
            rec.strikeAzimuth,
            rec.dipAngle,
            rec.dipDirection,
            rec.mode.name,
            latStr,
            lonStr,
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
        declinationDeg = state.declinationDeg,
        pitchOffsetDeg = state.pitchOffsetDeg,
        rollOffsetDeg = state.rollOffsetDeg
      )
      _uiState.update { it.copy(reading = updated) }
    }
  }
}
