package com.kds3393.just.dialog;

import java.util.ArrayList;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;
import kankan.wheel.widget.adapters.ArrayWheelAdapter;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.Music.MusicService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class ImageSlideShowTimeSetBuilder {
	private static final String TAG = "PageWheelSelector";
	private AlertDialog.Builder mBuilder;
	private AlertDialog mDialog;
	
	private WheelView mWheel;
	private Context mContext;
	
	public ImageSlideShowTimeSetBuilder(Context context) {
		initSize();
		mBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context, android.R.style.Theme_Holo_Dialog));
		mContext = context;
	}
	
	private int mMessageViewHeight; // 상단 Message view의 높이
	private float mMessageTextSize; // 상단 Icon Size width,height 동일
	
	private void initSize() {
		if (Size.ScreenType == Size.S800X1280X1) {
			mMessageViewHeight = 92;
			mMessageTextSize = 20;
		} else if (Size.ScreenType == Size.S1080X1920X3) {
			mMessageViewHeight = 168;
			mMessageTextSize = 12;
		} else { // 갤럭시 노트2, 갤럭시 S3 및 그 외 정의되지 않은 해상도
			mMessageViewHeight = 80;
			mMessageTextSize = 15;
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
	
	public long getTimeMills() {
		return (mWheel.getCurrentItem() + limitValue) * 1000;
	}
	
	public int getTime() {
		return mWheel.getCurrentItem() + limitValue;
	}
	
	public void setPageText(int index) {
		
	}
	
	// Wheel scrolled listener
	OnWheelScrollListener scrolledListener = new OnWheelScrollListener() {
		public void onScrollingStarted(WheelView wheel) {
		}
		
		public void onScrollingFinished(WheelView wheel) {
		}
	};
	
	// Wheel changed listener
	private OnWheelChangedListener changedListener = new OnWheelChangedListener() {
		public void onChanged(WheelView wheel, int oldValue, int newValue) {
			
		}
	};
	private int limitValue = 3;
	
	public void makeView(Context context) {
		LinearLayout dialoglayout = new LinearLayout(context);
		dialoglayout.setOrientation(LinearLayout.VERTICAL);
		dialoglayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		dialoglayout.setGravity(Gravity.CENTER);
		
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		dialoglayout.addView(layout);
		LayoutUtils.setLinearLayoutParams(layout, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		mWheel = new WheelView(context);
		layout.addView(mWheel);
		mWheel.addScrollingListener(scrolledListener);
		mWheel.addChangingListener(changedListener);
		mWheel.setCyclic(true);
		mWheel.setInterpolator(new AnticipateOvershootInterpolator());
		
		mWheel.setCurrentItem(0);
		
		mWheel.setViewAdapter(new NumericWheelAdapter(context, limitValue, 60));
		LayoutUtils.setLinearLayoutParams(mWheel, 174, 250, 0, 0, 0, 0);
		
		mDialog = mBuilder.create();
		mDialog.setView(dialoglayout);
		mDialog.setTitle("Slide show time setting");
	}
	
}
