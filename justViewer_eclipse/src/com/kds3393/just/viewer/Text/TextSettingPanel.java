package com.kds3393.just.viewer.Text;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.utils.ImageUtils;
import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.Config.SettingTextViewer;

public class TextSettingPanel extends LinearLayout {
	private static final String TAG = "TextSettingPanel";
	
    private OnChangeColorListener mOnChangeColorListener = null;
    public interface OnChangeColorListener {
    	public void onChangeColor(int backColor);
    }
	public void setOnChangeColorListener(OnChangeColorListener listener) {
		mOnChangeColorListener = listener;
	}
	
	public static TextSettingPanel make(Context context, ViewGroup parent,int width, int height) {
		return make(context,parent,width,height,0,0,0,0);
	}
	
	public static TextSettingPanel make(Context context, ViewGroup parent,int width, int height, int left, int top) {
		return make(context,parent,width,height,left,top,0,0);
	}
	
	public static TextSettingPanel make(Context context, ViewGroup parent,int width, int height, int left, int top, int right, int bottom) {
		TextSettingPanel view = new TextSettingPanel(context,width, height);
		parent.addView(view);
		LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom);
		return view;
	}
	
	private TextSettingInterface mTextViewInterface;
	
	private TextSettingPanel(Context context,int w, int h) {
		super(context);
		SettingTextViewer.initTextFont(context);
		setBackground(ImageUtils.makeRoundDrawable(125,30,Color.GRAY));
		setClickable(true);
		setOrientation(LinearLayout.VERTICAL);
		this.setGravity(Gravity.CENTER_VERTICAL);
		createPanel();
	}
	
	public void setTextView(TextSettingInterface view) {
		mTextViewInterface = view;
	}
	
	
	@Override
	public void setVisibility(int visibility) {
		if (visibility == View.VISIBLE) {
			setColorButtonImage(SettingTextViewer.getTextColor(getContext()));
			setSizeButtonImage(SettingTextViewer.getTextSize(getContext()));
			setGapButtonImage(SettingTextViewer.getTextGap(getContext()));
		}
		super.setVisibility(visibility);
	}
	
	TextButton mTextColor[] = new TextButton[5];
	TextButton mTextSize[] = new TextButton[5];
	TextButton mTextGap[] = new TextButton[3];
	TextButton mTextFont[] = new TextButton[4];
	private void createPanel() {
		inflate(getContext(), R.layout.v_text_setting, this);
		LinearLayout layout = (LinearLayout) findViewById(R.id.txt_setting_color);
		int btnSize = (int) getResources().getDimension(R.dimen.textviewer_btn_size);
		int btnLeft = (int) getResources().getDimension(R.dimen.textviewer_btn_l);
		for (int i=0;i<5;i++) {
			mTextColor[i] = new TextButton(getContext(),"가");
			layout.addView(mTextColor[i]);
			LayoutUtils.setLinearLayoutParams(mTextColor[i], btnSize, btnSize, btnLeft, 0);
			mTextColor[i].setColor(SettingTextViewer.sColors[i][0],SettingTextViewer.sColors[i][1]);
			mTextColor[i].setTag(i);
			mTextColor[i].setOnClickListener(mColorChangeListener);
		}
		setColorButtonImage(SettingTextViewer.getTextColor(getContext()));
		
		layout = (LinearLayout) findViewById(R.id.txt_setting_size);
		for (int i=0;i<5;i++) {
			mTextSize[i] = new TextButton(getContext(),"가");
			layout.addView(mTextSize[i]);
			LayoutUtils.setLinearLayoutParams(mTextSize[i], btnSize, btnSize, btnLeft, 0);
			mTextSize[i].setColor(Color.BLACK,Color.WHITE);
			mTextSize[i].setTextSize(SettingTextViewer.sSizes[i]);
			mTextSize[i].setTag(i);
			mTextSize[i].setOnClickListener(mSizeChangeListener);
		}
		setSizeButtonImage(SettingTextViewer.getTextSize(getContext()));
		
		layout = (LinearLayout) findViewById(R.id.txt_setting_gap);
		for (int i=0;i<3;i++) {
			mTextGap[i] = new TextButton(getContext(),SettingTextViewer.sGapsStr[i]);
			layout.addView(mTextGap[i]);
			LayoutUtils.setLinearLayoutParams(mTextGap[i], btnSize, btnSize, btnLeft, 0);
			mTextGap[i].setColor(Color.BLACK,Color.WHITE);
			mTextGap[i].setTextSize(15);
			mTextGap[i].setTag(i);
			mTextGap[i].setOnClickListener(mGapChangeListener);
		}
		setGapButtonImage(SettingTextViewer.getTextGap(getContext()));
		
		layout = (LinearLayout) findViewById(R.id.txt_setting_font1);
		for (int i=0;i<2;i++) {
			mTextFont[i] = new TextButton(getContext(),SettingTextViewer.sFontName[i]);
			layout.addView(mTextFont[i]);
			LayoutUtils.setLinearLayoutParams(mTextFont[i], btnSize * 3, btnSize, btnLeft, 0);
			mTextFont[i].setColor(Color.BLACK,Color.WHITE);
			mTextFont[i].setTextSize(15);
			mTextFont[i].setTag(i);
			mTextFont[i].setOnClickListener(mFontChangeListener);
			mTextFont[i].setTypeface(SettingTextViewer.sFonts.get(i));
		}
			
		LinearLayout layout2 = (LinearLayout) findViewById(R.id.txt_setting_font2);
		for (int i=2;i<4;i++) {
			mTextFont[i] = new TextButton(getContext(),SettingTextViewer.sFontName[i]);
			LayoutUtils.setLinearLayoutParams(mTextFont[i], btnSize * 3, btnSize, btnLeft, 0);
			layout2.addView(mTextFont[i]);
			mTextFont[i].setColor(Color.BLACK,Color.WHITE);
			mTextFont[i].setTextSize(15);
			mTextFont[i].setTag(i);
			mTextFont[i].setOnClickListener(mFontChangeListener);
			mTextFont[i].setTypeface(SettingTextViewer.sFonts.get(i));
		}
		setFontButtonImage(SettingTextViewer.getTextFont(getContext()));
	}
	
	private OnClickListener mColorChangeListener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			int index = (Integer) view.getTag();
			setColorButtonImage(index);

			if (mOnChangeColorListener != null)
				mOnChangeColorListener.onChangeColor(SettingTextViewer.sColors[index][0]);
			if (mTextViewInterface != null)
				mTextViewInterface.setTextColor(index);
			TextSettingPanel.this.invalidate();
			SettingTextViewer.setTextColor(getContext(),index);
		}
	};
	
	private OnClickListener mSizeChangeListener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			int index = (Integer) view.getTag();
			setSizeButtonImage(index);
			
			if (mTextViewInterface != null)
				mTextViewInterface.setTextSize(index);
			TextSettingPanel.this.invalidate();
			SettingTextViewer.setTextSize(getContext(),index);
		}
	};
	
	private OnClickListener mGapChangeListener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			int index = (Integer) view.getTag();
			setGapButtonImage(index);
			if (mTextViewInterface != null)
				mTextViewInterface.setLineSpacing(index);
			TextSettingPanel.this.invalidate();
			
			SettingTextViewer.setTextGap(getContext(),index);
		}
	};
	
	private OnClickListener mFontChangeListener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			int index = (Integer) view.getTag();
			setFontButtonImage(index);
			if (mTextViewInterface != null)
				mTextViewInterface.setTextFont(index);
			TextSettingPanel.this.invalidate();
			
			SettingTextViewer.setTextFont(getContext(),index);
		}
	};
	private void setColorButtonImage(int index) {
		for (int i=0;i<5;i++) {
			if ((Integer)mTextColor[i].getTag() != index) {
				mTextColor[i].setTextBackgroundColor(SettingTextViewer.sColors[i][0]);
				mTextColor[i].setSelectDrawable(false);
			}
		}
		mTextColor[index].setSelectDrawable(true);
	}
	
	private void setSizeButtonImage(int index) {
		for (int i=0;i<5;i++) {
			if ((Integer)mTextSize[i].getTag() != index) {
				mTextSize[i].setTextBackgroundColor(Color.BLACK);
				mTextSize[i].setSelectDrawable(false);
			}
		}
		mTextSize[index].setSelectDrawable(true);
	}
	
	private void setGapButtonImage(int index) {
		for (int i=0;i<3;i++) {
			if ((Integer)mTextGap[i].getTag() != index) {
				mTextGap[i].setTextBackgroundColor(Color.BLACK);
				mTextGap[i].setSelectDrawable(false);
			}
		}
		mTextGap[index].setSelectDrawable(true);
	}
	
	private void setFontButtonImage(int index) {
		int count = SettingTextViewer.sFonts.size();
		for (int i=0;i<count;i++) {
			if ((Integer)mTextFont[i].getTag() != index) {
				mTextFont[i].setTextBackgroundColor(Color.BLACK);
				mTextFont[i].setSelectDrawable(false);
			}
		}
		mTextFont[index].setSelectDrawable(true);
	}

	private class TextButton extends RelativeLayout {
		private TextView mTextView;
		
		public TextButton(Context context, String text) {
			super(context);
			
			ShapeDrawable outline = new ShapeDrawable(new RectShape());
			outline.getPaint().setColor(Color.BLACK);
			//outline.getPaint().setColor(Color.RED);
			outline.getPaint().setStrokeWidth(9);
			outline.getPaint().setStyle(Style.STROKE);
//			outline.getPaint().setAntiAlias(true);
			
			this.setBackground(outline);
			
			mTextView = ViewMaker.TextViewMaker(getContext(), this, text, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 7, 7, 7, 7);
			mTextView.setGravity(Gravity.CENTER);
		}
		
		public void setColor(int back, int text) {
			mTextView.setBackgroundColor(back);
			mTextView.setTextColor(text);
		}
		
		public void setTextBackgroundColor(int back) {
			mTextView.setBackgroundColor(back);
		}
		
		public void setSelectDrawable(boolean isSelected) {
			if (isSelected)
				((ShapeDrawable)getBackground()).getPaint().setColor(Color.RED);
			else
				((ShapeDrawable)getBackground()).getPaint().setColor(Color.BLACK);
		}
		
		public void setTextSize(int size) {
			mTextView.setTextSize(size);
		}
		
		public void setTypeface(Typeface tf) {
			mTextView.setTypeface(tf);
		}
	}
}
