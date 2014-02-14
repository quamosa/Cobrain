package com.cobrain.android.fragments;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cobrain.android.R;
import com.cobrain.android.adapters.SkuPagerAdapter;
import com.cobrain.android.adapters.CravesCategoryAdapter;
import com.cobrain.android.adapters.NavigationMenuItem;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.loaders.CraveFilterLoader;
import com.cobrain.android.loaders.SkuStripsLoader;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.User;

public class FriendCraveStripsFragment extends CraveStripsFragment<Skus> {
	public static final String TAG = "FriendCraveStripsFragment";

	SkuPagerAdapter craveAdapter;
	CraveFilterLoader craveFilterLoader = new CraveFilterLoader();
	TextView craveFilterHeader;
	CravesCategoryAdapter cravesCategoryAdapter;
	ArrayAdapter<NavigationMenuItem> craveFilterAdapter;
	PopupWindow filterMenu = (android.os.Build.VERSION.SDK_INT > 10) ? new PopupWindow() : null;
	ListView filterMenuListView;
	//int categoryId;	
	//private int currentPage = 1;
	SavedState savedState = new SavedState();
	View comingsoon;
	Menu menu;
	SkuStripsLoader loader;
	User owner;
	List<Integer> skus;
	ListView craveStripList;

	public static FriendCraveStripsFragment newInstance(User owner, List<Integer> skus) {
		FriendCraveStripsFragment f = new FriendCraveStripsFragment();
		f.owner = owner;
		f.skus = skus;
		return f;
	}
	

	public class SavedState {
		boolean saved;
		int selectedCategoryNavigationPosition;
		int categoryId;
		int position;
		int page;

		public void save() {
			saved = true;
		}
		public boolean isRestored() {
			return !saved;
		}
		public boolean isSaved() {
			return saved;
		}
		public void clear() {
			saved = false;
			position = 0; 
			page = 1;
		}
		public void restored() {
			if (saved) clear();
		}
	}

	void setPageTitle() {
		setTitle(owner.getName() + "'s Craves");
	}

	@Override
	public void onFragmentDetached(BaseCobrainFragment f) {
		if (f instanceof CravesFragment) {
			HomeFragment home = (HomeFragment) getSherlockActivity().getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
			if (home != null) {
				home.homePager.setVisibility(View.VISIBLE);
			}
			else {
				craveStripList.setVisibility(View.VISIBLE);
			}
			setPageTitle();
		}
	}

	@Override
	public void onFragmentAttached(BaseCobrainFragment f) {
		if (f instanceof CravesFragment) {
			HomeFragment home = (HomeFragment) getSherlockActivity().getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
			if (home != null) {
				home.homePager.setVisibility(View.GONE);
			}
			else craveStripList.setVisibility(View.GONE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		
		View v = inflater.inflate(R.layout.frg_friend_crave_strips_fragment, null);
		craveStripList = (ListView) v.findViewById(R.id.crave_strip_list);
		craveAdapter = new SkuPagerAdapter(getChildFragmentManager(), this);
		loader = new SkuStripsLoader();

		setPageTitle();
		
		if (!savedState.isSaved())
			savedState.categoryId = getResources().getInteger(R.integer.default_category_id);

		setupCraveStrips(v);
		//setupCategoryNavigationMenu();
		//setupFilterMenu(inflater);
		//setupComingSoonView(v);

		return v;
	}

	void setupCraveStrips(View v) {
		loader.initialize(this, craveStripList);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("categoryId", savedState.categoryId);
		outState.putInt("position", savedState.position);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		loaderUtils.initialize((ViewGroup) getView());
		update();

		super.onActivityCreated(savedInstanceState);
	}

	final String[] signals = new String[] {"shared", "recommended"};

	public void update() {
		if (controller != null) {
			controller.getCobrain().checkLogin();
			loaderUtils.dismiss();

			String[] captions = new String[] {
					owner.getName() + " wants your opinion on these Craves",
					"Would " + owner.getName() + " like these Craves?"
					};

			loader.load(owner, captions, signals);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		
		if (loader != null) {
			loader.dispose();
			loader = null;
		}

		craveStripList = null;

		//savedState.categoryId = craveLoader.getCategoryId();
		//savedState.position = cravePager.getCurrentItem();
		//savedState.page = getCurrentCravesPage(savedState.position);
		savedState.selectedCategoryNavigationPosition = actionBar.getSelectedNavigationIndex();
		savedState.save();
		
		craveAdapter.dispose();
		craveAdapter = null;

		if (craveFilterAdapter != null) {
			craveFilterAdapter.clear();
			craveFilterAdapter = null;
		}

		craveFilterLoader.dispose();
		//craveFilterLoader = null;
		
		if (filterMenu != null) {
			filterMenu.setContentView(null);
			filterMenu = null;
		}
		
		super.onDestroyView();
	}

	@Override
	public void onLoadStarted() {
		loaderUtils.showLoading("Loading your craves...");
	}

	@Override
	public void onLoadCompleted(Skus r) {
		if (r == null) {
			loaderUtils.showEmpty("We had a problem loading your friend's craves. Click here to try loading them again.");
			loaderUtils.setOnClickListener(new OnClickListener () {
				public void onClick(View v) {
					update();
				}
			});
		}
		else if (r.get().size() == 0) {
			//loaderUtils.showEmpty("Sorry we couldn't find any craves for you yet. Try training your Cobrain to get some craves.");
		}
		else {
			loaderUtils.dismissLoading();

			if (savedState.isSaved()) {
				//cravePager.setCurrentItem(savedState.position, false);
				savedState.restored();
			}
			
		}
	}


/*	int getCurrentCravesPage(int position) {
		final int LOAD_ITEMS_AHEAD = 0;
		int page = (int) Math.floor((position + 1 + LOAD_ITEMS_AHEAD) / (double)craveLoader.getCountPerPage()) + 1;
		return page;
	}
	
	@Override
	public void onPageSelected(int position) {
		int page = getCurrentCravesPage(position);
		if (page > 1 && !craveLoader.isPageLoaded(page - 1))
			craveLoader.loadPage(page - 1);
		
		craveLoader.loadPage(page);
	}*/

	//@Override
	public void showFilterMenu(View menuItemView) {
		//filterMenu.showAsDropDown(menuItemView, 0, 0, Gravity.RIGHT);
		
		View view = filterMenu.getContentView();

		try {
			view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		}
		catch(NullPointerException e) {
			e.printStackTrace();
		}
		filterMenu.showAsDropDown(menuItemView, menuItemView.getWidth() - view.getMeasuredWidth(), 0);
		
		//filterMenu.showAsDropDown(menuItemView, 0, 0);
	}

	/*
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
		NavigationMenuItem item = craveFilterAdapter.getItem(pos);
		setCategoryId(item.id);
		filterMenu.dismiss();
	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (savedState.isSaved()) {
			setCategoryId(savedState.categoryId);
		}
		else setCategoryId((int)itemId);
		return true;
	}*/

	boolean filterOnSale() {
		NavigationMenuItem mi = getSelectedCategoryMenuItem();
		if (mi == null) return false;
		return mi.id < 0;
	}
	NavigationMenuItem getSelectedCategoryMenuItem() {
		if (cravesCategoryAdapter.getParent() == null) return null;
		return cravesCategoryAdapter.getItem(cravesCategoryAdapter.getParent().getSelectedItemPosition());
	}
	
	/*
	boolean setCategoryId(int id) {
		switch (id) {
		case -1:
			showFilterMenu(false);
			loaderUtils.dismiss();
			comingsoon.setVisibility(View.VISIBLE);
			break;
			
		default:
			showFilterMenu(true);
			comingsoon.setVisibility(View.GONE);

			id = Math.abs(id);

			boolean onSale = filterOnSale();
			boolean restore = savedState.isSaved();
			
			if (craveLoader.setCategoryId(id) | craveLoader.setOnSaleRecommendationsOnly(onSale)) {
				savedState.categoryId = id;
				craveFilterLoader.load(id);
				if (restore)
					craveLoader.loadPage(savedState.page);
				else {
					savedState.clear();
					craveLoader.loadPage(1);
					//cravePager.setCurrentItem(0, false);
				}
				return true;
			}
			break;
		}
		
		return false;
	}*/

	private void showFilterMenu(boolean show) {
		if (menu != null) {
			MenuItem item = menu.findItem(R.id.menu_filter);
			item.setVisible(show);
		}
		//getSherlockActivity().invalidateOptionsMenu();
	}

	public void showBrowser(String url, String merchant) {
		//cravePager.setId(View.NO_ID); //so we don't save the pager state automatically; i want to do this myself
		//controller.showBrowser(url, R.id.content_frame, merchant);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
	}

	public void showCravesFragmentForSkus(CraveStrip<Skus> strip, Sku sku) {
		controller.showCraves(strip, sku, R.id.overlay_layout, true);
	}

}	