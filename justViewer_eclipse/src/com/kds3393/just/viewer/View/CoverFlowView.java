package com.kds3393.just.viewer.View;

import java.util.ArrayList;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.AbsListView.LayoutParams;

public class CoverFlowView extends Gallery {
	private static final String TAG = "ActionCarouselCoverFlowView";
	
	public static CoverFlowView make(Context context, ViewGroup parent,int width, int height) {
		return make(context,parent,width,height,0,0,0,0);
	}
	
	public static CoverFlowView make(Context context, ViewGroup parent,int width, int height, int left, int top) {
		return make(context,parent,width,height,left,top,0,0);
	}
	
	public static CoverFlowView make(Context context, ViewGroup parent,int width, int height, int left, int top, int right, int bottom) {
		CoverFlowView view = new CoverFlowView(context);
		parent.addView(view);
		LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom);
		return view;
	}
	
	private static Camera mCamera = new Camera();
	private int mMaxRotationAngle = 60;
	private int mMaxZoom = -120;
	private int mCoveflowCenter;
	private CoverFlowAdapter mAdapter;
	public CoverFlowView(Context context) {
		super(context);
		setStaticTransformationsEnabled(true);
		mCamera = new Camera();
		setGravity(Gravity.TOP);
		mAdapter = new CoverFlowAdapter(context); 
		this.setAdapter(mAdapter);
	}
	
	public int getMaxRotationAngle() {
		return mMaxRotationAngle;
	}
	
	public void setMaxRotationAngle(int maxRotationAngle) {
		mMaxRotationAngle = maxRotationAngle;
	}
	
	public int getMaxZoom() {
		return mMaxZoom;
	}
	
	public void setMaxZoom(int maxZoom) {
		mMaxZoom = maxZoom;
	}
	
	public void addItem(int resId) {
		mAdapter.add(resId);
	}
	private int mFirstResId;
	public void addFirstItem(int resId) {
		mFirstResId = resId;
		mAdapter.add(resId);
	}
	public void addItem(int[] resIds) {
		for (int id:resIds) {
			if (mFirstResId != id)
				mAdapter.add(id);
		}
	}
	
	private int getCenterOfCoverflow() {
        return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
    }
	
	private static int getCenterOfView(View view) {
        return view.getLeft() + view.getWidth() / 2;
    }
	
	protected boolean getChildStaticTransformation(View child, Transformation t) {
		final int childCenter = getCenterOfView(child);
		final int childWidth = child.getWidth() ;
		int rotationAngle = 0;
		
		t.clear();
		t.setTransformationType(Transformation.TYPE_MATRIX);
		
		if (childCenter == mCoveflowCenter) {
		    transformImageBitmap(child, t, 0);
		} else {
		    rotationAngle = (int) (((float) (mCoveflowCenter - childCenter)/ childWidth) *  mMaxRotationAngle);
		    if (Math.abs(rotationAngle) > mMaxRotationAngle) {
		    	rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
		    }
		    transformImageBitmap(child, t, rotationAngle);
		}
		return true;
	}
	
	private void transformImageBitmap(View child, Transformation t, int rotationAngle) {            
		mCamera.save();
		final Matrix imageMatrix = t.getMatrix();
		final int imageHeight = (int) child.getHeight();
		final int imageWidth = (int) child.getWidth();
		final int rotation = Math.abs(rotationAngle);
		mCamera.translate(0.0f, 0.0f, 100.0f);
		
		//As the angle of the view gets less, zoom in     
		if ( rotation < mMaxRotationAngle ) {
			float zoomAmount = (float) (mMaxZoom +  (rotation * 1.5));
			mCamera.translate(0.0f, 0.0f, zoomAmount);
		} 
		
		mCamera.rotateY(rotationAngle);
		mCamera.getMatrix(imageMatrix);
		int w = (int) (imageWidth/2);
		int h = (int) (imageHeight/2);
		
		int offset = (int) ((this.getHeight() - imageHeight) /2);
		imageMatrix.preTranslate(-w, -h + offset); 
		imageMatrix.postTranslate(w, h);
		mCamera.restore();
	}
	
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCoveflowCenter = getCenterOfCoverflow();
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    private class CoverFlowAdapter extends ArrayAdapter<Integer> {
    	public CoverFlowAdapter(Context context) {
	    	super(context,0);
	    }
    	
    	@Override
		public View getView (int position, View convertView, ViewGroup parent) {
    		ImageView view = (ImageView) convertView;
    		if (view == null) {
    			view = new ImageView(getContext());
    			view.setLayoutParams(new LayoutParams((int) (Size.DisplayWidth * 0.85), (int) (Size.DisplayHeight * 0.85)));
    		}
    		
    		int resId = getItem(position);
    		view.setBackgroundResource(resId);
			return view;
		}
    }
}