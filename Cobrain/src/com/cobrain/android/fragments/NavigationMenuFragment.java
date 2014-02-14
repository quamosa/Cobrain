package com.cobrain.android.fragments;

import java.util.ArrayList;

import com.cobrain.android.R;
import com.cobrain.android.adapters.NavigationMenuAdapter;
import com.cobrain.android.adapters.NavigationMenuItem;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.LoaderUtils;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NavigationMenuFragment extends BaseCobrainFragment implements OnItemClickListener {

	public static final String TAG = "NavigationMenuFragment";
	private Button signoutButton;
	private ListView menu;
	private ListView menuBottom;
	private ArrayAdapter<NavigationMenuItem> menuAdapter;
	private boolean loggingOut;
	private TextView username;
	private ImageView useravatar;
	ColorDrawable color = new ColorDrawable();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_navigation_menu, null);
		menu = (ListView) v.findViewById(R.id.navigation_menu_listview_top);
		menuBottom = (ListView) v.findViewById(R.id.navigation_menu_listview_bottom);
		username = (TextView) v.findViewById(R.id.user_name);
		useravatar = (ImageView) v.findViewById(R.id.user_avatar);
		setupNavigationMenu(menu);
		setupBottomNavigationMenu(menuBottom);
		
		signoutButton = (Button) v.findViewById(R.id.logout_button);		
		signoutButton.setOnClickListener(this);
		
		color.setColor(getResources().getColor(R.color.FeedsColor));

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
		update();
		super.onActivityCreated(savedInstanceState);
	}


	public void update() {
		UserInfo ui = controller.getCobrain().getUserInfo();
		username.setText(ui.getName());
		useravatar.setImageDrawable(color);
		ImageLoader.load(ui.getAvatarUrl(), useravatar, listener) ;
	}

	OnImageLoadListener listener = new OnImageLoadListener() {

		@Override
		public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
			return b;
		}

		@Override
		public void onLoad(String url, ImageView view, Bitmap b,
				boolean fromCache) {
			LoaderUtils.show(view, !fromCache);
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
			controller.showHome();
			break;
		case 1: //teach
			controller.showTeachMyCobrain(false);
			break;
		case 2: //saved
			controller.showSavedAndShare();
			break;
		case 3: //my shared craves
			//TODO: do I need to refresh the cache first?
			controller.showWishList(controller.getCobrain().getUserInfo(), false, null, false);
			controller.closeMenu(true);
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
	

}
