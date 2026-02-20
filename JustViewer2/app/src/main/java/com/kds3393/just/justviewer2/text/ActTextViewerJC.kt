package com.kds3393.just.justviewer2.text

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
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.kds3393.just.justviewer2.data.TextItemData
import com.kds3393.just.justviewer2.db.DBMgr
import com.kds3393.just.justviewer2.utils.SharePref
import com.kds3393.just.justviewer2.compose.ListViewModel
import com.kds3393.just.justviewer2.compose.MapViewModel
import com.kds3393.just.justviewer2.compose.dp2sp
import common.lib.base.getFileName
import common.lib.base.launchIO
import common.lib.base.launchMain
import common.lib.debug.CLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.roundToInt

class ActTextViewerJC : ActBase() {
    private lateinit var textData: TextItemData

    private val filePathList = ListViewModel<String>()  //관련 컨텐츠 파일 리스트
    private var contentPath by mutableStateOf("")   //현재 Viewer의 파일 경로
    private val textListState = ListViewModel<String>()     //컨텐츠의 Text
    private var hideLine by mutableIntStateOf(-1)       //숨긴 Text Index

    private val searchResultMap = MapViewModel<Int,ArrayList<MatchResult>>()   //검색 결과
    private var searchResultKeys : List<Int>? = null   //검색 내용으로 이동하기 위한 position 저장 배열

    private var isContentLoading by mutableStateOf(true)    //true:Text loading 중인 상태

    @Preview
    @Composable
    fun Preview() {
        isShowFastScroll = true
        isShowSearch = true
        textListState.add("text text key text key text keytext text textkey text key")
        Content()
    }
    private var firstIndex = -1     //첫 실행때 페이지 이동을 위한 값, 사용후 -값을 설정하여 1회만 사용하게 함
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentPath = intent.getStringExtra(ActMain.EXTRA_BROWSER_PATH)?:""
        intent.getStringArrayListExtra(ActMain.EXTRA_BROWSER_PATH_ARRAY)?.let { filePathList.set(it) }
        if (TextUtils.isEmpty(contentPath)) {
            finish()
            return
        }

        textData = DBMgr.instance.bookmarkLoad(contentPath)?:TextItemData(contentPath, 0)
        if (textData.mId < 0) {
            textData.mId = DBMgr.instance.insertTextData(textData)
        } else {
            firstIndex = textData.mPageNum
        }

        setContent {
            Box(modifier = Modifier.fillMaxSize(1f)) {
                if (isContentLoading) {
                    Loading()
                } else {
                    Content()
                    ManuUI()
                }
            }

        }

        readBook()
    }

    override fun onPause() {
        super.onPause()
        textData.mPageNum = firstVisibleItemIndex
        DBMgr.instance.updateTextData(textData)
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
        } else {
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

        // 코루틴 스코프와 현재 수행 중인 스크롤 작업을 저장할 변수
        val coroutineScope = rememberCoroutineScope()
        var scrollJob by remember { mutableStateOf<Job?>(null) }

        // 1. 스크롤바 버튼 위치 계산 (리스트가 움직일 때 버튼도 따라오도록)
        // derivedStateOf를 사용하여 불필요한 계산 최소화
        val indicatorOffset by remember {
            derivedStateOf {
                if (listSize <= 0 || boxHeight <= 0 || buttonHeight <= 0) 0f
                else {
                    val layoutInfo = listState.layoutInfo
                    val visibleItemsCount = layoutInfo.visibleItemsInfo.size
                    val maxScrollIndex = (listSize - visibleItemsCount).coerceAtLeast(1)

                    // 현재 리스트의 진행률 (0.0 ~ 1.0)
                    val progress = (listState.firstVisibleItemIndex.toFloat() / maxScrollIndex.toFloat()).coerceIn(0f, 1f)

                    // 이동 가능한 최대 픽셀 범위
                    val maxOffset = (boxHeight - buttonHeight).coerceAtLeast(0).toFloat()

                    progress * maxOffset
                }
            }
        }

        // 2. 드래그 중일 때 버튼의 임시 위치를 저장하는 변수
        // isDragging 상태를 두어 드래그 중에는 indicatorOffset(리스트 기준)을 무시하고 내 손가락(dragOffset)을 우선시함
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
                // 드래그 중이면 내 손가락 위치(dragOffset), 아니면 리스트 위치(indicatorOffset)를 따라감
                .offset {
                    IntOffset(0, (if (isDragging) dragOffset else indicatorOffset).roundToInt())
                }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        val maxOffset = (boxHeight - buttonHeight).coerceAtLeast(1).toFloat()

                        // 드래그 위치 즉시 업데이트 (UI 반응성 확보)
                        dragOffset = (dragOffset + delta).coerceIn(0f, maxOffset)

                        // 인덱스 계산
                        val layoutInfo = listState.layoutInfo
                        val visibleItemsCount = layoutInfo.visibleItemsInfo.size
                        val maxScrollIndex = (listSize - visibleItemsCount).coerceAtLeast(1)
                        val newIndex = ((dragOffset / maxOffset) * maxScrollIndex).toInt().coerceIn(0, listSize - 1)

                        // 핵심 성능 최적화: 이전 스크롤 취소 후 최신 위치로 이동
                        if (newIndex != listState.firstVisibleItemIndex) {
                            scrollJob?.cancel() // 1. 이전 이동 명령 취소
                            scrollJob = coroutineScope.launch {
                                listState.scrollToItem(newIndex) // 2. 최신 위치로 이동
                            }
                        }
                    },
                    onDragStarted = {
                        isDragging = true
                        dragOffset = indicatorOffset // 드래그 시작 시 현재 위치에서 시작
                    },
                    onDragStopped = {
                        isDragging = false
                        // 드래그가 끝나면 마지막 위치 확정
                    }
                )
                .background(Color.Red, RoundedCornerShape(4.dp))
            )
        }
    }

    private var prevIsEnable by mutableStateOf(false)       //숨긴 Text Index
    private var nextIsEnable by mutableStateOf(false)       //숨긴 Text Index
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

                CLog.e("KDS3393_TEST_MOVE prev moveIndex[${moveIndex}] moveLine[${if (moveIndex >= 0) searchResultKeys!![moveIndex] else ""}]")
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
                CLog.e("KDS3393_TEST_MOVE prev moveIndex[${moveIndex}] moveLine[${if (moveIndex >= 0) searchResultKeys!![moveIndex] else ""}]")
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

    private var textColorIndex by mutableIntStateOf(SharePref[SharePref.SHARE_TEXT_COLOR_INDEX, 3])       //Text와 background의 color
    private var textSizeIndex by mutableIntStateOf(SharePref[SharePref.SHARE_TEXT_SIZE_INDEX, 1])       //TextSize
    private var textGapIndex by mutableIntStateOf(SharePref[SharePref.SHARE_TEXT_GAP_INDEX, 0])       //text 줄 간격
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
                color = sColors[textColorIndex][1], // 설정된 테마 색상 적용
                lineHeight = sSizes[textSizeIndex].dp2sp * sGapss[textGapIndex],
                modifier = Modifier
                    .weight(1f) // 남은 가로 공간을 모두 차지하도록 설정
                    .padding(horizontal = 8.dp)
            )
        }
    }


    private var isShowMenu by mutableStateOf(true)  //메뉴 보여줄지 여부
    private var isShowSearch by mutableStateOf(false)    //검색 모드
    private var isShowSetting by mutableStateOf(false)    //설정
    private var isShowFastScroll by mutableStateOf(false)    //빠른 스크롤 모드
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
                }
            }
        }
    }

    private var corutineJob : Job? = null
    private fun readBook(charset: Charset? = StandardCharsets.UTF_8) {
        if (corutineJob?.isActive == true) {
            return
        }
        isContentLoading = true
        corutineJob = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            val file = File(contentPath)
            if (file.exists() && file.isFile && file.canRead()) {
                try {
                    textListState.clearList()
                    var count = 0
                    if (hideLine > 0) {
                        textListState.add("● ● ●")
                    }

                    if (charset != null) {
                        //SDK 26이상 지원
                        Files.lines(Paths.get(contentPath),charset).use { stream -> stream.forEach { line ->
                            if (hideLine > count) {
                                count++
                            } else {
                                textListState.add(line.replace("&nbsp;", ""))
                            }
                        }}
                    } else {
                        FileInputStream(contentPath).use { fis ->
                            // BOM 바이트가 존재하는지 확인합니다.
                            val bom = ByteArray(3)
                            val readBytes = fis.read(bom, 0, 3)
                            // UTF-8 BOM: 0xEF, 0xBB, 0xBF
                            val hasBOM = readBytes == 3 && bom[0] == 0xEF.toByte() && bom[1] == 0xBB.toByte() && bom[2] == 0xBF.toByte()
                            // BOM이 없으면 스트림의 위치를 다시 처음으로 설정합니다.
                            if (!hasBOM) {
                                fis.channel.position(0)
                            }
                            InputStreamReader(fis, StandardCharsets.UTF_8).use { isr ->
                                BufferedReader(isr).use { reader ->
                                    var line: String
                                    while (reader.readLine().also { line = it } != null) {
                                        textListState.add(line.replace("&nbsp;", ""))
                                    }
                                }
                            }
                        }
                    }
                    isContentLoading = false
                } catch (e: Exception) {
                    CLog.e("KDS3393_TEST_readBook Exception[$charset] = $contentPath")
                    CLog.e(e)
                    corutineJob?.cancel()
                    if (charset == StandardCharsets.UTF_8) {
                        readBook(StandardCharsets.UTF_16LE)
                    } else if (charset == StandardCharsets.UTF_16LE) {
                        readBook(null)
                    }
                }
            }
        }
    }

    //key : 검색할 키워드
    private suspend fun search(key:String) : Int {
        runBlocking {
            val job = launch {
                CLog.e("KDS3393_TEST_search START key = $key")
                var lineCount = 0
                if (key.isNotEmpty()) {
                    val pattern = Regex("\\b$key\\w*\\b")
                    searchResultMap.clearList()
                    for ((index, line) in textListState.get().withIndex()) {
                        lineCount++
                        pattern.findAll(line).forEach { matchResult ->
                            var matchs = searchResultMap.get()[index]
                            if (matchs == null) {
                                matchs = ArrayList()
                                searchResultMap.add(index,matchs)
                            }
                            matchs.add(matchResult)
                        }
                    }
                    searchResultKeys = searchResultMap.get().keys.sorted().sorted()
                    run breaker@ {
                        searchResultKeys?.forEachIndexed { index, line ->
                            if (firstVisibleItemIndex < line) {
                                prevIsEnable = index > 0
                                return@breaker
                            }
                        }
                    }
                    run breaker@ {
                        searchResultKeys?.forEachIndexed { index, line ->
                            if (firstVisibleItemIndex < line) {
                                nextIsEnable = index < searchResultKeys!!.size
                                return@breaker
                            }
                        }
                    }

                    CLog.e("KDS3393_TEST_search ResultKeys = $searchResultKeys")
                }
                CLog.e("KDS3393_TEST_search END key = $key lineCount[$lineCount] findCount[${searchResultMap.size()}]")
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