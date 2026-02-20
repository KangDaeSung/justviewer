package com.kds3393.just.viewer.View;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.debug.CLog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;

public class MoveScaleButton extends Button {
	private static final String TAG = "MoveScaleButton";
	
	public static MoveScaleButton make(Context context, ViewGroup parent,int width, int height) {
		return make(context,parent,width,height,0,0,0,0);
	}
	
	public static MoveScaleButton make(Context context, ViewGroup parent,int width, int height, int left, int top) {
		return make(context,parent,width,height,left,top,0,0);
	}
	
	public static MoveScaleButton make(Context context, ViewGroup parent,int width, int height, int left, int top, int right, int bottom) {
		MoveScaleButton view = new MoveScaleButton(context);
		parent.addView(view);
		LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom);
		return view;
	}
	
	public static final int MODE_FUNCTION = 0;
	public static final int MODE_EDIT = 1;
	public static final int MODE_SHOW = 2;
	
	private Point mMovePoint;
	private Size mBtnSize;
	private Size mParentSize;
	
	private int isMoveMode = MODE_FUNCTION;
	
	public MoveScaleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MoveScaleButton(Context context) {
		super(context);
	}
	
	private String mText;
	public void setText(String text) {
		mText = text;
	}
	
	private View mViewer;
	private ResizeButton mResizeBtn;
	public void setFunctionView(View view, ResizeButton btn) {
		mViewer = view;
		mResizeBtn = btn;
	}
	public void setMoveMode(int isMove) {
		isMoveMode = isMove;
		if (isMoveMode == MODE_FUNCTION) {
			super.setText("");
			this.setBackgroundColor(Color.TRANSPARENT);
			mResizeBtn.setVisibility(View.INVISIBLE);
		} else {
			super.setText(mText);
			this.setBackgroundColor(Color.parseColor("#b222252d"));
			if (isMoveMode == MODE_EDIT) {
				mResizeBtn.setVisibility(View.VISIBLE);
			} else {
				mResizeBtn.setVisibility(View.INVISIBLE);
			}
		}
	}
	
	private View mBoundView;
	private Rect mBoundRect;
	public void setBoundView(View view) {
		mBoundView = view;
		mBoundRect = new Rect();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (mBtnSize == null) {
			mBtnSize = new Size(getWidth(),getHeight());
			mParentSize = new Size(((View) getParent()).getWidth(),((View) getParent()).getHeight());
			mMovePoint = new Point();
			mMovePoint.x = this.getLeft();
			mMovePoint.y = this.getTop();
		}
	}
	
	private float mOldX = 0;
	private float mOldY = 0;
	private float lastDownX;
	private float lastDownY;
	private boolean mIsScroll = false;
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (isMoveMode == MODE_FUNCTION) {
			if (mViewer != null) {
				if (action == MotionEvent.ACTION_DOWN) {
					lastDownX = event.getRawX();
					lastDownY = event.getRawY();
					mIsScroll = false;
				} else if (action == MotionEvent.ACTION_MOVE) {
					int mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
					if(mIsScroll || (Math.abs(lastDownX - event.getRawX()) >= mTouchSlop  || 
					   Math.abs(lastDownY - event.getRawY()) >= mTouchSlop)){
						if (!mIsScroll) {
							event.setAction(MotionEvent.ACTION_DOWN);
							mViewer.onTouchEvent(event);
							event.setAction(MotionEvent.ACTION_MOVE);
						}
							
						mIsScroll = true;
						mViewer.onTouchEvent(event);
						return false;
					}
				} else if (action == MotionEvent.ACTION_UP) {
					if (mIsScroll) {
						mViewer.onTouchEvent(event);
						return false;
					}
				}
			}
			return super.onTouchEvent(event);
		} else if (isMoveMode == MODE_SHOW) {
			return false;
		}
		
		
		float x = event.getX();
		float y = event.getY();
		float distanceX = 0;
		float distanceY = 0;
		if (action == MotionEvent.ACTION_DOWN) {
			mOldX = x;
			mOldY = y;
			mBtnSize.Width = this.getWidth();
			mBtnSize.Height = this.getHeight();
			mBoundRect.set(mBoundView.getLeft(), mBoundView.getTop(), mBoundView.getRight(), mBoundView.getBottom());
			bringToFront();
			mResizeBtn.bringToFront();
		} else if (action == MotionEvent.ACTION_MOVE) {
			distanceX = x - mOldX;
			distanceY = y - mOldY;
			move(distanceX,distanceY);
		} else if (action == MotionEvent.ACTION_UP) {
			LayoutUtils.setRelativeLayoutParams(this,mBtnSize.Width,mBtnSize.Height,mMovePoint.x,mMovePoint.y,-1);
		}
		return true;
	}
	
	private void move(float distanceX, float distanceY) {
		int left = 0;
		int right = 0;
		int top = 0;
		int bottom = 0;
		
		int oldX = mMovePoint.x;
		int oldY = mMovePoint.y;
		
		mMovePoint.x += distanceX;
		if (mMovePoint.x < 0)
			mMovePoint.x = 0;
		left = mMovePoint.x;
		
		mMovePoint.y += distanceY;
		if (mMovePoint.y < 0)
			mMovePoint.y = 0;
		top = mMovePoint.y;
		
		right = mMovePoint.x + mBtnSize.Width;
		bottom = mMovePoint.y + mBtnSize.Height;
		if (mParentSize.Width < right) {
			right = mParentSize.Width;
			left = mParentSize.Width - mBtnSize.Width;
		}
		
		if (mParentSize.Height < bottom) {
			bottom = mParentSize.Height;
			top = mParentSize.Height - mBtnSize.Height;
		}
		
		mMovePoint.x = left;
		mMovePoint.y = top;
		
		LayoutUtils.setRelativeLayoutParams(this,mBtnSize.Width,mBtnSize.Height,mMovePoint.x,mMovePoint.y,-1);
		mResizeBtn.setViewPoint(mBtnSize.Width,mBtnSize.Height,left, top);
		//boolean isClash = mBoundRect.contains(left, top, left + mBtnSize.Width, top + mBtnSize.Height);
//		boolean isClash = isClash(left, top, left + mBtnSize.Width, top + mBtnSize.Height);
//		CLog.e(TAG, "KDS3393_isClash = " + isClash);
//		if (!isClash)
//			layout(left, top, left + mBtnSize.Width, top + mBtnSize.Height);
//		else {
//			mMovePoint.set(oldX, oldY);
//		}
	}
	
	private boolean isClash(int x,int y, int r, int b) {
		if (r > mBoundRect.left && x < mBoundRect.right && b > mBoundRect.top && y < mBoundRect.bottom)
			return true;
		return false;
	}
}
