package com.kds3393.just.viewer.Text;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.common.utils.ImageUtils;
import com.kds3393.just.viewer.Config.SettingTextViewer;
import com.kds3393.just.viewer.R;
import com.kds3393.just.viewer.View.TextButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TextSettingPanel extends LinearLayout {
	private static final String TAG = "TextSettingPanel";

    @BindView(R.id.txt_setting_color) LinearLayout txt_setting_color;
    @BindView(R.id.txt_setting_color_00_btn) TextButton txt_setting_color_00_btn;
    @BindView(R.id.txt_setting_color_01_btn) TextButton txt_setting_color_01_btn;
    @BindView(R.id.txt_setting_color_02_btn) TextButton txt_setting_color_02_btn;
    @BindView(R.id.txt_setting_color_03_btn) TextButton txt_setting_color_03_btn;
    @BindView(R.id.txt_setting_color_04_btn) TextButton txt_setting_color_04_btn;
    @BindView(R.id.txt_setting_size) LinearLayout txt_setting_size;
    @BindView(R.id.txt_setting_size_00_btn) TextButton txt_setting_size_00_btn;
    @BindView(R.id.txt_setting_size_01_btn) TextButton txt_setting_size_01_btn;
    @BindView(R.id.txt_setting_size_02_btn) TextButton txt_setting_size_02_btn;
    @BindView(R.id.txt_setting_size_03_btn) TextButton txt_setting_size_03_btn;
    @BindView(R.id.txt_setting_size_04_btn) TextButton txt_setting_size_04_btn;
    @BindView(R.id.txt_setting_gap) LinearLayout txt_setting_gap;
    @BindView(R.id.txt_setting_gap_00_btn) TextButton txt_setting_gap_00_btn;
    @BindView(R.id.txt_setting_gap_01_btn) TextButton txt_setting_gap_01_btn;
    @BindView(R.id.txt_setting_gap_02_btn) TextButton txt_setting_gap_02_btn;
    @BindView(R.id.txt_setting_font1) LinearLayout txt_setting_font1;
    @BindView(R.id.txt_setting_font_00_btn) TextButton txt_setting_font_00_btn;
    @BindView(R.id.txt_setting_font_01_btn) TextButton txt_setting_font_01_btn;
    @BindView(R.id.txt_setting_font_02_btn) TextButton txt_setting_font_02_btn;
    @BindView(R.id.txt_setting_font_03_btn) TextButton txt_setting_font_03_btn;
    @BindView(R.id.txt_setting_font2) LinearLayout txt_setting_font2;

    private OnChangeColorListener mOnChangeColorListener = null;

    public interface OnChangeColorListener {
    	public void onChangeColor(int backColor);
    }
	public void setOnChangeColorListener(OnChangeColorListener listener) {
		mOnChangeColorListener = listener;
	}

	private TextSettingInterface mTextViewInterface;

    public TextSettingPanel(Context context) {
        super(context);
        init();
    }

    public TextSettingPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

	private void init() {
        SettingTextViewer.initTextFont(getContext());
        setBackground(ImageUtils.makeRoundDrawable(125,30,Color.GRAY));
        setClickable(true);
        setOrientation(LinearLayout.VERTICAL);

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
        ButterKnife.bind(this, this);

        mTextColor[0] = txt_setting_color_00_btn;
        mTextColor[1] = txt_setting_color_01_btn;
        mTextColor[2] = txt_setting_color_02_btn;
        mTextColor[3] = txt_setting_color_03_btn;
        mTextColor[4] = txt_setting_color_04_btn;

		for (int i=0;i<5;i++) {
            mTextColor[i].setText("가");
			mTextColor[i].setColor(SettingTextViewer.sColors[i][0],SettingTextViewer.sColors[i][1]);
			mTextColor[i].setTag(i);
			mTextColor[i].setOnClickListener(mColorChangeListener);
		}
		setColorButtonImage(SettingTextViewer.getTextColor(getContext()));

        mTextSize[0] = txt_setting_size_00_btn;
        mTextSize[1] = txt_setting_size_01_btn;
        mTextSize[2] = txt_setting_size_02_btn;
        mTextSize[3] = txt_setting_size_03_btn;
        mTextSize[4] = txt_setting_size_04_btn;
		for (int i=0;i<5;i++) {
            mTextSize[i].setText("가");
			mTextSize[i].setColor(Color.BLACK,Color.WHITE);
			mTextSize[i].setTextSize(SettingTextViewer.sSizes[i]);
			mTextSize[i].setTag(i);
			mTextSize[i].setOnClickListener(mSizeChangeListener);
		}
		setSizeButtonImage(SettingTextViewer.getTextSize(getContext()));

        mTextGap[0] = txt_setting_gap_00_btn;
        mTextGap[1] = txt_setting_gap_01_btn;
        mTextGap[2] = txt_setting_gap_02_btn;

		for (int i=0;i<3;i++) {
			mTextGap[i].setText(SettingTextViewer.sGapsStr[i]);
			mTextGap[i].setColor(Color.BLACK,Color.WHITE);
			mTextGap[i].setTextSize(15);
			mTextGap[i].setTag(i);
			mTextGap[i].setOnClickListener(mGapChangeListener);
		}
		setGapButtonImage(SettingTextViewer.getTextGap(getContext()));

        mTextFont[0] = txt_setting_font_00_btn;
        mTextFont[1] = txt_setting_font_01_btn;
        mTextFont[2] = txt_setting_font_02_btn;
        mTextFont[3] = txt_setting_font_03_btn;
		for (int i=0;i<4;i++) {
			mTextFont[i].setText(SettingTextViewer.sFontName[i]);
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
}
