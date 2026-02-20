package com.kds3393.just.justviewer2.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kds3393.just.justviewer2.data.FileData
import common.lib.utils.FileUtils

@Composable
fun DlgAddFavoriteCompose(
    targetPath: String?, // 로컬 경로 (Null이면 네트워크 모드)
    existingItem: FileData?, // 이미 존재하는 데이터 (편집 모드용)
    onConfirm: (name: String, domain: String, port: String, id: String, pw: String) -> Unit,
    onCancel: () -> Unit
) {
    // 초기값 설정
    var name by remember { mutableStateOf(existingItem?.mDisplayName ?: FileUtils.getFileName(targetPath ?: "") ?: "") }

    // 네트워크 모드용 변수
    var domain by remember { mutableStateOf(existingItem?.mPath ?: "") }
    var port by remember { mutableStateOf("") } // 포트 파싱 로직은 필요시 추가
    var netId by remember { mutableStateOf(existingItem?.mNetId ?: "") }
    var netPw by remember { mutableStateOf(existingItem?.mNetPass ?: "") }

    // 네트워크 모드인지 판별 (경로가 없으면 네트워크 추가 모드)
    val isNetworkMode = targetPath == null || existingItem?.mType == FileData.TYTP_NETWORK

    // 기존 데이터에서 포트 분리 로직 (간단 구현)
    LaunchedEffect(existingItem) {
        if (existingItem != null && existingItem.mType == FileData.TYTP_NETWORK) {
            if (existingItem.mPath.contains(":")) {
                val split = existingItem.mPath.split(":")
                if (split.size > 2) { // http://domain:port 형태 고려
                    port = split.last()
                    domain = existingItem.mPath.substringBeforeLast(":")
                }
            }
        } else if (!isNetworkMode) {
            domain = targetPath ?: ""
        }
    }

    Dialog(onDismissRequest = onCancel) {
        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .width(300.dp)
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = if (isNetworkMode) "네트워크 저장소 추가" else "즐겨찾기 추가",
                    color = Color(0xFF7C79E1),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 18.dp)
                )

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Domain / Path Input (로컬일 경우 ReadOnly 혹은 Path 표시)
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = domain,
                        onValueChange = { domain = it },
                        label = { Text(if(isNetworkMode) "Domain" else "Path") },
                        singleLine = true,
                        modifier = Modifier.weight(if (isNetworkMode) 0.7f else 1f),
                        readOnly = !isNetworkMode // 로컬이면 수정 불가
                    )

                    if (isNetworkMode) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("Port") },
                            singleLine = true,
                            modifier = Modifier.weight(0.3f)
                        )
                    }
                }

                if (isNetworkMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = netId,
                        onValueChange = { netId = it },
                        label = { Text("ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = netPw,
                        onValueChange = { netPw = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = onCancel,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC6C6C6)),
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier.width(90.dp)
                    ) {
                        Text("Cancel", color = Color.White)
                    }

                    Button(
                        onClick = {
                            onConfirm(name, domain, port, netId, netPw)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C79E1)),
                        shape = RoundedCornerShape(15.dp),
                        modifier = Modifier.width(90.dp)
                    ) {
                        Text("Add", color = Color.White)
                    }
                }
            }
        }
    }
}