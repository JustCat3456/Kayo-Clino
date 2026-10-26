package com.example.clinometer.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clinometer.R
import com.example.clinometer.domain.ClinometerCalculator
import com.example.clinometer.model.AppLanguage
import com.example.clinometer.model.MeasureMode
import com.example.clinometer.model.MeasurementRecord
import com.example.clinometer.model.NotationFormat
import com.example.clinometer.ui.components.ClinometerDial
import com.example.clinometer.ui.components.ClinometerHelpSheet
import com.example.clinometer.ui.components.OnboardingCarouselDialog
import com.example.clinometer.ui.i18n.AppStrings
import com.example.clinometer.ui.i18n.AppStringsProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinometerScreen(
  uiState: ClinometerUiState,
  onToggleHold: () -> Unit,
  onSelectLanguage: (AppLanguage) -> Unit,
  onSelectMeasureMode: (MeasureMode) -> Unit,
  onSelectNotationFormat: (NotationFormat) -> Unit,
  onToggleReverseEastWest: () -> Unit,
  onUpdateDeclination: (Float) -> Unit,
  onRequestGpsDeclination: () -> Unit,
  onCalibrateFlatSurface: () -> Unit,
  onResetLevelCalibration: () -> Unit,
  onToggleVoiceReadout: (Boolean) -> Unit,
  onOpenOnboarding: () -> Unit,
  onCompleteOnboarding: () -> Unit,
  onToggleSimulationMode: (Boolean) -> Unit,
  onUpdateSimulatedReading: (Float, Float, Float) -> Unit,
  onSaveRecord: (String) -> Unit,
  onDeleteRecord: (Long) -> Unit,
  onExportCsv: () -> String,
  modifier: Modifier = Modifier
) {
  val view = LocalView.current
  val context = LocalContext.current
  val strings = remember(uiState.language) {
    AppStringsProvider.load(context, uiState.language)
  }

  var showSettingsDialog by remember { mutableStateOf(false) }
  var showSaveNoteDialog by remember { mutableStateOf(false) }
  var showRecordsSheet by remember { mutableStateOf(false) }
  var showHelpSheet by remember { mutableStateOf(false) }

  // 位置情報パーミッションランチャー
  val locationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    if (granted) {
      onRequestGpsDeclination()
    } else {
      Toast.makeText(context, strings.gpsFallbackToast, Toast.LENGTH_SHORT).show()
    }
  }

  val triggerGpsDeclination = {
    locationPermissionLauncher.launch(
      arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
      )
    )
  }

  val reading = uiState.reading
  val strikeText = ClinometerCalculator.formatStrike(reading, uiState.notationFormat)
  val dipText = ClinometerCalculator.formatDip(reading, uiState.notationFormat, uiState.language)

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = strings.appTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = buildString {
                append(
                  if (uiState.reverseEastWest) {
                    strings.clinoCompassPlate
                  } else {
                    strings.standardCompassPlate
                  }
                )
                append(" ・ ${strings.declinationPrefix} ${"%.1f".format(uiState.declinationDeg)}°")
                if (uiState.isLevelCalibrated) append(" ・ [${strings.levelCalibratedBadge}]")
                if (uiState.isSimulationMode) append(" ・ [SIM]")
              },
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        },
        actions = {
          // 使い方ヘルプページ表示ボタン (カルーセルではなく一覧型ヘルプシートを開く)
          IconButton(onClick = { showHelpSheet = true }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.HelpOutline,
              contentDescription = "Help"
            )
          }

          // E/W 反転切替ボタン
          IconButton(onClick = onToggleReverseEastWest) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "E/W Swap",
              tint = if (uiState.reverseEastWest) {
                MaterialTheme.colorScheme.primary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              }
            )
          }

          // 計測記録 (野帳) ボトムシート表示ボタン
          IconButton(onClick = { showRecordsSheet = true }) {
            BadgedBox(
              badge = {
                if (uiState.records.isNotEmpty()) {
                  Badge { Text(uiState.records.size.toString()) }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = "History"
              )
            }
          }

          // 設定ボタン
          IconButton(onClick = { showSettingsDialog = true }) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(6.dp))

      // 磁気干渉アラートバナー (精度低下時のみ表示)
      val hasLowAccuracy = !uiState.isSimulationMode && (
        uiState.sensorAccuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW ||
          uiState.sensorAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
        )
      AnimatedVisibility(visible = hasLowAccuracy) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        ) {
          Text(
            text = strings.magneticInterferenceWarning,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          )
        }
      }

      // 1. 測定モード切替 (面測定 vs 側面測定)
      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        MeasureMode.entries.forEachIndexed { index, mode ->
          SegmentedButton(
            selected = uiState.measureMode == mode,
            onClick = { onSelectMeasureMode(mode) },
            colors = SegmentedButtonDefaults.colors(
              activeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              activeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
              inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = MeasureMode.entries.size
            )
          ) {
            Text(
              text = strings.modeLabel(mode),
              style = MaterialTheme.typography.labelMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. 走向 (Strike) & 傾斜 (Dip) 大型カード表示
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        MeasurementCard(
          title = if (uiState.notationFormat == NotationFormat.DIP_DIRECTION) {
            strings.dipDirectionLabel
          } else {
            strings.strikeLabel
          },
          mainValue = strikeText,
          subValue = strings.rhrHeadingSub(
            reading.strikeAzimuth.roundToInt() % 360,
            reading.deviceAzimuth.roundToInt() % 360
          ),
          accentColor = MaterialTheme.colorScheme.primary,
          isHeld = uiState.isHeld,
          modifier = Modifier.weight(1f)
        )

        MeasurementCard(
          title = strings.dipLabel,
          mainValue = dipText,
          subValue = strings.dipDirSub(
            reading.dipDirection.roundToInt() % 360,
            ClinometerCalculator.toCardinal8(reading.dipDirection)
          ),
          accentColor = MaterialTheme.colorScheme.secondary,
          isHeld = uiState.isHeld,
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. 表記フォーマット切替チップ
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        NotationFormat.entries.forEach { format ->
          FilterChip(
            selected = uiState.notationFormat == format,
            onClick = { onSelectNotationFormat(format) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            label = {
              Text(
                text = strings.formatShortLabel(format),
                style = MaterialTheme.typography.labelSmall
              )
            }
          )
        }
      }

      // 4. ビジュアルクリノメーター盤 (タップでもHOLD切替可能)
      Box(
        modifier = Modifier
          .fillMaxWidth(0.88f)
          .clickable {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onToggleHold()
          },
        contentAlignment = Alignment.Center
      ) {
        ClinometerDial(
          reading = reading,
          reverseEastWest = uiState.reverseEastWest,
          isHeld = uiState.isHeld
        )

        if (uiState.isHeld) {
          Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = strings.holdLockedBadge,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
              )
            }
          }
        }
      }

      // 5. シミュレーション操作パネル (シミュレーションモード有効時)
      AnimatedVisibility(visible = uiState.isSimulationMode) {
        SimulationControlCard(
          strings = strings,
          reading = reading,
          onUpdateSimulatedReading = onUpdateSimulatedReading,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 6. 下部アクションボタン (HOLD & 野帳記録)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Button(
          onClick = {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            onToggleHold()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (uiState.isHeld) {
              MaterialTheme.colorScheme.tertiary
            } else {
              MaterialTheme.colorScheme.primary
            }
          ),
          shape = RoundedCornerShape(16.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          modifier = Modifier.weight(1.2f)
        ) {
          Icon(
            imageVector = if (uiState.isHeld) Icons.Default.LockOpen else Icons.Default.Lock,
            contentDescription = null
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (uiState.isHeld) strings.holdReleaseBtn else strings.holdLockBtn,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }

        FilledTonalButton(
          onClick = { showSaveNoteDialog = true },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
          ),
          shape = RoundedCornerShape(16.dp),
          contentPadding = PaddingValues(vertical = 14.dp),
          modifier = Modifier.weight(0.8f)
        ) {
          Icon(
            imageVector = Icons.Default.BookmarkAdd,
            contentDescription = null
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = strings.saveToFieldNoteBtn,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // 初回オンボーディング・使い方カルーセル
  if (uiState.showOnboarding) {
    OnboardingCarouselDialog(
      strings = strings,
      language = uiState.language,
      declinationDeg = uiState.declinationDeg,
      reverseEastWest = uiState.reverseEastWest,
      voiceReadoutEnabled = uiState.voiceReadoutEnabled,
      isGpsLoading = uiState.isGpsLoading,
      onSelectLanguage = onSelectLanguage,
      onUpdateDeclination = onUpdateDeclination,
      onRequestGpsDeclination = triggerGpsDeclination,
      onToggleReverseEastWest = onToggleReverseEastWest,
      onToggleVoiceReadout = onToggleVoiceReadout,
      onFinish = onCompleteOnboarding
    )
  }

  // 記録保存ダイアログ
  if (showSaveNoteDialog) {
    SaveMeasurementDialog(
      strings = strings,
      strikeText = ClinometerCalculator.formatJapaneseStrike(reading.strikeAzimuth),
      dipText = ClinometerCalculator.formatJapaneseDip(
        reading.dipAngle,
        reading.dipDirection,
        uiState.language
      ),
      onDismiss = { showSaveNoteDialog = false },
      onConfirm = { note ->
        onSaveRecord(note)
        showSaveNoteDialog = false
        Toast.makeText(context, strings.savedToast, Toast.LENGTH_SHORT).show()
      }
    )
  }

  // 設定ダイアログ
  if (showSettingsDialog) {
    ClinometerSettingsDialog(
      strings = strings,
      language = uiState.language,
      declinationDeg = uiState.declinationDeg,
      pitchOffsetDeg = uiState.pitchOffsetDeg,
      rollOffsetDeg = uiState.rollOffsetDeg,
      reverseEastWest = uiState.reverseEastWest,
      voiceReadoutEnabled = uiState.voiceReadoutEnabled,
      isSimulationMode = uiState.isSimulationMode,
      isGpsLoading = uiState.isGpsLoading,
      onSelectLanguage = onSelectLanguage,
      onUpdateDeclination = onUpdateDeclination,
      onRequestGpsDeclination = triggerGpsDeclination,
      onCalibrateFlatSurface = {
        onCalibrateFlatSurface()
        Toast.makeText(context, strings.calibratedToast, Toast.LENGTH_SHORT).show()
      },
      onResetLevelCalibration = onResetLevelCalibration,
      onToggleReverseEastWest = onToggleReverseEastWest,
      onToggleVoiceReadout = onToggleVoiceReadout,
      onToggleSimulationMode = onToggleSimulationMode,
      onOpenHelp = {
        showSettingsDialog = false
        showHelpSheet = true
      },
      onOpenOnboarding = {
        showSettingsDialog = false
        onOpenOnboarding()
      },
      onDismiss = { showSettingsDialog = false }
    )
  }

  // 使い方・地質測定ガイドボトムシート (Wikipedia / 産総研リンク・3D図解付き・6言語対応)
  if (showHelpSheet) {
    ClinometerHelpSheet(
      strings = strings,
      onDismiss = { showHelpSheet = false }
    )
  }

  // 計測履歴 (野帳) ボトムシート
  if (showRecordsSheet) {
    ModalBottomSheet(
      onDismissRequest = { showRecordsSheet = false },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
      RecordsSheetContent(
        strings = strings,
        language = uiState.language,
        records = uiState.records,
        onDeleteRecord = onDeleteRecord,
        onCopyCsv = {
          val csv = onExportCsv()
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          clipboard.setPrimaryClip(ClipData.newPlainText("clinometer_records.csv", csv))
          Toast.makeText(context, strings.csvCopiedToast, Toast.LENGTH_SHORT).show()
        }
      )
    }
  }
}

@Composable
private fun MeasurementCard(
  title: String,
  mainValue: String,
  subValue: String,
  accentColor: androidx.compose.ui.graphics.Color,
  isHeld: Boolean,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ),
    border = BorderStroke(
      width = if (isHeld) 2.dp else 1.dp,
      color = if (isHeld) MaterialTheme.colorScheme.tertiary else accentColor.copy(alpha = 0.5f)
    )
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = accentColor,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = mainValue,
        style = MaterialTheme.typography.headlineSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.ExtraBold,
          fontSize = 22.sp
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subValue,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun SimulationControlCard(
  strings: AppStrings,
  reading: com.example.clinometer.model.ClinometerReading,
  onUpdateSimulatedReading: (Float, Float, Float) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Science,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = strings.simTitle,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSecondaryContainer
        )
      }
      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = strings.simStrikeSlider(reading.strikeAzimuth.roundToInt()),
        style = MaterialTheme.typography.labelSmall
      )
      Slider(
        value = reading.strikeAzimuth,
        onValueChange = {
          onUpdateSimulatedReading(it, reading.dipAngle, reading.deviceAzimuth)
        },
        valueRange = 0f..359f
      )

      Text(
        text = strings.simDipSlider(reading.dipAngle.roundToInt()),
        style = MaterialTheme.typography.labelSmall
      )
      Slider(
        value = reading.dipAngle,
        onValueChange = {
          onUpdateSimulatedReading(reading.strikeAzimuth, it, reading.deviceAzimuth)
        },
        valueRange = 0f..90f
      )
    }
  }
}

@Composable
private fun SaveMeasurementDialog(
  strings: AppStrings,
  strikeText: String,
  dipText: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var note by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.saveDialogTitle) },
    text = {
      Column {
        Text(
          text = "${strings.strikeLabel}: $strikeText\n${strings.dipLabel}: $dipText",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text(strings.saveDialogNoteHint) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = { onConfirm(note) }) {
        Text(strings.saveBtn)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancelBtn)
      }
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ClinometerSettingsDialog(
  strings: AppStrings,
  language: AppLanguage,
  declinationDeg: Float,
  pitchOffsetDeg: Float,
  rollOffsetDeg: Float,
  reverseEastWest: Boolean,
  voiceReadoutEnabled: Boolean,
  isSimulationMode: Boolean,
  isGpsLoading: Boolean,
  onSelectLanguage: (AppLanguage) -> Unit,
  onUpdateDeclination: (Float) -> Unit,
  onRequestGpsDeclination: () -> Unit,
  onCalibrateFlatSurface: () -> Unit,
  onResetLevelCalibration: () -> Unit,
  onToggleReverseEastWest: () -> Unit,
  onToggleVoiceReadout: (Boolean) -> Unit,
  onToggleSimulationMode: (Boolean) -> Unit,
  onOpenHelp: () -> Unit,
  onOpenOnboarding: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.settingsTitle) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // 1. 言語設定 (6言語対応・折り返し表示)
        Text(
          text = strings.languageSectionTitle,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          AppLanguage.entries.forEach { lang ->
            FilterChip(
              selected = language == lang,
              onClick = { onSelectLanguage(lang) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
              ),
              label = { Text(lang.nativeName) }
            )
          }
        }

        HorizontalDivider()

        // 2. 磁気偏角設定 + GPS自動計算
        Text(
          text = strings.declinationSectionTitle(declinationDeg),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = strings.declinationHint,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(
          onClick = onRequestGpsDeclination,
          enabled = !isGpsLoading,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.GpsFixed,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isGpsLoading) strings.gpsCalculating else strings.gpsAutoDeclinationBtn,
            style = MaterialTheme.typography.labelMedium
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val presets = listOf(
            "Okinawa -5.5°" to -5.5f,
            "Tokyo -7.8°" to -7.8f,
            "Sapporo -9.5°" to -9.5f,
            "0°" to 0.0f
          )
          presets.forEach { (label, value) ->
            AssistChip(
              onClick = { onUpdateDeclination(value) },
              label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
        Slider(
          value = declinationDeg,
          onValueChange = onUpdateDeclination,
          valueRange = -15f..15f
        )

        HorizontalDivider()

        // 3. カメラ出っ張り・水平ゼロ点補正
        Text(
          text = strings.levelCalibrationTitle,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = strings.levelCalibrationDesc(pitchOffsetDeg, rollOffsetDeg),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilledTonalButton(
            onClick = onCalibrateFlatSurface,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.Straighten,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.calibrateZeroBtn, style = MaterialTheme.typography.labelSmall)
          }
          OutlinedButton(onClick = onResetLevelCalibration) {
            Text(strings.resetCalibrationBtn, style = MaterialTheme.typography.labelSmall)
          }
        }

        HorizontalDivider()

        // 4. 日本式 E/W 逆転盤面
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.reverseEwTitle,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = strings.reverseEwDesc,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = reverseEastWest,
            onCheckedChange = { onToggleReverseEastWest() }
          )
        }

        HorizontalDivider()

        // 5. HOLD時の音声読み上げ (TTS)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.voiceReadoutTitle,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = strings.voiceReadoutDesc,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = voiceReadoutEnabled,
            onCheckedChange = onToggleVoiceReadout
          )
        }

        HorizontalDivider()

        // 6. シミュレーションモード
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.simModeTitle,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = strings.simModeDesc,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = isSimulationMode,
            onCheckedChange = onToggleSimulationMode
          )
        }

        HorizontalDivider()

        // 7. 使い方ガイド ＆ 初期設定カルーセル表示 (デバッグ用)
        FilledTonalButton(
          onClick = onOpenHelp,
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(strings.openHelpBtn)
        }

        OutlinedButton(
          onClick = onOpenOnboarding,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Science,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(strings.openOnboardingDebugBtn)
        }

        HorizontalDivider()

        // 8. アプリ情報・クレジット・リリースノート・GitHub・ライセンス (一番最後)
        AboutAndLinksSection(strings = strings)
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text(strings.closeBtn)
      }
    }
  )
}

@Composable
private fun AboutAndLinksSection(
  strings: AppStrings
) {
  val context = LocalContext.current
  var showWhatsNewDialog by remember { mutableStateOf(false) }
  var showLicenseDialog by remember { mutableStateOf(false) }
  var showWhatIsKayoDialog by remember { mutableStateOf(false) }

  Text(
    text = strings.aboutSectionTitle,
    style = MaterialTheme.typography.titleSmall,
    fontWeight = FontWeight.Bold
  )

  // Made in Okinawa & Author クレジットカード
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        context.startActivity(
          Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/JustCat3456"))
        )
      }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Kayo-Clino  v1.1.0",
          style = MaterialTheme.typography.titleSmall,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.primary
        )
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.secondaryContainer
        ) {
          Text(
            text = strings.madeInOkinawaBadge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = strings.authorLabel,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.OpenInNew,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(13.dp)
        )
      }
    }
  }

  // 🌺 What is "Kayo"? (嘉陽層と褶曲のストーリー) ボタン
  FilledTonalButton(
    onClick = { showWhatIsKayoDialog = true },
    colors = ButtonDefaults.filledTonalButtonColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Icon(
      imageVector = Icons.Default.Explore,
      contentDescription = null,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = strings.whatIsKayoBtn,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Bold
    )
  }

  // What's New / リリースノート ボタン
  FilledTonalButton(
    onClick = { showWhatsNewDialog = true },
    modifier = Modifier.fillMaxWidth()
  ) {
    Icon(
      imageVector = Icons.Default.AutoAwesome,
      contentDescription = null,
      modifier = Modifier.size(18.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(strings.whatsNewBtn, style = MaterialTheme.typography.labelMedium)
  }

  // GitHub ソースコード ＆ ライセンス ボタン (2列)
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    OutlinedButton(
      onClick = {
        context.startActivity(
          Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://github.com/JustCat3456/kayo-clino")
          )
        )
      },
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Code,
        contentDescription = null,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = strings.githubSourceBtn,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    OutlinedButton(
      onClick = { showLicenseDialog = true },
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Description,
        contentDescription = null,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = strings.licenseBtn,
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  // What's New (リリースノート) ダイアログ
  if (showWhatsNewDialog) {
    AlertDialog(
      onDismissRequest = { showWhatsNewDialog = false },
      title = {
        Text(
          text = strings.whatsNewDialogTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = strings.whatsNewV110Title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = strings.whatsNewV110Body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = strings.whatsNewV100Title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = strings.whatsNewV100Body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          OutlinedButton(
            onClick = {
              context.startActivity(
                Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse("https://github.com/JustCat3456/kayo-clino/releases")
                )
              )
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.openGithubReleasesBtn, style = MaterialTheme.typography.labelSmall)
          }
        }
      },
      confirmButton = {
        Button(onClick = { showWhatsNewDialog = false }) {
          Text(strings.closeBtn)
        }
      }
    )
  }

  // MIT ライセンス全文表示ダイアログ
  if (showLicenseDialog) {
    val mitLicenseText = """
      MIT License

      Copyright (c) 2026 JustCat3456

      Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

      The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
    """.trimIndent()

    AlertDialog(
      onDismissRequest = { showLicenseDialog = false },
      title = {
        Text(
          text = strings.licenseDialogTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = mitLicenseText,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(onClick = { showLicenseDialog = false }) {
          Text(strings.closeBtn)
        }
      }
    )
  }

  // 🌺 What is "Kayo"? (嘉陽層と褶曲の解説ダイアログ)
  if (showWhatIsKayoDialog) {
    AlertDialog(
      onDismissRequest = { showWhatIsKayoDialog = false },
      title = {
        Column {
          Text(
            text = strings.whatIsKayoTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = strings.whatIsKayoSubtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
      },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Wikimedia Commons より引用した嘉陽層バン崎露頭の実写写真と学術キャプション・ライセンス表記
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = Color(0xFF0B1120)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column {
              Image(
                painter = painterResource(id = R.drawable.kayo_formation_folds),
                contentDescription = strings.whatIsKayoPhotoCaption,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(195.dp)
                  .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
              )
              Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(
                  text = strings.whatIsKayoPhotoCaption,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFE2E8F0)
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.clickable {
                    context.startActivity(
                      Intent(Intent.ACTION_VIEW, Uri.parse(strings.whatIsKayoPhotoUrl))
                    )
                  }
                ) {
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = strings.whatIsKayoPhotoCredit,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline,
                    color = Color(0xFF38BDF8)
                  )
                }
              }
            }
          }

          // 嘉陽層のシェブロン褶曲・横臥褶曲と沖縄の海を描く構造模式図
          KayoFoldIllustration()

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = strings.whatIsKayoSec1Title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = strings.whatIsKayoSec1Body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = strings.whatIsKayoSec2Title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = strings.whatIsKayoSec2Body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = strings.whatIsKayoSec3Title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = strings.whatIsKayoSec3Body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          OutlinedButton(
            onClick = {
              context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(strings.whatIsKayoLinkUrl))
              )
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.OpenInNew,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.whatIsKayoLinkBtn, style = MaterialTheme.typography.labelSmall)
          }
        }
      },
      confirmButton = {
        Button(onClick = { showWhatIsKayoDialog = false }) {
          Text(strings.closeBtn)
        }
      }
    )
  }
}

/**
 * 沖縄・嘉陽層のタービダイト砂岩泥岩互層とダイナミックなシェブロン褶曲（Fold）を描くイラスト
 */
@Composable
private fun KayoFoldIllustration() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(125.dp)
      .background(Color(0xFF0B1120), RoundedCornerShape(14.dp))
      .padding(8.dp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      // 背景の海 (沖縄東海岸のエメラルド〜ディープブルー)
      drawRect(
        color = Color(0xFF0F2942),
        topLeft = Offset(0f, h * 0.72f),
        size = androidx.compose.ui.geometry.Size(w, h * 0.28f)
      )

      // 嘉陽層のシェブロン褶曲 (5層の砂岩・泥岩タービダイト互層)
      val layerColors = listOf(
        Color(0xFF5E4200),
        Color(0xFFFBBF24),
        Color(0xFF422C00),
        Color(0xFFD97706),
        Color(0xFF38BDF8)
      )

      layerColors.forEachIndexed { index, color ->
        val offsetY = index * (h * 0.11f)
        val foldPath = Path().apply {
          moveTo(w * 0.04f, h * 0.38f + offsetY)
          lineTo(w * 0.24f, h * 0.10f + offsetY)
          lineTo(w * 0.46f, h * 0.46f + offsetY)
          lineTo(w * 0.70f, h * 0.08f + offsetY)
          lineTo(w * 0.96f, h * 0.36f + offsetY)
        }
        drawPath(
          path = foldPath,
          color = color,
          style = Stroke(
            width = if (index % 2 == 1) 7f else 9f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
          )
        )
      }

      // 海面の波ライン (エメラルドグリーン)
      val wavePath = Path().apply {
        moveTo(0f, h * 0.84f)
        quadraticTo(w * 0.25f, h * 0.78f, w * 0.50f, h * 0.84f)
        quadraticTo(w * 0.75f, h * 0.90f, w, h * 0.84f)
      }
      drawPath(
        path = wavePath,
        color = Color(0xFF34D399),
        style = Stroke(width = 4f, cap = StrokeCap.Round)
      )
    }

    Text(
      text = "Kayo Formation Chevron Folds (Nago, Okinawa)",
      style = MaterialTheme.typography.labelSmall,
      fontFamily = FontFamily.Monospace,
      color = Color(0xFFFFDEA1),
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 2.dp)
    )
  }
}

@Composable
private fun RecordsSheetContent(
  strings: AppStrings,
  language: AppLanguage,
  records: List<MeasurementRecord>,
  onDeleteRecord: (Long) -> Unit,
  onCopyCsv: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm:ss", Locale.JAPAN) }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Explore,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = strings.fieldNoteSheetTitle(records.size),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      if (records.isNotEmpty()) {
        FilledTonalButton(onClick = onCopyCsv) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(strings.copyCsvBtn, style = MaterialTheme.typography.labelMedium)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (records.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = strings.emptyFieldNoteText,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
      ) {
        items(records, key = { it.id }) { record ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${record.strikeFormatted}  /  ${record.dipFormatted}",
                  style = MaterialTheme.typography.titleMedium,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                if (record.note.isNotEmpty()) {
                  Text(
                    text = record.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                val gpsStr = if (record.latitude != null && record.longitude != null) {
                  " ・ (%.4f, %.4f)".format(Locale.US, record.latitude, record.longitude)
                } else {
                  ""
                }
                Text(
                  text = "${dateFormat.format(Date(record.timestampMillis))} ・ ${strings.modeLabel(record.mode)}$gpsStr",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              IconButton(onClick = { onDeleteRecord(record.id) }) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "Delete",
                  tint = MaterialTheme.colorScheme.error
                )
              }
            }
          }
        }
      }
    }
  }
}
