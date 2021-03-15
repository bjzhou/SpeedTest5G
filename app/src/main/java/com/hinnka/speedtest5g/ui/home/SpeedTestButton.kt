package com.hinnka.speedtest5g.ui.home

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.view.MotionEvent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hinnka.speedtest5g.ui.theme.purple200
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

@Composable
fun SpeedTestButton(visible: Boolean, onClick: () -> Unit) {

    val width = 80.dp

    val secondary = MaterialTheme.colors.secondary
    val secondaryVariant = purple200
    val infiniteTransition = rememberInfiniteTransition()
    val radius: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val anim2: Float by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    } }
    val canvasScale = remember { Animatable(0f) }
    val (startPoint, setStartPoint) = remember { mutableStateOf(PointF(0f, 0f)) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(visible) {
        if (visible) {
            canvasScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
        } else {
            canvasScale.animateTo(0f, spring())
        }
    }
    Canvas(modifier = Modifier
        .fillMaxSize()
        .pointerInteropFilter {
            if (canvasScale.value == 0f) {
                return@pointerInteropFilter false
            }
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    scope.launch {
                        canvasScale.animateTo(0.8f, spring())
                    }
                    setStartPoint(PointF(it.x, it.y))
                }
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    scope.launch {
                        canvasScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                    }
                    if (it.action == MotionEvent.ACTION_UP) {
                        if (abs(it.x - startPoint.x) < 50 && abs(it.y - startPoint.y) < 50) {
                            onClick()
                        }
                    }
                }
                else -> return@pointerInteropFilter false
            }
            true
        }) {
        scale(canvasScale.value) {
            rotate(radius) {
                drawCircle(
                    Brush.linearGradient(
                        colors = listOf(secondary, secondaryVariant),
                    ),
                    style = Stroke(25f),
                    radius = width.toPx()
                )
            }
            drawCircle(
                Brush.linearGradient(
                    colors = listOf(secondary, secondaryVariant),
                ),
                style = Stroke(5f),
                radius = width.toPx() + anim2 * 200,
                alpha = max(1f - anim2 / 0.8f, 0f)
            )
            drawCircle(
                Brush.linearGradient(
                    colors = listOf(secondary, secondaryVariant),
                ),
                style = Stroke(5f),
                radius = width.toPx() + max(anim2 - 0.5f, 0f) * 250,
                alpha = 1f - anim2
            )
            paint.textSize = 40.dp.toPx()
            scale(1 + radius / 360f * 0.2f) {
                drawContext.canvas.nativeCanvas.drawText("Start", center.x, center.y + 16.dp.toPx(), paint)
            }
        }
    }
}