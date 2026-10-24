package com.example.clinometer.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.clinometer.model.ClinometerReading
import com.example.clinometer.ui.theme.BubbleLevelGreen
import com.example.clinometer.ui.theme.CompassNorthRed
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 地学用クリノメーターのビジュアルコンパス＆水準器コンポーネント
 *
 * - 外周: 方位目盛り・N/E/S/W (日本式 E/W 逆転盤面モード対応)
 * - 中央: 地質図の走向線 (Strike Line) と 傾斜方向ベクトル (Dip Tick)
 * - 内周: 円形気泡水準器 (Bullseye Bubble Level)
 */
@Composable
fun ClinometerDial(
  reading: ClinometerReading,
  reverseEastWest: Boolean,
  isHeld: Boolean,
  modifier: Modifier = Modifier
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary
  val tertiaryColor = MaterialTheme.colorScheme.tertiary
  val onSurfaceColor = MaterialTheme.colorScheme.onSurface
  val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
  val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

  val cardinalPaint = remember(onSurfaceColor) {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = onSurfaceColor.toArgb()
      textAlign = Paint.Align.CENTER
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
  }

  val northPaint = remember {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = CompassNorthRed.toArgb()
      textAlign = Paint.Align.CENTER
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
  }

  val degreePaint = remember(onSurfaceVariantColor) {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = onSurfaceVariantColor.toArgb()
      textAlign = Paint.Align.CENTER
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
    }
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .padding(8.dp),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val outerRadius = size.minDimension / 2f * 0.94f
      val innerCompassRadius = outerRadius * 0.74f
      val levelRadius = outerRadius * 0.40f

      // 1. ベース円盤の背景と外枠リング
      drawCircle(
        color = surfaceVariantColor.copy(alpha = 0.45f),
        radius = outerRadius,
        center = center
      )
      drawCircle(
        color = if (isHeld) tertiaryColor else onSurfaceVariantColor.copy(alpha = 0.4f),
        radius = outerRadius,
        center = center,
        style = Stroke(width = if (isHeld) 6f else 3f)
      )

      // 端末上部の照準インデックス線 (12時位置)
      drawLine(
        color = CompassNorthRed,
        start = Offset(center.x, center.y - outerRadius - 8f),
        end = Offset(center.x, center.y - outerRadius + 18f),
        strokeWidth = 6f,
        cap = StrokeCap.Round
      )

      // 2. コンパスローズ (端末の方位角に応じて回転し、常に実際の北を指す)
      val compassRotation = -reading.deviceAzimuth
      rotate(degrees = compassRotation, pivot = center) {
        drawCompassScale(
          center = center,
          outerRadius = outerRadius,
          innerCompassRadius = innerCompassRadius,
          reverseEastWest = reverseEastWest,
          onSurfaceColor = onSurfaceColor,
          onSurfaceVariantColor = onSurfaceVariantColor,
          cardinalPaint = cardinalPaint,
          northPaint = northPaint,
          degreePaint = degreePaint
        )

        // 3. 地質図の走向線 (Strike Line) と 最大傾斜線 (Dip Tick)
        // コンパス座標系 (N=0°上, E=90°右) 上に走向・傾斜記号を描画
        drawStrikeAndDipSymbol(
          center = center,
          symbolRadius = innerCompassRadius * 0.90f,
          strikeAzimuth = reading.strikeAzimuth,
          dipDirection = reading.dipDirection,
          dipAngle = reading.dipAngle,
          strikeColor = primaryColor,
          dipColor = secondaryColor
        )
      }

      // 4. 内周の水準器ガイドリング
      drawCircle(
        color = onSurfaceVariantColor.copy(alpha = 0.25f),
        radius = levelRadius,
        center = center,
        style = Stroke(width = 2f)
      )
      drawCircle(
        color = onSurfaceVariantColor.copy(alpha = 0.35f),
        radius = levelRadius * 0.3f,
        center = center,
        style = Stroke(width = 2f)
      )
      // 十字ヘアライン
      drawLine(
        color = onSurfaceVariantColor.copy(alpha = 0.2f),
        start = Offset(center.x - levelRadius, center.y),
        end = Offset(center.x + levelRadius, center.y),
        strokeWidth = 1.5f
      )
      drawLine(
        color = onSurfaceVariantColor.copy(alpha = 0.2f),
        start = Offset(center.x, center.y - levelRadius),
        end = Offset(center.x, center.y + levelRadius),
        strokeWidth = 1.5f
      )

      // 5. 気泡 (バブル) の描画
      val maxBubbleOffset = levelRadius * 0.78f
      val bubbleCenter = Offset(
        x = center.x + reading.tiltX * maxBubbleOffset,
        y = center.y - reading.tiltY * maxBubbleOffset
      )
      val isLevel = reading.dipAngle < 2.0f
      val bubbleColor = if (isLevel) BubbleLevelGreen else tertiaryColor
      val bubbleRadius = levelRadius * 0.22f

      drawCircle(
        color = bubbleColor.copy(alpha = 0.35f),
        radius = bubbleRadius * 1.3f,
        center = bubbleCenter
      )
      drawCircle(
        color = bubbleColor,
        radius = bubbleRadius,
        center = bubbleCenter
      )
    }
  }
}

/**
 * コンパス目盛り・方位文字・北針の描画
 */
private fun DrawScope.drawCompassScale(
  center: Offset,
  outerRadius: Float,
  innerCompassRadius: Float,
  reverseEastWest: Boolean,
  onSurfaceColor: Color,
  onSurfaceVariantColor: Color,
  cardinalPaint: Paint,
  northPaint: Paint,
  degreePaint: Paint
) {
  cardinalPaint.textSize = outerRadius * 0.11f
  northPaint.textSize = outerRadius * 0.12f
  degreePaint.textSize = outerRadius * 0.058f

  for (deg in 0 until 360 step 5) {
    val angleRad = Math.toRadians((deg - 90).toDouble())
    val cosA = cos(angleRad).toFloat()
    val sinA = sin(angleRad).toFloat()

    val isCardinal = deg % 90 == 0
    val isMajor = deg % 30 == 0
    val isMedium = deg % 10 == 0

    val tickLength = when {
      isCardinal -> outerRadius * 0.09f
      isMajor -> outerRadius * 0.07f
      isMedium -> outerRadius * 0.045f
      else -> outerRadius * 0.025f
    }

    val strokeWidth = when {
      isCardinal -> 4.5f
      isMajor -> 3f
      else -> 1.5f
    }

    val tickColor = when {
      deg == 0 -> CompassNorthRed
      isCardinal || isMajor -> onSurfaceColor
      else -> onSurfaceVariantColor.copy(alpha = 0.6f)
    }

    val startR = outerRadius - 4f
    val endR = startR - tickLength
    drawLine(
      color = tickColor,
      start = Offset(center.x + cosA * startR, center.y + sinA * startR),
      end = Offset(center.x + cosA * endR, center.y + sinA * endR),
      strokeWidth = strokeWidth,
      cap = StrokeCap.Round
    )

    // 30度ごとの角度ラベル (クリノメーター式90度象限目盛り or 360度目盛り)
    if (isMajor && !isCardinal) {
      val textR = outerRadius * 0.84f
      val tx = center.x + cosA * textR
      val ty = center.y + sinA * textR + degreePaint.textSize * 0.35f
      val label = if (reverseEastWest) {
        // 日本式クリノメーター象限角 (N/S を 0°、E/W を 90° とする直読目盛り)
        val quad = if (deg % 180 <= 90) deg % 180 else 180 - (deg % 180)
        "$quad"
      } else {
        "$deg"
      }
      drawContext.canvas.nativeCanvas.drawText(label, tx, ty, degreePaint)
    }
  }

  // N / E / S / W の方位文字
  // reverseEastWest = true の場合は伝統的なクリノコンパス同様に E と W を反転表示
  val eastLabel = if (reverseEastWest) "W" else "E"
  val westLabel = if (reverseEastWest) "E" else "W"
  val cardinals = listOf(
    0 to "N",
    90 to eastLabel,
    180 to "S",
    270 to westLabel
  )

  val cardinalR = innerCompassRadius * 0.96f
  for ((deg, label) in cardinals) {
    val angleRad = Math.toRadians((deg - 90).toDouble())
    val tx = center.x + cos(angleRad).toFloat() * cardinalR
    val paint = if (deg == 0) northPaint else cardinalPaint
    val ty = center.y + sin(angleRad).toFloat() * cardinalR + paint.textSize * 0.36f
    drawContext.canvas.nativeCanvas.drawText(label, tx, ty, paint)
  }

  // 北 (0°) の赤い三角ポインタ
  val pointerTop = Offset(center.x, center.y - outerRadius + 10f)
  val pointerLeft = Offset(center.x - 12f, center.y - outerRadius + 30f)
  val pointerRight = Offset(center.x + 12f, center.y - outerRadius + 30f)
  val northPath = Path().apply {
    moveTo(pointerTop.x, pointerTop.y)
    lineTo(pointerLeft.x, pointerLeft.y)
    lineTo(pointerRight.x, pointerRight.y)
    close()
  }
  drawPath(path = northPath, color = CompassNorthRed)
}

/**
 * 地質図で用いられる走向・傾斜記号 (Strike Line + Dip Tick) の描画
 */
private fun DrawScope.drawStrikeAndDipSymbol(
  center: Offset,
  symbolRadius: Float,
  strikeAzimuth: Float,
  dipDirection: Float,
  dipAngle: Float,
  strikeColor: Color,
  dipColor: Color
) {
  // 走向線 (Strike Line): strikeAzimuth と strikeAzimuth + 180° を結ぶ直線
  val strikeRad = Math.toRadians((strikeAzimuth - 90f).toDouble())
  val dx = cos(strikeRad).toFloat() * symbolRadius
  val dy = sin(strikeRad).toFloat() * symbolRadius

  drawLine(
    color = strikeColor,
    start = Offset(center.x - dx, center.y - dy),
    end = Offset(center.x + dx, center.y + dy),
    strokeWidth = 7f,
    cap = StrokeCap.Round
  )

  // 傾斜線 (Dip Vector): 傾斜角に応じて中心から dipDirection 方向へ伸びる短線と矢印
  if (dipAngle >= 1.0f) {
    val dipRad = Math.toRadians((dipDirection - 90f).toDouble())
    val dipRatio = (dipAngle / 90f).coerceIn(0.25f, 1.0f)
    val tickLen = symbolRadius * 0.65f * dipRatio
    val endX = center.x + cos(dipRad).toFloat() * tickLen
    val endY = center.y + sin(dipRad).toFloat() * tickLen

    drawLine(
      color = dipColor,
      start = center,
      end = Offset(endX, endY),
      strokeWidth = 8f,
      cap = StrokeCap.Round
    )

    // 先端のドット
    drawCircle(
      color = dipColor,
      radius = 9f,
      center = Offset(endX, endY)
    )
  } else {
    // 水平層記号 (十字丸)
    drawCircle(
      color = strikeColor,
      radius = 12f,
      center = center,
      style = Stroke(width = 4f)
    )
  }
}
