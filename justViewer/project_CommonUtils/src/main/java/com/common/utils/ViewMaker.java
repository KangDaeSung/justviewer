package com.common.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

public class ViewMaker {
	
	public static TextView TextViewMaker(Context context, ViewGroup parentView, String text, int width, int height, int x, int y) {
		return TextViewMaker(context,parentView,text,width,height,x,y,0,0);
	}
	
	public static TextView TextViewMaker(Context context, ViewGroup parentView, String text, int width, int height, int x, int y, int right, int bottom ) {
		TextView textView = new TextView(context);
		parentView.addView(textView);
		textView.setText(text);
		LayoutUtils.setLayoutParams(parentView,textView,width,height,x,y,right,bottom);
		return textView;
	}
	
	public static EditText EditTextMaker(Context context, ViewGroup parentView, int width, int height, int x, int y) {
		return EditTextMaker(context,parentView,width,height,x,y,0,0);
	}
	
	public static EditText EditTextMaker(Context context, ViewGroup parentView, int width, int height, int x, int y, int right, int bottom ) {
		EditText textView = new EditText(context);
		parentView.addView(textView);
		LayoutUtils.setLayoutParams(parentView,textView,width,height,x,y,right,bottom);
		return textView;
	}
	
	public static ImageView ImageViewMaker(Context context, ViewGroup parentView, int width, int height, int x, int y) {
		return ImageViewMaker(context,parentView,width,height,x,y,0,0);
	}
	
	public static ImageView ImageViewMaker(Context context, ViewGroup parentView, int width, int height, int x, int y, int right, int bottom ) {
		ImageView view = new ImageView(context);
		parentView.addView(view);
		LayoutUtils.setLayoutParams(parentView,view,width,height,x,y,right,bottom);
		return view;
	}
	
	public static Button ButtonMaker(Context context, ViewGroup parentView, int width, int height, int x, int y) {
		return ButtonMaker(context,parentView,width,height,x,y,0,0);
	}
	
	public static Button ButtonMaker(Context context, ViewGroup parentView, int width, int height, int x, int y, int right, int bottom ) {
		Button view = new Button(context);
		parentView.addView(view);
		LayoutUtils.setLayoutParams(parentView,view,width,height,x,y,right,bottom);
		return view;
	}

	public static LinearLayout LinearMaker(Context context, ViewGroup parentView, int width, int height) {
		return LinearMaker(context,parentView,width,height,0,0,0,0);
	}
	
	public static LinearLayout LinearMaker(Context context, ViewGroup parentView, int width, int height, int x, int y) {
		return LinearMaker(context,parentView,width,height,x,y,0,0);
	}
	
	public static LinearLayout LinearMaker(Context context, ViewGroup parentView, int width, int height, int x, int y, int right, int bottom ) {
		LinearLayout view = new LinearLayout(context);
		parentView.addView(view);
		LayoutUtils.setLayoutParams(parentView,view,width,height,x,y,right,bottom);
		return view;
	}
	
	public static RelativeLayout RelativeMaker(Context context, ViewGroup parentView, int width, int height) {
		return RelativeMaker(context,parentView,width,height,0,0,0,0);
	}
	
	public static RelativeLayout RelativeMaker(Context context, ViewGroup parentView, int width, int height, int x, int y) {
		return RelativeMaker(context,parentView,width,height,x,y,0,0);
	}
	
	public static RelativeLayout RelativeMaker(Context context, ViewGroup parentView, int width, int height, int x, int y, int right, int bottom ) {
		RelativeLayout view = new RelativeLayout(context);
		parentView.addView(view);
		LayoutUtils.setLayoutParams(parentView,view,width,height,x,y,right,bottom);
		return view;
	}
	
	public static FrameLayout FrameMaker(Context context, ViewGroup parentView, int width, int height) {
		return FrameMaker(context,parentView,width,height,0,0,0,0);
	}
	
	public static FrameLayout FrameMaker(Context context, ViewGroup parentView, int width, int height, int x, int y) {
		return FrameMaker(context,parentView,width,height,x,y,0,0);
	}
	
	public static FrameLayout FrameMaker(Context context, ViewGroup parentView, int width, int height, int x, int y, int right, int bottom ) {
		FrameLayout view = new FrameLayout(context);
		parentView.addView(view);
		LayoutUtils.setLayoutParams(parentView,view,width,height,x,y,right,bottom);
		return view;
	}
}
