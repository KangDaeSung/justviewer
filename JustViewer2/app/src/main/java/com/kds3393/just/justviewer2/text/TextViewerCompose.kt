package com.kds3393.just.justviewer2.text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kds3393.just.justviewer2.compose.dp2sp
import common.lib.debug.CLog


val sSizes = intArrayOf(14, 16, 18, 20, 22)
val sGapss = arrayOf(1.0f, 1.3f, 1.6f)
val sGapsStr = arrayOf("좁게", "중간", "넓게")
val sColors = arrayOf(
    arrayOf(Color.Black, Color.White),
    arrayOf(Color.White, Color.Black),
    arrayOf(Color.Gray, Color.White),
    arrayOf(Color(247, 248, 216), Color.DarkGray),
    arrayOf(Color(193, 211, 167), Color.DarkGray))

@Preview
@Composable
fun Preview() {
    TextViewerSetting(3,2, 0) { color,size,gap ->

    }
}

@Composable
fun TextViewerSetting(colorIndex:Int, sizeIndex:Int, gapIndex:Int, callback:(Int?,Int?,Int?) -> Unit) {
    val rowSize = 40.dp
    val borderSize = 3.dp
    CLog.e("KDS3393_TEST_TextViewerSetting")
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(4.dp),
        shape = RoundedCornerShape(4.dp),
    ) {
        Column(modifier = Modifier
            .padding(top = 4.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(rowSize)) {
                Text(text = "색상",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .width(40.dp))
                sColors.forEachIndexed { index, color ->
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(color[0])
                            .size(34.dp)
                            .let {
                                if (colorIndex != index) {
                                    it
                                } else {
                                    it.border(width = borderSize, color = Color.Red, shape = RectangleShape)
                                }
                            }.clickable { callback(index,null,null) }) {
                        Text(text = "가",fontSize = sSizes[sizeIndex].dp2sp, color = color[1])
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(rowSize)) {
                Text(text = "크기",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .width(40.dp))
                sSizes.forEachIndexed { index, size ->
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(sColors[colorIndex][0])
                            .size(34.dp)
                            .let {
                                if (sizeIndex != index) {
                                    it
                                } else {
                                    it.border(width = borderSize, color = Color.Red, shape = RectangleShape)
                                }
                            }.clickable { callback(null,index,null) }) {
                        Text(text = "가", fontSize = size.dp2sp, color = sColors[colorIndex][1])
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.height(rowSize)) {
                Text(text = "간격",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .width(40.dp))
                sGapss.forEachIndexed { index, size ->
                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(sColors[colorIndex][0])
                            .size(34.dp)
                            .let {
                                if (gapIndex != index) {
                                    it
                                } else {
                                    it.border(width = borderSize, color = Color.Red, shape = RectangleShape)
                                }
                            }.clickable { callback(null,null,index) }) {
                        Text(text = sGapsStr[index], fontSize = 14.dp2sp, color = sColors[colorIndex][1])
                    }

                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }
    }
}