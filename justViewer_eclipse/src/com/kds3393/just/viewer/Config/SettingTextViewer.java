package com.kds3393.just.viewer.Config;

import java.util.ArrayList;

import com.kds3393.just.dialog.TextSettingBuilder;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class SettingTextViewer extends PreferenceCategory {
	
	public static SettingTextViewer make(Context context, PreferenceScreen root) {
		SettingTextViewer pref = new SettingTextViewer(context,root);
		
		return pref;
	}
	
	public static final String PREF_TEXT_SLIDING_PAGING 			= "pref_text_sliding_paging";
	public static final String PREF_TEXT_COLOR 						= "pref_text_color";
	public static final String PREF_TEXT_SIZE 						= "pref_text_size";
	public static final String PREF_TEXT_GAP 						= "pref_text_gap";
	public static final String PREF_TEXT_FONT 						= "pref_text_font";
	
	public static final String PREF_TEXT_SCROLL_SPEED 				= "pref_text_speed";
	public static final int SLOW_SCROLL_SPEED = 1500;
	public static final int NORMAL_SCROLL_SPEED = 900;
	public static final int FAST_SCROLL_SPEED = 250;
	
	public static final String PREF_TEXT_PAGE_DIRECTION 			= "pref_page_direction";
	public static final int DIRECTION_VERTICAL = 0;
	public static final int DIRECTION_HORIZONTAL = 1;
	
	public static final String PREF_TEXT_USE_MOVE_BUTTON 			= "pref_text_use_move_button";
	
    private TextSettingBuilder mTextSettingBuilder;
    
	public SettingTextViewer(Context context,PreferenceScreen root) {
		super(context);
        this.setTitle("텍스트 책 뷰어");
        root.addPreference(this);
        
        	createTextSettingPref(this);
        	createAutoScrollSpeedPref(this);
        	createPagingType(this);
        	createUseTextMoveButtonPref(this);
	}

	private void createTextSettingPref(PreferenceCategory category) {
		// 텍스트의 설정
        PreferenceScreen textPref = ((PreferenceActivity) getContext()).getPreferenceManager().createPreferenceScreen(getContext());
        textPref.setTitle("텍스트 설정");
        textPref.setSummary("텍스트의 font, color, 크기, 줄 간격 등을 설정할 수 있습니다.");
        category.addPreference(textPref);
        textPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				mTextSettingBuilder = new TextSettingBuilder(getContext());
				mTextSettingBuilder.getBuilder().setPositiveButton(android.R.string.ok, new Dialog.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int arg1) {
						dialog.dismiss();
					}
				});
				mTextSettingBuilder.makeView(getContext());
				if (!mTextSettingBuilder.getDialog().isShowing()) {
					mTextSettingBuilder.getDialog().show();
		    	}
				return false;
			}
		});
	}
	
	private void createAutoScrollSpeedPref(PreferenceCategory category) {
        ListPreference pref = new ListPreference(getContext());
        
        CharSequence[] lists = new CharSequence[3];
        lists[0] = "느림";
        lists[1] = "보통";
        lists[2] = "빠름";
        pref.setEntries(lists);
        CharSequence[] listValues = new CharSequence[3];
        listValues[0] = "0";
        listValues[1] = "1";
        listValues[2] = "2";
        pref.setEntryValues(listValues);
        pref.setDialogTitle("속도 설정");
        pref.setTitle("자동 스크롤 속도 설정");
        category.addPreference(pref);
        pref.setKey(PREF_TEXT_SCROLL_SPEED);
        pref.setDefaultValue("1");
        pref.setValue(String.valueOf(getScrollSpeed(getContext())));
        if (pref.getValue() == null) 
        	pref.setValue("1");
        
        if (pref.getValue().equalsIgnoreCase("0"))
        	pref.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 느린 속도로 넘어갑니다.");
        else if (pref.getValue().equalsIgnoreCase("1"))
        	pref.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 보통 속도로 넘어갑니다.");
        else
        	pref.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 빠른 속도로 넘어갑니다.");
        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
		        if (((String) newValue).equalsIgnoreCase("0"))
		        	preference.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 느린 속도로 넘어갑니다.");
		        else if (((String) newValue).equalsIgnoreCase("1"))
		        	preference.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 보통 속도로 넘어갑니다.");
		        else
		        	preference.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 빠른 속도로 넘어갑니다.");
				return true;
			}
		});
	}
	
	private void createPagingType(PreferenceCategory category) {
		ListPreference pref = new ListPreference(getContext());
        
        CharSequence[] lists = new CharSequence[2];
        lists[0] = "수직 이동";
        lists[1] = "수평 이동";
        pref.setEntries(lists);
        CharSequence[] listValues = new CharSequence[2];
        listValues[0] = "0";
        listValues[1] = "1";
        pref.setEntryValues(listValues);
        pref.setTitle("page 이동 방향");
        pref.setDialogTitle("page 이동 방향 설정");
        category.addPreference(pref);
        pref.setKey(PREF_TEXT_PAGE_DIRECTION);
        pref.setDefaultValue("0");
        pref.setValue(String.valueOf(getPageDirection(getContext())));
        if (pref.getValue() == null) 
        	pref.setValue("0");
        
        if (pref.getValue().equalsIgnoreCase("0"))
        	pref.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 수직 방향으로 이동합니다.");
        else if (pref.getValue().equalsIgnoreCase("1"))
        	pref.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 수평 방향으로 이동합니다.");
        pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
		        if (((String) newValue).equalsIgnoreCase("0"))
		        	preference.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 수직 방향으로 이동합니다.");
		        else if (((String) newValue).equalsIgnoreCase("1"))
		        	preference.setSummary("볼륨 버튼 또는 이동 버튼을 누를시 수평 방향으로 이동합니다.");
				return true;
			}
		});
	}
	
    private void createUseTextMoveButtonPref(PreferenceCategory category) {
    	//이동 버튼 사용 여부 설정
        CheckBoxPreference moveBtnPref = new CheckBoxPreference(getContext());
        moveBtnPref.setKey(PREF_TEXT_USE_MOVE_BUTTON);
        moveBtnPref.setDefaultValue(true);
        moveBtnPref.setTitle("이동 버튼 사용");
        moveBtnPref.setSummary("텍스트 뷰어에서 위치 조절이 가능한 보이지 않는 버튼을 사용하여 페이지 이동");
        category.addPreference(moveBtnPref);
    }
	
	public static void initTextFont(Context context) {
		if (sFonts.size() <= 0) {
			sFonts.add(Typeface.DEFAULT);
			sFonts.add(Typeface.createFromAsset(context.getAssets(),"NanumBrush.otf"));
			sFonts.add(Typeface.createFromAsset(context.getAssets(),"NanumGothic.otf"));
			sFonts.add(Typeface.createFromAsset(context.getAssets(),"NanumMyeongjo.otf"));
		}
	}
	public static final String sFontName[] = {	//0:background color, 1:text color
		"기본폰트",
		"나눔브러쉬",
		"나눔고딕",
		"나눔명조",
	};
	public static final ArrayList<Typeface> sFonts = new ArrayList<Typeface>();
	
	public static final int sColors[][] = {	//0:background color, 1:text color
			{Color.BLACK,Color.WHITE},
			{Color.WHITE,Color.BLACK},
			{Color.GRAY,Color.WHITE},
			{Color.rgb(247, 248, 216),Color.DKGRAY},
			{Color.rgb(193, 211, 167),Color.DKGRAY},
	};
	
	public static final int sSizes[] = {
			13,
			15,
			17,
			19,
			21
	};
	
	public static final String sGapsStr[] = {
			"좁게",
			"중간",
			"넓게"
	};
	
	public static final float sGaps[] = {
			1.0f,
			1.3f,
			1.6f
	};
	
	public static void setTextSlidingAndPaging(Context _ctx, boolean isLandscape) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		if (isLandscape)
			edit.putString(PREF_TEXT_SLIDING_PAGING, "0");
		else
			edit.putString(PREF_TEXT_SLIDING_PAGING, "1");
		edit.commit();
	}
	
	public static boolean getTextSlidingAndPaging(Context _ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		String str = pref.getString(PREF_TEXT_SLIDING_PAGING, "1");
		if (str == null)
			return true;
		else if (str.equalsIgnoreCase("0"))
			return false;
		return true;
	}
	
	public static void setTextColor(Context _ctx, int index) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(PREF_TEXT_COLOR, index);
		edit.commit();
	}
	
	public static int getTextColor(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return pref.getInt(PREF_TEXT_COLOR, 3);
	}
	
	public static void setTextSize(Context _ctx, int index) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(PREF_TEXT_SIZE, index);
		edit.commit();
	}
	
	public static int getTextSize(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return pref.getInt(PREF_TEXT_SIZE, 2);
	}
	
	public static float getTextSizeValue(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		int index = pref.getInt(PREF_TEXT_SIZE, 0);
		return sSizes[index];
	}
	
	public static void setTextGap(Context _ctx, int index) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(PREF_TEXT_GAP, index);
		edit.commit();
	}
	
	public static int getTextGap(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return pref.getInt(PREF_TEXT_GAP, 1);
	}
	
	
	public static float getTextGapValue(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		int index = pref.getInt(PREF_TEXT_GAP, 1);
		return sGaps[index];
	}
	
	public static void setTextFont(Context _ctx, int index) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(PREF_TEXT_FONT, index);
		edit.commit();
	}
	
	public static int getTextFont(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return pref.getInt(PREF_TEXT_FONT, 0);
	}
	
	public static Typeface getTextFontTypeface(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		int index = pref.getInt(PREF_TEXT_FONT, 1);
		return sFonts.get(index);
	}
	
	
	public static void setScrollSpeed(Context _ctx, int speed) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(PREF_TEXT_SCROLL_SPEED, String.valueOf(speed));
		edit.commit();
	}
	
	public static int getScrollSpeed(Context _ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		String str = pref.getString(PREF_TEXT_SCROLL_SPEED, "1");
		return Integer.parseInt(str);
	}
	
	public static void setPageDirection(Context _ctx, int speed) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		SharedPreferences.Editor edit = pref.edit();
		edit.putString(PREF_TEXT_PAGE_DIRECTION, String.valueOf(speed));
		edit.commit();
	}
	
	public static int getPageDirection(Context _ctx) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		String str = pref.getString(PREF_TEXT_PAGE_DIRECTION, "0");
		return Integer.parseInt(str);
	}
	
	public static int getScrollSpeedValue(Context _ctx) {
		int index = getScrollSpeed(_ctx);
		if (index == 0) {
			return SLOW_SCROLL_SPEED;
		} else if (index == 1) {
			return NORMAL_SCROLL_SPEED;
		} else {
			return FAST_SCROLL_SPEED;
		}
	}
	
	public static boolean getUsePageMoveBtn(Context _ctx){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_ctx);
		return pref.getBoolean(PREF_TEXT_USE_MOVE_BUTTON, true);
	}
}
