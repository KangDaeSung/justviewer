@file:OptIn(ExperimentalMaterial3Api::class)

package com.kds3393.just.justviewer2.compose

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.activity.ActBase
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(act: ActBase, title: String = "", barHeight:Dp = 80.dp,
           backBtnColor:Color = Color(0xff333333),
           containerColor:Color = Color.White,
           contextColor:Color = Color.Black,
           titleContent: (@Composable BoxScope.() -> Unit)? = null,
           actions: @Composable RowScope.() -> Unit = {}) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    CenterAlignedTopAppBar(
        modifier = Modifier.height(barHeight),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
        title = {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxHeight()) {
                if (titleContent == null) {
                    CText(title,
                        maxLines = 1,
                        fontSize = 16.dp2sp,
                        color = contextColor,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    titleContent()
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { act.backPressed() }) {
                Icon(painter = painterResource(id = R.drawable.svg_arrow), tint = backBtnColor, contentDescription = "go back", modifier = Modifier.rotate(180f))
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorerBar(act: ActBase, title: String = "", path:String = "", barHeight:Dp = 80.dp,
           backBtnColor:Color = Color(0xff333333),
           containerColor:Color = Color.White,
           contextColor:Color = Color.Black,
           actions: @Composable RowScope.() -> Unit = {}) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    TopAppBar(
        modifier = Modifier.height(barHeight),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
        title = {
            Column(verticalArrangement = Arrangement.Center) {
                if (title.isNotEmpty()) {
                    CText(title,
                        maxLines = 1,
                        fontSize = 15.dp2sp,
                        color = contextColor,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
                if (path.isNotEmpty()) {
                    CText(path,
                        maxLines = 1,
                        fontSize = 10.dp2sp,
                        color = contextColor,
                        fontWeight = FontWeight.Normal,
                        overflow = TextOverflow.Ellipsis,
                        minFontSize = 6.dp2sp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            )
                        )
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = { act.backPressed() }) {
                Icon(painter = painterResource(id = R.drawable.svg_arrow), tint = backBtnColor, contentDescription = "go back", modifier = Modifier.rotate(180f))
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun Loading(modifier:Modifier = Modifier) {
    Image(
        imageVector = Icons.Default.Refresh,
        contentDescription = "Loading",
        modifier = modifier,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
    )
}

data class TabInfo(val id:Int, val title:String)
@Composable
fun TabNavi(tabs:ArrayList<TabInfo>,
            modifier: Modifier = Modifier,
            indicatorColor : Color = Color.Black,
            onSelectedTab:(Int,TabInfo) -> Unit) {
    var selectTabIndex by remember { mutableIntStateOf(0) }
    TabRow(
        selectedTabIndex = selectTabIndex,
        containerColor = Color.White,
        contentColor = Color.Black,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectTabIndex]),
                color = indicatorColor
            )
        },
        modifier = modifier
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectTabIndex == index,
                onClick = {
                    selectTabIndex = index
                    onSelectedTab(index, tab)
                },
                modifier = Modifier.padding(16.dp)
            ) {
                CText(text = tab.title, fontSize = 16.dp2sp, minFontSize = 10.dp2sp)
            }
        }
    }
}

@Composable
fun AniTabNavi(
    items: List<String>,
    modifier: Modifier,
    indicatorPadding: PaddingValues = PaddingValues(horizontal = 5.dp, vertical = 5.dp),
    selectedItemIndex: Int = 0,
    onSelectedTab: (index: Int) -> Unit
) {
    var tabWidth by remember { mutableStateOf(0) }

    val itemSize = (tabWidth.toDp() - indicatorPadding.calculateStartPadding(LayoutDirection.Ltr) - indicatorPadding.calculateEndPadding(LayoutDirection.Ltr)) / items.size
    val indicatorOffset: Dp by animateDpAsState(
        if (selectedItemIndex == 0) {
            0.dp
        } else {
            itemSize * selectedItemIndex
        }
    )

    Box(modifier = modifier
        .onGloballyPositioned { coordinates ->
            tabWidth = coordinates.size.width
        }
        .background(color = Colors.Gray200, shape = RoundedCornerShape(50))) {

        Box(modifier = Modifier
            .padding(indicatorPadding)
            .fillMaxHeight()
            .width(itemSize)
            .offset(x = indicatorOffset)
            .clip(RoundedCornerShape(50))
            .background(Colors.White))

        Row(modifier = Modifier.fillMaxSize().padding(indicatorPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            items.forEachIndexed { index, title ->
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .width(itemSize)
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        interactionSource = MutableInteractionSource(),
                        indication = null
                    ) { onSelectedTab(index) },
                    contentAlignment = Alignment.Center) {
                    CText(text = title, fontSize = 14.dp2sp)
                }
            }
        }
    }
}

/**
 * 왼쪽에 V아이콘이 있는 버튼
 */
@Composable
fun SelectButton(text : String, modifier : Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier
        .border(width = 1.dp, color = Color(0xffC3C3C3), shape = RoundedCornerShape(50))
        .padding(start = 14.dp, end = 14.dp, top = 5.dp, bottom = 5.dp)) {
        Icon(painter = painterResource(id = R.drawable.svg_arrow),
            tint = Color(0xff333333),
            contentDescription = "", modifier = Modifier.rotate(90f))
        CText(text = text, fontSize = 12.dp2sp, color = Color(0xff666666), modifier = Modifier.padding(start = 8.dp))
    }
}

/**
 * 오른쪽에 V아이콘이 있는 버튼
 */
@Composable
fun SelectButtonV2(text : String, modifier : Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier
        .border(width = 1.dp, color = Color(0xffE0E0E0), shape = RoundedCornerShape(50))
        .padding(start = 14.dp, end = 14.dp, top = 7.dp, bottom = 7.dp)) {
        CText(text = text, fontSize = 14.dp2sp, color = Color(0xff333333), modifier = Modifier)
        Icon(painter = painterResource(id = R.drawable.svg_arrow),
            tint = Color(0xff333333),
            contentDescription = "", modifier = Modifier
                .padding(start = 7.dp)
                .size(4.dp, 8.dp)
                .rotate(90f))
    }
}

data class SelecterColors(
    val selectedTextColor: Color = Color.White,
    val normalTextColor: Color = Color.Black,
    val selectedBackgroundColor: Color = Color.Black,
    val normalBackgroundColor: Color = Color.White,
)

/**
 * 하단에서 올라오는 Bottom Sheet Dialog 선택한 Item의 color는 SelecterColors를 통해 변경한다.
 * 하단에 NaviBar와 overlap되면 bottomPadding을 추가하거나 Activity에 아래의 코드를 추가해주자
 *      WindowCompat.setDecorFitsSystemWindows(window, false)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(colors: SelecterColors = SelecterColors(),
                bottomPadding: Dp? = null,
                selectIndex: Int, list: ArrayList<String>,
                onDismess: () -> Unit, onSuccess: (Int) -> Unit) {
    val sheetState: SheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val modifier = if (bottomPadding != null) {
        Modifier.padding(bottom = bottomPadding)
    } else {
        Modifier.navigationBarsPadding()
    }
    ModalBottomSheet(onDismissRequest = {
        onDismess()
    }, sheetState = sheetState) { // Sheet content
        LazyColumn(modifier = modifier) {
            itemsIndexed(items = list) { index, item ->
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .height(57.dp)
                    .fillMaxWidth()
                    .background(if (index != selectIndex) colors.normalBackgroundColor else colors.selectedBackgroundColor)
                    .clickable {
                        scope
                            .launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onDismess()
                                    onSuccess(index)
                                }
                            }
                    }) {
                    CText(text = item, fontSize = 14.dp2sp, textAlign = TextAlign.Center,
                        color = if (index != selectIndex) colors.normalTextColor else colors.selectedTextColor,
                        modifier = Modifier)
                }
            }
        }
    }
}

/**
 * ---- 형태의 indicator
 * @param pageCount 무한 스크롤일 경우 실제 pageCount를 설정한다.
 */
@Composable
fun LineIndicator(pagerState: PagerState,
                  modifier: Modifier = Modifier,
                  pageCount:Int = pagerState.pageCount,
                  minShowNum:Int = 2,        //default : 1개인 경우 안보임 2개부터 보임
                  minWidth:Dp = 70.dp,
                  lineHeight:Dp =  1.dp,
                  gapSize : Dp = 0.dp,
                  deselectColor : Color = Color.White,
                  selectColor : Color) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    if (pagerState.pageCount >= minShowNum) {
        Box(modifier = modifier.defaultMinSize(minWidth = minWidth).onSizeChanged { size = it }) {
            if (size.width > 0) {
                val lineWidth = (size.width - ((pageCount - 1) * gapSize.toPx())) / pageCount
                Row(modifier = Modifier.align(Alignment.TopCenter)) {
                    repeat(pageCount) {
                        Box(Modifier
                            .padding(start = if (it > 0) gapSize else 0.dp)
                            .height(lineHeight)
                            .width(lineWidth.toDp())
                            .background(color = deselectColor)
                        )
                    }
                }
                Box(Modifier
                    .jumpingDotTransition(pagerState, pageCount, gapSize, 1f)
                    .height(lineHeight)
                    .width(lineWidth.toDp())
                    .background(color = selectColor)
                )
            }
        }
    }
}

/**
 * ㆍㆍㆍㆍ 형태의 indicator
 * @param pageCount 무한 스크롤일 경우 실제 pageCount를 설정한다.
 */
@Composable
fun DotIndicator(pagerState: PagerState,
                 modifier: Modifier = Modifier,
                 pageCount:Int = pagerState.pageCount,
                 minShowNum:Int = 2,        //default : 1개인 경우 안보임 2개부터 보임
                 circleSize : Dp = 7.dp,
                 gapSize : Dp = 4.dp,
                 deselectColor : Color = Color.White,
                 selectColor : Color) {
    if (pagerState.pageCount >= minShowNum) {
        Box(modifier = modifier) {
            Row(modifier = Modifier.align(Alignment.TopCenter)) {
                repeat(pageCount) {
                    Box(
                        Modifier
                            .padding(start = if (it > 0) gapSize else 0.dp)
                            .size(circleSize)
                            .background(color = deselectColor, shape = CircleShape)
                    )
                }
            }
            Box(
                Modifier
                    .jumpingDotTransition(pagerState, pageCount, gapSize, 0.8f)
                    .size(circleSize)
                    .background(color = selectColor, shape = CircleShape)
            )
        }
    }
}

private fun Modifier.jumpingDotTransition(pagerState: PagerState, pageCount:Int = pagerState.pageCount, gap: Dp, jumpScale: Float) =
    graphicsLayer {
        val pageOffset = pagerState.currentPageOffsetFraction
        val realPage = if (pageCount == pagerState.pageCount) {
            pagerState.currentPage
        } else {
            pagerState.currentPage % pageCount
        }
        val scrollPosition = realPage + pageOffset
        translationX = scrollPosition * (size.width + gap.roundToPx()) // 8.dp - spacing between dots

        val scale: Float
        val targetScale = jumpScale - 1f

        scale = if (pageOffset.absoluteValue < .5) {
            1.0f + (pageOffset.absoluteValue * 2) * targetScale
        } else {
            jumpScale + ((1 - (pageOffset.absoluteValue * 2)) * targetScale)
        }

        scaleX = scale
        scaleY = scale
    }

data class TableData(val id:Int, val title:String, val value:String)
@Composable
fun TableInfo(infos:List<TableData>, titleSize : TextUnit,
              modifier: Modifier = Modifier,
              isEdit : Boolean = false,
              contentSpacing : Dp = 16.dp,
              titleColor : Color = Color.Black,
              content: @Composable ConstraintLayoutScope.(TableData, Modifier) -> Unit) {
    ConstraintLayout(modifier = modifier) {
        val titleIds = ArrayList<ConstrainedLayoutReference>()
        val infoIds = ArrayList<ConstrainedLayoutReference>()
        repeat(infos.size) {
            titleIds.add(createRef())
            infoIds.add(createRef())
        }
        val endBarrier = createEndBarrier(*titleIds.toTypedArray())

        infos.forEachIndexed { index, tableData ->
            CText(text = tableData.title, fontSize = titleSize, color = titleColor,
                modifier = Modifier.constrainAs(titleIds[index]) {
                    if (index == 0) {
                        top.linkTo(parent.top)
                    } else {
                        top.linkTo(infoIds[index - 1].bottom, margin = contentSpacing)
                    }
                })
            content(tableData, Modifier.constrainAs(infoIds[index]) {
                if (isEdit) {
                    top.linkTo(titleIds[index].bottom, margin = 10.dp)
                    start.linkTo(titleIds[index].start)
                    end.linkTo(parent.end)
                } else {
                    top.linkTo(titleIds[index].top)
                    start.linkTo(endBarrier, margin = 16.dp)
                    end.linkTo(parent.end)
                }
                width = Dimension.fillToConstraints
            })
        }
    }
}

/**
 * itemList에서 cellSize 만큼 row에 View를 만들어줌
 */
@Composable
fun GridLine(cellSize:Int, groupIndex:Int, itemList:MutableList<*>, modifier:Modifier = Modifier,
             spacing:Dp = 0.dp,
             content: @Composable BoxScope.(Any) -> Unit) {
    var widthPx by remember { mutableIntStateOf(0) }
    Row(modifier = modifier
        .fillMaxWidth()
        .onSizeChanged {
            widthPx = it.width
        }) {
        if (widthPx > 0) {
            val startIndex = groupIndex * cellSize
            for (index in startIndex until cellSize * (groupIndex + 1)) {
                if (index < itemList.size) {
                    val item = itemList[index]
                    val itemWidth = (widthPx - (spacing.toPx() * (cellSize - 1))) / cellSize
                    Box(modifier = Modifier
                        .padding(start = if (index - startIndex > 0) spacing else 0.dp)
                        .width(itemWidth.toDp())) {
                        content(item!!)
                    }
                }
            }
        }
    }
}

/**
 * 이미지가 없는 경우
 */
@Composable
fun ImageEmpty(imageRes:Int, modifier : Modifier = Modifier,
               colorFilter: ColorFilter? = null,
               iconWidth: Dp = 151.dp,
               iconAlpha: Float = 0.4f) {
    Box(contentAlignment= Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)

    ) {
        Image(painter = painterResource(id = imageRes),
            colorFilter = colorFilter,
            contentDescription = "",
            modifier = Modifier
                .width(iconWidth)
                .alpha(iconAlpha)
                .aspectRatio(66f / 60f))
    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val keyboardState = remember { mutableStateOf(false) }
    val view = LocalView.current

    val viewTreeObserver = view.viewTreeObserver

    DisposableEffect(viewTreeObserver) {
        val onGlobalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            keyboardState.value = if (keypadHeight > screenHeight * 0.15) {
                true
            } else {
                false
            }
        }
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalListener)

        onDispose {
            viewTreeObserver.removeOnGlobalLayoutListener(onGlobalListener)
        }
    }

    return keyboardState
}

@Composable
fun FirestLineDotTextView(modifier: Modifier = Modifier,
                          dot:String = "●", text:String, fontSize:TextUnit, color:Color,
                          splitCode:String = "\n") {
    Column(modifier = modifier) {
        text.split(splitCode).forEach { text ->
            Row(modifier = Modifier) {
                CText(text = dot, fontSize = fontSize, color = color, modifier = Modifier.alignBy(FirstBaseline))
                CText(text = text, fontSize = fontSize, color = color,
                    modifier = Modifier.padding(start = 5.dp).alignBy(FirstBaseline))
            }
        }
    }
}

//View 방식의 구방식으로 키보드가 올라와 있는지 여부 판단
@Composable
fun rememberImeVisibleByView(): State<Boolean> {
    val view = LocalView.current
    val imeVisible = remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            imeVisible.value = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return imeVisible
}

fun Color.toHex(): String {
    val intColor = this.toArgb()
    // 상위 2바이트(Alpha) 제외 → "#RRGGBB" 형태
    return String.format("#%06X", 0xFFFFFF and intColor)
}