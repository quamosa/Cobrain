package com.cobrain.android.fragments;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListFragment;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.controllers.Cobrain.CobrainView;
import com.cobrain.android.utils.LoaderUtils;

public class BaseCobrainListFragment extends SherlockListFragment implements OnClickListener, CobrainView {
	CobrainController controller;
	LoaderUtils loaderUtils = new LoaderUtils();
	ActionBar actionBar;
	View abHide;
	boolean silentMode;
	
	@Override
	public void onAttach(Activity activity) {
		controller = (CobrainController) activity;
		controller.showOptionsMenu(true);
		actionBar = controller.getSupportActionBar();
		abHide = new View(activity.getApplicationContext());
		super.onAttach(activity);
	}

	@Override
	public void onError(String message) {
		if (loaderUtils != null) loaderUtils.dismiss();
		if (controller != null) controller.showErrorDialog(message);
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

	@Override
	public boolean getSilentMode() {
		return silentMode;
	}

	public void setSubTitle(CharSequence title) {
	}

	public CobrainController getCobrainController() {
		return null;
	}

}
