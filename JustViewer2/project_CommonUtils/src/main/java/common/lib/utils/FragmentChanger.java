package common.lib.utils;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import common.lib.R;

public class FragmentChanger {
	private static final String TAG = "FragmentChanger";
	
	static public void changeWithSlideAnimation(FragmentManager manager, int targetFragmentId, Fragment fragment, String backStack) {
		Log.e(TAG,"KDS3393_TEST_change = " + manager.findFragmentById(targetFragmentId));
		
		if (manager.findFragmentById(targetFragmentId) != null && manager.findFragmentById(targetFragmentId).getClass() == fragment.getClass()) {
			return;
		}

		FragmentTransaction fragmentTransaction = manager.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_down,R.anim.slide_in_up, R.anim.slide_out_down);
		if (backStack == null) {
			manager.popBackStack();
			fragmentTransaction.replace(targetFragmentId, fragment);
		} else {
			fragmentTransaction.add(targetFragmentId, fragment);
			fragmentTransaction.addToBackStack(backStack);
		}
		
		fragmentTransaction.commitAllowingStateLoss();
	}
	
	static public void changeWithFadeAnimation(FragmentManager manager, int targetFragmentId, Fragment fragment, String backStack) {
		
		if (manager.findFragmentById(targetFragmentId) != null && manager.findFragmentById(targetFragmentId).getClass() == fragment.getClass()) {
			return;
		}
		
		FragmentTransaction fragmentTransaction = manager.beginTransaction();
		fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out); 
		
		if (backStack == null) {
			manager.popBackStack();
			fragmentTransaction.replace(targetFragmentId, fragment);
		} else {
			fragmentTransaction.add(targetFragmentId, fragment);
			fragmentTransaction.addToBackStack(backStack);
		}
		
		fragmentTransaction.commitAllowingStateLoss();
	}
	
	static public void change(FragmentManager manager, int targetFragmentId, Fragment fragment, String backStack) {
		if (manager.findFragmentById(targetFragmentId) != null && manager.findFragmentById(targetFragmentId).getClass().equals(fragment.getClass())) {
			return;
		}
		
		FragmentTransaction fragmentTransaction = manager.beginTransaction();
		if (backStack == null) {
			manager.popBackStack();
			fragmentTransaction.replace(targetFragmentId, fragment);
		} else {
			fragmentTransaction.add(targetFragmentId, fragment);
			fragmentTransaction.addToBackStack(backStack);
		}
		
		fragmentTransaction.commitAllowingStateLoss();
	}
	
	static public void forceChange(FragmentManager manager, int targetFragmentId, Fragment fragment, String backStack) {
		FragmentTransaction fragmentTransaction = manager.beginTransaction();
		if (backStack == null) {
			manager.popBackStack();
			fragmentTransaction.replace(targetFragmentId, fragment);
		} else {
			fragmentTransaction.add(targetFragmentId, fragment);
			fragmentTransaction.addToBackStack(backStack);
		}

		fragmentTransaction.commitAllowingStateLoss();
	}
}
