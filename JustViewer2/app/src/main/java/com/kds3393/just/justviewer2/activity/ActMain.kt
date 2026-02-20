package com.kds3393.just.justviewer2.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import com.kds3393.just.justviewer2.R
import com.kds3393.just.justviewer2.data.FileData
import com.kds3393.just.justviewer2.databinding.ActMainBinding
import com.kds3393.just.justviewer2.fragment.FrmBase
import com.kds3393.just.justviewer2.fragment.FrmLocalJC
import com.kds3393.just.justviewer2.fragment.FrmMainJC
import com.kds3393.just.justviewer2.music.player.MusicService
import com.kds3393.just.justviewer2.music.player.MusicService.LocalBinder
import com.kds3393.just.justviewer2.utils.CToast
import common.lib.adapter.ViewPagerAdapter
import common.lib.app.APP_PACK_NAME
import common.lib.base.isPermission
import common.lib.utils.PermissionChecker

class ActMain : ActBase() {
    lateinit var binding: ActMainBinding
    private var mViewPagerAdapter: ViewPagerAdapter? = null

    private val permissionManager = PermissionChecker(this)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainMusicPanel.setLyricsView(binding.mainMusicLyrics)
        binding.mainMusicPanel.setMusicList(binding.mainMusicList)

        permissionManager.addPermissions(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE)

        permissionManager.setDenyListener {
            finish()
        }
        permissionManager.setGrantListener {
            permissionProgress()
        }
        permissionManager.check()
    }

    fun permissionProgress(checkNoti:Boolean = true) {
        if (checkNoti && Build.VERSION.SDK_INT >= 33 && !isPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            permissionManager.clearPermission()
            permissionManager.addPermissions(Manifest.permission.POST_NOTIFICATIONS)
            permissionManager.setGrantListener {
                permissionProgress()
            }
            permissionManager.check()
        } else if (!Environment.isExternalStorageManager()) {
            val uri = "package:$APP_PACK_NAME".toUri()
            runActResult(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)) { _, _, _ ->
                if (Environment.isExternalStorageManager()) {
                    initUI()
                } else {
                    CToast.normal("모든 파일에 대한 접근 권한이 없습니다.")
                }
            }
        } else {
            initUI()
        }
    }
    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, mConnection, BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        binding.mainMusicPanel.onResume()
    }

    override fun onStop() {
        binding.mainMusicPanel.clear()
        unbindService(mConnection)
        super.onStop()
    }


    override fun onFinish(): Boolean {
        if (mSubFrmLocal != null && mSubFrmLocal!!.isFragmentUIActive) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            fragmentTransaction.hide(mSubFrmLocal!!).commitAllowingStateLoss()
        } else if (binding.mainMusicPanel.isShow) {
            binding.mainMusicPanel.setSwitchMusicPanel(false)
        } else {
            val page = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.actMainViewpager + ":" + binding.actMainViewpager.currentItem)
            if (page != null) {
                if (!(page as FrmBase).onBackPressed()) {
                    return false
                }
            }
            if (binding.actMainViewpager.currentItem != 0) {
                binding.actMainViewpager.currentItem = 0
            } else {
                return true
            }
        }
        return false
    }

    fun initUI() {
        mViewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        mFrmMain = FrmMainJC()
        mViewPagerAdapter!!.addFragment(mFrmMain, "MAIN")
        if (mMusicService != null) {
            mFrmMain!!.setMusicService(mMusicService)
        }
        binding.actMainViewpager.offscreenPageLimit = 2
        binding.actMainViewpager.adapter = mViewPagerAdapter
        mViewPagerAdapter!!.notifyDataSetChanged()
    }

    private var mFrmMain: FrmMainJC? = null
    private var mFrmLocal: FrmLocalJC? = null
    fun newFileList(data: FileData) {
        if (data.mType == FileData.TYPE_LOCAL_DIR) {
            if (mFrmLocal == null) {
                mFrmLocal = FrmLocalJC().apply {
                    type = FrmLocalJC.TYPE_LOCAL_EXPLORER
                    rootPath = data.mPath
                }
                mViewPagerAdapter!!.addFragment(mFrmLocal, data.mDisplayName)
                mViewPagerAdapter!!.notifyDataSetChanged()
            } else {
                mFrmLocal!!.rootPath = data.mPath
                mFrmLocal!!.setFileList(data.mPath)
                mViewPagerAdapter!!.setPageTitle(mViewPagerAdapter!!.getFragmentPosition(mFrmLocal), data.mDisplayName)
            }
            binding.actMainViewpager.currentItem = mViewPagerAdapter!!.getFragmentPosition(mFrmLocal)
        } else if (data.mType == FileData.TYTP_NETWORK) {
            //TODO 나중에 보자
        }
    }

    var mSubFrmLocal: FrmLocalJC? = null
    override fun onFunction(action: String, vararg obj: Any?) {
        if (CMD_RELOAD_MAIN == action) {
            mFrmMain!!.onResume()
        }
    }

    // ----------------------------------- Music Service -------------------------------------
    private var mMusicService: MusicService? = null
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as LocalBinder
            binding.mainMusicPanel.setMusicService(binder.service)
            mMusicService = binder.service
            mFrmMain?.setMusicService(mMusicService)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {}
    }

    companion object {
        private const val TAG = "ActMain"
        const val EXTRA_BROWSER_PATH = "EXTRA_BROWSER_PATH"
        const val EXTRA_BROWSER_PATH_ARRAY = "EXTRA_BROWSER_PATH_ARRAY"
        const val CMD_RELOAD_MAIN = "CMD_RELOAD_MAIN"
    }
}