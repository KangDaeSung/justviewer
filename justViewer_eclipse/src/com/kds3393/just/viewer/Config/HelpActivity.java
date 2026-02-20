package com.kds3393.just.viewer.Config;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.View.CoverFlowView;

public class HelpActivity extends ParentActivity {

	int[] mHelpImageResource = {
			R.drawable.help_main,
			R.drawable.help_music
	};
	public static final String EXTRA_SHOW_PAGE_INDEX = "extra_show_page_index";
	public static final String EXTRA_SHOW_KEY = "extra_show_key";
	private int mIndex = 0;
	private String mShowKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSize();
        mIndex = getIntent().getIntExtra(EXTRA_SHOW_PAGE_INDEX,0);
        mShowKey = getIntent().getStringExtra(EXTRA_SHOW_KEY);
        
        setHelpShowType(this,mShowKey,true);
        
        setContentView(createMainView());
    }
	
    private int mPageViewHeight;
    private float mPageViewFontSize;
    private Size mArrowSize;
    private void initSize() {
    	if (Size.ScreenType == Size.S800X1280X1) {
    		mPageViewHeight = (int) (50 * Size.Density);
    		mPageViewFontSize = 30;
    		mArrowSize = new Size(48,85);
		} else if (Size.ScreenType == Size.S1080X1920X3) {
			mPageViewHeight = (int) (50 * Size.Density);
			mArrowSize = new Size((int) (48 * Size.Density),(int) (85 * Size.Density));
			mPageViewFontSize = 20;
		} else { // 갤럭시 노트2, 갤럭시 S3 및 그 외 정의되지 않은 해상도
			mPageViewHeight = (int) (50 * Size.Density);
			mArrowSize = new Size((int) (48 * Size.Density),(int) (85 * Size.Density));
			mPageViewFontSize = (float) (30 / ((0.5 * Size.Density) + 0.5));
		}
    }
    
    private CoverFlowView mHelpView;
    private TextView mPageText;
    private ImageView mPrevArraw;
    private ImageView mNextArraw;
    private RelativeLayout createMainView() {
    	RelativeLayout main = new RelativeLayout(this);
    	main.setBackgroundColor(Color.parseColor("#b222252d"));
    	
    		mPageText = ViewMaker.TextViewMaker(this, main, "", LayoutParams.MATCH_PARENT,  mPageViewHeight, 0, 0);
    		mPageText.setGravity(Gravity.CENTER);
    		mPageText.setTextSize(mPageViewFontSize);
    		
	    	mHelpView = CoverFlowView.make(this, main, (int) (Size.DisplayWidth * 0.9), (int) (Size.DisplayHeight * 0.9), 0, 0);
	    	LayoutUtils.setRelativeRule(mHelpView, RelativeLayout.CENTER_IN_PARENT);
	    	mHelpView.addFirstItem(mIndex);
	    	mHelpView.addItem(mHelpImageResource);
	    	mHelpView.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					mPageText.setText((position + 1) + " / " + mHelpImageResource.length);
					if (position <= 0) {
						mPrevArraw.setVisibility(View.INVISIBLE);
					} else {
						mPrevArraw.setVisibility(View.VISIBLE);
					}
					if (position >= mHelpImageResource.length - 1) {
						mNextArraw.setVisibility(View.INVISIBLE);
					} else {
						mNextArraw.setVisibility(View.VISIBLE);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
				}
			});
	    	ImageView exitBtn = ViewMaker.ImageViewMaker(this, main, (int) (Size.DisplayWidth * 0.1),  (int) (Size.DisplayWidth * 0.1), 0, 20, 20, 0);
	    	LayoutUtils.setRelativeRule(exitBtn, RelativeLayout.ALIGN_PARENT_RIGHT);
	    	exitBtn.setImageResource(R.drawable.close);
	    	exitBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					finish();
				}
			});
	    	int top = (int) (((float)Size.DisplayHeight - (float)mArrowSize.Height) / 2.0f);
	    	mPrevArraw = ViewMaker.ImageViewMaker(this, main, mArrowSize.Width, mArrowSize.Height,0,top);
	    	mPrevArraw.setBackgroundResource(R.drawable.arrow_left);
	    	mPrevArraw.setVisibility(View.INVISIBLE);
	    	LayoutUtils.setRelativeRule(mPrevArraw, RelativeLayout.ALIGN_PARENT_LEFT);
	    	
	    	mNextArraw = ViewMaker.ImageViewMaker(this, main, mArrowSize.Width, mArrowSize.Height,0,top);
	    	mNextArraw.setBackgroundResource(R.drawable.arrow_right);
	    	LayoutUtils.setRelativeRule(mNextArraw, RelativeLayout.ALIGN_PARENT_RIGHT);
	    	
	    	
    	return main;
    }

    private static boolean sDontShowMainHelp = false;
    private static boolean sDontShowMusicHelp = false;
    public static void showHelp(Context context, String page) {
    	int showPageId=0;
    	if (page.equalsIgnoreCase(HELP_MAIN_SHOW)) {
        	if (!sDontShowMainHelp)
        		sDontShowMainHelp = getHelpShowType(context,page);
    		if (sDontShowMainHelp)
    			return;
    		showPageId = R.drawable.help_main;
    	} else if (page.equalsIgnoreCase(HELP_MUSIC_PANEL_SHOW)) {
        	if (!sDontShowMusicHelp)
        		sDontShowMusicHelp = getHelpShowType(context,page);
    		if (sDontShowMusicHelp)
    			return;
    		showPageId = R.drawable.help_music;
    	} else if (page.equalsIgnoreCase(HELP_BROWSER_SHOW)) {
    		showPageId = 1;
    	} else {
    		return;
    	}
    	
		Intent intent = new Intent(context, HelpActivity.class);
		intent.putExtra(EXTRA_SHOW_PAGE_INDEX, showPageId);
		intent.putExtra(EXTRA_SHOW_KEY, page);
		context.startActivity(intent);
    }
    
    public final static String HELP_MAIN_SHOW = "main_help_show";
    public final static String HELP_MUSIC_PANEL_SHOW = "music_help_show";
    public final static String HELP_BROWSER_SHOW = "browser_help_show";
	public static void setHelpShowType(Context _ctx, String key, boolean isShow){
		SharedPreferences pref = _ctx.getSharedPreferences(key, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putBoolean(key, isShow);
		edit.commit();
	}
	
	public static boolean getHelpShowType(Context _ctx,String key) {
		SharedPreferences pref = _ctx.getSharedPreferences(key, 0);
		return pref.getBoolean(key, false);
	}
}
