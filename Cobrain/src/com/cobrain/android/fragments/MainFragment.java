package com.cobrain.android.fragments;

import com.cobrain.android.R;
import com.cobrain.android.service.Cobrain.CobrainController;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment extends BaseCobrainFragment {

	public static final String TAG = "MainFragment";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.content_frame, null);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (savedInstanceState == null) {
			getFragmentManager()
			.beginTransaction()
			.replace(R.id.menu_frame, new NavigationMenuFragment(), NavigationMenuFragment.TAG)
			.commitAllowingStateLoss();

			/*getFragmentManager()
			.beginTransaction()
			.replace(R.id.content_frame, new CravesFragment())
			.commit();*/

			getFragmentManager()
			.beginTransaction()
			.replace(R.id.menu_frame_two, new FriendsListFragment(), FriendsListFragment.TAG)
			.commitAllowingStateLoss();

		}
		
		actionBar.show();

		int defaultView = getArguments().getInt("defaultView");
		
		//show default cobrain view
		switch(defaultView) {
		case CobrainController.VIEW_TEACH:
			controller.showTeachMyCobrain();
			break;
		case CobrainController.VIEW_FRIENDS_MENU:
			controller.showHome();
			controller.showFriendsMenu();
			break;
		default:
			controller.showHome();
		}

		super.onActivityCreated(savedInstanceState);
	}

	/*public void setAboveWidthRes(SlidingMenu sm, int i) {
		int width;
		Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay();
		try {
			Class<?> cls = Display.class;
			Class<?>[] parameterTypes = {Point.class};
			Point parameter = new Point();
			Method method = cls.getMethod("getSize", parameterTypes);
			method.invoke(display, parameter);
			width = parameter.x;
		} catch (Exception e) {
			width = display.getWidth();
		}
		sm.setAboveOffset(width-i);
	}*/
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	public void showNavigationMenu() {
		SlidingMenu sm = controller.getSlidingMenu();
		if (sm.isMenuShowing()) {
			sm.showContent();
		}
		else sm.showMenu(true);
	}

	public FriendsListFragment showFriendsMenu() {
		SlidingMenu sm = controller.getSlidingMenu();
		if (sm.isSecondaryMenuShowing()) {
		//	sm.showContent();
		}
		else {
			sm.showSecondaryMenu(true);
		}

		FriendsListFragment f = (FriendsListFragment) getFragmentManager().findFragmentByTag(FriendsListFragment.TAG);
		//if (f != null) f.update();
		
		return f;
	}

}
