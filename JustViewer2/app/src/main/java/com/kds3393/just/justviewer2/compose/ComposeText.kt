@file:OptIn(ExperimentalMaterial3Api::class)

package com.kds3393.just.justviewer2.compose

import android.os.Build
import android.text.SpannableString
import android.text.style.URLSpan
import android.text.util.Linkify
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import common.lib.debug.CLog

@Composable
fun CText(
    text: String?,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    isPlaceHolder: Boolean = false, //로딩중을 표현하기 위해 Text가 null이면 TextColor로 Box를 만들어 보여준다.
    minFontSize: TextUnit? = null,  //최소 font size를 설정하면 자동으로 Text Width에 맞춰서 Text의 size를 줄인다.
    style: TextStyle = LocalTextStyle.current
) {
    val _text = text ?: ""
    if (isPlaceHolder && text == null) {
        Box(modifier = modifier.then(Modifier.background(color, shape = RoundedCornerShape(4.dp))))
        return
    }
    if (minFontSize != null) {
        var textSize by remember { mutableStateOf(fontSize) }
        var readyToDraw by remember { mutableStateOf(false) }
        val textLayout : (TextLayoutResult) -> Unit = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight && !readyToDraw) {
                val nextFontSizeValue = textSize.value - 1f.sp.value
                if (nextFontSizeValue <= minFontSize.value) {
                    textSize = minFontSize.value.sp
                    readyToDraw = true
                } else {
                    textSize = nextFontSizeValue.sp
                }
            } else {
                readyToDraw = true
            }
        }
        Text(text = _text, modifier = modifier, color = color, autoSize = null, fontSize = textSize, fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = null,
            letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap,
            maxLines = maxLines, minLines = minLines,
            onTextLayout = textLayout,
            style = style
        )
    } else {
        Text(text = _text, modifier = modifier, color = color, autoSize = null, fontSize = fontSize, fontStyle = fontStyle, fontWeight = fontWeight, fontFamily = null,
            letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign, lineHeight = lineHeight, overflow = overflow, softWrap = softWrap,
            maxLines = maxLines, minLines = minLines,
            onTextLayout = onTextLayout,
            style = style
        )
    }
}

@Composable
fun IconText(modifier: Modifier = Modifier,
             iconId: Int,
             iconTint : Color = Color.Unspecified,
             iconPadding : Dp = 0.dp,
             iconSize : DpSize? = null,
             text: String,
             textColor: Color = Color.Unspecified,
             fontSize: TextUnit,
             fontWeight: FontWeight = FontWeight.Normal,
             maxLines: Int = Int.MAX_VALUE,
             overflow: TextOverflow = TextOverflow.Clip,
             ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        val iconModifier = if (iconSize != null) {
            Modifier.size(iconSize)
        } else {
            Modifier
        }
        Icon(painter = painterResource(id = iconId),
            tint = iconTint,
            contentDescription = "",
            modifier = iconModifier)
        Text(text = text, color = textColor, fontSize = fontSize, fontWeight = fontWeight,
            maxLines = maxLines, overflow = overflow,
            modifier = Modifier.padding(start = iconPadding))
    }
}

@Composable
fun TextIcon(modifier: Modifier = Modifier,
             iconId: Int,
             iconTint : Color = Color.Unspecified,
             iconPadding : Dp = 0.dp,
             iconSize : DpSize? = null,
             text: String,
             textColor: Color = Color.Unspecified,
             fontSize: TextUnit,
             fontWeight: FontWeight = FontWeight.Normal,
             maxLines: Int = Int.MAX_VALUE,
             overflow: TextOverflow = TextOverflow.Clip,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = modifier) {
        val iconModifier = if (iconSize != null) {
            Modifier.size(iconSize)
        } else {
            Modifier
        }
        Text(text = text, color = textColor, fontSize = fontSize, fontWeight = fontWeight,
            maxLines = maxLines, overflow = overflow)
        Icon(painter = painterResource(id = iconId),
            tint = iconTint,
            contentDescription = "",
            modifier = Modifier.padding(start = iconPadding).then(iconModifier))
    }
}

@Composable
fun LinkifyText(
    text: String,
    modifier: Modifier = Modifier,
    linkColor: Color = Color.Blue,
    linkEntire: Boolean = false,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    clickable: Boolean = true,
    onClickLink: ((linkText: String) -> Unit)? = null
) {
    val uriHandler = LocalUriHandler.current
    val linkInfos = if (linkEntire) listOf(LinkInfo(text, 0, text.length)) else SpannableStr.getLinkInfos(
        text
    )
    val annotatedString = buildAnnotatedString {
        append(text)
        linkInfos.forEach {
            addStyle(
                style = SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                ),
                start = it.start,
                end = it.end
            )
            addStringAnnotation(
                tag = "tag",
                annotation = it.url,
                start = it.start,
                end = it.end
            )
        }
    }
    if (clickable) {
        ClickableText(
            text = annotatedString,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout,
            style = style,
            onClick = { offset ->
                val item =  annotatedString.getStringAnnotations(
                    start = offset,
                    end = offset,
                ).firstOrNull()

                if(linkEntire){
                    CLog.e("LinkifyText: Entire Link")
                    onClickLink?.invoke("")
                }else{
                    if(item != null) {
                        uriHandler.openUri(item.item)
                        onClickLink?.invoke(annotatedString.substring(item.start, item.end))
                    }else{
                        CLog.e("LinkifyText: Entire Link" )
                        onClickLink?.invoke("")
                    }
                }
            }
        )
    } else {
        Text(
            text = annotatedString,
            modifier = modifier,
            color = color,
            fontSize = fontSize,
            fontStyle = fontStyle,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout,
            style = style
        )
    }
}

@Composable
private fun ClickableText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current,
    onClick: (Int) -> Unit
) {
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onClick) {
        detectTapGestures { pos ->
            layoutResult.value?.let { layoutResult ->
                onClick(layoutResult.getOffsetForPosition(pos))
            }
        }
    }
    Text(
        text = text,
        modifier = modifier.then(pressIndicator),
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = {
            layoutResult.value = it
            onTextLayout(it)
        },
        style = style
    )
}

fun highlightNumberInText(text: String, style:SpanStyle) : AnnotatedString {
    val numberRegex = Regex("\\d+")

    return buildAnnotatedString {
        var lastIndex = 0

        for (match in numberRegex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1

            // 숫자 앞의 텍스트 추가
            append(text.substring(lastIndex, start))

            // 숫자는 스타일 적용
            withStyle(style = style) {
                append(text.substring(start, end))
            }

            lastIndex = end
        }

        // 남은 텍스트 추가
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

fun highlightNumberInText(text: String, fontSize:TextUnit, fontWeight:FontWeight = FontWeight.Normal) : AnnotatedString {
    val numberRegex = Regex("\\d+")

    return buildAnnotatedString {
        var lastIndex = 0

        for (match in numberRegex.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1

            // 숫자 앞의 텍스트 추가
            append(text.substring(lastIndex, start))

            // 숫자는 스타일 적용
            withStyle(style = SpanStyle(fontSize = fontSize, fontWeight = FontWeight.Bold)) {
                append(text.substring(start, end))
            }

            lastIndex = end
        }

        // 남은 텍스트 추가
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

private data class LinkInfo(
    val url: String,
    val start: Int,
    val end: Int
)

private class SpannableStr(source: CharSequence): SpannableString(source) {
    companion object {
        fun getLinkInfos(text: String): List<LinkInfo> {
            val spannableStr = SpannableStr(text)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Linkify.addLinks(spannableStr, Linkify.ALL) { str: String -> URLSpan(str)  }
            } else {
                Linkify.addLinks(spannableStr, Linkify.ALL)
            }
            return spannableStr.linkInfos
        }
    }
    private inner class Data(
        val what: Any?,
        val start: Int,
        val end: Int
    )
    private val spanList = mutableListOf<Data>()

    private val linkInfos: List<LinkInfo>
        get() = spanList.filter { it.what is URLSpan }.map {
            LinkInfo(
                (it.what as URLSpan).url,
                it.start,
                it.end
            )
        }

    override fun removeSpan(what: Any?) {
        super.removeSpan(what)
        spanList.removeAll { it.what == what }
    }

    override fun setSpan(what: Any?, start: Int, end: Int, flags: Int) {
        super.setSpan(what, start, end, flags)
        spanList.add(Data(what, start, end))
    }
}

