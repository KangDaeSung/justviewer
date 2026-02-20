package com.kds3393.just.viewer.Config;

import com.common.utils.LayoutUtils;
import com.common.utils.Size;
import com.common.utils.ViewMaker;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class HelpView extends RelativeLayout {
	private static final String TAG = "";
	
	public int HELP_TYPE_MAIN = 0;
	
	private int mHelpType = HELP_TYPE_MAIN;
	public HelpView(Context context, int type) {
		super(context);
		mHelpType = type;
		createView();
	}
	
	private ImageView mHelpView;
	private Button mPrevBtn;
	private Button mNextBtn;
	
	private void createView() {
		mHelpView = ViewMaker.ImageViewMaker(getContext(), this, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0);
		
		mPrevBtn = ViewMaker.ButtonMaker(getContext(), this, 100, 100, 20, 0);
		mPrevBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
			}
		});
		
		mNextBtn = ViewMaker.ButtonMaker(getContext(), this, 100, 100, 0, 0, 20, 0);
		LayoutUtils.setRelativeRule(mNextBtn, RelativeLayout.ALIGN_PARENT_RIGHT);
		mNextBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				
			}
		});
	}

}
