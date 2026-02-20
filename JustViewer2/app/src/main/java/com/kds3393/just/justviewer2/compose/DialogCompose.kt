@file:OptIn(ExperimentalMaterial3Api::class)

package com.kds3393.just.justviewer2.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Preview
@Composable
fun Preview() {
    MessageDialog(title = "TITLE",
        message = "MESSAGE",
        confirm = "OK",
        cancel = "Cancel")
}

/**
 *  Dialog 사용방식이 기존과 달라 어느정도 불편을 해결하지 못하면 기존 dialog를 사용하는게 좋을거 같음
 *  1. dialog의 사용 유무가 불확실할때도 미리 코드화 해놔야함 - 필요할때만 사용하고 시음
 *  2. Class화 하여 코드화 했을 경우 dialog가 연속으로 2개 이상 호출될 경우 callback을 매칭시켜야 함
 *     - stack에 dialog의 callback과 정보가 있는 event data를 등록하고 하나라도 있으면 show 없으면 dismiss
 */
@Composable
fun MessageDialog(title:String? = null,
                  message:String,
                  imgUrl:String? = null,
                  confirm:String? = null,
                  cancel:String? = null,
                  onConfirm: (() -> Unit)? = null,
                  onCancel: (() -> Unit)? = null) {
    Dialog(onDismissRequest = { onCancel?.invoke() }) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 10.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Scaffold(
                contentColor = Color.White,
                topBar = {
                    if (title?.isNotEmpty() == true) {
                        CText(text = title,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.dp2sp, color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 42.dp)
                                .wrapContentHeight(align = Alignment.CenterVertically))
                    }
                },
                bottomBar = {
                    Row(modifier = Modifier.background(Color.Red)) {
                        if (cancel?.isNotEmpty() == true) {
                            Button(onClick = { onCancel?.invoke() },
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE0E0E0),
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)) {
                                CText(text = cancel,textAlign = TextAlign.Center, fontSize = 16.dp2sp)
                            }
                        }
                        if (confirm != null) {
                            Button(onClick = { onConfirm?.invoke() },
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)) {
                                CText(text = confirm, textAlign = TextAlign.Center, fontSize = 16.dp2sp)
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (imgUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imgUrl)
                                .build(),
                            contentDescription = "",
                            contentScale = ContentScale.Crop
                        )
                    }
                    CText(
                        modifier = Modifier.padding(8.dp),
                        text = message,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 14.dp2sp
                    )
                }
            }
        }
    }
}