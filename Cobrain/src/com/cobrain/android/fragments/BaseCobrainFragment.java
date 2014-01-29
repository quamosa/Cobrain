package com.cobrain.android.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.cobrain.android.service.Cobrain.CobrainController;
import com.cobrain.android.service.Cobrain.CobrainView;
import com.cobrain.android.utils.LoaderUtils;
import com.fortysevendeg.swipelistview.SwipeListView;

public class BaseCobrainFragment extends SherlockFragment implements OnClickListener, CobrainView {
	CobrainController controller;
	LoaderUtils loaderUtils = new LoaderUtils();
	ActionBar actionBar;
	View abHide;
	boolean updateRequested;
	StateSaver state = new StateSaver();
	boolean silentMode;
	
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
		actionBar = controller.getSupportActionBar();
		abHide = new View(activity.getApplicationContext());
		super.onAttach(activity);
	}

	@Override
	public void onError(String message) {
		if (loaderUtils != null) loaderUtils.dismiss();
		if (!silentMode) if (controller != null) controller.showErrorDialog(message);
	}

	public void setTitle(CharSequence title) {
		actionBar.setTitle(title);
	}
	public CharSequence getTitle() {
		return actionBar.getTitle();
	}
	
	public LoaderUtils getLoaderUtils() {
		return loaderUtils;
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

}
