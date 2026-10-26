package com.example.clinometer

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.view.KeyEvent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clinometer.domain.ClinometerCalculator
import com.example.clinometer.model.AppLanguage
import com.example.clinometer.ui.ClinometerScreen
import com.example.clinometer.ui.ClinometerViewModel
import com.example.clinometer.ui.i18n.AppStringsProvider
import com.example.clinometer.ui.theme.ClinometerTheme
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

  private val viewModel: ClinometerViewModel by viewModels()
  private var tts: TextToSpeech? = null
  private var isTtsReady = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 野外計測中に画面がスリープしないよう常時ONに設定
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    tts = TextToSpeech(this, this)

    setContent {
      ClinometerTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val strings = remember(uiState.language) {
          AppStringsProvider.load(this@MainActivity, uiState.language)
        }

        ClinometerScreen(
          uiState = uiState,
          onToggleHold = {
            val isNowHeld = viewModel.toggleHold()
            if (isNowHeld) speakReadingIfNeeded()
          },
          onSelectLanguage = { viewModel.setLanguage(it) },
          onSelectMeasureMode = { viewModel.setMeasureMode(it) },
          onSelectNotationFormat = { viewModel.setNotationFormat(it) },
          onToggleReverseEastWest = { viewModel.toggleReverseEastWest() },
          onUpdateDeclination = { viewModel.setDeclination(it) },
          onRequestGpsDeclination = {
            viewModel.updateDeclinationFromGps { result ->
              if (result != null) {
                Toast.makeText(
                  this,
                  strings.gpsSuccessToast(result.declinationDeg, result.latitude, result.longitude),
                  Toast.LENGTH_SHORT
                ).show()
              } else {
                Toast.makeText(this, strings.gpsFallbackToast, Toast.LENGTH_SHORT).show()
              }
            }
          },
          onCalibrateFlatSurface = { viewModel.calibrateFlatSurface() },
          onResetLevelCalibration = { viewModel.resetLevelCalibration() },
          onToggleVoiceReadout = { viewModel.setVoiceReadout(it) },
          onOpenOnboarding = { viewModel.openOnboarding() },
          onCompleteOnboarding = { viewModel.completeOnboarding() },
          onToggleSimulationMode = { viewModel.setSimulationMode(it) },
          onUpdateSimulatedReading = { strike, dip, devAzimuth ->
            viewModel.updateSimulatedReading(strike, dip, devAzimuth)
          },
          onSaveRecord = { note -> viewModel.saveCurrentReading(note) },
          onDeleteRecord = { id -> viewModel.deleteRecord(id) },
          onExportCsv = { viewModel.exportRecordsCsv() }
        )
      }
    }
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isTtsReady = true
    }
  }

  override fun onDestroy() {
    tts?.stop()
    tts?.shutdown()
    super.onDestroy()
  }

  /**
   * オーバーハング面や画面が見えない角度での計測時に、
   * 音量キー (Up / Down) で HOLD (固定/解除) と音声読み上げを実行する
   */
  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      val isNowHeld = viewModel.toggleHold()
      vibrateOnHold(isNowHeld)
      if (isNowHeld) speakReadingIfNeeded()
      return true
    }
    return super.onKeyDown(keyCode, event)
  }

  private fun speakReadingIfNeeded() {
    val state = viewModel.uiState.value
    if (!state.voiceReadoutEnabled || !isTtsReady) return
    val engine = tts ?: return

    val locale = Locale.forLanguageTag(state.language.localeTag)
    engine.language = locale
    val speechText = ClinometerCalculator.buildSpeechText(state.reading, state.language)
    engine.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "hold_readout")
  }

  private fun vibrateOnHold(isHeld: Boolean) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val manager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
      manager.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    if (vibrator.hasVibrator()) {
      val duration = if (isHeld) 70L else 35L
      vibrator.vibrate(
        VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
      )
    }
  }
}
