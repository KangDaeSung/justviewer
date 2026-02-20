package com.kds3393.just.justviewer2.image

import android.graphics.Bitmap
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.Glide
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.activity.ActBase
import com.kds3393.just.justviewer2.activity.ActMain
import com.kds3393.just.justviewer2.config.SharedPrefHelper
import com.kds3393.just.justviewer2.data.BookInfo
import com.kds3393.just.justviewer2.db.DBMgr
import com.kds3393.just.justviewer2.utils.glide.ZipData
import common.lib.base.getFileName
import common.lib.utils.FileUtils
import common.lib.utils.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.tools.zip.ZipEntry
import java.io.File
import kotlin.math.max
import kotlin.math.min

class ActImageViewerJC : ActBase() {
    private lateinit var bookInfo: BookInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 전체 화면 설정 (기존 supportActionBar?.hide() 대체)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        Size.InitScreenSize(this)

        val targetPath = intent.getStringExtra(ActMain.EXTRA_BROWSER_PATH)
        val allPaths = intent.getStringArrayListExtra(ActMain.EXTRA_BROWSER_PATH_ARRAY)

        val newBook = loadBook(targetPath, allPaths)
        if (newBook != null) {
            bookInfo = newBook
        } else {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                ImageViewerScreen(
                    bookInfo = bookInfo,
                    onBackPress = {
                        save()
                        finish()
                    },
                    onDelete = {
                        deleteCurrentBook()
                    }
                )
            }
        }
    }

    override fun onPause() {
        save()
        super.onPause()
    }

    private fun loadBook(targetPath: String?, allPaths: ArrayList<String>?): BookInfo? {
        if (targetPath == null) return null
        var book = DBMgr.instance.imageDataLoad(targetPath)
        if (book == null) {
            book = BookInfo(targetPath)
        }
        allPaths?.let { book.books = allPaths }
        // 로딩 시 필요한 초기화 작업 (엔트리 로드 등)
        book.loadBook {
            // Callback for legacy loading logic if needed,
            // but in Compose we might handle data loading in LaunchedEffect
        }
        return book
    }

    private fun save() {
        // 현재 Compose UI 상태에서 bookInfo가 업데이트 되어 있다고 가정하거나
        // PagerState 등을 통해 최신 페이지를 가져와야 함.
        // 여기서는 예시로 기존 로직을 따르되, UI 상태와 동기화가 필요함.
        DBMgr.instance.updateImageData(bookInfo)
    }

    private fun deleteCurrentBook() {
        FileUtils.deleteFile(File(bookInfo.targetPath))
        Toast.makeText(this, "${bookInfo.targetPath.getFileName()} 삭제됨", Toast.LENGTH_SHORT).show()
        if (bookInfo.removeCurrentBook()) {
            // 책이 삭제되고 다음/이전 책이 있으면 UI 갱신 (Compose에서는 State 변경으로 처리)
            // 실제 구현 시에는 bookInfo를 MutableState로 관리하여 Recomposition 유도 필요
        } else {
            finish()
        }
    }
}

@Composable
fun ImageViewerScreen(
    bookInfo: BookInfo,
    onBackPress: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isRightToLeft = remember { SharedPrefHelper.getImagePageType(context) }
    val initialPage = if (isRightToLeft) bookInfo.currentPage else (bookInfo.entryArray.size - 1 - bookInfo.currentPage)

    val pagerState = rememberPagerState(
        initialPage = max(0, min(initialPage, bookInfo.entryArray.size - 1)),
        pageCount = { bookInfo.entryArray.size }
    )

    // UI 가시성 (터치 시 토글)
    var isUiVisible by remember { mutableStateOf(false) }

    // 현재 보고 있는 실제 페이지 인덱스 (저장용)
    LaunchedEffect(pagerState.currentPage) {
        val realIndex = if (isRightToLeft) {
            pagerState.currentPage
        } else {
            bookInfo.entryArray.size - 1 - pagerState.currentPage
        }
        bookInfo.currentPage = realIndex
    }

    // 뒤로가기 핸들링
    BackHandler {
        if (isUiVisible) {
            isUiVisible = false
        } else {
            onBackPress()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. 이미지 페이저 (HorizontalPager)
        // reverseLayout을 사용하여 R-to-L, L-to-R 지원
        HorizontalPager(
            state = pagerState,
            reverseLayout = !isRightToLeft, // false면 왼쪽부터(일반), true면 오른쪽부터(만화책)
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            // 이미지 로드 및 줌 기능이 포함된 컴포저블
            ZoomableImageItem(
                zipPath = bookInfo.targetPath,
                entryName = bookInfo.entryArray[pageIndex],
                onTap = { isUiVisible = !isUiVisible }
            )
        }

        // 2. 상단 네비게이션 바 (파일명 등)
        AnimatedVisibility(
            visible = isUiVisible,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopControlBar(title = FileUtils.getFileName(bookInfo.targetPath))
        }

        // 3. 하단 컨트롤 바 (슬라이더, 메뉴)
        AnimatedVisibility(
            visible = isUiVisible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomControlBar(
                currentPage = pagerState.currentPage + 1,
                totalPages = bookInfo.entryArray.size,
                onSliderChange = { newPage ->
                    scope.launch { pagerState.scrollToPage(newPage - 1) }
                },
                onPrevBook = { /* 이전 책 로직 구현 */ },
                onNextBook = { /* 다음 책 로직 구현 */ },
                onDelete = onDelete
            )
        }
    }
}

@Composable
fun ZoomableImageItem(
    zipPath: String,
    entryName: ZipEntry,
    onTap: () -> Unit
) {
    val context = LocalContext.current
    val bitmapState = produceState<Bitmap?>(initialValue = null, key1 = entryName) {
        value = withContext(Dispatchers.IO) {
            try {
                // 기존 PageView.kt의 로직을 참고하여 Glide로 로드
                val zipData = ZipData(zipPath, entryName, Size.DisplayWidth, Size.DisplayHeight)

                // 2. Glide.with에 'context' 변수를 넣습니다.
                Glide.with(context)
                    .asBitmap()
                    .load(zipData)
                    .submit()
                    .get() // 동기 호출 (IO 스레드에서 실행되므로 안전함)
            } catch (e: Exception) {
                e.printStackTrace() // 에러 확인을 위해 로그 출력
                null
            }
        }
    }

    // 줌/팬 상태
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // 줌 제스처 감지
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 3f) // 최대 3배 줌
                    if (scale == 1f) offset = Offset.Zero
                    else {
                        // 줌 상태일 때만 이동 가능
                        val newOffset = offset + pan
                        // 경계 체크 로직은 복잡하므로 생략하거나 간단히 구현
                        offset = newOffset
                    }
                }
            }
            // 탭 제스처 (UI 토글)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        // 더블 탭 시 줌 초기화/확대 토글
                        scale = if (scale > 1f) 1f else 2f
                        offset = Offset.Zero
                    }
                )
            }
    ) {
        if (bitmapState.value != null) {
            Image(
                bitmap = bitmapState.value!!.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        } else {
            // 로딩 중 표시 (Text 혹은 ProgressBar)
            Text(
                text = "Loading...",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun TopControlBar(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun BottomControlBar(
    currentPage: Int,
    totalPages: Int,
    onSliderChange: (Int) -> Unit,
    onPrevBook: () -> Unit,
    onNextBook: () -> Unit,
    onDelete: () -> Unit
) {
    // 슬라이더 드래그 중의 임시 값을 저장하는 상태
    // 외부에서 currentPage가 변경될 때(예: 스와이프) 동기화되도록 remember의 key로 currentPage를 전달
    var sliderValue by remember(currentPage) { mutableFloatStateOf(currentPage.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(16.dp)
    ) {
        // 페이지 정보 표시 (드래그 중인 임시 페이지 번호 표시)
        Text(
            text = "${sliderValue.toInt()} / $totalPages",
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Slider(
            value = sliderValue,
            onValueChange = {
                // 드래그 중에는 UI상의 슬라이더 위치와 텍스트만 업데이트
                sliderValue = it
            },
            onValueChangeFinished = {
                // 사용자가 터치를 떼어 슬라이더 조작이 끝났을 때 실제 페이지 이동 콜백 호출
                onSliderChange(sliderValue.toInt())
            },
            valueRange = 1f..totalPages.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Blue,
                activeTrackColor = Color.Blue,
                inactiveTrackColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 하단 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevBook) {
                Icon(painter = painterResource(id = R.drawable.h_book_driection_left), contentDescription = "Prev", tint = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            // 삭제 버튼
            IconButton(onClick = onDelete) {
                Icon(painter = painterResource(id = android.R.drawable.ic_menu_delete), contentDescription = "Delete", tint = Color.White)
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onNextBook) {
                Icon(painter = painterResource(id = R.drawable.h_book_driection_right), contentDescription = "Next", tint = Color.White)
            }
        }
    }
}