package com.example.clinometer.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clinometer.domain.ClinometerCalculator
import com.example.clinometer.model.MeasureMode
import com.example.clinometer.model.MeasurementRecord
import com.example.clinometer.model.NotationFormat
import com.example.clinometer.ui.components.ClinometerDial
import com.example.clinometer.ui.components.ClinometerHelpSheet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinometerScreen(
  uiState: ClinometerUiState,
  onToggleHold: () -> Unit,
  onSelectMeasureMode: (MeasureMode) -> Unit,
  onSelectNotationFormat: (NotationFormat) -> Unit,
  onToggleReverseEastWest: () -> Unit,
  onUpdateDeclination: (Float) -> Unit,
  onToggleSimulationMode: (Boolean) -> Unit,
  onUpdateSimulatedReading: (Float, Float, Float) -> Unit,
  onSaveRecord: (String) -> Unit,
  onDeleteRecord: (Long) -> Unit,
  onExportCsv: () -> String,
  modifier: Modifier = Modifier
) {
  val view = LocalView.current
  val context = LocalContext.current

  var showSettingsDialog by remember { mutableStateOf(false) }
  var showSaveNoteDialog by remember { mutableStateOf(false) }
  var showRecordsSheet by remember { mutableStateOf(false) }
  var showHelpSheet by remember { mutableStateOf(false) }

  val reading = uiState.reading
  val strikeText = ClinometerCalculator.formatStrike(reading, uiState.notationFormat)
  val dipText = ClinometerCalculator.formatDip(reading, uiState.notationFormat)

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "地学クリノメーター",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = buildString {
                append(if (uiState.reverseEastWest) "クリノコンパス盤 (E/W逆転)" else "標準コンパス盤")
                append(" ・ 偏角 ${"%.1f".format(uiState.declinationDeg)}°")
                if (uiState.isSimulationMode) append(" ・ [SIM]")
              },
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          // E/W 反転切替ボタン
          IconButton(onClick = onToggleReverseEastWest) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "E/W 反転切替",
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
                contentDescription = "計測履歴"
              )
            }
          }

          // 設定・シミュレーション切替ボタン
          IconButton(onClick = { showSettingsDialog = true }) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "設定"
            )
          }

          // 使い方・ヘルプボタン
          IconButton(onClick = { showHelpSheet = true }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.HelpOutline,
              contentDescription = "使い方・ヘルプ"
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

      // 1. 測定モード切替 (面測定 vs 側面測定)
      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        MeasureMode.entries.forEachIndexed { index, mode ->
          SegmentedButton(
            selected = uiState.measureMode == mode,
            onClick = { onSelectMeasureMode(mode) },
            shape = SegmentedButtonDefaults.itemShape(
              index = index,
              count = MeasureMode.entries.size
            )
          ) {
            Text(
              text = mode.label,
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
        // 走向カード
        MeasurementCard(
          title = if (uiState.notationFormat == NotationFormat.DIP_DIRECTION) {
            "傾斜方位 (Dip Dir)"
          } else {
            "走向 (Strike)"
          },
          mainValue = strikeText,
          subValue = "RHR: %03d° / 方位: %03d°".format(
            reading.strikeAzimuth.roundToInt() % 360,
            reading.deviceAzimuth.roundToInt() % 360
          ),
          accentColor = MaterialTheme.colorScheme.primary,
          isHeld = uiState.isHeld,
          modifier = Modifier.weight(1f)
        )

        // 傾斜カード
        MeasurementCard(
          title = "傾斜 (Dip)",
          mainValue = dipText,
          subValue = "最大傾斜方向: %03d° (%s)".format(
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
            label = {
              Text(
                text = when (format) {
                  NotationFormat.JAPANESE -> "日本式 (N30°E)"
                  NotationFormat.RIGHT_HAND_RULE -> "右手則 (RHR)"
                  NotationFormat.DIP_DIRECTION -> "Dip/DipDir"
                },
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

        // HOLD 中バッジ
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
                text = "HOLD 固定中",
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
            text = if (uiState.isHeld) "HOLD 解除" else "HOLD 固定 (音量キー可)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }

        FilledTonalButton(
          onClick = { showSaveNoteDialog = true },
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
            text = "野帳へ記録",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // 記録保存ダイアログ
  if (showSaveNoteDialog) {
    SaveMeasurementDialog(
      strikeText = ClinometerCalculator.formatJapaneseStrike(reading.strikeAzimuth),
      dipText = ClinometerCalculator.formatJapaneseDip(reading.dipAngle, reading.dipDirection),
      onDismiss = { showSaveNoteDialog = false },
      onConfirm = { note ->
        onSaveRecord(note)
        showSaveNoteDialog = false
        Toast.makeText(context, "計測値を野帳に記録しました", Toast.LENGTH_SHORT).show()
      }
    )
  }

  // 設定ダイアログ (磁気偏角・シミュレーションモード・E/W反転・ヘルプ)
  if (showSettingsDialog) {
    ClinometerSettingsDialog(
      declinationDeg = uiState.declinationDeg,
      reverseEastWest = uiState.reverseEastWest,
      isSimulationMode = uiState.isSimulationMode,
      onUpdateDeclination = onUpdateDeclination,
      onToggleReverseEastWest = onToggleReverseEastWest,
      onToggleSimulationMode = onToggleSimulationMode,
      onOpenHelp = {
        showSettingsDialog = false
        showHelpSheet = true
      },
      onDismiss = { showSettingsDialog = false }
    )
  }

  // 使い方・ヘルプボトムシート
  if (showHelpSheet) {
    ClinometerHelpSheet(
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
        records = uiState.records,
        onDeleteRecord = onDeleteRecord,
        onCopyCsv = {
          val csv = onExportCsv()
          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
          clipboard.setPrimaryClip(ClipData.newPlainText("clinometer_records.csv", csv))
          Toast.makeText(context, "CSVをクリップボードにコピーしました", Toast.LENGTH_SHORT).show()
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
          fontSize = 23.sp
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
          text = "シミュレーション操作 (POCO F7 / 机上テスト用)",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSecondaryContainer
        )
      }
      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "走向方位 (Strike RHR): ${reading.strikeAzimuth.roundToInt()}°",
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
        text = "最大傾斜角 (Dip): ${reading.dipAngle.roundToInt()}°",
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
  strikeText: String,
  dipText: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var note by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("野帳へ計測値を記録") },
    text = {
      Column {
        Text(
          text = "走向: $strikeText\n傾斜: $dipText",
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
          value = note,
          onValueChange = { note = it },
          label = { Text("露頭番号・岩相メモ (例: Loc.1 砂岩泥岩互層)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = { onConfirm(note) }) {
        Text("保存")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("キャンセル")
      }
    }
  )
}

@Composable
private fun ClinometerSettingsDialog(
  declinationDeg: Float,
  reverseEastWest: Boolean,
  isSimulationMode: Boolean,
  onUpdateDeclination: (Float) -> Unit,
  onToggleReverseEastWest: () -> Unit,
  onToggleSimulationMode: (Boolean) -> Unit,
  onOpenHelp: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("クリノメーター設定") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. 磁気偏角設定
        Text(
          text = "磁気偏角補正 (真北基準): ${"%.1f".format(declinationDeg)}°",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "日本の地磁気は西偏（マイナス）です。調査地域に合わせて選択してください。",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          val presets = listOf(
            "沖縄 -5.5°" to -5.5f,
            "東京 -7.8°" to -7.8f,
            "札幌 -9.5°" to -9.5f,
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

        // 2. 日本式 E/W 逆転盤面
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "日本式クリノメーター盤 (E/W逆転)",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "磁針から走向を直読できるよう盤面のEとWを反転表示します",
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

        // 3. シミュレーションモード
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "シミュレーションモード",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "センサー値の代わりに手動スライダーで走向・傾斜をテストします",
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

        // 4. 使い方ガイド・ヘルプへの導線
        FilledTonalButton(
          onClick = onOpenHelp,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("使い方・地質測定ガイドを見る")
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("閉じる")
      }
    }
  )
}

@Composable
private fun RecordsSheetContent(
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
          text = "デジタル野帳 (記録一覧: ${records.size}件)",
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
          Text("CSVコピー", style = MaterialTheme.typography.labelMedium)
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
          text = "まだ記録がありません。「野帳へ記録」ボタンから保存できます。",
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
                Text(
                  text = "${dateFormat.format(Date(record.timestampMillis))} ・ ${record.mode.label}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              IconButton(onClick = { onDeleteRecord(record.id) }) {
                Icon(
                  imageVector = Icons.Default.Delete,
                  contentDescription = "削除",
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
