package com.kds3393.just.viewer.View;

import com.common.utils.LayoutUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.CompoundButton;

public class CheckButton extends CompoundButton {
	private static final String TAG = "CheckButton" ;
	
	public static CheckButton make(Context context, ViewGroup parent,int width, int height) {
		return make(context,parent,width,height,0,0,0,0);
	}
	
	public static CheckButton make(Context context, ViewGroup parent,int width, int height, int left, int top) {
		return make(context,parent,width,height,left,top,0,0);
	}
	
	public static CheckButton make(Context context, ViewGroup parent,int width, int height, int left, int top, int right, int bottom) {
		CheckButton view = new CheckButton(context);
		parent.addView(view);
		LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom);
		return view;
	}
	
	private int mNonCheckDrawableResource;
	private int mCheckedDrawableResource;

	public CheckButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setClickable(true);
	}
	
	public CheckButton(Context context) {
		super(context);
		setClickable(true);
	}
	
	public void setCheckImage(int nonCheck, int checked) {
		mNonCheckDrawableResource = nonCheck;
		mCheckedDrawableResource = checked;
		changeCheckImg();
	}
	
	public void setChecked(boolean isChecked) {
		super.setChecked(isChecked);
		changeCheckImg();
	}

	
	private void changeCheckImg() {
		if (isChecked()) {
			setBackgroundResource(mCheckedDrawableResource);
		} else {
			setBackgroundResource(mNonCheckDrawableResource);
		}
		invalidate();
	}
}
