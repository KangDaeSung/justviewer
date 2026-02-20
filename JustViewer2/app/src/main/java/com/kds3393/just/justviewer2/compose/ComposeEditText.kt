@file:OptIn(ExperimentalMaterial3Api::class)

package com.kds3393.just.justviewer2.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun CTextField(
    value: String,
    fontSize: TextUnit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    val textStyle = TextStyle(
        fontSize = fontSize
    )
    TextField(
        value = value,
        modifier = modifier,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        prefix = prefix,
        suffix = suffix,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        shape = shape,
        colors = colors)
}

@Composable
fun CEditText(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    placeholderText: String = "",
    fontSize: TextUnit,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textColor: Color = Color.Black,
    hintColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    backgroundColor: Color = Color.Transparent,
    indicatorColor: Color = Color.Transparent,
    focusedIndicatorColor:Color = Color.Transparent,
    linePadding : Dp = 0.dp
) {
    val mergedTextStyle = LocalTextStyle.current.copy(
        color = textColor,
        fontSize = fontSize,
        textAlign = textAlign
    )
    BasicTextField(modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        textStyle = mergedTextStyle,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.background(backgroundColor, TextFieldDefaults.shape)
                .editLine(interactionSource, indicatorColor, focusedIndicatorColor).padding(bottom = linePadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.invoke()
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) Text(
                            placeholderText,
                            style = LocalTextStyle.current.copy(
                                color = hintColor,
                                fontSize = fontSize
                            )
                        )
                        innerTextField()
                    }
                    trailingIcon?.invoke()
                }
            }
        }
    )
}

@Composable
fun CEditText(
    value: TextFieldValue,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    placeholderText: String = "",
    fontSize: TextUnit,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    textColor: Color = Color.Black,
    hintColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    backgroundColor: Color = Color.Transparent,
    indicatorColor: Color = Color.Transparent,
    focusedIndicatorColor:Color = Color.Transparent,
    linePadding : Dp = 0.dp
) {
    val mergedTextStyle = LocalTextStyle.current.copy(
        color = textColor,
        fontSize = fontSize,
        textAlign = textAlign
    )
    BasicTextField(modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        interactionSource = interactionSource,
        textStyle = mergedTextStyle,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.background(backgroundColor, TextFieldDefaults.shape)
                .editLine(interactionSource, indicatorColor, focusedIndicatorColor).padding(bottom = linePadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.invoke()
                    Box(Modifier.weight(1f)) {
                        if (value.text.isEmpty()) Text(
                            placeholderText,
                            style = LocalTextStyle.current.copy(
                                color = hintColor,
                                fontSize = fontSize
                            )
                        )
                        innerTextField()
                    }
                    trailingIcon?.invoke()
                }
            }
        }
    )
}

fun Modifier.editLine(
    interactionSource: InteractionSource,
    indicatorColor: Color,
    focusedIndicatorColor: Color,
    focusedIndicatorLineThickness: Dp = TextFieldDefaults.FocusedIndicatorThickness,
    unfocusedIndicatorLineThickness: Dp = TextFieldDefaults.UnfocusedIndicatorThickness
) = composed(inspectorInfo = debugInspectorInfo {
    name = "editLine"
    properties["interactionSource"] = interactionSource
    properties["indicatorColor"] = indicatorColor
    properties["focusedIndicatorColor"] = focusedIndicatorColor
    properties["focusedIndicatorLineThickness"] = focusedIndicatorLineThickness
    properties["unfocusedIndicatorLineThickness"] = unfocusedIndicatorLineThickness
}) {
    val stroke = animateBorderStrokeAsState(
        interactionSource,
        indicatorColor,
        focusedIndicatorColor,
        focusedIndicatorLineThickness,
        unfocusedIndicatorLineThickness
    )
    return@composed this.then(Modifier.drawEditTextIndicatorLine(stroke.value))
}

@Composable
private fun animateBorderStrokeAsState(
    interactionSource: InteractionSource,
    indicatorColor: Color,
    focusedIndicatorColor: Color,
    focusedBorderThickness: Dp,
    unfocusedBorderThickness: Dp
): State<BorderStroke> {
    val focused by interactionSource.collectIsFocusedAsState()
    val color = if (focused) focusedIndicatorColor else indicatorColor
    val targetThickness = if (focused) focusedBorderThickness else unfocusedBorderThickness
    val animatedThickness = animateDpAsState(targetThickness, tween(durationMillis = 150), label = "")
    return rememberUpdatedState(BorderStroke(animatedThickness.value, SolidColor(color)))
}

internal fun Modifier.drawEditTextIndicatorLine(indicatorBorder: BorderStroke): Modifier {
    val strokeWidthDp = indicatorBorder.width
    return drawWithContent {
        drawContent()
        if (strokeWidthDp == Dp.Hairline) return@drawWithContent
        val strokeWidth = strokeWidthDp.value * density
        val y = size.height - strokeWidth
        drawLine(
            indicatorBorder.brush,
            Offset(0f, y),
            Offset(size.width, y),
            strokeWidth
        )
    }
}