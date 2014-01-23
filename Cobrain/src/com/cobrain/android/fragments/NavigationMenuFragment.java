package com.cobrain.android.fragments;

import java.util.ArrayList;

import com.cobrain.android.R;
import com.cobrain.android.adapters.NavigationMenuAdapter;
import com.cobrain.android.adapters.NavigationMenuItem;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class NavigationMenuFragment extends BaseCobrainFragment implements OnItemClickListener {

	public static final String TAG = "NavigationMenuFragment";
	private Button signoutButton;
	private ListView menu;
	private ListView menuBottom;
	private ArrayAdapter<NavigationMenuItem> menuAdapter;
	private boolean loggingOut;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.navigation_menu, null);
		menu = (ListView) v.findViewById(R.id.navigation_menu_listview_top);
		menuBottom = (ListView) v.findViewById(R.id.navigation_menu_listview_bottom);
		setupNavigationMenu(menu);
		setupBottomNavigationMenu(menuBottom);
		
		signoutButton = (Button) v.findViewById(R.id.logout_button);		
		signoutButton.setOnClickListener(this);
		
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
			controller.showTeachMyCobrain();
			break;
		case 2: //saved
			controller.showSavedAndShare();
			break;
		case 3: //my shared craves
			//TODO: do I need to refresh the cache first?
			controller.showWishList(controller.getCobrain().getUserInfo().getCachedWishList(), false, false);
			controller.closeMenu(true);
			break;
		case 4: //nerve center
			controller.showNerveCenter();
			break;
		}

		controller.setMenuItemSelected((ListView)arg0, position, true);
	
	}
	

}
