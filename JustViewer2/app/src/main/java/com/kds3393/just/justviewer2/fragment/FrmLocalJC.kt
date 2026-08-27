package com.kds3393.just.justviewer2.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.AudioFile
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VideoFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.activity.ActBase
import com.kds3393.just.justviewer2.activity.ActMain
import com.kds3393.just.justviewer2.compose.CIcon
import com.kds3393.just.justviewer2.compose.CText
import com.kds3393.just.justviewer2.compose.Colors
import com.kds3393.just.justviewer2.compose.ExplorerBar
import com.kds3393.just.justviewer2.compose.ListViewModel
import com.kds3393.just.justviewer2.compose.click
import com.kds3393.just.justviewer2.compose.clickableOnce
import com.kds3393.just.justviewer2.compose.dp2sp
import com.kds3393.just.justviewer2.data.FileData
import com.kds3393.just.justviewer2.db.DBMgr
import com.kds3393.just.justviewer2.image.ActImageViewer
import com.kds3393.just.justviewer2.renamer.ActRenamer
import com.kds3393.just.justviewer2.text.ActTextViewerJC
import com.kds3393.just.justviewer2.utils.ACTION
import com.kds3393.just.justviewer2.utils.Event
import com.kds3393.just.justviewer2.utils.SharePref
import common.lib.debug.CLog
import common.lib.utils.FileUtils
import common.lib.utils.SharedBus
import java.io.File

enum class SortType {
    NAME, SIZE, RANDOM
}

class FrmLocalJC : FrmBase() {

    val fileListState = ListViewModel<FileData>()

    var currentPath by mutableStateOf("")
    var isSelectionMode by mutableStateOf(false)
    val selectedItems = mutableStateListOf<FileData>()

    var showDeleteDialog by mutableStateOf(false)

    var currentSortType by mutableStateOf(SortType.NAME)
    var isSortAscending by mutableStateOf(true)
    var isBookmarkFirst by mutableStateOf(false) // 북마크 우선 정렬 상태

    var isSearchMode by mutableStateOf(false)
    var searchQuery by mutableStateOf("")
    var currentRawFiles by mutableStateOf<List<File>?>(null)

    val addFavoriteQueue = mutableStateListOf<FileData>()
    val currentAddFavoriteTarget: FileData? get() = addFavoriteQueue.firstOrNull()

    var type: Int = 0
        set(param) { field = setArg("type", param) }

    var rootPath: String = ""
        set(param) { field = setArg("rootPath", param) }

    companion object {
        const val TYPE_LOCAL_EXPLORER = 0

        const val KEY_SORT_TYPE = "KEY_LOCAL_SORT_TYPE"
        const val KEY_SORT_ASC = "KEY_LOCAL_SORT_ASC"
        const val KEY_BOOKMARK_FIRST = "KEY_LOCAL_BOOKMARK_FIRST"
    }

    private fun loadSortPreferences() {
        val savedSortName = SharePref[KEY_SORT_TYPE, SortType.NAME.name]

        currentSortType = try {
            SortType.valueOf(savedSortName)
        } catch (e: Exception) {
            SortType.NAME
        }
        isSortAscending = SharePref[KEY_SORT_ASC, true]
        isBookmarkFirst = SharePref[KEY_BOOKMARK_FIRST, false]
    }

    private fun changeSortAndSave(type: SortType, isAsc: Boolean) {
        currentSortType = type
        isSortAscending = isAsc

        SharePref.put(KEY_SORT_TYPE, currentSortType.name)
        SharePref.put(KEY_SORT_ASC, isSortAscending)

        updateFileList()
    }

    private fun changeBookmarkFirstAndSave(isFirst: Boolean) {
        isBookmarkFirst = isFirst
        SharePref.put(KEY_BOOKMARK_FIRST, isBookmarkFirst)

        updateFileList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        type = getArg("type", type)
        rootPath = getArg("rootPath", rootPath)

        loadSortPreferences()

        SharedBus.register<Event.Bookmark>(lifecycleScope) {
            updateFileList()
        }
        SharedBus.register<Event.FileAction>(lifecycleScope) { action ->
            if (action.action == ACTION.FILE_REMOVE) {
                val targetPaths = action.files
                val updatedFiles = currentRawFiles?.filter { file ->
                    file.path !in targetPaths && file.absolutePath !in targetPaths
                }
                if (currentRawFiles?.size != updatedFiles?.size) {
                    currentRawFiles = updatedFiles
                    selectedItems.removeAll { it.mPath in targetPaths }
                    updateFileList()
                }
            }
        }
        return ComposeView(requireContext()).apply {
            setContent {
                LaunchedEffect(Unit) {
                    if (currentPath.isEmpty()) {
                        initPath(rootPath)
                    }
                }

                LaunchedEffect(searchQuery, currentSortType, isBookmarkFirst) {
                    if (isSearchMode || searchQuery.isNotEmpty() || currentPath.isNotEmpty()) {
                        updateFileList()
                    }
                }

                MaterialTheme {
                    LocalScreen()
                    ShowAddFavorite()
                }
            }
        }
    }

    // --- Logic Functions ---

    fun initPath(path: String) {
        loadFileLists(path)
    }

    fun setFileList(path: String) {
        rootPath = path
        loadFileLists(path)
    }

    private fun loadFileLists(path: String?) {
        if (path == null) return

        var targetPath = path
        var file = File(targetPath)

        while (!file.exists() && file.parent != null) {
            file = File(file.parent!!)
        }
        targetPath = file.path
        currentPath = targetPath

        clearSelection()
        closeSearchMode()

        if (targetPath.lastIndexOf("zip") > 0) {
            currentRawFiles = emptyList()
            updateFileList()
        } else {
            val target = File(targetPath)
            if (target.isDirectory) {
                currentRawFiles = FileUtils.getDirFileList(targetPath)
                updateFileList()
            }
        }
    }

    private fun updateFileList() {
        val files = currentRawFiles
        val result = ArrayList<FileData>()

        if (currentPath != rootPath && currentPath != "/") {
            val parentData = FileData(File(currentPath))
            parentData.mDisplayName = ".."
            parentData.mPath = ".."
            parentData.mIsDirectory = true
            result.add(parentData)
        }

        if (files == null) {
            fileListState.set(result)
            return
        }

        val bookmarkPaths = DBMgr.instance.getBookmarkList().map { it.targetPath }.toSet()
        val favoriteMap = DBMgr.instance.getFileFavoriteList(currentPath)
        val dirs = ArrayList<FileData>()
        val fileItems = ArrayList<FileData>()
        val favoItems = ArrayList<FileData>()

        for (f in files) {
            if (f.isHidden) continue

            if (searchQuery.isNotEmpty()) {
                if (!f.name.contains(searchQuery, ignoreCase = true)) {
                    continue
                }
            }

            val data = FileData(f)

            data.mIsBookmarked = bookmarkPaths.contains(data.mPath)

            if (favoriteMap.containsKey(data.mPath)) {
                data.mIsFavoriteFile = true
                favoItems.add(data)
            } else if (f.isDirectory) {
                dirs.add(data)
            } else {
                fileItems.add(data)
            }
        }

        favoItems.sortBy { it.mDisplayName }
        dirs.sortBy { it.mDisplayName }

        // 1차 정렬
        when (currentSortType) {
            SortType.NAME -> fileItems.sortBy { it.mDisplayName }
            SortType.SIZE -> if (isSortAscending) {
                fileItems.sortBy { File(it.mPath).length() }
            } else {
                fileItems.sortByDescending { File(it.mPath).length() }
            }
            SortType.RANDOM -> fileItems.shuffle()
        }

        // 2차 정렬: 북마크 항목 상단 표시 (코틀린의 정렬은 Stable하므로 기존 순서 유지)
        if (isBookmarkFirst) {
            fileItems.sortByDescending { it.mIsBookmarked }
        }

        result.addAll(favoItems)
        result.addAll(dirs)
        result.addAll(fileItems)

        fileListState.set(result)
    }

    private fun moveParent(rootPath: String): Boolean {
        if (currentPath == rootPath || currentPath == "/") {
            return false
        }
        val file = File(currentPath)
        loadFileLists(file.parent)
        return true
    }

    private fun closeSearchMode() {
        isSearchMode = false
        searchQuery = ""
        updateFileList()
    }

    private fun toggleItemSelection(data: FileData) {
        val existingIndex = selectedItems.indexOfFirst { it.mPath == data.mPath }
        if (existingIndex >= 0) {
            selectedItems.removeAt(existingIndex)
        } else {
            selectedItems.add(data)
        }

        if (selectedItems.isEmpty()) {
            isSelectionMode = false
        }
    }

    private fun clearSelection() {
        if (isSelectionMode) {
            isSelectionMode = false
        }
        selectedItems.clear()
        addFavoriteQueue.clear()
    }

    private fun deleteSelectedItems() {
        val targets = ArrayList(selectedItems)
        var isDeleted = false
        for (item in targets) {
            val file = File(item.mPath)
            if (file.exists()) {
                FileUtils.deleteFile(file)
                val extension = FileUtils.getExtension(file.name)
                if (listOf("avi", "mp4", "wmv", "mkv").contains(extension.lowercase())) {
                    val smiPath = file.absolutePath.substringBeforeLast(".") + ".smi"
                    FileUtils.deleteFile(File(smiPath))
                }
                isDeleted = true
            }
        }
        if (isDeleted) {
            loadFileLists(currentPath)
        }
        showDeleteDialog = false
    }

    private fun addSelectedToFavorites() {
        val targets = ArrayList(selectedItems)
        val dirTargets = ArrayList<FileData>()
        for (item in targets) {
            if (!item.mIsDirectory) {
                val file = File(item.mPath)
                val existingItem = DBMgr.instance.loadFavorite(item.mPath)
                if (existingItem != null) {
                    DBMgr.instance.deleteFavoriteData(existingItem.mId)
                } else {
                    val data = FileData(FileData.TYPE_LOCAL_FILE, file.path, file.parent)
                    DBMgr.instance.insertFavoriteData(data)
                }
            } else {
                dirTargets.add(item)
            }
        }
        if (dirTargets.isNotEmpty()) {
            addFavoriteQueue.addAll(dirTargets)
        } else {
            loadFileLists(currentPath)
            clearSelection()
        }
    }

    private fun confirmAddFavorite(name: String) {
        val target = currentAddFavoriteTarget ?: return
        val data = FileData(FileData.TYPE_LOCAL_DIR, target.mPath, name)
        DBMgr.instance.insertFavoriteData(data)
        addFavoriteQueue.remove(target)
        if (addFavoriteQueue.isEmpty()) {
            loadFileLists(currentPath)
            clearSelection()
        }
        SharedBus.post(Event.FavoriteDir(target.mPath))
    }

    // --- UI Composables ---

    @Composable
    fun LocalScreen() {
        val canGoUp = currentPath.isNotEmpty() && currentPath != rootPath && currentPath != "/"
        val shouldInterceptBack = isSelectionMode || isSearchMode || canGoUp

        var showSortMenu by remember { mutableStateOf(false) }

        BackHandler(enabled = shouldInterceptBack) {
            when {
                isSearchMode -> closeSearchMode()
                isSelectionMode -> clearSelection()
                else -> moveParent(rootPath)
            }
        }

        Scaffold(
            topBar = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (isSearchMode) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            onClose = { closeSearchMode() }
                        )
                    } else {
                        val fileList = fileListState.stateList
                        ExplorerBar(
                            act = requireActivity() as ActBase,
                            title = FileUtils.getFileName(currentPath),
                            path = currentPath,
                            fileCount = fileList.size,
                            selectedCount = selectedItems.size,
                            containerColor = Colors.White,
                            contextColor = Color.Black,
                            barHeight = 54.dp
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                val sortName = when (currentSortType) {
                                    SortType.NAME -> "이름순"
                                    SortType.SIZE -> if (isSortAscending) "용량순 ▲" else "용량순 ▼"
                                    SortType.RANDOM -> "랜덤순"
                                }
                                CText(sortName, fontSize = 15.dp2sp, modifier = Modifier
                                    .clickableOnce { showSortMenu = true }
                                    .padding(5.dp))

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false },
                                    modifier = Modifier.background(Colors.White)
                                ) {
                                    // 북마크 최상단 체크박스
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Checkbox(
                                                    checked = isBookmarkFirst,
                                                    onCheckedChange = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                CText("북마크", fontSize = 15.dp2sp, color = Colors.Black)
                                            }
                                        },
                                        onClick = {
                                            changeBookmarkFirstAndSave(!isBookmarkFirst)
                                            showSortMenu = false
                                        }
                                    )

                                    HorizontalDivider(color = Colors.Gray200)

                                    DropdownMenuItem(
                                        text = { CText("이름순", fontSize = 15.dp2sp, color = if(currentSortType == SortType.NAME) Colors.Default else Colors.Black) },
                                        onClick = {
                                            changeSortAndSave(SortType.NAME, true)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            val sizeText = if (currentSortType == SortType.SIZE) {
                                                if (isSortAscending) "용량순 (높은순으로)" else "용량순 (낮은순으로)"
                                            } else {
                                                "용량순"
                                            }
                                            CText(sizeText, fontSize = 15.dp2sp, color = if(currentSortType == SortType.SIZE) Colors.Default else Colors.Black)
                                        },
                                        onClick = {
                                            val newAsc = if (currentSortType == SortType.SIZE) !isSortAscending else true
                                            changeSortAndSave(SortType.SIZE, newAsc)
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { CText("랜덤순", fontSize = 15.dp2sp, color = if(currentSortType == SortType.RANDOM) Colors.Default else Colors.Black) },
                                        onClick = {
                                            changeSortAndSave(SortType.RANDOM, true)
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                modifier = Modifier
                                    .clickableOnce { isSearchMode = true }
                                    .size(24.dp)
                            )
                        }
                    }
                }
            },
            bottomBar = {
                if (isSelectionMode) {
                    BottomActionMenu(
                        onDelete = { showDeleteDialog = true },
                        onFavorite = { addSelectedToFavorites() },
                        onRename = {
                            val intent = Intent(activity, ActRenamer::class.java)
                            intent.putStringArrayListExtra(ActRenamer.EXTRA_RENAME_FILE_LIST, ArrayList(selectedItems.map { it.mPath }))
                            runActResult(intent) { _, _, _ ->
                                loadFileLists(currentPath)
                            }
                        },
                    )
                }
            }
        ) { paddingValues ->
            val fileList = fileListState.stateList
            Box(modifier = Modifier.padding(paddingValues)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Colors.White)
                ) {
                    items(fileList, key = { it.mPath }) { fileData ->
                        FileRowItem(
                            item = fileData,
                            isSelected = selectedItems.any { it.mPath == fileData.mPath },
                            searchQuery = searchQuery,
                            onItemClick = {
                                if (fileData.mPath == "..") {
                                    moveParent(rootPath)
                                } else if (fileData.mIsDirectory) {
                                    loadFileLists(fileData.mPath)
                                } else {
                                    val file = File(fileData.mPath)
                                    if (activity is ActBase) {
                                        FileManager.startViewer(activity as ActBase, file, null)
                                    }
                                }
                            },
                            onCheckToggle = {
                                if (!isSelectionMode) {
                                    isSelectionMode = true
                                }
                                toggleItemSelection(fileData)
                            }
                        )
                        HorizontalDivider(color = Colors.Gray200, thickness = 0.5.dp)
                    }
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { CText("Delete", fontSize = 18.dp2sp, fontWeight = FontWeight.Bold) },
                        text = { CText("${selectedItems.size}개의 파일 및 폴더를 삭제하시겠습니까?", fontSize = 14.dp2sp) },
                        confirmButton = {
                            TextButton(onClick = { deleteSelectedItems() }) {
                                CText("확인", fontSize = 15.dp2sp, color = Colors[0x0000FF])
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                CText("취소", fontSize = 15.dp2sp, color = Colors.Red)
                            }
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun SearchBar(
        query: String,
        onQueryChange: (String) -> Unit,
        onClose: () -> Unit
    ) {
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Colors.White)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            CText("Search...", color = Color.Gray, fontSize = 16.dp2sp)
                        }
                        innerTextField()
                    }
                },
                textStyle = TextStyle(
                    fontSize = 16.dp2sp,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close Search",
                modifier = Modifier
                    .clickableOnce { onClose() }
                    .size(24.dp)
            )
        }
    }

    @Composable
    fun HighlightedText(
        text: String,
        query: String,
        modifier: Modifier = Modifier,
        color: Color = Colors[0x6E6E6E],
        fontSize: TextUnit = 16.dp2sp,
        overflow: TextOverflow = TextOverflow.Ellipsis
    ) {
        val annotatedString = buildAnnotatedString {
            if (query.isEmpty()) {
                append(text)
            } else {
                val startIndex = text.indexOf(query, ignoreCase = true)
                if (startIndex >= 0) {
                    val endIndex = startIndex + query.length
                    append(text.take(startIndex))
                    withStyle(style = SpanStyle(color = Colors.Default, fontWeight = FontWeight.Bold)) {
                        append(text.substring(startIndex, endIndex))
                    }
                    append(text.substring(endIndex))
                } else {
                    append(text)
                }
            }
        }

        val textMeasurer = rememberTextMeasurer()

        BoxWithConstraints(modifier = modifier) {
            val textLayoutResult = textMeasurer.measure(
                text = annotatedString,
                style = TextStyle(fontSize = 13.dp2sp),
                maxLines = 1
            )

            val dynamicMaxLines = if (textLayoutResult.size.width > constraints.maxWidth) 2 else 1

            Text(
                text = annotatedString,
                color = color,
                fontSize = fontSize,
                maxLines = dynamicMaxLines,
                overflow = overflow,
                softWrap = dynamicMaxLines > 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.dp2sp,
                    maxFontSize = fontSize,
                    stepSize = 1.dp2sp
                )
            )
        }
    }

    @Composable
    fun FileRowItem(
        item: FileData,
        isSelected: Boolean,
        searchQuery: String,
        onItemClick: () -> Unit,
        onCheckToggle: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(if (isSelected) Colors[0xE0E0E0] else Colors.White)
                .clickableOnce { if (isSelectionMode) onCheckToggle() else onItemClick() }
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(22.dp, 28.dp)) {
                CIcon(painter = FileManager.getFileIconResource(item),
                    tint = Colors.Gray600,
                    modifier = Modifier.align(Alignment.CenterStart).size(22.dp))
                if (item.mIsBookmarked) {
                    CIcon(painter = Icons.Outlined.Bookmark,
                        tint = Colors.Default,
                        modifier = Modifier.align(Alignment.TopEnd).size(14.dp))
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            HighlightedText(
                text = item.mDisplayName ?: "",
                query = searchQuery,
                color = Colors[0x6E6E6E],
                fontSize = 16.dp2sp,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(5.dp))

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clickableOnce { onCheckToggle() },
                contentAlignment = Alignment.Center
            ) {
                if (isSelectionMode) {
                    CIcon(painter = painterResource(id = if (isSelected) R.drawable.check_on else R.drawable.check_off))
                } else {
                    CIcon(painter = if (item.mIsFavoriteFile) Icons.Default.Star else Icons.Default.CheckCircleOutline,
                        tint = Colors.Gray600,
                    )
                }
            }
        }
    }

    @Composable
    fun BottomActionMenu(
        onDelete: () -> Unit,
        onFavorite: () -> Unit,
        onRename: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Colors[0x22252D]),
            verticalAlignment = Alignment.CenterVertically
        ) {

            CIcon(painter = Icons.Default.Delete, tint = Colors.Default,
                modifier = Modifier.click(showRipple = false, onClick = onDelete).padding(horizontal = 10.dp)
            )
            CIcon(painter = Icons.Default.Star, tint = Colors.Default,
                modifier = Modifier.click(showRipple = false, onClick = onFavorite).padding(horizontal = 10.dp)
            )
            CIcon(painter = Icons.Default.TextFields, tint = Colors.Default,
                modifier = Modifier.click(showRipple = false, onClick = onRename).padding(horizontal = 10.dp)
            )
        }
    }

    @Composable
    fun ShowAddFavorite() {
        if (currentAddFavoriteTarget != null) {
            val target = currentAddFavoriteTarget!!
            val existingItem = remember(target) { DBMgr.instance.loadFavorite(target.mPath) }

            LaunchedEffect(target) {
                if (existingItem != null) {
                    Toast.makeText(context, "동일한 즐겨찾기가 있습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            var name by remember {
                mutableStateOf(existingItem?.mDisplayName ?: FileUtils.getFileName(target.mPath))
            }
            val displayPath = existingItem?.mPath ?: target.mPath

            Dialog(onDismissRequest = {
                addFavoriteQueue.clear()
                loadFileLists(currentPath)
                clearSelection()
            }) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Colors.White),
                    modifier = Modifier
                        .width(300.dp)
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CText(
                            text = "즐겨찾기 추가",
                            color = Colors[0x7C79E1],
                            fontSize = 20.dp2sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 18.dp)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { CText("Name", fontSize = 14.dp2sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = displayPath,
                            onValueChange = { },
                            label = { CText("Path", fontSize = 14.dp2sp) },
                            singleLine = true,
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    addFavoriteQueue.clear()
                                    loadFileLists(currentPath)
                                    clearSelection()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Colors[0xC6C6C6]),
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier.width(90.dp)
                            ) {
                                CText("Cancel", fontSize = 15.dp2sp, color = Colors.White)
                            }

                            Button(
                                onClick = {
                                    if (name.isEmpty()) {
                                        Toast.makeText(context, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        confirmAddFavorite(name)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Colors[0x7C79E1]),
                                shape = RoundedCornerShape(15.dp),
                                modifier = Modifier.width(90.dp)
                            ) {
                                CText("Add", fontSize = 15.dp2sp, color = Colors.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

object FileManager {
    fun getFileIconResource(item: FileData): ImageVector {
        if (item.mIsDirectory) {
            return Icons.Outlined.Folder
        }
        val ext = item.mExt?.lowercase() ?: ""

        return when (ext) {
            "zip" -> Icons.Outlined.Inventory2
            "mp3", "flac" -> Icons.Outlined.AudioFile
            "jpg", "jpeg", "gif", "png" -> Icons.Outlined.Image
            "mp4", "avi", "wmv", "mkv" -> Icons.Outlined.VideoFile
            "txt", "smi", "log" -> Icons.AutoMirrored.Outlined.TextSnippet
            else -> Icons.AutoMirrored.Default.InsertDriveFile
        }
    }

    fun startViewer(context: Context, file: File, fileDatas: ArrayList<FileData>?) {
        val array = ArrayList<String>()
        val extension = FileUtils.getExtension(file.name)
        if (fileDatas == null) {
            array.add(file.path)
        } else {
            for (f in fileDatas) {
                array.add(f.mPath)
            }
        }
        if (extension.equals("zip", ignoreCase = true)) {
            val intent = Intent(context, ActImageViewer::class.java)
            intent.putExtra(ActMain.EXTRA_BROWSER_PATH, file.absolutePath)
            if (array.isNotEmpty()) {
                intent.putExtra(ActMain.EXTRA_BROWSER_PATH_ARRAY, array)
            }
            context.startActivity(intent)
        } else if (extension.equals("avi", ignoreCase = true) || extension.equals("mp4", ignoreCase = true) || extension.equals("wmv", ignoreCase = true) || extension.equals("mkv", ignoreCase = true)) {
            val intent = Intent(Intent.ACTION_VIEW, file.path.toUri())
            intent.setDataAndType(file.path.toUri(), "video/mp4")
            context.startActivity(intent)
        } else if (extension.equals("txt", ignoreCase = true) || extension.equals("smi", ignoreCase = true) || extension.equals("log", ignoreCase = true)) {
            val intent = Intent(context, ActTextViewerJC::class.java)
            intent.putExtra(ActMain.EXTRA_BROWSER_PATH, file.absolutePath)
            context.startActivity(intent)
        } else if (extension.equals("epub", ignoreCase = true)) {
            CLog.e("KDS3393_TEST_file\n" + "path[${getPath(context, Uri.fromFile(file))}]")
        }
    }

    fun getPath(context:Context, uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = context.contentResolver.query(uri!!, projection, null, null, null) ?: return null
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val s = cursor.getString(columnIndex)
        cursor.close()
        return s
    }
}