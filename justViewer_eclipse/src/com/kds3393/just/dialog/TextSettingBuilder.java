package com.kds3393.just.dialog;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.kds3393.just.viewer.Text.TextSettingPanel;
import com.kds3393.just.viewer.Text.VTextListView;

public class TextSettingBuilder {
    private static final String TAG = "PageWheelSelector";
    private AlertDialog.Builder mBuilder;
    private AlertDialog mDialog;
    
    private ImageView mThumbImg;
    private TextView mMessage;
    private Context mContext;
    public TextSettingBuilder(Context context) {
    	initSize();
    	mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Dialog));
    	mContext = context;
    }
    
    private int mSampleTextHeight;		
	private Size mSettingPanel;				//상단 Top의 icon을 중앙 정렬하기 위한 Left margin 
	private int mIconTopMargin;				//상단 Top의 icon을 중앙 정렬하기 위한 Top margin
	private int mTopPathTextWidth;			//상단 Top의 path가 나오는 TextView의 Width, height = fill
	private int mIconGap;					//상단 Top의 Icon간 Gap
	
	private int mBrowserHeight = 0;
	
	private int mBottomBarHeight;			//하단 Bar의 Height, Width는 Fill
	private int mBottomIconSize;			//하단 Icon Size width,height 동일
	private int mBottomSideGap;				//하단 아이콘 gap
	
	private float mEmptyTextSize;				//Favorite가 없을때 나오는 Text Size
	private void initSize() {
		if (Size.ScreenType == Size.S800X1280X1) {
			mSampleTextHeight = 92;
			mSettingPanel = new Size(Size.DisplayWidth - 20,380);
		} else if (Size.ScreenType == Size.S1080X1920X3) {
			mSampleTextHeight = 155;
			mSettingPanel = new Size(Size.DisplayWidth - 120,550);
		} else { // 갤럭시 노트2, 갤럭시 S3 및 그 외 정의되지 않은 해상도
			mSampleTextHeight = (int) (180 / ((0.5 * Size.Density) + 0.5));
			mSettingPanel = new Size(Size.DisplayWidth - 20,(int)(Size.DisplayHeight * 0.33));
		}

		
	}
	
    public Context getContext() {
    	return mContext;
    }
    
    public AlertDialog.Builder getBuilder() {
        return mBuilder;
    }
    
    public AlertDialog getDialog() {
    	return mDialog;
    }
    
    
    // Wheel scrolled listener
    OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
        public void onScrollingStarted(WheelView wheel) {}
        public void onScrollingFinished(WheelView wheel) {
        }
    };
    
    // Wheel changed listener
    private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
        public void onChanged(WheelView wheel, int oldValue, int newValue) {
   			
        }
    };
	
    public void makeView(Context context) {
    	LinearLayout dialoglayout = new LinearLayout(context);
    	dialoglayout.setOrientation(LinearLayout.VERTICAL);
    	dialoglayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    	dialoglayout.setGravity(Gravity.CENTER);
    	
    		VTextListView textView = new VTextListView(context,null);
    		textView.isSingleSetting();
    		dialoglayout.addView(textView);
	    	LayoutUtils.setLayoutParams(dialoglayout, textView, (int) (mSettingPanel.Width *0.8), mSampleTextHeight,0,10);
	    	textView.runSettingText();
	    	
    		TextSettingPanel textSetting = TextSettingPanel.make(context, dialoglayout, mSettingPanel.Width, mSettingPanel.Height);
    		textSetting.setBackgroundColor(Color.TRANSPARENT);
    		textSetting.setTextView(textView);
    		
        mDialog = mBuilder.create();
        mDialog.setView(dialoglayout);
        mDialog.setTitle("Text Setting");
    }
    
    
}
