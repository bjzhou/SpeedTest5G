package com.hinnka.speedtest5g.ui.home

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.speedtest5g.ext.logD
import com.hinnka.speedtest5g.ext.px
import com.hinnka.speedtest5g.model.TestState
import com.hinnka.speedtest5g.ui.theme.purple200
import com.hinnka.speedtest5g.ui.theme.uploadColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SpeedTestPan(visible: Boolean, mbpsState: State<Float>, testState: State<TestState>) {

    val radius = 100.dp

    val secondary = MaterialTheme.colors.secondary
    val secondaryVariant = purple200

    val canvasScale = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val paint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.RIGHT
            color = android.graphics.Color.WHITE
            textSize = 9.sp.px
            typeface = Typeface.DEFAULT_BOLD
        }
    }
    val mbpsPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            color = 0x80ffffff.toInt()
            textSize = 12.sp.px
        }
    }
    val path = remember { Path() }


    LaunchedEffect(visible) {
        if (visible) {
            canvasScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, 100f))
        } else {
            canvasScale.animateTo(0f, spring())
        }
    }

    val mbps = mbpsState.value
    val last = progress.targetValue
    val newTarget = when {
        mbps <= 10 -> mbps / 50f
        mbps <= 20 -> mbps / 100f + 0.1f
        mbps <= 50 -> mbps / 300f + 7f / 30f
        mbps <= 100 -> mbps / 500f + 0.3f
        mbps <= 200 -> mbps / 1000f + 0.4f
        mbps <= 500 -> mbps / 3000f + 8f / 15f
        mbps <= 1000 -> mbps / 5000f + 0.6f
        mbps <= 2000 -> mbps / 10000f + 0.7f
        mbps <= 5000 -> mbps / 30000f + 5f / 6f
        else -> 1f
    }
    if (progress.value != last && newTarget != last) {
        LaunchedEffect(last) {
            progress.snapTo(last)
        }
    }
    LaunchedEffect(mbps) {
        progress.animateTo(
            newTarget,
            tween(1000, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        scale(canvasScale.value) {
            drawArc(
                Color(0xFF38485f),
                startAngle = -200f,
                sweepAngle = 220f,
                useCenter = false,
                topLeft = center - Offset(radius.toPx(), radius.toPx()),
                size = Size(radius.toPx() * 2, radius.toPx() * 2),
                style = Stroke(30f, cap = StrokeCap.Round),
            )
            drawArc(
                Brush.linearGradient(
                    colors = if (testState.value == TestState.Download)
                        listOf(secondary, secondaryVariant)
                    else listOf(uploadColor, secondaryVariant),
                ),
                startAngle = -200f,
                sweepAngle = 220f * progress.value,
                useCenter = false,
                topLeft = center - Offset(radius.toPx(), radius.toPx()),
                size = Size(radius.toPx() * 2, radius.toPx() * 2),
                style = Stroke(30f, cap = StrokeCap.Round),
            )
            for (i in 0..50) {
                val angle = (-200 + 220f / 50 * i) * PI / 180
                val r1 = 110.dp.toPx()
                val r2 = (if (i % 5 == 0) 118 else 115).dp.toPx()
                val r3 = 125.dp.toPx()
                val p0x = center.x + cos(angle) * r1
                val p0y = center.y + sin(angle) * r1
                val p1x = center.x + cos(angle) * r2
                val p1y = center.y + sin(angle) * r2
                val p2x = center.x + cos(angle) * r3
                var p2y = center.y + sin(angle) * r3

                drawLine(
                    Color.White,
                    Offset(p0x.toFloat(), p0y.toFloat()),
                    Offset(p1x.toFloat(), p1y.toFloat()),
                    strokeWidth = if (i % 5 == 0) 1.dp.toPx() else Stroke.HairlineWidth
                )

                if (i % 5 == 0) {
                    val text = when (i) {
                        0 -> "0"
                        5 -> "5M"
                        10 -> "10M"
                        15 -> "20M"
                        20 -> "50M"
                        25 -> "100M"
                        30 -> "200M"
                        35 -> "500M"
                        40 -> "1G"
                        45 -> "2G"
                        else -> "5G"
                    }
                    when {
                        i < 25 -> paint.textAlign = Paint.Align.RIGHT
                        i == 25 -> paint.textAlign = Paint.Align.CENTER
                        i > 25 -> paint.textAlign = Paint.Align.LEFT
                    }
                    when {
                        i <= 10 || i >= 40 -> p2y += 10f
                    }
                    paint.textSize = 9.sp.px
                    drawContext.canvas.nativeCanvas.drawText(
                        text,
                        p2x.toFloat(), p2y.toFloat(), paint
                    )
                }
            }

            val angle = (-200 + 220f * progress.value) * PI / 180
            val r0 = 80.dp.toPx()
            val r1 = 10.dp.toPx()
            val p0x = center.x + cos(angle) * r0
            val p0y = center.y + sin(angle) * r0
            val p1x = center.x + cos(angle - 0.4f) * r1
            val p1y = center.y + sin(angle - 0.4f) * r1
            val p2x = center.x + cos(angle + 0.4f) * r1
            val p2y = center.y + sin(angle + 0.4f) * r1
            path.reset()
            path.moveTo(p0x.toFloat(), p0y.toFloat())
            path.lineTo(p1x.toFloat(), p1y.toFloat())
            path.lineTo(p2x.toFloat(), p2y.toFloat())
            path.lineTo(p0x.toFloat(), p0y.toFloat())
            path.close()
            drawPath(
                path,
                color = Color.White,
                alpha = 0.8f,
            )

            drawCircle(
                Color(0xffcccccc),
                radius = 10.dp.toPx(),
            )

            paint.textSize = 20.sp.px
            paint.textAlign = Paint.Align.CENTER
            drawContext.canvas.nativeCanvas.drawText(
                String.format("%.2f", mbps),
                center.x, center.y + 60.dp.toPx(), paint
            )
            drawContext.canvas.nativeCanvas.drawText(
                "mbps",
                center.x, center.y + 75.dp.toPx(), mbpsPaint
            )
        }
    }
}