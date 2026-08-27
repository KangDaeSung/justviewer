package com.kds3393.just.justviewer2.text

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kds3393.just.justviewer2.activity.ActBase
import com.kds3393.just.justviewer2.activity.ActMain
import com.kds3393.just.justviewer2.compose.CText
import com.kds3393.just.justviewer2.db.DBMgr
import com.kds3393.just.justviewer2.utils.SharePref
import com.kds3393.just.justviewer2.compose.ListViewModel
import com.kds3393.just.justviewer2.compose.MapViewModel
import com.kds3393.just.justviewer2.compose.dp2sp
import com.kds3393.just.justviewer2.data.BOOKMARK_TYPE_TEXT
import com.kds3393.just.justviewer2.data.BookmarkData
import com.kds3393.just.justviewer2.utils.ACTION
import com.kds3393.just.justviewer2.utils.Event
import common.lib.base.getFileName
import common.lib.base.launchIO
import common.lib.base.launchMain
import common.lib.debug.CLog
import common.lib.utils.SharedBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

// 파일 읽기 결과와 인코딩 정보를 담기 위한 Data Class
data class FileReadResult(
    val lines: List<String>,
    val encoding: String
)

class ActTextViewerJC : ActBase() {
    private lateinit var textData: BookmarkData

    private val filePathList = ListViewModel<String>()
    private var contentPath by mutableStateOf("")
    private val textListState = ListViewModel<String>()

    private val searchResultMap = MapViewModel<Int,ArrayList<MatchResult>>()
    private var searchResultKeys : List<Int>? = null

    private var isContentLoading by mutableStateOf(true)

    // 파일 정보 상태
    private var fileEncoding by mutableStateOf("")
    private var fileSizeBytes by mutableLongStateOf(0L)
    private var charCount by mutableIntStateOf(0)
    private var lineCount by mutableIntStateOf(0)
    private var isShowFileInfo by mutableStateOf(false)

    @Preview
    @Composable
    fun Preview() {
        isShowFastScroll = true
        isShowSearch = true
        textListState.add("text sample line 1")
        Content()
    }
    private var firstIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentPath = intent.getStringExtra(ActMain.EXTRA_BROWSER_PATH)?:""
        intent.getStringArrayListExtra(ActMain.EXTRA_BROWSER_PATH_ARRAY)?.let { filePathList.set(it) }
        if (TextUtils.isEmpty(contentPath)) {
            finish()
            return
        }

        textData = DBMgr.instance.loadBookmark(contentPath,BOOKMARK_TYPE_TEXT)?: BookmarkData(targetPath = contentPath, bookType = BOOKMARK_TYPE_TEXT)
        if (textData.id < 0) {
            textData.id = DBMgr.instance.insertBookmark(textData)
        } else {
            firstIndex = textData.currentPage
        }

        setContent {
            Box(modifier = Modifier.fillMaxSize(1f)) {
                if (isContentLoading) {
                    Loading()
                } else {
                    Content()
                    ManuUI()
                    FileInfoDialog()
                    DeleteConfirmDialog()
                }
            }
        }

        readBook()
    }

    override fun onPause() {
        super.onPause()
        textData.currentPage = firstVisibleItemIndex
        DBMgr.instance.updateBookmark(textData)
    }

    override fun onFinish(): Boolean {
        return if (isShowSearch) {
            isShowSearch = false
            searchKey = ""
            searchResultMap.clearList()
            false
        } else if (isShowSetting) {
            isShowSetting = false
            false
        } else if (isShowFastScroll) {
            isShowFastScroll = false
            false
        } else if (isShowFileInfo) {
            isShowFileInfo = false
            false
        } else if (isShowDeleteDialog) {
            isShowDeleteDialog = false
            false
        }else {
            true
        }
    }

    @Composable
    fun Loading() {
        Column(verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()) {
            Text(contentPath.getFileName(), fontSize = 20.dp2sp, textAlign = TextAlign.Center, modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp))
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }

    private val highlightStyle = SpanStyle(background = Color.Yellow)
    private var firstVisibleItemIndex = 0
    @Composable
    fun Content() {
        val list = textListState.stateList
        val listState = rememberLazyListState()
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }.collectLatest { index ->
                firstVisibleItemIndex = index
            }
        }

        if (firstIndex > 0) {
            launchMain {
                listState.scrollToItem(index = firstIndex)
                firstIndex = -1
            }
        }

        val searchList = searchResultMap.stateList
        Column {
            if (isShowSearch) {
                prevIsEnable = false
                nextIsEnable = false
                Search() { movePos ->
                    launchMain {
                        listState.scrollToItem(index = movePos)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(sGapss[textGapIndex].dp),
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .background(sColors[textColorIndex][0])
                        .fillMaxHeight()
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            isShowMenu = if (!isShowSearch) {
                                !isShowMenu
                            } else {
                                false
                            }
                            if (!isShowMenu) {
                                isShowSetting = false
                            }
                        }
                ) {
                    itemsIndexed(items = list,
                        key = { index, _ -> index },
                        contentType = { _, _ -> 0 }) { index, item ->
                        val search = searchList[index]
                        val annotationString = if (search != null) {
                            buildAnnotatedString {
                                var currentLength = 0
                                search.forEach { matchResult ->
                                    if (currentLength < matchResult.range.first) {
                                        append(item.substring(currentLength, matchResult.range.first))
                                    }
                                    withStyle(style = highlightStyle) {
                                        append(item.substring(matchResult.range.first, matchResult.range.last + 1))
                                    }

                                    currentLength = length
                                }
                                if (currentLength < item.length) {
                                    append(item.substring(currentLength, item.length))
                                }
                                toAnnotatedString()
                            }
                        } else {
                            buildAnnotatedString {
                                append(item)
                            }
                        }
                        TextItem(index,annotationString)
                    }
                }
                if (isShowFastScroll) {
                    ScrollBar(listState, list.size)
                }
            }
        }
    }

    @Composable
    fun ScrollBar(listState: LazyListState, listSize: Int) {
        var boxHeight by remember { mutableIntStateOf(0) }
        var buttonHeight by remember { mutableIntStateOf(0) }

        val coroutineScope = rememberCoroutineScope()
        var scrollJob by remember { mutableStateOf<Job?>(null) }

        val indicatorOffset by remember {
            derivedStateOf {
                if (listSize <= 0 || boxHeight <= 0 || buttonHeight <= 0) 0f
                else {
                    val layoutInfo = listState.layoutInfo
                    val visibleItemsCount = layoutInfo.visibleItemsInfo.size
                    val maxScrollIndex = (listSize - visibleItemsCount).coerceAtLeast(1)

                    val progress = (listState.firstVisibleItemIndex.toFloat() / maxScrollIndex.toFloat()).coerceIn(0f, 1f)
                    val maxOffset = (boxHeight - buttonHeight).coerceAtLeast(0).toFloat()
                    progress * maxOffset
                }
            }
        }

        var isDragging by remember { mutableStateOf(false) }
        var dragOffset by remember { mutableFloatStateOf(0f) }

        Box(modifier = Modifier
            .background(Color(0x33000000))
            .width(30.dp)
            .fillMaxHeight()
            .onSizeChanged { boxHeight = it.height }) {

            Box(modifier = Modifier
                .size(30.dp)
                .onSizeChanged { buttonHeight = it.height }
                .offset {
                    IntOffset(0, (if (isDragging) dragOffset else indicatorOffset).roundToInt())
                }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val maxOffset = (boxHeight - buttonHeight).coerceAtLeast(1).toFloat()
                        dragOffset = (dragOffset + delta).coerceIn(0f, maxOffset)

                        val layoutInfo = listState.layoutInfo
                        val visibleItemsCount = layoutInfo.visibleItemsInfo.size
                        val maxScrollIndex = (listSize - visibleItemsCount).coerceAtLeast(1)
                        val newIndex = ((dragOffset / maxOffset) * maxScrollIndex).toInt().coerceIn(0, listSize - 1)

                        if (newIndex != listState.firstVisibleItemIndex) {
                            scrollJob?.cancel()
                            scrollJob = coroutineScope.launch {
                                listState.scrollToItem(newIndex)
                            }
                        }
                    },
                    onDragStarted = {
                        isDragging = true
                        dragOffset = indicatorOffset
                    },
                    onDragStopped = {
                        isDragging = false
                    }
                )
                .background(Color.Red, RoundedCornerShape(4.dp))
            )
        }
    }

    private var prevIsEnable by mutableStateOf(false)
    private var nextIsEnable by mutableStateOf(false)
    @Composable
    fun Search(onMoveCallback:(Int) -> Unit) {
        var searchKey by remember { mutableStateOf("") }

        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(color = Color.White)
                .fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(width = 3.dp, color = Color.LightGray, shape = RectangleShape)
                    .background(Color.White)) {
                TextField(value = searchKey, onValueChange = { searchKey = it},
                    label = { Text("Search", fontSize = 16.dp2sp) },
                    textStyle = TextStyle(fontSize = 14.dp2sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        launchIO {
                            search(searchKey)
                        }
                    }),
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = Color.Transparent),
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent))
                Icon(imageVector = Icons.Outlined.Close, tint = Color.Black, contentDescription = "Icon",
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .clickable {
                            launchIO {
                                isShowSearch = false
                                searchKey = ""
                                searchResultMap.clearList()
                            }
                        })
            }
            IconButton(onClick = {
                var moveIndex = -1
                run breaker@ {
                    searchResultKeys?.forEachIndexed { index, line ->
                        if (firstVisibleItemIndex <= line) {
                            return@breaker
                        } else {
                            moveIndex = index
                        }
                    }
                }

                if (moveIndex >= 0) {
                    nextIsEnable = moveIndex < searchResultKeys!!.size
                }
                prevIsEnable = moveIndex > 0
                if (moveIndex >= 0) {
                    onMoveCallback(searchResultKeys!![moveIndex])
                }
            },modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.KeyboardArrowUp,
                    tint = if (prevIsEnable) Color.Black else Color.LightGray,
                    contentDescription = "Icon") }
            IconButton(onClick = {
                var moveIndex = -1
                run breaker@ {
                    searchResultKeys?.forEachIndexed { index, line ->
                        if (firstVisibleItemIndex < line) {
                            moveIndex = index
                            return@breaker
                        }
                    }
                }
                if (moveIndex >= 0) {
                    nextIsEnable = moveIndex < searchResultKeys!!.size
                    prevIsEnable = moveIndex > 0
                    onMoveCallback(searchResultKeys!![moveIndex])
                } else {
                    nextIsEnable = false
                }
            },modifier = Modifier.size(30.dp)) {
                Icon(imageVector = Icons.Default.KeyboardArrowDown,
                    tint = if (nextIsEnable) Color.Black else Color.LightGray,
                    contentDescription = "Icon")
            }
        }
    }

    private var textColorIndex by mutableIntStateOf(SharePref[SharePref.SHARE_TEXT_COLOR_INDEX, 3])
    private var textSizeIndex by mutableIntStateOf(SharePref[SharePref.SHARE_TEXT_SIZE_INDEX, 1])
    private var textGapIndex by mutableIntStateOf(SharePref[SharePref.SHARE_TEXT_GAP_INDEX, 0])
    private var searchKey by mutableStateOf("")
    @Composable
    fun TextItem(index: Int, item: AnnotatedString) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isShowFastScroll) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .background(Color.White)
                        .padding(horizontal = 2.dp)
                ) {
                    Text(
                        text = index.toString(),
                        fontSize = 10.dp2sp,
                        textAlign = TextAlign.End,
                        color = Color.Black,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }

            Text(
                text = item,
                fontSize = sSizes[textSizeIndex].dp2sp,
                color = sColors[textColorIndex][1],
                lineHeight = sSizes[textSizeIndex].dp2sp * sGapss[textGapIndex],
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )
        }
    }


    private var isShowMenu by mutableStateOf(true)
    private var isShowSearch by mutableStateOf(false)
    private var isShowSetting by mutableStateOf(false)
    private var isShowFastScroll by mutableStateOf(false)
    private var isShowDeleteDialog by mutableStateOf(false)

    @Composable
    fun ManuUI() {
        if (isShowMenu) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x77000000))
                        .height(48.dp)) {
                    IconButton(onClick = {
                        finish()
                    }) { Icon(imageVector = Icons.Default.ArrowBackIosNew, tint = Color.White, contentDescription = "Icon") }
                    CText(contentPath.getFileName(),
                        maxLines = 1, fontSize = 16.dp2sp, color = Color.White,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis,
                    )
                    IconButton(onClick = {
                        isShowSearch = true
                        isShowMenu = false
                        isShowSetting = false
                    }) { Icon(imageVector = Icons.Default.Search, tint = Color.White, contentDescription = "Icon") }
                }
                Spacer(modifier = Modifier
                    .weight(1f)
                    .clickable {
                        isShowSetting = false
                    })
                if (isShowSetting) {
                    TextViewerSetting(textColorIndex,textSizeIndex,textGapIndex) { color,size,gap ->
                        if (color != null) {
                            textColorIndex = color
                            SharePref.put(SharePref.SHARE_TEXT_COLOR_INDEX,textColorIndex)
                        }
                        if (size != null) {
                            textSizeIndex = size
                            SharePref.put(SharePref.SHARE_TEXT_SIZE_INDEX,textSizeIndex)
                        }
                        if (gap != null) {
                            textGapIndex = gap
                            SharePref.put(SharePref.SHARE_TEXT_GAP_INDEX,textGapIndex)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x77000000))
                        .height(48.dp)
                        .padding(start = 8.dp, end = 8.dp)) {
                    IconButton(onClick = {
                        isShowSetting = true
                    }) { Icon(imageVector = Icons.Default.FontDownload, tint = Color.White, contentDescription = "Icon") }

                    IconButton(onClick = {
                        isShowFastScroll = true
                        isShowMenu = false
                        isShowSetting = false
                    }) { Icon(imageVector = Icons.Default.FindInPage, tint = Color.White, contentDescription = "Icon") }

                    IconButton(onClick = {
                        isShowDeleteDialog = true
                        isShowMenu = false
                        isShowSetting = false
                    }) { Icon(imageVector = Icons.Default.Delete, tint = Color.White, contentDescription = "Delete File") }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = {
                        isShowFileInfo = true
                        isShowMenu = false
                        isShowSetting = false
                    }) { Icon(imageVector = Icons.Default.Info, tint = Color.White, contentDescription = "Document Info") }
                }
            }
        }
    }

    @Composable
    fun DeleteConfirmDialog() {
        if (isShowDeleteDialog) {
            AlertDialog(
                onDismissRequest = { isShowDeleteDialog = false },
                title = { Text(text = "파일 삭제", style = MaterialTheme.typography.titleLarge) },
                text = { Text("현재 보고 있는 '${contentPath.getFileName()}' 파일을 삭제하시겠습니까?") },
                confirmButton = {
                    TextButton(onClick = {
                        isShowDeleteDialog = false
                        deleteCurrentFile()
                    }) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isShowDeleteDialog = false }) {
                        Text("취소")
                    }
                }
            )
        }
    }

    private fun deleteCurrentFile() {
        val file = File(contentPath)
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(this@ActTextViewerJC, "파일이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                SharedBus.post(Event.FileAction(ACTION.FILE_REMOVE, listOf(contentPath)))
                finish()
            } else {
                Toast.makeText(this@ActTextViewerJC, "파일 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@ActTextViewerJC, "파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // 문서 정보 팝업
    @Composable
    fun FileInfoDialog() {
        if (isShowFileInfo) {
            AlertDialog(
                onDismissRequest = { isShowFileInfo = false },
                title = { Text(text = "문서 정보", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 요청하신 디코더 명칭 표시 추가
                        Text("사용된 디코더: $fileEncoding")

                        val kb = fileSizeBytes / 1024.0
                        val mb = kb / 1024.0
                        val sizeStr = when {
                            mb >= 1.0 -> String.format("%.2f MB", mb)
                            kb >= 1.0 -> String.format("%.2f KB", kb)
                            else -> "$fileSizeBytes B"
                        }
                        Text("용량: $sizeStr")

                        Text("줄 수: ${String.format("%,d", lineCount)} 줄")
                        Text("글자 수: ${String.format("%,d", charCount)} 자 (공백 포함)")

                        val volumes = charCount / 150000f
                        Text("예상 권수: ${String.format("%d", Math.round(volumes))} 권\n(200자 원고지 300매 기준)")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { isShowFileInfo = false }) {
                        Text("확인")
                    }
                }
            )
        }
    }

    private var corutineJob : Job? = null
    private fun readBook() {
        if (corutineJob?.isActive == true) {
            return
        }
        isContentLoading = true
        corutineJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val file = File(contentPath)
            if (file.exists() && file.isFile && file.canRead()) {
                try {
                    fileSizeBytes = file.length()
                    textListState.clearList()

                    val result = readFileWithComprehensiveEncoding(file)

                    var tempCharCount = 0
                    for (line in result.lines) {
                        val cleanLine = line.replace("\uFEFF", "").replace("&nbsp;", "")
                        tempCharCount += cleanLine.length
                        textListState.add(cleanLine)
                    }

                    fileEncoding = result.encoding
                    charCount = tempCharCount
                    lineCount = result.lines.size

                    isContentLoading = false
                } catch (e: Exception) {
                    CLog.e("KDS3393_TEST_readBook Exception = $contentPath")
                    isContentLoading = false
                }
            } else {
                isContentLoading = false
            }
        }
    }

    private fun readFileWithComprehensiveEncoding(file: File): FileReadResult {
        val bytes = file.readBytes()
        if (bytes.isEmpty()) return FileReadResult(emptyList(), "Unknown")

        // BOM 체크 및 디코더 명칭 설정
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return FileReadResult(String(bytes, 3, bytes.size - 3, StandardCharsets.UTF_8).lines(), "UTF-8 (BOM)")
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return FileReadResult(String(bytes, StandardCharsets.UTF_16LE).lines(), "UTF-16LE (BOM)")
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return FileReadResult(String(bytes, StandardCharsets.UTF_16BE).lines(), "UTF-16BE (BOM)")
        }

        // UTF-8 엄격 검사
        try {
            val utf8Decoder = StandardCharsets.UTF_8.newDecoder().apply {
                onMalformedInput(CodingErrorAction.REPORT)
                onUnmappableCharacter(CodingErrorAction.REPORT)
            }
            val decoded = utf8Decoder.decode(ByteBuffer.wrap(bytes)).toString()
            return FileReadResult(decoded.lines(), "UTF-8")
        } catch (e: Exception) {}

        // CP949 시도
        val cp949CharsetName = try {
            Charset.forName("CP949").name()
        } catch (e: Exception) {
            "EUC-KR"
        }

        val cp949Decoder = Charset.forName(cp949CharsetName).newDecoder().apply {
            onMalformedInput(CodingErrorAction.REPLACE)
            onUnmappableCharacter(CodingErrorAction.REPLACE)
        }

        return try {
            val decoded = cp949Decoder.decode(ByteBuffer.wrap(bytes)).toString()
            FileReadResult(decoded.lines(), cp949CharsetName)
        } catch (e: Exception) {
            FileReadResult(String(bytes, StandardCharsets.UTF_8).lines(), "UTF-8 (Fallback)")
        }
    }

    private fun search(key:String) : Int {
        runBlocking {
            val job = launch {
                if (key.isNotEmpty()) {
                    val pattern = Regex("\\b$key\\w*\\b")
                    searchResultMap.clearList()
                    for ((index, line) in textListState.get().withIndex()) {
                        pattern.findAll(line).forEach { matchResult ->
                            var matchs = searchResultMap.get()[index]
                            if (matchs == null) {
                                matchs = ArrayList()
                                searchResultMap.add(index,matchs)
                            }
                            matchs.add(matchResult)
                        }
                    }
                    searchResultKeys = searchResultMap.get().keys.sorted()
                }
            }
            job.join()

            if (searchResultMap.size() > 0) {
                launchMain {
                    searchKey = key
                }
            } else {
                Toast.makeText(this@ActTextViewerJC, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        return searchResultMap.size()
    }
}