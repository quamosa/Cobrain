package com.cobrain.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.cobrain.android.controllers.ScenarioCraveStrip;
import com.cobrain.android.controllers.SkuCraveStrip;
import com.cobrain.android.loaders.CraveFilterLoader;
import com.cobrain.android.loaders.ScenarioStripsLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.model.Scenario;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;

public class CraveStripsFragment<T> extends BaseCobrainFragment implements OnLoadListener<T>, OnItemClickListener {
	public static final String TAG = "CraveStripsFragment";

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
	ScenarioStripsLoader loader;
	boolean onSale;

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
		setTitle("Cobrain");
	}

	@Override
	public void onFragmentDetached(BaseCobrainFragment f) {
		if (f instanceof CravesFragment) {
			HomeFragment home = (HomeFragment) getSherlockActivity().getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
			if (home != null) {
				home.homePager.setVisibility(View.VISIBLE);
			}
			else getView().setVisibility(View.VISIBLE);
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
			else getView().setVisibility(View.GONE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		
		View v = inflater.inflate(R.layout.frg_crave_strips_fragment, null);
		craveAdapter = new SkuPagerAdapter(getChildFragmentManager(), this);
		loader = new ScenarioStripsLoader();

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
		ListView craveStripList = (ListView) v.findViewById(R.id.crave_strip_list);
		loader.initialize(this, craveStripList, onSale);
	}
	
	void setupComingSoonView(View v) {
		comingsoon = View.inflate(getActivity().getApplicationContext(), R.layout.view_coming_soon, null);
		ViewGroup vg = (ViewGroup) v;
		vg.addView(comingsoon, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		comingsoon.setClickable(true);
		comingsoon.setVisibility(View.GONE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt("categoryId", savedState.categoryId);
		outState.putInt("position", savedState.position);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//FIXME: update();
		loaderUtils.initialize((ViewGroup) getView());

		super.onActivityCreated(savedInstanceState);
	}

	public void update() {
		if (controller != null) {
			controller.getCobrain().checkLogin();
			loaderUtils.dismiss();
			if (!onSale) {
				loader.addHeaderStrip();
			}
			loader.load();
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
		
		//savedState.categoryId = craveLoader.getCategoryId();
		//savedState.position = cravePager.getCurrentItem();
		//savedState.page = getCurrentCravesPage(savedState.position);
		savedState.selectedCategoryNavigationPosition = actionBar.getSelectedNavigationIndex();
		savedState.save();
		
		if (craveAdapter != null) {
			craveAdapter.dispose();
			craveAdapter = null;
		}

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
		//loaderUtils.showLoading("Loading your craves...");
	}

	@Override
	public void onLoadCompleted(T r) {
		if (r == null) {
			loaderUtils.showEmpty("We had a problem loading your craves. Click here to try loading them again.");
			loaderUtils.setOnClickListener(new OnClickListener () {
				public void onClick(View v) {
					update();
				}
			});
		}
		else if ((r instanceof Scenario) && ((Scenario)r).getSkus().size() == 0) {
			//loaderUtils.showEmpty("Sorry we couldn't find any craves for you yet. Try training your Cobrain to get some craves.");
		}
		else {
			//loaderUtils.dismissLoading();

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

	/*public void showCravesFragmentForScenario(Scenario scenario, Sku sku) {
		for (int i = 0; i < loader.craveStrips.size(); i++) {
			ScenarioCraveStrip strip = (ScenarioCraveStrip) loader.craveStrips.get(i);
			if (strip.scenarioId == scenario.getId()) {
				controller.showCraves(strip, sku, R.id.overlay_layout, true);
				return;
			}
		}
	}*/

	public void showZoomedCraveStrip(CraveStrip strip, Sku sku) {
		controller.showCraves(strip, sku, R.id.overlay_layout, true);
	}

	public static CraveStripsFragment<?> newInstance(boolean onSale) {
		CraveStripsFragment<?> f = new CraveStripsFragment<Object>();
		f.onSale = onSale;
		return f;
	}

}	