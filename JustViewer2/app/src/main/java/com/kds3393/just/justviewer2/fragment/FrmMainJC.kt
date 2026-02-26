package com.kds3393.just.justviewer2.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.activity.ActBase
import com.kds3393.just.justviewer2.activity.ActMain
import com.kds3393.just.justviewer2.compose.CIcon
import com.kds3393.just.justviewer2.compose.CText
import com.kds3393.just.justviewer2.compose.Colors
import com.kds3393.just.justviewer2.compose.ExplorerBar
import com.kds3393.just.justviewer2.compose.ListViewModel
import com.kds3393.just.justviewer2.compose.clickableOnce
import com.kds3393.just.justviewer2.compose.dp2sp
import com.kds3393.just.justviewer2.data.FileData
import com.kds3393.just.justviewer2.db.DBInfo
import com.kds3393.just.justviewer2.db.DBMgr
import com.kds3393.just.justviewer2.dialog.BaseDialog
import com.kds3393.just.justviewer2.dialog.DlgAlert
import com.kds3393.just.justviewer2.music.player.MusicService
import com.kds3393.just.justviewer2.utils.ACTION
import com.kds3393.just.justviewer2.utils.Event
import com.kds3393.just.justviewer2.utils.SubStringComparator
import common.lib.debug.CLog
import common.lib.utils.FileUtils
import common.lib.utils.SharedBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Collections
import java.util.Locale

class FrmMainJC : FrmBase() {
    val driveListState = ListViewModel<FileData>()
    val favoriteListState = ListViewModel<FileData>()

    private var mMusicService: MusicService? = null

    // UI 상태 관리
    private var isCheckMode by mutableStateOf(false)
    private var playingFolderPath by mutableStateOf<String?>(null)
    private var isPlaying by mutableStateOf(false)

    // 선택된 아이템 IDs
    val selectedDriveIds = mutableStateListOf<Long>()
    val selectedFavoriteIds = mutableStateListOf<Long>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        SharedBus.register<Event.Music>(lifecycleScope) { event ->
            if (event.action == ACTION.MUSIC_STATE_CHANGE) {
                syncMusicState()
            }
        }

        SharedBus.register<Event.MusicFileRemove>(lifecycleScope) {
            loadDrives()
            loadFavorites()
        }

        SharedBus.register<Event.FavoriteDir>(lifecycleScope) {
            loadFavorites()
        }

        return ComposeView(requireContext()).apply {
            setContent {
                LaunchedEffect(Unit) {
                    loadDrives()
                    loadFavorites()
                }

                syncMusicState()

                MaterialTheme {
                    MainScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadDrives()
        loadFavorites()
        syncMusicState()
    }

    private fun loadDrives() {
        val list = ArrayList<FileData>()

        // 1. Internal Storage
        val internalPath = Environment.getExternalStorageDirectory().path
        list.add(FileData(FileData.TYPE_LOCAL_DIR, internalPath, "Internal storage").apply { mId = -1 })

        // 2. SD Card
        val sdcard = FileUtils.externalMounts
        if (!sdcard.isNullOrEmpty()) {
            list.add(FileData(FileData.TYPE_LOCAL_DIR, sdcard, "SDCard").apply { mId = -2 })
        }

        // 3. Network Drives
        val netDrives = DBMgr.instance.getFavoriteList(FileData.TYTP_NETWORK)
        list.addAll(netDrives)

        driveListState.set(list)
    }

    private fun loadFavorites() {
        val favorites = DBMgr.instance.getFavoriteList(FileData.TYPE_LOCAL_DIR)
        favoriteListState.set(favorites)
    }

    fun setMusicService(service: MusicService?) {
        mMusicService = service
        syncMusicState()
    }

    private fun syncMusicState() {
        if (mMusicService != null) {
            playingFolderPath = mMusicService!!.playFolderPath
            isPlaying = mMusicService!!.isPlaying
        }
    }

    private fun playMusicFolder(path: String) {
        val service = mMusicService ?: return
        if (service.playFolderPath == path) {
            if (service.isPlaying) service.pause() else service.play()
            syncMusicState()
        } else {
            val file = File(path)
            val playList = ArrayList<String>()
            val files = if (file.isDirectory) FileUtils.getDirFileList(path) else listOf(file)

            files?.forEach {
                val ext = FileUtils.getExtension(it.name)
                if (it.isFile && (ext.equals("mp3", true) || ext.equals("flac", true))) {
                    playList.add(it.path)
                }
            }
            Collections.sort(playList, SubStringComparator())

            if (playList.isNotEmpty()) {
                val intent = Intent(context, MusicService::class.java)
                intent.putExtra(MusicService.EXTRA_MUSIC_FILE_LIST, playList)
                intent.putExtra(MusicService.EXTRA_MUSIC_FOLDER_PATH, path)
                context?.startService(intent)
                syncMusicState()
            }
        }
    }

    private fun deleteSelectedItems() {
        val drivesToDelete = driveListState.get().filter { selectedDriveIds.contains(it.mId) && it.mType == FileData.TYTP_NETWORK }
        drivesToDelete.forEach { DBMgr.instance.deleteItem(DBInfo.FAVO_BOOKMARK_TABLE, it.mId) }

        val favsToDelete = favoriteListState.get().filter { selectedFavoriteIds.contains(it.mId) }
        favsToDelete.forEach { DBMgr.instance.deleteItem(DBInfo.FAVO_BOOKMARK_TABLE, it.mId) }

        setEditMode(false)
        loadDrives()
        loadFavorites()
    }

    override fun setEditMode(isEdit: Boolean) {
        isCheckMode = isEdit
        if (!isEdit) {
            selectedDriveIds.clear()
            selectedFavoriteIds.clear()
        }
    }

    private fun showDeleteDialog(data: FileData) {
        DlgAlert(activity, "삭제된 즐겨찾기", "즐겨찾기로 선택된 경로가 존재하지 않습니다.\n즐겨찾기를 삭제할까요?", object : BaseDialog.OnDialogListener {
            override fun onOk() {
                DBMgr.instance.deleteItem(DBInfo.FAVO_BOOKMARK_TABLE, data.mId)
                loadFavorites()
            }
            override fun onCancel() {}
        }).show()
    }

    private var showDeleteConfirm by mutableStateOf(false)
    @Composable
    fun MainScreen() {
        BackHandler(enabled = isCheckMode) {
            setEditMode(false)
        }

        Scaffold(
            topBar = {
                ExplorerBar(act = requireActivity() as ActBase, title = "JustViewer", containerColor = Colors.White, contextColor = Color.Black, barHeight = 54.dp)
            },
            bottomBar = {
                if (isCheckMode) {
                    BottomAppBar(
                        containerColor = Colors[0x22252D],
                        contentColor = Colors.White,
                        modifier = Modifier.height(50.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                            CIcon(
                                painter = painterResource(id = R.drawable.h_media_delete),
                                contentDescription = "Delete",
                                modifier = Modifier
                                    .padding(end = 10.dp)
                                    .clickableOnce {
                                        if (selectedDriveIds.isNotEmpty() || selectedFavoriteIds.isNotEmpty()) {
                                            showDeleteConfirm = true
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Colors.White)
            ) {
                val driveList = driveListState.stateList
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(5.dp),
                    modifier = Modifier.wrapContentHeight()
                ) {
                    items(driveList, key = { it.mId }) { drive ->
                        DriveItemRow(drive)
                    }
                }

                CText(
                    text = "즐겨찾기",
                    fontSize = 14.dp2sp,
                    color = Colors.Black,
                    modifier = Modifier.padding(start = 10.dp, top = 5.dp, bottom = 5.dp)
                )

                val favoriteList = favoriteListState.stateList
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(favoriteList, key = { it.mId }) { favorite ->
                        FavoriteItemRow(favorite)
                        HorizontalDivider(color = Colors[0xE0E0E0], thickness = 0.5.dp)
                    }
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { CText("Delete", fontSize = 18.dp2sp, fontWeight = FontWeight.Bold) },
                text = { CText("${selectedDriveIds.size + selectedFavoriteIds.size}개의 항목을 삭제하시겠습니까?", fontSize = 14.dp2sp) },
                confirmButton = {
                    TextButton(onClick = {
                        deleteSelectedItems()
                        showDeleteConfirm = false
                    }) { CText("확인", fontSize = 15.dp2sp, color = Colors[0x0000FF]) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { CText("취소", fontSize = 15.dp2sp, color = Colors.Red) }
                }
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DriveItemRow(item: FileData) {
        val isSelected = selectedDriveIds.contains(item.mId)
        val sizeText = getDriveSizeText(item.mPath, item.mType)

        Card(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .height(60.dp)
                .combinedClickable(
                    onClick = {
                        if (isCheckMode) {
                            if (item.mType == FileData.TYTP_NETWORK) {
                                if (isSelected) selectedDriveIds.remove(item.mId) else selectedDriveIds.add(item.mId)
                            }
                        } else {
                            (activity as? ActMain)?.newFileList(item)
                        }
                    },
                    onLongClick = {
                        setEditMode(true)
                        if (item.mType == FileData.TYTP_NETWORK) {
                            if (isSelected) selectedDriveIds.remove(item.mId) else selectedDriveIds.add(item.mId)
                        }
                    }
                ),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Colors.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    CText(text = item.mDisplayName ?: "", fontSize = 15.dp2sp, color = Colors.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    CText(text = sizeText, fontSize = 10.dp2sp, color = Colors.Gray400)
                }

                if (isCheckMode && item.mType == FileData.TYTP_NETWORK) {
                    CIcon(
                        painter = painterResource(id = R.drawable.check_btn),
                        contentDescription = "Check",
                        tint = if (isSelected) Colors.UnSet else Colors.Gray400,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun FavoriteItemRow(item: FileData) {
        val isSelected = selectedFavoriteIds.contains(item.mId)
        val musicCount = getMusicCount(item.mPath)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Colors.White)
                .combinedClickable(
                    onClick = {
                        if (isCheckMode) {
                            if (isSelected) selectedFavoriteIds.remove(item.mId) else selectedFavoriteIds.add(item.mId)
                        } else {
                            val file = File(item.mPath)
                            if (file.exists()) {
                                if (file.isDirectory) {
                                    (activity as? ActMain)?.newFileList(item)
                                } else {
                                    FileManager.startViewer(activity as ActBase, file, null)
                                }
                            } else {
                                showDeleteDialog(item)
                            }
                        }
                    },
                    onLongClick = {
                        setEditMode(true)
                        if (isSelected) selectedFavoriteIds.remove(item.mId) else selectedFavoriteIds.add(item.mId)
                    }
                )
                .padding(horizontal = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CIcon(painter = FileManager.getFileIconResource(item),
                tint = Colors.Gray600,
                modifier = Modifier.size(22.dp))

            Spacer(modifier = Modifier.width(10.dp))

            CText(
                text = item.mDisplayName ?: "",
                color = Colors[0x6E6E6E],
                fontSize = 16.dp2sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (musicCount > 0) {
                CText(
                    text = musicCount.toString(),
                    color = Colors[0x6E6E6E],
                    fontSize = 14.dp2sp,
                    modifier = Modifier.padding(end = 5.dp)
                )

                val isCurrentPlaying = (playingFolderPath == item.mPath) && isPlaying
                val playIcon = if (isCurrentPlaying) R.drawable.h_media_pause else R.drawable.h_media_play

                IconButton(
                    onClick = { playMusicFolder(item.mPath) },
                    modifier = Modifier.size(36.dp)
                ) {
                    CIcon(
                        painter = painterResource(id = playIcon),
                        contentDescription = "Play"
                    )
                }
            }

            if (isCheckMode) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clickableOnce {
                            if (isSelected) selectedFavoriteIds.remove(item.mId) else selectedFavoriteIds.add(item.mId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CIcon(
                        painter = painterResource(id = if (isSelected) R.drawable.check_on else R.drawable.check_off),
                        contentDescription = "Check",
                        tint = if (isSelected) Colors.UnSet else Colors.Gray400
                    )
                }
            }
        }
    }

    @Composable
    private fun getDriveSizeText(path: String, type: Int): String {
        if (type == FileData.TYTP_NETWORK) return "NETWORK"

        var sizeText by remember(path) { mutableStateOf("") }
        LaunchedEffect(path) {
            withContext(Dispatchers.IO) {
                try {
                    val stat = StatFs(path)
                    val totalSize = stat.blockCountLong.toFloat() * stat.blockSizeLong.toFloat() / 1024 / 1024 / 1024
                    val freeSize = stat.freeBlocksLong * stat.blockSizeLong.toFloat() / 1024 / 1024 / 1024
                    val usedSize = totalSize - freeSize
                    sizeText = String.format(Locale.getDefault(), "%.2fGB / %.2fGB", usedSize, totalSize)
                } catch (e: Exception) {
                    CLog.e(e)
                    sizeText = "Unknown"
                }
            }
        }
        return sizeText
    }

    @Composable
    private fun getMusicCount(path: String): Int {
        var count by remember(path) { mutableStateOf(0) }
        LaunchedEffect(path) {
            withContext(Dispatchers.IO) {
                val file = File(path)
                var c = 0
                if (file.isDirectory) {
                    val files = FileUtils.getDirFileList(path)
                    files?.forEach { f ->
                        val ext = FileUtils.getExtension(f.name)
                        if (ext.equals("mp3", true) || ext.equals("flac", true)) {
                            c++
                        }
                    }
                } else if (file.isFile) {
                    val ext = FileUtils.getExtension(file.name)
                    if (ext.equals("mp3", true) || ext.equals("flac", true)) {
                        c = 1
                    }
                }
                count = c
            }
        }
        return count
    }
}