package com.cobrain.android.fragments;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.controllers.Cobrain.CobrainView;
import com.cobrain.android.utils.LoaderUtils;

public class BaseCobrainFragment extends SherlockFragment implements OnClickListener, CobrainView {
	public CobrainController controller;
	public LoaderUtils loaderUtils = new LoaderUtils();
	ActionBar actionBar;
	View abHide;
	boolean updateRequested;
	StateSaver state = new StateSaver();
	boolean silentMode;

	public BaseCobrainFragment() {}

	public class StateSaver {
		Bundle savedState = new Bundle();
		
		public int getInt(String key, int def) {
			if (!savedState.containsKey(key)) return def;
			int i = savedState.getInt(key);
			savedState.remove(key);
			return i;
		}

		public String getString(String key, String def) {
			if (!savedState.containsKey(key)) return def;
			String i = savedState.getString(key);
			savedState.remove(key);
			return i;
		}
		
		public boolean getBoolean(String key, boolean def) {
			if (!savedState.containsKey(key)) return def;
			boolean i = savedState.getBoolean(key);
			savedState.remove(key);
			return i;
		}

		public void putInt(String key, int val) {
			savedState.putInt(key, val);
		}

		public void putString(String key, String val) {
			savedState.putString(key, val);
		}
		
		public void putBoolean(String key, boolean val) {
			savedState.putBoolean(key, val);
		}

		public Bundle getBundle() {
			return savedState;
		}

		public void restore(ListView list, String key) {
			int index = getInt(key + ".index", 0);
			int top = getInt(key + ".top", 0);
			if (list != null) list.setSelectionFromTop(index, top);
		}
		public void save(ListView list, String key) {
			int index = list.getFirstVisiblePosition();
			View v = list.getChildAt(0);
			int top = (v == null) ? 0 : v.getTop();
			putInt(key + ".index", index);
			putInt(key + ".top", top);
		}
	}

	public void requestUpdate() {
		updateRequested = true;
	}
	public boolean checkForUpdate() {
		if (updateRequested) {
			updateRequested = false;
			update();
			return true;
		}
		return false;
	}
	public void update() {
	}
	
	@Override
	public void onAttach(Activity activity) {
		controller = (CobrainController) activity;
		controller.showOptionsMenu(true);
		actionBar = controller.getSupportActionBar();
		abHide = new View(activity.getApplicationContext());
		controller.dispatchOnFragmentAttached(this);
		super.onAttach(activity);
	}

	@Override
	public void onError(String message) {
		if (loaderUtils != null) loaderUtils.dismiss();
		if (!silentMode) if (controller != null) controller.showErrorDialog(message);
	}
	
	public LoaderUtils getLoaderUtils() {
		return loaderUtils;
	}
	
	@Override
	public void setTitle(CharSequence title) {
		controller.setTitle(title);
	}
	@Override
	public void setSubTitle(CharSequence title) {
		controller.setSubTitle(title);
	}
	@Override
	public CobrainController getCobrainController() {
		return controller;
	}
	
	void hideActionBar() {
		//wasActionBarShown = actionBar.isShowing();
/*		actionBar.setCustomView(abHide);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		controller.showOptionsMenu(false);
*/
		actionBar.hide();
	}
	
	void restoreActionBar() {
		//actionBar.setDisplayShowCustomEnabled(false);
		//if (wasActionBarShown) actionBar.show();
	}
	
	@Override
	public void onDetach() {
		if (actionBar.getCustomView() == abHide) actionBar.setCustomView(null);
		controller.dispatchOnFragmentDetached(this);
		abHide = null;
		controller = null;
		actionBar = null;
		super.onDetach();
	}

	@Override
	public void onClick(View v) {
//		switch(v.getId()) {
//		case R.id.navigation_button:
//			controller.showNavigationMenu();
//			break;
//		case R.id.filter_button:
//			controller.showFriendsMenu();
//			break;
//		}
	}

	@Override
	public void onSlidingMenuOpened() {
	}

	@Override
	public void onSlidingMenuClosed() {
	}
	
	@Override
	public void setSilentMode(boolean silent) {
		silentMode = silent;
	}

	public void onFragmentDetached(BaseCobrainFragment f) {}
	public void onFragmentAttached(BaseCobrainFragment f) {}
	
	public void dispatchOnFragmentDetached(BaseCobrainFragment f) {
		onFragmentDetached(f);
		List<Fragment> fragments = getChildFragmentManager().getFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				if (f != fragment && fragment instanceof BaseCobrainFragment) {
					BaseCobrainFragment bf = (BaseCobrainFragment)fragment;
					bf.dispatchOnFragmentDetached(f);
				}
			}
		}
	}
	public void dispatchOnFragmentAttached(BaseCobrainFragment f) {
		onFragmentAttached(f);
		List<Fragment> fragments = getChildFragmentManager().getFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				if (f != fragment && fragment instanceof BaseCobrainFragment) {
					BaseCobrainFragment bf = (BaseCobrainFragment)fragment;
					bf.dispatchOnFragmentAttached(f);
				}
			}
		}
	}

}
