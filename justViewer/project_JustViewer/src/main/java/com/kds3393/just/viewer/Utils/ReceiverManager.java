package com.kds3393.just.viewer.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class ReceiverManager extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String str = intent.getAction();
		checkBattery(intent);
		if (mOnUserPresentListener != null && str.equals(Intent.ACTION_USER_PRESENT)) {
			mOnUserPresentListener.onUserPresent();
		}
	}
	
	
	public interface OnBatteryChangeListener {
		public void onReceiveBattery(int plugType, int level, int scale);
	}
	
	private OnBatteryChangeListener mOnBatteryChangeListener = null;
	public void setOnBatteryChangeListener(Context context, OnBatteryChangeListener listener) {
		if (mOnBatteryChangeListener == null) {
			context.registerReceiver(this, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
			mOnBatteryChangeListener = listener;
		}
	}
	
	public interface OnUserPresentListener {
		public void onUserPresent();
	}
	
	private OnUserPresentListener mOnUserPresentListener = null;
	public void setOnUserPresentListener(Context context, OnUserPresentListener listener) {
		if (mOnUserPresentListener != null) {
			context.registerReceiver(this, new IntentFilter(Intent.ACTION_USER_PRESENT));
			mOnUserPresentListener = listener;
		}
	}
	
	public void unRegister(Context context) {
		if (mOnBatteryChangeListener != null || 
				mOnUserPresentListener != null)
			context.unregisterReceiver(this);
			
		mOnBatteryChangeListener = null;
		mOnUserPresentListener = null;
	}
	
	public boolean checkBattery(Intent intent) {
		if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
//			0 : Unplugged
//			BatteryManager.BATTERY_PLUGGED_AC
//			BatteryManager.BATTERY_PLUGGED_USB
//			BatteryManager.BATTERY_PLUGGED_AC|BatteryManager.BATTERY_PLUGGED_USB
			int plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
			mOnBatteryChangeListener.onReceiveBattery(plugType, level, scale);
			
/*
//			BatteryManager.BATTERY_HEALTH_GOOD
//			BatteryManager.BATTERY_HEALTH_OVERHEAT
//			BatteryManager.BATTERY_HEALTH_DEAD
//			BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE
//			BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE
			int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
			
//			BatteryManager.BATTERY_STATUS_CHARGING
//			BatteryManager.BATTERY_STATUS_DISCHARGING
//			BatteryManager.BATTERY_STATUS_NOT_CHARGING
//			BatteryManager.BATTERY_STATUS_FULL
//			BatteryManager.BATTERY_STATUS_UNKNOWN
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
			
			int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
			int temper = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
*/
			return true;
		}
		return false;
	}
	
	/**
	 * @brief 배터리
	 */
}
