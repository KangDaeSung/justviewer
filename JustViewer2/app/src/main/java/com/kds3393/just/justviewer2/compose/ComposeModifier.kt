package com.kds3393.just.justviewer2.compose

import android.graphics.BlurMaskFilter
import android.os.Build
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * showRipple = false : 클릭했을때 버튼 눌림 효과를 제거하기 위한 옵션 추가
 */
fun Modifier.click(
    enabled: Boolean = true,
    showRipple: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = if (showRipple) LocalIndication.current else null,
        enabled = enabled,
        onClickLabel = onClickLabel,
        role = role,
        onClick = onClick,
    )
}


fun Modifier.clickableOnce(
    enabled: Boolean = true,
    showRipple: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    disableDelay : Int = 1000,
    onClick: () -> Unit
) = composed (
    inspectorInfo = debugInspectorInfo {
        name = "clickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }
) {
    var touchTimer by remember { mutableLongStateOf(System.currentTimeMillis() - disableDelay) }
    then(Modifier.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onClick = {
            if (System.currentTimeMillis() - touchTimer > disableDelay) {
                touchTimer = System.currentTimeMillis()
                onClick()
            }
        },
        role = role,
        interactionSource = remember { MutableInteractionSource() },
        indication = if (showRipple) LocalIndication.current else null
    ))
}

fun Modifier.dropShadow(
    shape: Shape,
    color: Color = Color.Black.copy(0.25f),
    blur: Dp = 4.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp,
    spread: Dp = 0.dp
) = this.drawBehind {
    val shadowSize = Size(size.width + spread.toPx(), size.height + spread.toPx())
    val shadowOutline = shape.createOutline(shadowSize, layoutDirection, this)

    val paint = Paint().apply {
        this.color = color
    }

    if (blur.toPx() > 0) {
        paint.asFrameworkPaint().apply {
            maskFilter = BlurMaskFilter(blur.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
    }

    drawIntoCanvas { canvas ->
        canvas.save()
        canvas.translate(offsetX.toPx(), offsetY.toPx())
        canvas.drawOutline(shadowOutline, paint)
        canvas.restore()
    }
}


fun Modifier.cBlur(
    radius: Dp = 25.dp,
): Modifier {
    return this.blur(radius)
}

fun Modifier.fakeBlur(
    overlayColor: Color = Color.Black.copy(alpha = 0.20f),
    downScale: Float = 0.92f
): Modifier = this
    .graphicsLayer {
        scaleX = downScale
        scaleY = downScale
        alpha = 0.95f
    }
    .drawBehind {
        drawRect(overlayColor)
    }