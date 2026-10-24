package com.example.clinometer

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.clinometer.ui.ClinometerScreen
import com.example.clinometer.ui.ClinometerViewModel
import com.example.clinometer.ui.theme.ClinometerTheme

class MainActivity : ComponentActivity() {

  private val viewModel: ClinometerViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 野外での計測中に画面が自動スリープしないよう常時ONに設定
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    setContent {
      ClinometerTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        ClinometerScreen(
          uiState = uiState,
          onToggleHold = { viewModel.toggleHold() },
          onSelectMeasureMode = { viewModel.setMeasureMode(it) },
          onSelectNotationFormat = { viewModel.setNotationFormat(it) },
          onToggleReverseEastWest = { viewModel.toggleReverseEastWest() },
          onUpdateDeclination = { viewModel.setDeclination(it) },
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

  /**
   * オーバーハング面や画面が見えない角度での計測時に、
   * 音量キー (Up / Down) で HOLD (固定/解除) を操作できるようにする
   */
  override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      val isNowHeld = viewModel.toggleHold()
      vibrateOnHold(isNowHeld)
      return true
    }
    return super.onKeyDown(keyCode, event)
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
