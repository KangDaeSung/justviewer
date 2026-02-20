package com.kds3393.just.viewer.View;

import android.content.Context;
import android.widget.LinearLayout;

public class KLinearLayout extends LinearLayout {
	public boolean mIsRequestLayout = true;
	public KLinearLayout(Context context) {
		super(context);
		
	}
	@Override
	public void requestLayout() {
		if (mIsRequestLayout)
			super.requestLayout();
	}

}
