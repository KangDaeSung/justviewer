package com.kds3393.just.viewer.Movie;

import com.common.utils.debug.CLog;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;

public class CenterLineDrawable extends ShapeDrawable {
	private static final String TAG = "CenterLineDrawable";
	Paint mLinePnt = new Paint();
	int backColor = Color.GRAY;
	@Override
	public void draw(Canvas canvas) {
		int height = getBounds().bottom - getBounds().top;
		int top = getBounds().top + (height - 6) / 2;
		canvas.drawColor(backColor);
		canvas.drawRect(0, top, getBounds().right, top+6, mLinePnt);
	}
	
	public void setBackGroundColor(int color) {
		backColor = color;
	}
	
	public void setLineColor(int color) {
		mLinePnt.setColor(color);
	}
	
}
