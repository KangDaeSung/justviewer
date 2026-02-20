package com.kds3393.just.viewer.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.utils.LayoutUtils;
import com.common.utils.ViewMaker;
import com.kds3393.just.viewer.R;

public class BatteryView extends LinearLayout {
	private static final String TAG = "BatteryView";
	
	public static BatteryView make(Context context, ViewGroup parent,int width, int height) {
		return make(context,parent,width,height,0,0,0,0);
	}
	
	public static BatteryView make(Context context, ViewGroup parent,int width, int height, int left, int top) {
		return make(context,parent,width,height,left,top,0,0);
	}
	
	public static BatteryView make(Context context, ViewGroup parent,int width, int height, int left, int top, int right, int bottom) {
		BatteryView view = new BatteryView(context);
		parent.addView(view);
		LayoutUtils.setLayoutParams(parent, view, width, height, left, top, right, bottom);
		return view;
	}
	
	private TextView mBatteryText;
	private ImageView mBatteryImage;
	public BatteryView(Context context) {
		super(context);
		init(context);
	}
	
	public BatteryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		inflate(context, R.layout.v_battery, this);
		setGravity(Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
		setOrientation(LinearLayout.HORIZONTAL);
		mBatteryText = (TextView) findViewById(R.id.battery_txt);
		mBatteryImage = (ImageView) findViewById(R.id.battery_img);
	}

	public void setBatteryInfo(int plugType, int level, int scale) {
		int lv = (int) ((float)level / (float)scale * 100);
		if (lv <= 25)
			mBatteryImage.setImageResource(R.drawable.i_battery_low);
		else if (lv <= 80)
			mBatteryImage.setImageResource(R.drawable.i_battery_half);
		else
			mBatteryImage.setImageResource(R.drawable.i_battery_full);
		mBatteryText.setText(lv + "%");
	}
}
