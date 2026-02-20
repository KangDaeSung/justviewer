package com.kds3393.just.viewer.Config;

import java.util.ArrayList;

import com.kds3393.just.dialog.Mp3SleepTimerBuilder;
import com.kds3393.just.dialog.TextSettingBuilder;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Music.MusicService;
import com.common.utils.debug.CLog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.LinearLayout;

public class SettingActivity extends PreferenceActivity {
	private static final String TAG = "SettingActivity";
	public static final String PREF_APP_SCREEN_TIMEOUT 				= "pref_screen_timeout";
	
	public static final String PREF_USE_VOLUME_MOVE_BUTTON 			= "pref_use_volume_button";

	
	public static final String PREF_MOVIE_ORIENTATION 				= "pref_movie_orientation";
	
	public static final String PREF_MUSIC_LIST_ORDER 				= "pref_music_list_order";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setPreferenceScreen(createPreferenceHierarchy());
    }

    private PreferenceScreen createPreferenceHierarchy() {
        // Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
	        createAppSetting(root);
	        SettingImageViewer.make(this, root);
	        SettingTextViewer.make(this, root);
	        createMoviePlayerSetting(root);
	        createMusicPlayerSetting(root);
	        
	        createInfoSetting(root);
        return root;
    }
    
	/**
	 * @brief 앱 기본 설정
	 */
    private void createAppSetting(PreferenceScreen root) {
        PreferenceCategory justViewerPrefCat = new PreferenceCategory(this);
        justViewerPrefCat.setTitle("Just Viewer");
        root.addPreference(justViewerPrefCat);
        
        	createScreenTimeOutPref(justViewerPrefCat);
        	
	        //볼륨버튼으로 이동 사용 설정
	        CheckBoxPreference useVolumeBtnPref = new CheckBoxPreference(this);
	        useVolumeBtnPref.setKey(PREF_USE_VOLUME_MOVE_BUTTON);
	        useVolumeBtnPref.setDefaultValue(true);
	        useVolumeBtnPref.setTitle("볼륨 버튼으로 이동");
	        useVolumeBtnPref.setSummary("이미지 뷰어와 텍스트 뷰어에서 볼륨 버튼을 사용하여 페이지를 이동");
	        justViewerPrefCat.addPreference(useVolumeBtnPref);
    }
    

    
    /**
	 * @brief 동영상 뷰어 설정
	 */
    private void createMoviePlayerSetting(PreferenceScreen root) {
        PreferenceCategory MoviePlayerPrefCat = new PreferenceCategory(this);
        MoviePlayerPrefCat.setTitle("동영상 뷰어");
        root.addPreference(MoviePlayerPrefCat);
        
	        ListPreference listPref = new ListPreference(this);
	        
	        CharSequence[] lists = new CharSequence[2];
	        lists[0] = "가로 모드";
	        lists[1] = "세로 모드";
	        listPref.setEntries(lists);
	        CharSequence[] listValues = new CharSequence[2];
	        listValues[0] = "0";
	        listValues[1] = "1";
	        listPref.setEntryValues(listValues);
	        listPref.setDialogTitle("동영상 화면 모드");
	        listPref.setTitle("화면 방향 설정");
	        MoviePlayerPrefCat.addPreference(listPref);
	        listPref.setKey(PREF_MOVIE_ORIENTATION);
	        listPref.setDefaultValue("0");
	        listPref.setValue(getMovieOrientation(this)?"0":"1");
	        if (listPref.getValue() == null) 
	        	listPref.setValue("0");
	        
	        if (listPref.getValue().equalsIgnoreCase("0"))
	        	listPref.setSummary("가로 보기가 기본으로 설정되어 있습니다.");
	        else
	        	listPref.setSummary("세로 보기가 기본으로 설정되어 있습니다.");
	        listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
			        if (((String) newValue).equalsIgnoreCase("0"))
			        	preference.setSummary("가로 보기가 기본으로 설정되어 있습니다.");
			        else
			        	preference.setSummary("세로 보기가 기본으로 설정되어 있습니다.");
					return true;
				}
			});
    }
    
	/**
	 * @brief 뮤직 플레이 설정
	 */
    private void createMusicPlayerSetting(PreferenceScreen root) {
        PreferenceCategory musicPrefCat = new PreferenceCategory(this);
        musicPrefCat.setTitle("음악");
        root.addPreference(musicPrefCat);

	        // 음악 순서를 랜덤하게 배열 설정
	        CheckBoxPreference nextScreenCheckBoxPref = new CheckBoxPreference(this);
	        nextScreenCheckBoxPref.setKey(PREF_MUSIC_LIST_ORDER);
	        nextScreenCheckBoxPref.setTitle("음악 순서 랜덤");
	        nextScreenCheckBoxPref.setSummary("음악 플레이 순서를 무작위로 선정한다.");
	        musicPrefCat.addPreference(nextScreenCheckBoxPref);
    }

    /**
	 * @brief 기본 정보 설정
	 */
    private void createInfoSetting(PreferenceScreen root) {
        // 기본 정보 설정
        PreferenceCategory infoPrefCat = new PreferenceCategory(this);
        infoPrefCat.setTitle("정보");
        root.addPreference(infoPrefCat);
        
        	createHelpPref(infoPrefCat);
        	createDevPref(infoPrefCat);
    }
    	
    

    
    private void createScreenTimeOutPref(PreferenceCategory category) {
        // Checkbox preference
        ListPreference listPref = new ListPreference(this);
        
        CharSequence[] lists = new CharSequence[5];
        lists[0] = "1 분";
        lists[1] = "2 분";
        lists[2] = "10 분";
        lists[3] = "자동 꺼짐 사용 안함";
        lists[4] = "기존 설정 유지";
        listPref.setEntries(lists);
        CharSequence[] listValues = new CharSequence[5];
        listValues[0] = "60000";
        listValues[1] = "120000";
        listValues[2] = "600000";
        listValues[3] = "36000000";
        listValues[4] = "-1";
        listPref.setEntryValues(listValues);
        listPref.setDialogTitle("화면 자동 꺼짐");
        listPref.setTitle("화면 자동 꺼짐");
        category.addPreference(listPref);
        listPref.setKey(PREF_APP_SCREEN_TIMEOUT);
        
        if (listPref.getValue() == null) 
        	listPref.setValue("120000");
        
        listPref.setSummary(getScreenTimeOutSummary(Integer.parseInt(listPref.getValue())));
        
        listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				preference.setSummary(getScreenTimeOutSummary(Integer.parseInt((String) newValue)));
				CLog.e(TAG, "KDS3393_newValue = " + newValue);
				return true;
			}
		});
    }
    
    private String getScreenTimeOutSummary(int value) {
    	String str = "";
        if (value == 60000) {
        	str = "1분 동안 사용하지 않을 때\n";
        } else if (value == 120000) {
        	str = "2분 동안 사용하지 않을 때\n";
        } else if (value == 600000) {
        	str = "10분 동안 사용하지 않을 때\n";
        } else if (value == 36000000) {
        	str = "화면 자동 꺼짐 사용 암함\n";
        } else if (value == -2) {
        	return  "기존 설정 그대로 사용";
        }
    	return str + "(앱을 종료하면 기존에 설정된 상태로 돌아 감)";
    }
    
    private void createHelpPref(PreferenceCategory category) {
        PreferenceScreen helpPref = getPreferenceManager().createPreferenceScreen(this);
        helpPref.setIntent(new Intent(this, HelpActivity.class));
        helpPref.setKey("help_preference");
        helpPref.setTitle("Help");
        helpPref.setSummary("사용법을 이미지로 보여드립니다.");
        category.addPreference(helpPref);
    }
    
    private void createDevPref(PreferenceCategory category) {
        PreferenceScreen InfoPref = getPreferenceManager().createPreferenceScreen(this);
        InfoPref.setKey("app_info_preference");
        InfoPref.setTitle("앱 정보");
        InfoPref.setSummary("");
        category.addPreference(InfoPref);
        
	        PreferenceScreen devInfoPref = getPreferenceManager().createPreferenceScreen(this);
	        devInfoPref.setEnabled(false);
	        devInfoPref.setTitle("개발자 정보");
	        devInfoPref.setSummary("개발자 : 강대성\n디자이너 : 박유금\n");
	        InfoPref.addPreference(devInfoPref);
	        
	        PreferenceScreen licensePref = getPreferenceManager().createPreferenceScreen(this);
	        licensePref.setEnabled(false);
	        licensePref.setTitle("사용된 라이센스 정보");
	        StringBuilder builder = new StringBuilder();
	        builder.append("commons compress 1.5\n");
	        builder.append("Apache (http://www.opensource.org/licenses/apache2.0.php)\n\n");
	        
	        builder.append("Icon Image\n");
	        builder.append("Double-J Design (http://www.doublejdesign.co.uk/)\n");
	        builder.append("FatCow ( http://www.fatcow.com/ )\n");
	        licensePref.setSummary(builder.toString());
	        InfoPref.addPreference(licensePref);
    }
    
	public static int getScreenTimeOut(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return Integer.parseInt(pref.getString(PREF_APP_SCREEN_TIMEOUT, "120000"));
	}
	
	public static boolean getUseVolumeMoveBtn(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return pref.getBoolean(PREF_USE_VOLUME_MOVE_BUTTON, true);
	}
	
	public static void setMovieOrientation(Context _ctx, boolean isLandscape) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		if (isLandscape)
			edit.putString(PREF_MOVIE_ORIENTATION, "0");
		else
			edit.putString(PREF_MOVIE_ORIENTATION, "1");
		edit.commit();
	}
	
	public static boolean getMovieOrientation(Context _ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		String str = pref.getString(PREF_MOVIE_ORIENTATION, "0");
		if (str == null)
			return true;
		else if (str.equalsIgnoreCase("1"))
			return false;
		return true;
	}
	
	public static void setMusicListShuffle(Context _ctx, boolean isShuffle){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		edit.putBoolean(PREF_MUSIC_LIST_ORDER, isShuffle);
		edit.commit();
	}
	
	public static boolean getMusicListShuffle(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return pref.getBoolean(PREF_MUSIC_LIST_ORDER, false);
	}
}