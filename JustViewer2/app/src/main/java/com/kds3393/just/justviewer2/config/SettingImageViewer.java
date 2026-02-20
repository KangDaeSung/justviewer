package com.kds3393.just.justviewer2.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

/**
 * @brief 이미지 뷰어 설정
 */
public class SettingImageViewer extends PreferenceCategory {
	
	public static SettingImageViewer make(Context context, PreferenceScreen root) {
		SettingImageViewer pref = new SettingImageViewer(context,root);
		return pref;
	}
    
	public static final String PREF_IMAGE_BOOK_DIRECTION 			= "pref_book_direction";
	public static final String PREF_IMAGE_USE_MOVE_BUTTON 			= "pref_image_use_move_button";
	
	public SettingImageViewer(Context context, PreferenceScreen root) {
		super(context);
        this.setTitle("이미지 책 뷰어");
        root.addPreference(this);
        
        createBookDirectionPref(this);
        createUseImageMoveButtonPref(this);
	}

    private void createBookDirectionPref(PreferenceCategory category) {
        ListPreference listPref = new ListPreference(getContext());
        
        CharSequence[] lists = new CharSequence[2];
        lists[0] = "오른쪽으로 넘김";
        lists[1] = "왼쪽으로 넘김";
        listPref.setEntries(lists);
        CharSequence[] listValues = new CharSequence[2];
        listValues[0] = "1";
        listValues[1] = "0";
        listPref.setEntryValues(listValues);
        listPref.setDialogTitle("책 방향 선택");
        listPref.setTitle("책을 넘기는 방향");
        category.addPreference(listPref);
        listPref.setKey(PREF_IMAGE_BOOK_DIRECTION);
        listPref.setValue(getIsPageRight(getContext())?"1":"0");
        if (listPref.getValue() == null) 
        	listPref.setValue("1");
        if (listPref.getValue().equalsIgnoreCase("1"))
        	listPref.setSummary("오른쪽으로 책을 넘기도록 설정되어 있습니다.");
        else
        	listPref.setSummary("왼쪽으로 책을 넘기도록 설정되어 있습니다.");
        listPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
		        if (((String) newValue).equalsIgnoreCase("1"))
		        	preference.setSummary("오른쪽으로 책을 넘기도록 설정되어 있습니다.");
		        else
		        	preference.setSummary("왼쪽으로 책을 넘기도록 설정되어 있습니다.");
				return true;
			}
		});
    }
	
    private void createUseImageMoveButtonPref(PreferenceCategory category) {
    	//이동 버튼 사용 여부 설정
        CheckBoxPreference moveBtnPref = new CheckBoxPreference(getContext());
        moveBtnPref.setKey(PREF_IMAGE_USE_MOVE_BUTTON);
        moveBtnPref.setDefaultValue(true);
        moveBtnPref.setTitle("이동 버튼 사용");
        moveBtnPref.setSummary("이미지 뷰어에서 위치 조절이 가능한 보이지 않는 버튼을 사용하여 페이지 이동");
        category.addPreference(moveBtnPref);
    }
    
	public static void setIsPageRight(Context _ctx, boolean isRight) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		if (isRight)
			edit.putString(PREF_IMAGE_BOOK_DIRECTION, "1");
		else
			edit.putString(PREF_IMAGE_BOOK_DIRECTION, "0");
		edit.commit();
	}
	
	public static boolean getIsPageRight(Context _ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		String str = pref.getString(PREF_IMAGE_BOOK_DIRECTION, "1");
		if (str == null)
			return true;
		else if (str.equalsIgnoreCase("0"))
			return false;
		return true;
	}
	
	public static boolean getUsePageMoveBtn(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return pref.getBoolean(PREF_IMAGE_USE_MOVE_BUTTON, true);
	}
}
