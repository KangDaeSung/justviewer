package com.kds3393.just.viewer.Movie;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.VideoView;

public class MovieViewer extends MovieView{
	private static final String TAG = "MovieViewer";

	public float mScale = 1.0f;
	public float mVideoSetScale = 1.0f;
	public int mVideoWidth = 0;
	public int mVideoHeight = 0;
	private MovieController mMediaController;
	
	@Override
	public void requestLayout() {
		super.requestLayout();
	}

	public MovieViewer(Context context) {
		super(context);
	}
	
	public void setMediaController(MovieController controller) {
        mMediaController = controller;
        mMediaController.setMediaPlayer(this);
        mMediaController.setEnabled(true);
    }
	
	public void show() {
		if (mScale == 1.0f && mMediaController != null) {
            mMediaController.show();
        }
	}
	
	public void hide() {
		if (mMediaController != null) {
            mMediaController.hide();
        }
	}
	
	public int setScale(int width, int height) {
		mMediaController.mScale = mScale;
		if (mVideoWidth == 0 && mVideoHeight == 0) {
			mVideoWidth = getWidth();
			mVideoHeight = getHeight();
		}
		
		if (mScale > 1.0f) {
			if (mMediaController != null) {
	            mMediaController.forceHide();
	        }
		}
		float xScale = 1;
		float yScale = 1;
		xScale = (float)(width*mVideoSetScale)/mVideoWidth;
		yScale = (float)(height*mVideoSetScale)/mVideoHeight;
		int gravity = 0;
		if (yScale > xScale)
			gravity = Gravity.CENTER_VERTICAL;
		else
			gravity = Gravity.CENTER_HORIZONTAL;

		setMeasuredDimension((int) (mVideoWidth * mScale),(int) (mVideoHeight * mScale));
		
		return gravity;
	}
}