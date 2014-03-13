package com.cobrain.android.fragments;

import java.util.ArrayList;

import com.cobrain.android.MainActivity;
import com.cobrain.android.R;
import com.cobrain.android.adapters.NavigationMenuAdapter;
import com.cobrain.android.adapters.NavigationMenuItem;
import com.cobrain.android.controllers.Cobrain.CobrainMenuView;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.Badge;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.UserInfo.OnUserInfoChanged;
import com.cobrain.android.utils.LoaderUtils;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Debug;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NavigationMenuFragment extends BaseCobrainFragment implements CobrainMenuView, OnItemClickListener, OnUserInfoChanged {

	public static final String TAG = "NavigationMenuFragment";
	private Button signoutButton;
	private ListView menu;
	private ListView menuBottom;
	private ArrayAdapter<NavigationMenuItem> menuAdapter;
	private boolean loggingOut;
	private TextView username;
	private ImageView useravatar;
	ColorDrawable color = new ColorDrawable();
	private ImageView userbadge;
	ImageLoader avatarLoader;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_navigation_menu, null);
		menu = (ListView) v.findViewById(R.id.navigation_menu_listview_top);
		menuBottom = (ListView) v.findViewById(R.id.navigation_menu_listview_bottom);
		username = (TextView) v.findViewById(R.id.user_name);
		useravatar = (ImageView) v.findViewById(R.id.user_avatar);
		userbadge = (ImageView) v.findViewById(R.id.user_badge);
		setupNavigationMenu(menu);
		setupBottomNavigationMenu(menuBottom);
		
		signoutButton = (Button) v.findViewById(R.id.logout_button);		
		signoutButton.setOnClickListener(this);
		
		color.setColor(getResources().getColor(R.color.FeedsColor));
		
		int sz = (int) getActivity().getResources().getDimension(R.dimen.avatar_size);
		avatarLoader = new ImageLoader("avatar", 4*sz*sz);
		
		return v;
	}
	
	void setupNavigationMenu(final ListView menu) {
		ArrayList<NavigationMenuItem> menuItems = new ArrayList<NavigationMenuItem>();
		Resources res = getActivity().getApplicationContext().getResources();
		TypedArray items = res.obtainTypedArray(R.array.navigation_menu_items);

		for (int i = 0; i < items.length();) {
			NavigationMenuItem nmi = new NavigationMenuItem();
			nmi.caption = items.getString(i++);
			nmi.icon = items.getDrawable(i++);
			nmi.id = items.getInt(i++, 0);
			menuItems.add(nmi);
		}

		menuAdapter = new NavigationMenuAdapter(getActivity().getApplicationContext(), menuItems);
		menu.setAdapter(menuAdapter);
		menu.setOnItemClickListener(this);
	}

	void setupBottomNavigationMenu(final ListView menu) {
		ArrayList<NavigationMenuItem> menuItems = new ArrayList<NavigationMenuItem>();
		Resources res = getActivity().getApplicationContext().getResources();
		TypedArray items = res.obtainTypedArray(R.array.navigation_bottom_menu_items);

		for (int i = 0; i < items.length();) {
			NavigationMenuItem nmi = new NavigationMenuItem();
			nmi.caption = items.getString(i++);
			nmi.icon = items.getDrawable(i++);
			nmi.id = items.getInt(i++, 0);
			menuItems.add(nmi);
		}

		menu.setAdapter(new NavigationMenuAdapter(getActivity().getApplicationContext(), menuItems));
		menu.setOnItemClickListener(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	Runnable updateRunnable = new Runnable() {
		public void run() {
			update();
		}
	};
	
	//this keep checking every 2 seconds
	void tryUpdateAgainLater() {
		Debug.waitForDebugger();
		View v = getView();
		if (v != null) 
			if (userbadge != null)
				v.postDelayed(updateRunnable, 2 * 1000);
	}

	public void update() {
		/*
		if (controller == null) {
			tryUpdateAgainLater();
			return;
		}
		*/
		
		if (controller == null) return; 
		UserInfo ui = controller.getCobrain().getUserInfo();
		String s = null;

		/*
		if (ui == null) {
			tryUpdateAgainLater();
			return;
		}
		*/
		
		if (ui != null) {
			ui.registerUserInfoChangedListener(this);
			if (ui.hasBadge(Badge.TASTEMAKER)) {
				s = String.format("%s<br><i><font color=\"#%x\">Tastemaker</font></i>", 
						ui.getName(),
						getActivity().getResources().getColor(R.color.Tastemaker) & 0xffffff);
				userbadge.setImageResource(R.drawable.ic_badge_tastemaker);
				userbadge.setVisibility(View.VISIBLE);
			}
			else
				if (ui.hasBadge(Badge.TRENDSETTER)) {
					s = String.format("%s<br><i><font color=\"#%x\">Trendsetter</font></i>", 
							ui.getName(),
							getActivity().getResources().getColor(R.color.Trendsetter) & 0xffffff);
					userbadge.setImageResource(R.drawable.ic_badge_trendsetter);
					userbadge.setVisibility(View.VISIBLE);
				}
				else {
					s = ui.getName();
					userbadge.setVisibility(View.GONE);
				}
			
			avatarLoader.load(ui.getAvatarUrl(), useravatar, listener) ;
		}

		if (username != null) {
			if (MainActivity.environment != null && !MainActivity.environment.equals("PROD")) {
				Activity a = getActivity();
				PackageInfo pInfo;
				String appInfo = MainActivity.environment;
				
				try {
					pInfo = a.getPackageManager().getPackageInfo(a.getPackageName(), 0);
					appInfo += " " + pInfo.versionName + " (" + pInfo.versionCode + ")";
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				
				if (s == null) s = appInfo;
				else s += "<br>" + appInfo;
			}
			
			if (s != null) username.setText(Html.fromHtml(s));
		}
	}

	OnImageLoadListener listener = new OnImageLoadListener() {

		@Override
		public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
			return b;
		}

		@Override
		public void onLoad(String url, ImageView view, Bitmap b,
				int fromCache) {
			LoaderUtils.show(view, fromCache == ImageLoader.CACHE_NONE);
		}
		
	};
	
	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		case R.id.logout_button:
			if (!loggingOut) {
				loggingOut = true;
				controller.showProgressDialog("Please wait...", "Logging you out...");
				controller.getCobrain().logout();
			}
			break;
		}
	}

	@Override
	public void onDestroyView() {
		if (controller != null) {
			UserInfo ui = controller.getCobrain().getUserInfo();
			if (ui != null) ui.unregisterUserInfoChangedListener(this);
		}
		userbadge = null;
		username = null;
		useravatar = null;
		menu.setOnItemClickListener(null);
		menu.setAdapter(null);
		menu = null;
		NavigationMenuAdapter a = ((NavigationMenuAdapter)menuBottom.getAdapter());
		menuBottom.setOnItemClickListener(null);
		menuBottom.setAdapter(null);
		menuBottom = null;
		a.clear();
		menuAdapter.clear();
		menuAdapter = null;
		signoutButton = null;
		super.onDestroyView();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
		switch((int)id) {
		case 0: //home
			controller.showHome(HomeFragment.TAB_HOME_RACK, false);
			break;
		case 1: //teach
			controller.showTeachMyCobrain(false);
			break;
		case 2: //saved
			controller.showHome(HomeFragment.TAB_PRIVATE_RACK, false);
			//controller.showSavedAndShare();
			break;
		case 3: //my shared craves
			controller.showHome(HomeFragment.TAB_SHARED_RACK, false);
			//TODO: do I need to refresh the cache first?
			//controller.showWishList(controller.getCobrain().getUserInfo(), false, null, false);
			//controller.closeMenu(true);
			break;
		case 7: //on sale
			controller.showHome(HomeFragment.TAB_SALE_RACK, false);
			break;
		case 4: //nerve center
			controller.showNerveCenter();
			break;
		case 6: //about us
			controller.showBrowser(getString(R.string.url_about_us, getString(R.string.url_cobrain_app)), R.id.content_frame, "Cobrain", false);
			controller.closeMenu(true);
		}

		controller.setMenuItemSelected((ListView)arg0, position, true);
	
	}

	@Override
	public void onUserInfoChanged(UserInfo ui) {
		getActivity().runOnUiThread(updateRunnable);
	}

	@Override
	public int getMenuTypeCount() {
		return 2;
	}
	
	@Override
	public ListAdapter getAdapter(int type) {
		return menuAdapter;
	}

	@Override
	public ListView getMenu(int type) {
		switch(type) {
		case 1: return menuBottom;
		default: return menu;
		}
	}

	@Override
	public int getMenuItemPosition(int type, String id) {
		long lid = Long.valueOf(id);
		
		ArrayAdapter<NavigationMenuItem> a = null;
		
		switch (type) {
		case 0:
			a = menuAdapter;
			break;
		case 1:
			if (menuBottom != null) {
				a = (NavigationMenuAdapter) menuBottom.getAdapter();
			}
			break;
		}

		if (a != null) {
			for (int i = 0; i < a.getCount(); i++) {
				if (a.getItemId(i) == lid) {
					return i;
				}
			}
		}

		return -1;
	}

}
