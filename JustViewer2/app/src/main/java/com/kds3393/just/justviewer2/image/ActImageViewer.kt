package com.kds3393.just.justviewer2.image

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kds3393.just.justviewer2.config.SettingImageViewer
import com.kds3393.just.justviewer2.config.SharedPrefHelper
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.activity.ActBase
import com.kds3393.just.justviewer2.activity.ActMain
import com.kds3393.just.justviewer2.compose.CText
import com.kds3393.just.justviewer2.data.BookInfo
import com.kds3393.just.justviewer2.databinding.ActImageviewerBinding
import com.kds3393.just.justviewer2.db.DBMgr
import com.kds3393.just.justviewer2.dialog.BaseDialog
import com.kds3393.just.justviewer2.dialog.DlgAlert
import com.kds3393.just.justviewer2.image.ImageViewer.OnPageSelectedListener
import com.kds3393.just.justviewer2.music.player.MusicService
import com.kds3393.just.justviewer2.music.player.MusicService.LocalBinder
import com.kds3393.just.justviewer2.views.PageControlView.OnPageControlListener
import com.kds3393.just.justviewer2.compose.dp2sp
import common.lib.debug.CLog
import common.lib.utils.FileUtils
import common.lib.utils.Size
import common.lib.base.getFileName
import common.lib.base.gone
import common.lib.base.isShow
import common.lib.base.show
import java.io.File

class ActImageViewer : ActBase() {
    lateinit var binding : ActImageviewerBinding
    lateinit var bookInfo: BookInfo

    private var sliderValue by mutableFloatStateOf(1f)
    private var sliderMax by mutableFloatStateOf(0f)
    private var isShowSlider by mutableStateOf(true)    //Slider를 보여줄지 여부
    private var isShowNavi by mutableStateOf(true)      //Navi가 보여지고 있는지 여부

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Size.InitScreenSize(this)
        val targetPath = intent.getStringExtra(ActMain.EXTRA_BROWSER_PATH)
        val allPaths = intent.getStringArrayListExtra(ActMain.EXTRA_BROWSER_PATH_ARRAY)

        val newBook = loadBook(targetPath,allPaths)
        if (newBook != null) {
            bookInfo = newBook
        } else {
            finish()
            return
        }
        binding = ActImageviewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageComposeView.setContent {
            var isTextVisible by remember { mutableStateOf(false) }
            if (sliderMax > 0) {
                val naviPadding = if (isShowNavi) {
                    64.dp
                } else {
                    20.dp
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = naviPadding)) {
                    if (isTextVisible) {
                        CText(text = "${sliderValue.toInt()} / ${sliderMax.toInt()}", fontSize = 20.dp2sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 20.dp))
                    }
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            isTextVisible = true
                        },
                        onValueChangeFinished = {
                            binding.imageviewer.gotoPage(sliderValue.toInt() - 1)
                            setHideNaviBar()
                            isTextVisible = false
                        },
                        valueRange = 1f..sliderMax,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Blue,
                            activeTrackColor = Color.Blue,
                            inactiveTrackColor = Color.LightGray,
                        )
                    )
                }
            }
        }

        createMainView()
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }

    override fun onBackPressed() {
        if (binding.layoutMoveEdit.isMoveButtonEditMode) {
            binding.layoutMoveEdit.cancelEditMode()
            isShowSlider = true
        } else if (binding.imageMusicPanel.isShow) {
            binding.imageMusicPanel.setSwitchMusicPanel(false)
        } else if (binding.layoutNavi.isShow()) {
            setHideNaviBar()
        } else {
            super.onBackPressed()
        }
    }

    private fun createMainView() {
        createViewer()
        createMoveButton()
        createNavi()
    }

    override fun onPause() {
        save()
        super.onPause()
    }

    override fun onStop() {
        binding.imageMusicPanel.clear()
        unbindService(mConnection)
        super.onStop()
    }

    private fun loadBook(targetPath:String?,allPaths:ArrayList<String>?) : BookInfo? {
        if (targetPath == null) {
            return null
        }
        var book = DBMgr.instance.imageDataLoad(targetPath)
        if (book == null) {
            book = BookInfo(targetPath)
        }
        allPaths?.let { book.books = allPaths }
        return book
    }

    private fun save() {
        val index = if (SettingImageViewer.getIsPageRight(this)) {
            binding.imageviewer.getPageIndex()
        } else {
            binding.imageviewer.pageCount - 1 - binding.imageviewer.getPageIndex()
        }
        binding.imageviewer.bookInfo.currentPage = index
        DBMgr.instance.updateImageData(binding.imageviewer.bookInfo)
        CLog.e("KDS3393_TEST_save data bookInfo = ${binding.imageviewer.bookInfo}")
    }

    private fun createViewer() {
        binding.imageviewer.setOnPageSelectedListener(object : OnPageSelectedListener {
            override fun onPageSelected(CurrentPage: PageView?, index: Int) {
                setPageText(index)
                sliderValue = index + 1f
            }

            override fun onBookSelected(path: String) {
                binding.txtNaviTitle.text = FileUtils.getFileName(path)
                if (!binding.layoutNavi.isShow()) {
                    binding.txtNaviTitle.show(true)
                    binding.txtNaviTitle.postDelayed({
                        if (!binding.layoutNavi.isShow()) {
                            binding.txtNaviTitle.gone(true)
                        }
                    },1500)
                }
            }

            override fun onSingleTab() {
                setShowHideNaviBar()
            }

            override fun onLoaded(pageSize: Int) {
                sliderMax = pageSize.toFloat()
            }
        })
        binding.imageviewer.setup(bookInfo)
    }

    private fun setShowHideNaviBar() {
        if (binding.layoutMoveEdit.isMoveButtonEditMode) return
        if (!binding.layoutNavi.isShow()) {
            binding.layoutNavi.show(true)
            binding.layoutMoveEdit.setMode(com.kds3393.just.justviewer2.views.MoveScaleButton.MODE_SHOW)
            binding.txtNaviTitle.show(true)
            isShowSlider = true
            isShowNavi = true
        } else {
            setHideNaviBar()
        }
    }

    private fun setHideNaviBar() {
        binding.layoutNavi.gone(true)
        binding.layoutMoveEdit.setMode(com.kds3393.just.justviewer2.views.MoveScaleButton.MODE_FUNCTION)
        binding.txtNaviTitle.gone(true)
        isShowSlider = true
        isShowNavi = false
    }

    private fun createNavi() {
        createNaviBottomMenu()
    }

    private fun createMoveButton() {
        binding.layoutMoveEdit.setViewer(binding.imageviewer)
        binding.layoutMoveEdit.setOnPageControlListener(object : OnPageControlListener {
            override fun onPrev() {
                moveLeft()
            }

            override fun onNext() {
                moveRight()
            }
        })
    }

    private fun setPageText(pageIndex: Int) {
        var index = pageIndex
        if (!SettingImageViewer.getIsPageRight(this)) {
            index = binding.imageviewer.pageCount - 1 - index
        }
        val str = (index + 1).toString() + "/" + binding.imageviewer.pageCount
        binding.txtNaviPage.text = str
    }

    private fun moveLeft() {
        binding.imageviewer.moveLeft()
    }

    private fun moveRight() {
        binding.imageviewer.moveRight()
    }

    private fun createNaviBottomMenu() {
        if (SharedPrefHelper.getImagePageType(this@ActImageViewer)) {
            binding.pageDirection.setBackgroundResource(R.drawable.h_book_driection_right)
        } else {
            binding.pageDirection.setBackgroundResource(R.drawable.h_book_driection_left)
        }
        binding.pageDirection.setOnClickListener { view ->
            val isRight = !SharedPrefHelper.getImagePageType(this@ActImageViewer)
            if (isRight) {
                view.setBackgroundResource(R.drawable.h_book_driection_right)
                Toast.makeText(this@ActImageViewer, "오른쪽으로 넘기도록 설정되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                view.setBackgroundResource(R.drawable.h_book_driection_left)
                Toast.makeText(this@ActImageViewer, "왼쪽으로 넘기도록 설정되었습니다.", Toast.LENGTH_SHORT).show()
            }
            binding.imageviewer.setDirection(isRight)
        }
        if (SettingImageViewer.getUsePageMoveBtn(this)) {
            binding.pageEditmode.setOnClickListener {
                binding.layoutMoveEdit.isMoveButtonEditMode = true
                binding.layoutNavi.gone(true)
                isShowSlider = true
                isShowNavi = false
            }
        } else {
            binding.pageEditmode.gone(true)
        }
        binding.removeFile.setOnClickListener {
            val dialog = DlgAlert(this, "Delete", "${bookInfo.targetPath.getFileName()}을 삭제하시겠습니까?", object : BaseDialog.OnDialogListener {
                override fun onOk() {
                    FileUtils.deleteFile(File(bookInfo.targetPath))
                    Toast.makeText(this@ActImageViewer, "${bookInfo.targetPath.getFileName()}가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    if (bookInfo.removeCurrentBook()) {
                        binding.imageviewer.setup(bookInfo)
                    } else {
                        finish()
                    }
                }

                override fun onCancel() {}
            })
            dialog.show()
        }

        binding.prevBook.setOnClickListener {
            binding.imageviewer.moveBook(false)
        }

        binding.nextBook.setOnClickListener {
            binding.imageviewer.moveBook(true)
        }
    }

    // ----------------------------------- Music Service -------------------------------------
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) { // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalBinder
            binding.imageMusicPanel.setMusicService(binder.service)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }
}