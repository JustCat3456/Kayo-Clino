package com.example.clinometer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.clinometer.R
import com.example.clinometer.model.AppLanguage
import com.example.clinometer.ui.i18n.AppStrings
import com.example.clinometer.ui.theme.CompassNorthRed
import kotlinx.coroutines.launch

/**
 * 初回起動時およびデバッグメニューから表示する4ステップのオンボーディング・カルーセル
 * 全スライドで背面の Card サイズを固定統一し、文字量を抑えてシンプルに表示する。
 */
@Composable
fun OnboardingCarouselDialog(
  strings: AppStrings,
  language: AppLanguage,
  declinationDeg: Float,
  reverseEastWest: Boolean,
  voiceReadoutEnabled: Boolean,
  isGpsLoading: Boolean,
  onSelectLanguage: (AppLanguage) -> Unit,
  onUpdateDeclination: (Float) -> Unit,
  onRequestGpsDeclination: () -> Unit,
  onToggleReverseEastWest: () -> Unit,
  onToggleVoiceReadout: (Boolean) -> Unit,
  onFinish: () -> Unit
) {
  val pagerState = rememberPagerState(pageCount = { 4 })
  val coroutineScope = rememberCoroutineScope()

  Dialog(
    onDismissRequest = onFinish,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(vertical = 16.dp),
      shape = RoundedCornerShape(28.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // 上部スキップバー
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${pagerState.currentPage + 1} / 4",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          TextButton(onClick = onFinish) {
            Text(strings.skipBtn)
          }
        }

        // カルーセル本体（全ページで高さを 450.dp に完全統一して背面サイズの変化と4枚目の見切れを防止）
        HorizontalPager(
          state = pagerState,
          modifier = Modifier
            .fillMaxWidth()
            .height(450.dp),
          verticalAlignment = Alignment.Top
        ) { page ->
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
          ) {
            // ステップごとのアイコン／カスタム図解イラスト
            OnboardingIllustration(
              page = page,
              modifier = Modifier
                .size(96.dp)
                .padding(bottom = 8.dp)
            )

            when (page) {
              0 -> OnboardingPage1Language(
                strings = strings,
                language = language,
                onSelectLanguage = onSelectLanguage
              )

              1 -> OnboardingPage2MeasureModes(
                strings = strings
              )

              2 -> OnboardingPage3Features(
                strings = strings,
                voiceReadoutEnabled = voiceReadoutEnabled,
                onToggleVoiceReadout = onToggleVoiceReadout
              )

              3 -> OnboardingPage4Setup(
                strings = strings,
                declinationDeg = declinationDeg,
                reverseEastWest = reverseEastWest,
                isGpsLoading = isGpsLoading,
                onUpdateDeclination = onUpdateDeclination,
                onRequestGpsDeclination = onRequestGpsDeclination,
                onToggleReverseEastWest = onToggleReverseEastWest
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ページインジケータードット
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          repeat(4) { index ->
            val selected = pagerState.currentPage == index
            Box(
              modifier = Modifier
                .height(8.dp)
                .width(if (selected) 24.dp else 8.dp)
                .clip(CircleShape)
                .background(
                  if (selected) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant
                  }
                )
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 下部ナビゲーションボタン
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (pagerState.currentPage > 0) {
            TextButton(
              onClick = {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
              }
            ) {
              Text(strings.backBtn)
            }
          } else {
            Spacer(modifier = Modifier.width(64.dp))
          }

          Button(
            onClick = {
              if (pagerState.currentPage < 3) {
                coroutineScope.launch {
                  pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
              } else {
                onFinish()
              }
            },
            shape = RoundedCornerShape(14.dp)
          ) {
            Text(
              text = if (pagerState.currentPage < 3) strings.nextBtn else strings.startBtn,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun OnboardingIllustration(page: Int, modifier: Modifier = Modifier) {
  if (page == 0) {
    // 1枚目: Kayo-Clino の新しいアプリアイコンを表示
    Box(
      modifier = modifier,
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.ic_kayo_icon),
        contentDescription = "Kayo-Clino App Icon",
        modifier = Modifier.fillMaxSize()
      )
    }
    return
  }

  val primary = MaterialTheme.colorScheme.primary
  val secondary = MaterialTheme.colorScheme.secondary
  val tertiary = MaterialTheme.colorScheme.tertiary
  val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

  Surface(
    modifier = modifier,
    shape = CircleShape,
    color = surfaceVariant.copy(alpha = 0.55f)
  ) {
    Canvas(modifier = Modifier.padding(16.dp)) {
      val cx = size.width / 2f
      val cy = size.height / 2f
      val r = size.minDimension / 2f

      when (page) {
        1 -> {
          // 地層斜面とスマホ背面密着のイラスト
          drawLine(
            color = secondary,
            start = Offset(cx - r * 0.85f, cy + r * 0.55f),
            end = Offset(cx + r * 0.85f, cy - r * 0.35f),
            strokeWidth = 9f,
            cap = StrokeCap.Round
          )
          rotate(degrees = -28f, pivot = Offset(cx, cy)) {
            drawRoundRect(
              color = primary,
              topLeft = Offset(cx - r * 0.5f, cy - 20f),
              size = Size(r * 1.0f, 16f),
              cornerRadius = CornerRadius(6f, 6f)
            )
          }
        }

        2 -> {
          // スマホ側面の音量キーとHOLDロック波紋のイラスト
          drawRoundRect(
            color = primary,
            topLeft = Offset(cx - r * 0.42f, cy - r * 0.75f),
            size = Size(r * 0.84f, r * 1.5f),
            cornerRadius = CornerRadius(14f, 14f),
            style = Stroke(width = 5.5f)
          )
          // 音量キー強調
          drawRoundRect(
            color = secondary,
            topLeft = Offset(cx + r * 0.44f, cy - r * 0.35f),
            size = Size(10f, r * 0.45f),
            cornerRadius = CornerRadius(4f, 4f)
          )
          drawCircle(color = tertiary, radius = r * 0.22f, center = Offset(cx, cy))
        }

        else -> {
          // 地球磁場と偏角補正のイラスト
          drawCircle(color = primary, radius = r * 0.85f, style = Stroke(width = 5f))
          drawLine(
            color = CompassNorthRed,
            start = Offset(cx, cy + r * 0.6f),
            end = Offset(cx - r * 0.2f, cy - r * 0.65f),
            strokeWidth = 7f,
            cap = StrokeCap.Round
          )
          drawLine(
            color = tertiary,
            start = Offset(cx, cy + r * 0.6f),
            end = Offset(cx, cy - r * 0.7f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
          )
        }
      }
    }
  }
}

/**
 * 1枚目: 「Kayo-Clino へようこそ」と「表示言語」のみをシンプルに表示
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingPage1Language(
  strings: AppStrings,
  language: AppLanguage,
  onSelectLanguage: (AppLanguage) -> Unit
) {
  Text(
    text = strings.onboardingStep1Title,
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center
  )
  Spacer(modifier = Modifier.height(16.dp))

  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Icon(
      imageVector = Icons.Default.Language,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary
    )
    Text(
      text = strings.languageSectionTitle,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.primary,
      fontWeight = FontWeight.Bold
    )
  }
  Spacer(modifier = Modifier.height(10.dp))

  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    verticalArrangement = Arrangement.spacedBy(6.dp),
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
        label = {
          Text(
            text = lang.nativeName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (language == lang) FontWeight.Bold else FontWeight.Normal
          )
        }
      )
    }
  }
}

/**
 * 2枚目: 「面測定」と「側面測定」を視覚的な強調カードで表示
 */
@Composable
private fun OnboardingPage2MeasureModes(
  strings: AppStrings
) {
  Text(
    text = strings.onboardingStep2Title,
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center
  )
  Spacer(modifier = Modifier.height(12.dp))

  // 面測定 (背面密着) 強調カード
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
      Text(
        text = strings.modeSurfaceLabel,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = strings.onboardingStep2SurfaceDesc,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }

  Spacer(modifier = Modifier.height(8.dp))

  // 側面測定 (伝統式) 強調カード
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
      Text(
        text = strings.modeEdgeLabel,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.secondary
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = strings.onboardingStep2EdgeDesc,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

/**
 * 3枚目: 「音量キーを押すだけで計測値をHOLD」と「アクセシビリティ 音声読み上げ」をシンプルに表示
 */
@Composable
private fun OnboardingPage3Features(
  strings: AppStrings,
  voiceReadoutEnabled: Boolean,
  onToggleVoiceReadout: (Boolean) -> Unit
) {
  Text(
    text = strings.onboardingStep3Title,
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center
  )
  Spacer(modifier = Modifier.height(6.dp))
  Text(
    text = strings.onboardingStep3Body,
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center
  )
  Spacer(modifier = Modifier.height(14.dp))

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.VolumeUp,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = strings.voiceReadoutTitle,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold
        )
      }
      Switch(
        checked = voiceReadoutEnabled,
        onCheckedChange = onToggleVoiceReadout
      )
    }
  }
}

/**
 * 4枚目: 説明文を省き、GPS磁気偏角とコンパス盤の初期設定コントロールのみをシンプルに配置
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnboardingPage4Setup(
  strings: AppStrings,
  declinationDeg: Float,
  reverseEastWest: Boolean,
  isGpsLoading: Boolean,
  onUpdateDeclination: (Float) -> Unit,
  onRequestGpsDeclination: () -> Unit,
  onToggleReverseEastWest: () -> Unit
) {
  Text(
    text = strings.onboardingStep4Title,
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.Bold,
    textAlign = TextAlign.Center
  )
  Spacer(modifier = Modifier.height(10.dp))

  FilledTonalButton(
    onClick = onRequestGpsDeclination,
    enabled = !isGpsLoading,
    colors = ButtonDefaults.filledTonalButtonColors(
      containerColor = MaterialTheme.colorScheme.secondaryContainer,
      contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null)
    Spacer(modifier = Modifier.width(8.dp))
    Text(if (isGpsLoading) strings.gpsCalculating else strings.gpsAutoDeclinationBtn)
  }

  Spacer(modifier = Modifier.height(6.dp))
  Text(
    text = strings.declinationSectionTitle(declinationDeg),
    style = MaterialTheme.typography.labelMedium,
    color = MaterialTheme.colorScheme.primary,
    fontWeight = FontWeight.Bold
  )

  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    verticalArrangement = Arrangement.spacedBy(2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    val presets = listOf(
      "Okinawa -5.5°" to -5.5f,
      "Tokyo -7.8°" to -7.8f,
      "Sapporo -9.5°" to -9.5f,
      "0°" to 0f
    )
    presets.forEach { (label, value) ->
      AssistChip(
        onClick = { onUpdateDeclination(value) },
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
      )
    }
  }

  Spacer(modifier = Modifier.height(6.dp))
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Default.Explore,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = strings.reverseEwTitle,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold
        )
      }
      Switch(
        checked = reverseEastWest,
        onCheckedChange = { onToggleReverseEastWest() }
      )
    }
  }
}
