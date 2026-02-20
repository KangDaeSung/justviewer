package com.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.LayoutUtils;

public class ProgressCircle extends View{
	private static final String TAG = "PregressCircle";
	
	public static ProgressCircle make(Context context, ViewGroup parent,int width, int height) {
		return make(context,parent,width,height,0,0,0,0);
	}
	
	public static ProgressCircle make(Context context, ViewGroup parent,int width, int height, int left, int top) {
		return make(context,parent,width,height,left,top,0,0);
	}
	
	public static ProgressCircle make(Context context, ViewGroup parent,int width, int height, int left, int top, int right, int bottom) {
		ProgressCircle view = new ProgressCircle(context);
		parent.addView(view);
		LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom);
		return view;
	}
	
	private float mMax = 100;
	private float mValue = 0;
	
	private boolean mIsInfinity = false;
	private float mInfinityStartDegrees = -90;
	private ProgressInfinityTask mInfinityTask = new ProgressInfinityTask();;
	
	private float mDegrees = 0;
	
	private float mScale = 0.5f;
	
    private Paint mPaints;
    private Paint mBGCirclePaints;
    private Paint mTextPaints;
    private float mTextSize = 36;
    
    private RectF mBigOval;
    private int mCircleStroke = 2;
    
    private boolean mIsShowText = false;
    private int mPersent = 0;
    
	public ProgressCircle(Context context) {
        super(context);
        init();
	}
	
	public ProgressCircle(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
        mPaints = new Paint();
        mPaints.setStyle(Paint.Style.STROKE);
        mPaints.setStrokeWidth(mCircleStroke);
        mPaints.setColor(Color.WHITE);
        mPaints.setAntiAlias(true);
        mPaints.setStrokeCap(Paint.Cap.BUTT);
        
        mBGCirclePaints = new Paint();
        mBGCirclePaints.setStyle(Paint.Style.STROKE);
        mBGCirclePaints.setStrokeWidth(mCircleStroke);
        mBGCirclePaints.setColor(Color.BLACK);
        mBGCirclePaints.setAntiAlias(true);
        mBGCirclePaints.setStrokeCap(Paint.Cap.BUTT);
        
        mTextPaints = new Paint();  
        mTextPaints.setAntiAlias(true);  
        mTextPaints.setTextSize(mTextSize); 
        mTextPaints.setColor(Color.BLACK);  
        
        mBigOval = new RectF();
	}

	public void setInfinity(boolean isInfinity) {
		mIsInfinity = isInfinity;
		if (mIsInfinity && mInfinityTask.getStatus() != AsyncTask.Status.RUNNING)
			mInfinityTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR , "");
	}
	
	public boolean isInfinity() {
		return mIsInfinity;
	}
	
    public void setMax(float max) {
    	mMax = max;
    }
    
    public float getMax() {
    	return mMax;
    }
    public void setProgress(int value) {
    	mValue = value;
    	mDegrees = (mValue/mMax) * 360.0f;
    	mPersent = (int) ((mValue/mMax) * 100);
    	invalidate();
    }
	
    public float getProgress() {
    	return mValue;
    }
    public void setCircleColor(int color) {
    	mPaints.setColor(color);
    }
    
    public void setBGCircleColor(int color) {
    	mBGCirclePaints.setColor(color);
    }

    public void setCircleScale(float scale) {
    	mScale = scale;
    }

    public void setStrokeWidth(int stroke) {
    	mCircleStroke = stroke;
    	mPaints.setStrokeWidth(mCircleStroke);
    	mBGCirclePaints.setStrokeWidth(mCircleStroke);
    }
    
    public void setShowPersent(boolean isShow) {
    	mIsShowText = isShow;
    }
    
    public void setPersentColor(int color) {
    	mTextPaints.setColor(color);
    }
    private void drawArcs(Canvas canvas, RectF oval, boolean useCenter, Paint paint) {
        canvas.drawArc(oval, -90, mDegrees, useCenter, paint);
        canvas.drawArc(oval, -90, mDegrees - 360, useCenter, mBGCirclePaints);
    }
    
    private void drawInfinityArcs(Canvas canvas, RectF oval, boolean useCenter, Paint paint) {
        canvas.drawArc(oval, mInfinityStartDegrees, 90, useCenter, paint);
        canvas.drawArc(oval, mInfinityStartDegrees - 360, (90 - 360), useCenter, mBGCirclePaints);
    }
    
    @Override 
    protected void onDraw(Canvas canvas) {
    	if (mValue >= 0) {
        	setBound();
        	//canvas.drawColor(Color.alpha(Color.CYAN));
        	if (mIsInfinity) {
        		drawInfinityArcs(canvas, mBigOval, false, mPaints);
        	} else {
        		drawArcs(canvas, mBigOval, false, mPaints);
        		if (mIsShowText) {
        			drawText(canvas);
        		}
        	}
    	}
    }
    
    private void drawText(Canvas canvas) {
    	String testText = mMax + "%";
    	float limitWidth = (float) (mBigOval.width() * 0.4);
    	for (int i=0;i<24;i++) {
    		float texthor = mTextPaints.measureText(testText) / 2;
    		if (limitWidth < texthor) {
    			mTextSize--;
    			mTextPaints.setTextSize(mTextSize);
    		}
    	}
    			
		String text = mPersent + "%";
		float textHeight = mTextPaints.descent() - mTextPaints.ascent();
	    float texttopOffset = (textHeight / 2) - mTextPaints.descent();
		float textLeftOffset = mTextPaints.measureText(text) / 2;
		canvas.drawText(mPersent + "%", getWidth() / 2 - textLeftOffset, getHeight() / 2 + texttopOffset, mTextPaints);
    }
    
    private void setBound() {
    	int width = getWidth();
    	int height = getHeight();
    	int size = (int) ((width<height?width:height) * mScale);
    	int left = (width - size) / 2;
    	int top = (height - size) / 2;
    	mBigOval.set(left, top, left + size, top + size);
    }
    
	protected class ProgressInfinityTask extends AsyncTask<Object, Object, Boolean> {
		@Override
		protected void onPreExecute() {
			mInfinityStartDegrees = -90;
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			try {
				while(true) {
					Thread.sleep(100);
					if (mIsInfinity) {
						publishProgress();
					} else {
						return true;
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return false; 
		}
		
		@Override
		protected void onProgressUpdate(Object... values) {
			if (mIsInfinity) {
				mInfinityStartDegrees +=10;
				if (mInfinityStartDegrees > 270) {
					mInfinityStartDegrees = -90;
				}
				invalidate();
			}
		}

		@Override
		protected void onPostExecute(Boolean isError) {

		}
	}
}
