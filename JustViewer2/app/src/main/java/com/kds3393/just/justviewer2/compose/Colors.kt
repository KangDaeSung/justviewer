package com.kds3393.just.justviewer2.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import androidx.annotation.IntRange
import androidx.core.graphics.toColorInt

class Colors {
    fun get(value: ULong) : Color {
        return Color(value)
    }

    companion object {
        @Stable val UnSet = Color.Unspecified
        @Stable val Trans = Color.Transparent

        @Stable val Default = Color(0xFF3F51B5)

        @Stable val Black = Color(0xff141414)
        @Stable val RealBlack = Color.Black
        @Stable val White = Color.White
        @Stable val Red = Color(0xFFFF7F82)

        @Stable val Gray100 = Color(0xffF3F3F3)
        @Stable val Gray200 = Color(0xffDEDEDE)
        @Stable val Gray300 = Color(0xffCDCDCD)
        @Stable val Gray400 = Color(0xffBDBDBD)
        @Stable val Gray500 = Color(0xff999999)
        @Stable val Gray600 = Color(0xff828282)
        @Stable val Gray700 = Color(0xff6B6B6B)
        @Stable val Gray800 = Color(0xff363636)
        @Stable val Gray900 = Color(0xff212121)

        operator fun get(color: Int, @IntRange(from = 0, to = 100) alphaPercent: Int = 100): Color {
            return get(color, alphaPercent / 100f)
        }

        operator fun get(color: Int, alphaPercent: Float): Color {
            // Alpha 값 계산 (0~255 범위)
            val alpha = (255 * alphaPercent).toInt()
            // 기존 색상의 RGB 값 추출
            val red = (color shr 16) and 0xFF
            val green = (color shr 8) and 0xFF
            val blue = color and 0xFF
            return Color(red, green, blue, alpha)
        }

        fun randomColor() : Color {
            val random = Random(System.currentTimeMillis())
            return Color(random.nextInt(0xff), random.nextInt(0xff), random.nextInt(0xff), 0xff )
        }

        fun stringToColor(colorTxt:String, default: Color = Color.Transparent): Color {
            return try {
                val hex = colorTxt.removePrefix("#")
                val colorInt = "#$hex".toColorInt()
                Color(colorInt)
            } catch (e: Exception) {
                default
            }
        }
    }
}