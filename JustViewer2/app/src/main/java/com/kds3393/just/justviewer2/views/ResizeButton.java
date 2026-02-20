package com.kds3393.just.justviewer2.views;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

import common.lib.utils.LayoutUtils;
import common.lib.utils.Size;

public class ResizeButton extends androidx.appcompat.widget.AppCompatImageButton {
	private static final String TAG = "ResizeButton";
	
	public static ResizeButton make(Context context, ViewGroup parent, int width, int height) {
		return make(context,parent,width,height,0,0,0,0);
	}
	
	public static ResizeButton make(Context context, ViewGroup parent, int width, int height, int left, int top) {
		return make(context,parent,width,height,left,top,0,0);
	}
	
	public static ResizeButton make(Context context, ViewGroup parent, int width, int height, int left, int top, int right, int bottom) {
		ResizeButton view = new ResizeButton(context);
		parent.addView(view);
		LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom);
		return view;
	}
	private Size mSize = new Size();
	private Point mPoint = new Point();
	private View mScaleView;
	private Size mScaleViewSize = new Size();
	private Size mBackGroundSize = new Size();
	public ResizeButton(Context context) {
		super(context);
	}
	
	public ResizeButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		setSize(getWidth(), getHeight(), left, top);
	}

	public void setSize(int w, int h, int x, int y) {
		mSize.Width = w;
		mSize.Height = h;
		mPoint.x = x;
		mPoint.y = y;
	}
	
	public void setScaleButton(View view) {
		mScaleView = view;
	}
	
	public void setMaxScaleSize(int w, int h) {
		mBackGroundSize.Width = w;
		mBackGroundSize.Height = h;
	}
	
	public void setViewPoint(int w, int h, int x, int y) {
		mPoint.x = (int) (x + w) - mSize.Width;
		mPoint.y = (int) (y + h) - mSize.Height;
		setResizeButtonViewParams(mPoint);
	}
	
	private float mOldX = 0;
	private float mOldY = 0;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		float distanceX = 0;
		float distanceY = 0;
		if (action == MotionEvent.ACTION_DOWN) {
			mOldX = x;
			mOldY = y;
			mScaleViewSize.Width = mScaleView.getWidth();
			mScaleViewSize.Height = mScaleView.getHeight();
			mScaleView.bringToFront();
			this.bringToFront();
		} else if (action == MotionEvent.ACTION_MOVE) {
			distanceX = x - mOldX;
			distanceY = y - mOldY;
			resize(distanceX,distanceY);
		} else if (action == MotionEvent.ACTION_UP) {
			setResizeButtonViewParams(mPoint);
			setScaleViewParams(mScaleViewSize);
		}
		
		return true;
	}
	
	private void resize(float distanceX, float distanceY) {
		int width = 0;
		int height = 0;
		width = (int) (mScaleViewSize.Width + distanceX);
		height = (int) (mScaleViewSize.Height + distanceY);
		if (mBackGroundSize.Width < (mScaleView.getLeft() + width)) {
			width = (int) (mBackGroundSize.Width - mScaleView.getLeft());
		}
		if (width < mSize.Width) width = mSize.Width;
		
		if (mBackGroundSize.Height < (mScaleView.getTop() + height)) {
			height = (int) (mBackGroundSize.Height - mScaleView.getTop());
		}
		if (height < mSize.Height) height = mSize.Height;
		
		mScaleViewSize.Width = width;
		mScaleViewSize.Height = height;
		
		mPoint.x = (int) (mScaleView.getLeft() + mScaleViewSize.Width) - mSize.Width;
		mPoint.y = (int) (mScaleView.getTop() + mScaleViewSize.Height) - mSize.Height;
		setResizeButtonViewParams(mPoint);
		setScaleViewParams(mScaleViewSize);
	}
	
	private void setScaleViewParams(Size size) {
		LayoutParams params = mScaleView.getLayoutParams();
		params.width = size.Width;
		params.height = size.Height;
		mScaleView.setLayoutParams(params);
	}
	
	private void setResizeButtonViewParams(Point p) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.getLayoutParams();
		params.leftMargin = p.x;
		params.topMargin = p.y;
		this.setLayoutParams(params);
	}
}
