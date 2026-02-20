package com.kds3393.just.viewer.View;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MenuView extends FrameLayout implements OnClickListener {
	private static final String TAG = "MenuView";
	
	private static int HIDE_ANIMATION_DURATION = 400;
	private static final AccelerateInterpolator S_ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
	
	protected Animation mFadeOutAnimation;
	protected Animation mFadeInAnimation;
	
	private int mLayoutWidth = 0;
	private int mLayoutHeight = 0;
	private int mIconSize = 0;
	private int mSpacing = 0;
	private ArrayList<IconView> mIcons = new ArrayList<IconView>();
	
	private boolean mIsSelected = false;
	private OnIconClickListener mOnIconClickListener = null;
	public interface OnIconClickListener {
		public void onIconClick(int id, boolean isSelected);
	}
	
	public void setOnIconClickListener(OnIconClickListener l) {
		mOnIconClickListener = l;
	}
	
	public MenuView(Context context) {
		super(context);
		init();
	}
	
	public MenuView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		mFadeOutAnimation = new AlphaAnimation(1.0f, 0.0f);
		mFadeOutAnimation.setDuration(HIDE_ANIMATION_DURATION);
		mFadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
		mFadeInAnimation.setDuration(HIDE_ANIMATION_DURATION);
	}
	public void addIconView(int id, int resId) {
		IconView icon = new IconView(getContext());
		if (id < 0) {
			id = mIcons.size();
		}
		icon.setId(id);
		icon.setImageResource(resId);
		icon.setSize(mIconSize);
		icon.setOnClickListener(this);
		mIcons.add(icon);
		addView(icon);
	}
	
	public void setIconSize(int size) {
		mIconSize = size;
		for (IconView v : mIcons) {
			v.setSize(mIconSize);
		}
	}
	
	public void setSpacing(int spacing) {
		mSpacing = spacing;
	}
	
	public void deSelection() {
		for (IconView v : mIcons) {
			if (v.isSelected()) {
				onClick(v);
				return;
			}
		}
	}
	public void setSelection(int id) {
		for (IconView v : mIcons) {
			if (v.getId() == id) {
				if (v.isSelected()) {
					return;
				}
				onClick(v);
			}
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int count = mIcons.size();
		if (count <= 0) {
			return;
		}
		
		super.onLayout(changed, l, t, r, b);
		
		mLayoutWidth = r - l;
		mLayoutHeight = b - t;
		
		if (mSpacing == 0) {
			mSpacing = mIconSize / 3;
		}
		
		int lrMargin = (mLayoutWidth - ((mSpacing * (count - 1)) + (mIconSize * count))) / 2;
		int tbMargin = (mLayoutHeight - mIconSize) / 2;
		for (int i = 0; i < count; i++) {
			int left = lrMargin + (mSpacing * i) + (mIconSize * i);
			mIcons.get(i).layout(left, tbMargin, left + mIconSize, tbMargin + mIconSize);
		}
	}
	
	@Override
	public void onClick(View v) {
		mIsSelected = ((IconView)v).onClick();
		for (IconView icon : mIcons) {
			if (icon != v) {
				if (mIsSelected) {
					icon.startAnimation(mFadeOutAnimation);
					icon.setVisibility(View.GONE);
				} else {
					icon.startAnimation(mFadeInAnimation);
					icon.setVisibility(View.VISIBLE);
				}
			}
		}
		if (mOnIconClickListener != null) {
			mOnIconClickListener.onIconClick(v.getId(), mIsSelected);
		}
	}
	
	public class IconView extends ImageView {
		private int mOriLeft = 0;
		private boolean mIsSelected = false;
		
		public IconView(Context context) {
			super(context);
		}
		
		public void setSize(int size) {
			setLayoutParams(new FrameLayout.LayoutParams(size, size));
		}
		
		public int getOriLeft() {
			return mOriLeft;
		}
		
		public boolean isSelected() {
			return mIsSelected;
		}
		
		public boolean onClick() {
			bringToFront();
			mIsSelected = !mIsSelected;
			int offset = 0;
			if (mIsSelected) {
				offset = mSpacing - mOriLeft;
			} else {
				offset = 0;
			}
			animate().translationX(offset).setDuration(HIDE_ANIMATION_DURATION).
			setInterpolator(S_ACCELERATE_INTERPOLATOR).
			setListener(new AnimatorListener() {
	            @Override
	            public void onAnimationStart(Animator animation) {

	            }
	            @Override public void onAnimationRepeat(Animator animation) { }
	            @Override public void onAnimationEnd(Animator animation) { }
	            @Override public void onAnimationCancel(Animator animation) { }
	        });
			return mIsSelected;
		}
		
		@Override
		public void layout(int l, int t, int r, int b) {
			mOriLeft = l;
			super.layout(l, t, r, b);
		}
	}
}
