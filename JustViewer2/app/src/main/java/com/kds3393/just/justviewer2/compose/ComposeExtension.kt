package com.kds3393.just.justviewer2.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

inline val Int.dp2sp: TextUnit @Composable get() = this.dp2sp()
inline val Double.dp2sp: TextUnit @Composable get() = this.dp2sp()

@Composable
fun Int.dp2sp(): TextUnit {
    return with(LocalDensity.current) {
        this@dp2sp.dp.toSp()
    }
}

@Composable
fun Double.dp2sp(): TextUnit {
    return with(LocalDensity.current) {
        this@dp2sp.dp.toSp()
    }
}

@Composable
fun Dp.toPx() = with(LocalDensity.current) { this@toPx.toPx() }
@Composable
fun Int.toDp() = with(LocalDensity.current) { this@toDp.toDp() }
@Composable
fun Float.toDp() = with(LocalDensity.current) { this@toDp.toDp() }