package com.kds3393.just.viewer;

import com.common.app.CApp;
import com.common.utils.debug.CLog;

public class JustViewerApp extends CApp {
	private static final String TAG = "JustViewerApp";

	@Override
	protected void init() {
		CLog.SHOW_LOG = true;
		super.init();
	}
	
}