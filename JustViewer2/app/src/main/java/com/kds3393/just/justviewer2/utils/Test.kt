package com.kds3393.just.justviewer2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import kotlin.random.Random
import androidx.core.graphics.toColorInt


/**
 * 테스트중 필요한 기능 모음
 * 실제 배포시에는 모두 지워야 한다는 기준으로 사용
 */
object Test {
    @Stable val c1 = Color(0x77ff0000)
    @Stable val c2 = Color(0x77ffff00)
    @Stable val c3 = Color(0x770000FF)
    @Stable val c4 = Color.Red
    @Stable val c5 = Color.Yellow
    @Stable val c6 = Color.Blue

    @Stable val v1 = "#77ff0000".toColorInt()
    @Stable val v2 = "#77ffff00".toColorInt()
    @Stable val v3 = "#770000FF".toColorInt()
    @Stable val v4 = android.graphics.Color.RED
    @Stable val v5 = android.graphics.Color.YELLOW
    @Stable val v6 = android.graphics.Color.BLUE

    private var random = Random(System.currentTimeMillis())
    fun randomColorCompose() : Color {
        return Color(random.nextInt(0xff), random.nextInt(0xff), random.nextInt(0xff), 0xff )
    }

    fun randomColorView() : Color {
        return Color(random.nextInt(0xff), random.nextInt(0xff), random.nextInt(0xff), 0xff )
    }

    fun code(onTestCode:() -> Unit) {
        onTestCode()
    }

    @Composable
    fun Compose(content: @Composable () -> Unit) {
        content()
    }
}