package com.kds3393.just.viewer.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.kds3393.just.viewer.ParentActivity;
import com.kds3393.just.viewer.Config.HelpActivity;
import com.kds3393.just.viewer.Music.MusicService;
import com.kds3393.just.viewer.Music.MusicService.LocalBinder;
import com.kds3393.just.viewer.View.MainView;
import com.kds3393.just.viewer.provider.FavoriteData;

public class ActJustViewer extends ParentActivity {
	private static final String TAG = "MainActivity";
	
	private MainView mJustViewerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mJustViewerView = new MainView(this);
        setContentView(mJustViewerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        FavoriteData.OrganizeDB(getContentResolver());
    }
    
    @Override
	protected void onResume() {
    	mJustViewerView.onResume();
		super.onResume();
		HelpActivity.showHelp(this, HelpActivity.HELP_MAIN_SHOW);
	}
    
    @Override
	protected void onPause() {
    	super.onPause();
	}

	@Override
	protected void onStop() {
		mJustViewerView.onStop();
		
		unbindService(mConnection);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
    
    @Override
	public void onBackPressed() {
    	if (!mJustViewerView.onBackPressed())
    		return;
		
		super.onBackPressed();
	}
    
    
    // ----------------------------------- Music Service -------------------------------------
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mJustViewerView.setMusicService(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
}
