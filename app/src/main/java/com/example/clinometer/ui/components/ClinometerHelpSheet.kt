package com.example.clinometer.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clinometer.ui.i18n.AppStrings

/**
 * 地学クリノメーターの使い方・解説ヘルプを表示するボトムシート (6言語対応)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinometerHelpSheet(
  strings: AppStrings,
  onDismiss: () -> Unit,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  val context = LocalContext.current

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // ヘッダー部
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = strings.helpPageTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = strings.helpPageSubtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

      // 1. クイックスタート
      HelpSectionCard(
        title = strings.helpSec1Title,
        icon = Icons.Default.Navigation,
        iconTint = MaterialTheme.colorScheme.primary
      ) {
        StepItem(
          stepNumber = "1",
          title = strings.helpStep1Title,
          description = strings.helpStep1Desc
        )
        StepItem(
          stepNumber = "2",
          title = strings.helpStep2Title,
          description = strings.helpStep2Desc
        )
        StepItem(
          stepNumber = "3",
          title = strings.helpStep3Title,
          description = strings.helpStep3Desc
        )
        StepItem(
          stepNumber = "4",
          title = strings.helpStep4Title,
          description = strings.helpStep4Desc
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. 測定モードの違い & 面測定のスマホの当て方図解
      HelpSectionCard(
        title = strings.helpSec2Title,
        icon = Icons.Default.Layers,
        iconTint = MaterialTheme.colorScheme.secondary
      ) {
        Text(
          text = strings.helpSurfaceModeHeading,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = strings.helpSurfaceModeIntro,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 10.dp)
        )

        // 走向に対するスマホの向き（縦・横・回転自由）の立体図解
        SurfaceMeasurementDiagram(strings = strings)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = strings.helpEdgeModeHeading,
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.secondary
        )
        Text(
          text = strings.helpEdgeModeIntro,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 3. 表記形式（走向・傾斜）
      HelpSectionCard(
        title = strings.helpSec3Title,
        icon = Icons.Default.CompassCalibration,
        iconTint = MaterialTheme.colorScheme.tertiary
      ) {
        NotationItem(
          name = strings.helpNotation1Name,
          example = "N 30° E  45° SE",
          explanation = strings.helpNotation1Desc
        )
        NotationItem(
          name = strings.helpNotation2Name,
          example = "030° / 45°",
          explanation = strings.helpNotation2Desc
        )
        NotationItem(
          name = strings.helpNotation3Name,
          example = "45° / 120°",
          explanation = strings.helpNotation3Desc
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. クリノメーターの豆知識：E/W逆転
      HelpSectionCard(
        title = strings.helpSec4Title,
        icon = Icons.Default.SwapHoriz,
        iconTint = MaterialTheme.colorScheme.primary
      ) {
        Text(
          text = strings.helpSec4Body,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          lineHeight = 20.sp
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 5. 便利な野外機能
      HelpSectionCard(
        title = strings.helpSec5Title,
        icon = Icons.Default.Lock,
        iconTint = MaterialTheme.colorScheme.secondary
      ) {
        FeatureItem(
          title = strings.helpFeat1Title,
          description = strings.helpFeat1Desc
        )
        FeatureItem(
          title = strings.helpFeat2Title,
          description = strings.helpFeat2Desc
        )
        FeatureItem(
          title = strings.helpFeat3Title,
          description = strings.helpFeat3Desc
        )
        FeatureItem(
          title = strings.helpFeat4Title,
          description = strings.helpFeat4Desc
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 外部Webページへのリンクボタン (Wikipedia / 産総研 地質調査総合センター)
      Card(
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = strings.helpWebCardTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = strings.helpWebCardDesc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilledTonalButton(
              onClick = {
                val intent = Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(strings.helpWikiUrl)
                )
                context.startActivity(intent)
              },
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.helpWikiBtn, style = MaterialTheme.typography.labelSmall)
            }

            OutlinedButton(
              onClick = {
                val intent = Intent(
                  Intent.ACTION_VIEW,
                  Uri.parse(strings.helpSurveyUrl)
                )
                context.startActivity(intent)
              },
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.helpSurveyBtn, style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(strings.closeBtn)
      }
    }
  }
}

@Composable
private fun HelpSectionCard(
  title: String,
  icon: ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.height(10.dp))
      content()
    }
  }
}

@Composable
private fun StepItem(
  stepNumber: String,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(22.dp)
        .background(MaterialTheme.colorScheme.primary, CircleShape),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = stepNumber,
        color = MaterialTheme.colorScheme.onPrimary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun NotationItem(
  name: String,
  example: String,
  explanation: String
) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = example,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
    }
    Text(
      text = explanation,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(4.dp))
  }
}

@Composable
private fun FeatureItem(
  title: String,
  description: String
) {
  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Text(
      text = "・$title",
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = description,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.padding(start = 12.dp)
    )
  }
}

/**
 * 面測定（背面密着）時のスマホの構え方・幾何学的原理を図解するコンポーネント
 */
@Composable
private fun SurfaceMeasurementDiagram(
  strings: AppStrings,
  modifier: Modifier = Modifier
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val surfaceColor = MaterialTheme.colorScheme.surface

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = strings.helpDiagramTitle,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = primaryColor,
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
          Text(
            text = strings.helpDiagramBadge,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 立体ダイアグラム (Canvas)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(230.dp)
          .background(surfaceColor, RoundedCornerShape(12.dp))
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height

          // 1. 傾斜した地層面 (3Dパースの平行四辺形)
          val planePath = Path().apply {
            moveTo(w * 0.10f, h * 0.36f)
            lineTo(w * 0.68f, h * 0.14f)
            lineTo(w * 0.90f, h * 0.64f)
            lineTo(w * 0.32f, h * 0.86f)
            close()
          }
          drawPath(
            path = planePath,
            color = primaryColor.copy(alpha = 0.10f)
          )
          drawPath(
            path = planePath,
            color = primaryColor.copy(alpha = 0.60f),
            style = Stroke(width = 2.dp.toPx())
          )

          // 2. 走向線 (Strike Line: 水平な線)
          val strikeY = h * 0.40f
          val strikePath = Path().apply {
            moveTo(w * 0.04f, strikeY)
            lineTo(w * 0.74f, strikeY)
          }
          drawPath(
            path = strikePath,
            color = tertiaryColor,
            style = Stroke(
              width = 2.5.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
          )
          drawCircle(color = tertiaryColor, radius = 4.dp.toPx(), center = Offset(w * 0.04f, strikeY))
          drawCircle(color = tertiaryColor, radius = 4.dp.toPx(), center = Offset(w * 0.74f, strikeY))

          // 3. 最大傾斜線 (Dip Direction: 走向線に直交して下るベクトル)
          val dipStartX = w * 0.39f
          val dipStartY = h * 0.28f
          val dipEndX = w * 0.61f
          val dipEndY = h * 0.74f
          drawLine(
            color = secondaryColor,
            start = Offset(dipStartX, dipStartY),
            end = Offset(dipEndX, dipEndY),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
          )
          // 鏃
          val arrowPath = Path().apply {
            moveTo(dipEndX, dipEndY)
            lineTo(dipEndX - 18f, dipEndY - 8f)
            lineTo(dipEndX - 8f, dipEndY - 18f)
            close()
          }
          drawPath(path = arrowPath, color = secondaryColor)

          // 4. 地層面に密着させたスマホ
          val phoneCenterX = w * 0.50f
          val phoneCenterY = h * 0.50f
          val phoneW = w * 0.28f
          val phoneH = h * 0.40f

          rotate(degrees = -16f, pivot = Offset(phoneCenterX, phoneCenterY)) {
            drawRoundRect(
              color = surfaceColor,
              topLeft = Offset(phoneCenterX - phoneW / 2, phoneCenterY - phoneH / 2),
              size = Size(phoneW, phoneH),
              cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
            )
            drawRoundRect(
              color = onSurfaceColor.copy(alpha = 0.85f),
              topLeft = Offset(phoneCenterX - phoneW / 2, phoneCenterY - phoneH / 2),
              size = Size(phoneW, phoneH),
              cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
              style = Stroke(width = 2.5.dp.toPx())
            )
            drawRoundRect(
              color = primaryColor.copy(alpha = 0.20f),
              topLeft = Offset(phoneCenterX - phoneW / 2 + 5f, phoneCenterY - phoneH / 2 + 8f),
              size = Size(phoneW - 10f, phoneH - 16f),
              cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            drawCircle(
              color = primaryColor.copy(alpha = 0.7f),
              radius = phoneW * 0.28f,
              center = Offset(phoneCenterX, phoneCenterY),
              style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
              color = secondaryColor,
              radius = 5.dp.toPx(),
              center = Offset(phoneCenterX, phoneCenterY)
            )
          }

          // 5. 自由回転を示す円弧矢印 (↻)
          drawArc(
            color = primaryColor,
            startAngle = 20f,
            sweepAngle = 290f,
            useCenter = false,
            topLeft = Offset(phoneCenterX - phoneW * 0.70f, phoneCenterY - phoneH * 0.54f),
            size = Size(phoneW * 1.40f, phoneH * 1.08f),
            style = Stroke(
              width = 2.dp.toPx(),
              pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
          )

          // 6. 法線ベクトル (スマホ背面から垂直に伸びるZ軸)
          val normalStartX = phoneCenterX
          val normalStartY = phoneCenterY
          val normalEndX = phoneCenterX - w * 0.16f
          val normalEndY = phoneCenterY - h * 0.25f
          drawLine(
            color = onSurfaceColor,
            start = Offset(normalStartX, normalStartY),
            end = Offset(normalEndX, normalEndY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
          )
          drawCircle(color = onSurfaceColor, radius = 3.5.dp.toPx(), center = Offset(normalEndX, normalEndY))
        }

        // オーバーレイのテキストラベル
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(6.dp),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = strings.helpDiagramNormalLabel,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.SemiBold,
              lineHeight = 13.sp
            )
            Text(
              text = strings.helpDiagramPlaneLabel,
              style = MaterialTheme.typography.labelSmall,
              color = primaryColor,
              fontWeight = FontWeight.Bold
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
          ) {
            Text(
              text = strings.helpDiagramStrikeLabel,
              style = MaterialTheme.typography.labelSmall,
              color = tertiaryColor,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = strings.helpDiagramDipLabel,
              style = MaterialTheme.typography.labelSmall,
              color = secondaryColor,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // ポイント解説
      Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
      ) {
        Row(verticalAlignment = Alignment.Top) {
          Text("● ", color = primaryColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
          Text(
            text = strings.helpDiagramPoint1Title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = strings.helpDiagramPoint1Desc,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(start = 14.dp)
        )

        Row(verticalAlignment = Alignment.Top) {
          Text("● ", color = secondaryColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
          Text(
            text = strings.helpDiagramPoint2Title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = strings.helpDiagramPoint2Desc,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(start = 14.dp)
        )

        Row(verticalAlignment = Alignment.Top) {
          Text("● ", color = tertiaryColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
          Text(
            text = strings.helpDiagramPoint3Title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = strings.helpDiagramPoint3Desc,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(start = 14.dp)
        )
      }
    }
  }
}
