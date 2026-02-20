package com.kds3393.just.justviewer2.activity

import android.content.*
import android.os.Bundle
import android.preference.*
import android.preference.Preference.OnPreferenceChangeListener
import common.lib.debug.CLog
import com.kds3393.just.justviewer2.config.SettingImageViewer
import com.kds3393.just.justviewer2.config.SettingTextViewer

class SettingActivity : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceScreen = createPreferenceHierarchy()
    }

    private fun createPreferenceHierarchy(): PreferenceScreen { // Root
        val root = preferenceManager.createPreferenceScreen(this)
        createAppSetting(root)
        SettingImageViewer.make(this, root)
        SettingTextViewer.make(this, root)
        createMoviePlayerSetting(root)
        createMusicPlayerSetting(root)
        createInfoSetting(root)
        return root
    }

    /**
     * @brief 앱 기본 설정
     */
    private fun createAppSetting(root: PreferenceScreen) {
        val justViewerPrefCat = PreferenceCategory(this)
        justViewerPrefCat.title = "Just Viewer"
        root.addPreference(justViewerPrefCat)
        createScreenTimeOutPref(justViewerPrefCat)
    }

    /**
     * @brief 동영상 뷰어 설정
     */
    private fun createMoviePlayerSetting(root: PreferenceScreen) {
        val MoviePlayerPrefCat = PreferenceCategory(this)
        MoviePlayerPrefCat.title = "동영상 뷰어"
        root.addPreference(MoviePlayerPrefCat)
        val listPref = ListPreference(this)
        val lists = arrayOfNulls<CharSequence>(2)
        lists[0] = "가로 모드"
        lists[1] = "세로 모드"
        listPref.entries = lists
        val listValues = arrayOfNulls<CharSequence>(2)
        listValues[0] = "0"
        listValues[1] = "1"
        listPref.entryValues = listValues
        listPref.dialogTitle = "동영상 화면 모드"
        listPref.title = "화면 방향 설정"
        MoviePlayerPrefCat.addPreference(listPref)
        listPref.key = PREF_MOVIE_ORIENTATION
        listPref.setDefaultValue("0")
        listPref.value = if (getMovieOrientation(this)) "0" else "1"
        if (listPref.value == null) listPref.value = "0"
        if (listPref.value.equals("0", ignoreCase = true)) listPref.summary = "가로 보기가 기본으로 설정되어 있습니다." else listPref.summary = "세로 보기가 기본으로 설정되어 있습니다."
        listPref.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            if ((newValue as String).equals("0", ignoreCase = true)) preference.summary = "가로 보기가 기본으로 설정되어 있습니다." else preference.summary = "세로 보기가 기본으로 설정되어 있습니다."
            true
        }
    }

    /**
     * @brief 뮤직 플레이 설정
     */
    private fun createMusicPlayerSetting(root: PreferenceScreen) {
        val musicPrefCat = PreferenceCategory(this)
        musicPrefCat.title = "음악"
        root.addPreference(musicPrefCat)

        // 음악 순서를 랜덤하게 배열 설정
        val nextScreenCheckBoxPref = CheckBoxPreference(this)
        nextScreenCheckBoxPref.key = PREF_MUSIC_LIST_ORDER
        nextScreenCheckBoxPref.title = "음악 순서 랜덤"
        nextScreenCheckBoxPref.summary = "음악 플레이 순서를 무작위로 선정한다."
        musicPrefCat.addPreference(nextScreenCheckBoxPref)
    }

    /**
     * @brief 기본 정보 설정
     */
    private fun createInfoSetting(root: PreferenceScreen) { // 기본 정보 설정
        val infoPrefCat = PreferenceCategory(this)
        infoPrefCat.title = "정보"
        root.addPreference(infoPrefCat)
        createHelpPref(infoPrefCat)
        createDevPref(infoPrefCat)
    }

    private fun createScreenTimeOutPref(category: PreferenceCategory) { // Checkbox preference
        val listPref = ListPreference(this)
        val lists = arrayOfNulls<CharSequence>(5)
        lists[0] = "1 분"
        lists[1] = "2 분"
        lists[2] = "10 분"
        lists[3] = "자동 꺼짐 사용 안함"
        lists[4] = "기존 설정 유지"
        listPref.entries = lists
        val listValues = arrayOfNulls<CharSequence>(5)
        listValues[0] = "60000"
        listValues[1] = "120000"
        listValues[2] = "600000"
        listValues[3] = "36000000"
        listValues[4] = "-1"
        listPref.entryValues = listValues
        listPref.dialogTitle = "화면 자동 꺼짐"
        listPref.title = "화면 자동 꺼짐"
        category.addPreference(listPref)
        listPref.key = PREF_APP_SCREEN_TIMEOUT
        if (listPref.value == null) listPref.value = "120000"
        listPref.summary = getScreenTimeOutSummary(listPref.value.toInt())
        listPref.onPreferenceChangeListener = OnPreferenceChangeListener { preference, newValue ->
            preference.summary = getScreenTimeOutSummary(newValue.toString().toInt())
            CLog.e(TAG, "KDS3393_newValue = $newValue")
            true
        }
    }

    private fun getScreenTimeOutSummary(value: Int): String {
        var str = ""
        if (value == 60000) {
            str = "1분 동안 사용하지 않을 때\n"
        } else if (value == 120000) {
            str = "2분 동안 사용하지 않을 때\n"
        } else if (value == 600000) {
            str = "10분 동안 사용하지 않을 때\n"
        } else if (value == 36000000) {
            str = "화면 자동 꺼짐 사용 암함\n"
        } else if (value == -2) {
            return "기존 설정 그대로 사용"
        }
        return "$str(앱을 종료하면 기존에 설정된 상태로 돌아 감)"
    }

    private fun createHelpPref(category: PreferenceCategory) { //        PreferenceScreen helpPref = getPreferenceManager().createPreferenceScreen(this);
        //        helpPref.setIntent(new Intent(this, HelpActivity.class));
        //        helpPref.setKey("help_preference");
        //        helpPref.setTitle("Help");
        //        helpPref.setSummary("사용법을 이미지로 보여드립니다.");
        //        category.addPreference(helpPref);
    }

    private fun createDevPref(category: PreferenceCategory) {
        val InfoPref = preferenceManager.createPreferenceScreen(this)
        InfoPref.key = "app_info_preference"
        InfoPref.title = "앱 정보"
        InfoPref.summary = ""
        category.addPreference(InfoPref)
        val devInfoPref = preferenceManager.createPreferenceScreen(this)
        devInfoPref.isEnabled = false
        devInfoPref.title = "개발자 정보"
        devInfoPref.summary = "개발자 : 강대성\n디자이너 : 박유금\n"
        InfoPref.addPreference(devInfoPref)
        val licensePref = preferenceManager.createPreferenceScreen(this)
        licensePref.isEnabled = false
        licensePref.title = "사용된 라이센스 정보"
        val builder = StringBuilder()
        builder.append("commons compress 1.5\n")
        builder.append("Apache (http://www.opensource.org/licenses/apache2.0.php)\n\n")
        builder.append("Icon Image\n")
        builder.append("Double-J Design (http://www.doublejdesign.co.uk/)\n")
        builder.append("FatCow ( http://www.fatcow.com/ )\n")
        licensePref.summary = builder.toString()
        InfoPref.addPreference(licensePref)
    }

    companion object {
        private const val TAG = "SettingActivity"
        const val PREF_APP_SCREEN_TIMEOUT = "pref_screen_timeout"
        const val PREF_MOVIE_ORIENTATION = "pref_movie_orientation"
        const val PREF_MUSIC_LIST_ORDER = "pref_music_list_order"
        fun getScreenTimeOut(_ctx: Context?): Int {
            val pref = PreferenceManager.getDefaultSharedPreferences(_ctx)
            return pref.getString(PREF_APP_SCREEN_TIMEOUT, "120000")!!.toInt()
        }

        fun setMovieOrientation(_ctx: Context?, isLandscape: Boolean) {
            val pref = PreferenceManager.getDefaultSharedPreferences(_ctx)
            val edit = pref.edit()
            if (isLandscape) edit.putString(PREF_MOVIE_ORIENTATION, "0") else edit.putString(PREF_MOVIE_ORIENTATION, "1")
            edit.commit()
        }

        fun getMovieOrientation(_ctx: Context?): Boolean {
            val pref = PreferenceManager.getDefaultSharedPreferences(_ctx)
            val str = pref.getString(PREF_MOVIE_ORIENTATION, "0")
            if (str == null) return true else if (str.equals("1", ignoreCase = true)) return false
            return true
        }

        fun setMusicListShuffle(_ctx: Context?, isShuffle: Boolean) {
            val pref = PreferenceManager.getDefaultSharedPreferences(_ctx)
            val edit = pref.edit()
            edit.putBoolean(PREF_MUSIC_LIST_ORDER, isShuffle)
            edit.commit()
        }

        @JvmStatic
        fun getMusicListShuffle(_ctx: Context?): Boolean {
            val pref = PreferenceManager.getDefaultSharedPreferences(_ctx)
            return pref.getBoolean(PREF_MUSIC_LIST_ORDER, false)
        }
    }
}