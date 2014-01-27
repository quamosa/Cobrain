package com.cobrain.android.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cobrain.android.R;
import com.cobrain.android.adapters.CravePagerAdapter;
import com.cobrain.android.adapters.CravesCategoryAdapter;
import com.cobrain.android.adapters.NavigationMenuItem;
import com.cobrain.android.loaders.CraveFilterLoader;
import com.cobrain.android.loaders.CraveLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.model.Category;
import com.cobrain.android.model.CategoryTree;
import com.cobrain.android.model.RecommendationsResults;
import com.cobrain.android.utils.HelperUtils;

public class CravesFragment extends BaseCobrainFragment implements OnLoadListener<RecommendationsResults>, OnPageChangeListener, OnItemClickListener, OnNavigationListener {
	public static final String TAG = "CravesFragment";

	ViewPager cravePager;
	CravePagerAdapter craveAdapter;
	CraveLoader craveLoader = new CraveLoader();
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

	public class SavedState {
		boolean saved;
		int selectedCategoryNavigationPosition;
		int categoryId;
		int position;
		int page;
		
		boolean initial = true;

		public boolean isInitial() {
			if (initial) {
				initial = false;
				return true;
			}
			else return false;
		}
		
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

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		//if (menu.findItem(R.id.menu_filter) == null) {
			inflater.inflate(R.menu.crave_filter, menu);
		//}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_filter:
			View v = getActivity().findViewById(item.getItemId());
			showFilterMenu(v);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {
		
		setHasOptionsMenu(true);
		
		View v = inflater.inflate(R.layout.main_craves_frame, null);
		cravePager = (ViewPager) v.findViewById(R.id.crave_pager);
		craveAdapter = new CravePagerAdapter(getChildFragmentManager(), this);
		cravePager.setAdapter(craveAdapter);
		craveLoader.initialize(controller, craveAdapter);
		craveLoader.setOnLoadListener(this);
		cravePager.setOnPageChangeListener(this);

		setTitle(null);
		
		setupCategoryNavigationMenu();
		setupFilterMenu(inflater);
		setupComingSoonView(v);

		return v;
	}

	void setupComingSoonView(View v) {
		comingsoon = View.inflate(getActivity().getApplicationContext(), R.layout.view_coming_soon, null);
		ViewGroup vg = (ViewGroup) v;
		vg.addView(comingsoon, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		comingsoon.setClickable(true);
		comingsoon.setVisibility(View.GONE);
	}
	
	private void setupFilterMenu(LayoutInflater inflater) {
		View v = inflater.inflate(R.layout.menu_craves_filter, null);
		
		if (filterMenu == null) {
			filterMenu = new PopupWindow(v);
		}
		else filterMenu.setContentView(v);
		filterMenuListView = (ListView) v.findViewById(R.id.menu_category_filter_listview);
		filterMenuListView.setOnItemClickListener(this);

		craveFilterHeader = (TextView) v.findViewById(R.id.category);
		craveFilterHeader.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (craveFilterLoader.getParentCategoryId() > 0)
					setCategoryId(craveFilterLoader.getParentCategoryId());
				filterMenu.dismiss();
			}
		});

		craveFilterLoader.initialize(controller);
		craveFilterLoader.setOnLoadListener(new OnLoadListener<CategoryTree>() {

			@Override
			public void onLoadStarted() {
				loaderUtils.showLoading(null);
			}
			
			@Override
			public void onLoadCompleted(CategoryTree r) {
				loaderUtils.dismissLoading();

				String rGetName = null;
				boolean show = false;
				NavigationMenuItem mi = getSelectedCategoryMenuItem();
				String pCatName = mi.labelCopy.toString(); //craveFilterLoader.getParentCategoryName();
				CharSequence selectedCatName = null;

				if (r != null) {
					ArrayList<NavigationMenuItem> items = new ArrayList<NavigationMenuItem>();

					for (Category c : r.getChildren()) {
						NavigationMenuItem item = new NavigationMenuItem();
						item.caption = HelperUtils.Strings.wordCase(c.getName());
						item.id = c.getId();
						item.label = item.caption;
						items.add(item);
						if (c.getId() == savedState.categoryId) selectedCatName = item.caption;
					}
					
					if (items.size() == 0) {
						NavigationMenuItem item = new NavigationMenuItem();
						item.caption = "All " + pCatName;
						item.id = craveFilterLoader.getParentCategoryId();
						item.label = item.caption;
						items.add(item);
					}
					
					craveFilterAdapter = new ArrayAdapter<NavigationMenuItem>(
							getActivity().getApplicationContext(), 
							R.layout.list_item_craves_filter,
							R.id.caption,
							items);
					
					filterMenuListView.setAdapter(craveFilterAdapter);
					
					//show = (craveFilterLoader.getParentCategoryId() != r.getId());
					//rGetName = HelperUtils.Strings.wordCase(r.getName());
					show = true;
					if (selectedCatName != null) rGetName = selectedCatName.toString();
				}
				else rGetName = pCatName;

				//craveFilterHeader.setText(rGetName);
				craveFilterHeader.setCompoundDrawablesWithIntrinsicBounds(mi.icon, null, null, null);

				if (show) {
					CharSequence label = Html.fromHtml(rGetName + " " + ((show) ? "<small><font color='#a0a0a0'> in " + pCatName + "</font></small>" : ""));
					mi.label = label;
				}
				else mi.label = mi.labelCopy;
				cravesCategoryAdapter.notifyDataSetChanged();
			
				View v = filterMenu.getContentView();
				
				v.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				
				DisplayMetrics displayMetrics = controller.getSupportActionBar().getThemedContext().
						getResources().getDisplayMetrics();
				int widthMeasureSpec = MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels , MeasureSpec.AT_MOST);
				int heightMeasureSpec = MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, MeasureSpec.AT_MOST);

				filterMenuListView.measure(widthMeasureSpec, heightMeasureSpec);
				try {
					v.measure(0, heightMeasureSpec);
				}
				catch (NullPointerException e) {
					e.printStackTrace();
				}

				int wi = v.getMeasuredWidth();
				int h = v.getMeasuredHeight();
				
				for (int i = 0; i < filterMenuListView.getCount(); i++) {
					View vi = filterMenuListView.getAdapter().getView(i, null, filterMenuListView);
					vi.measure(0, 0);
					wi = Math.max(wi, vi.getMeasuredWidth());
				}
				
			    filterMenu.setWidth(wi);
			    filterMenu.setHeight(h);
			    filterMenu.setBackgroundDrawable(new ColorDrawable());
			    filterMenu.setFocusable(true);
			    filterMenu.setOutsideTouchable(true);
			    //filterMenu.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			}
			
		});
	}


	void setupCategoryNavigationMenu() {
		if (!savedState.isSaved())
			savedState.categoryId = getResources().getInteger(R.integer.default_category_id);

		ArrayList<NavigationMenuItem> menuItems = new ArrayList<NavigationMenuItem>();
		Context c = getActivity().getApplicationContext(); // actionBar.getThemedContext();// 
		Resources res = c.getResources();
		TypedArray items = res.obtainTypedArray(R.array.craves_category_menu_items);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		for (int i = 0; i < items.length();) {
			NavigationMenuItem nmi = new NavigationMenuItem();
			nmi.caption = Html.fromHtml(items.getString(i++));
			nmi.icon = items.getDrawable(i++);
			nmi.id = items.getInt(i++, 0);
			nmi.label = items.getString(i++);
			nmi.labelCopy = nmi.label;
			menuItems.add(nmi);
		}

		cravesCategoryAdapter = new CravesCategoryAdapter(c, menuItems);
		cravesCategoryAdapter.setLayoutId(R.layout.list_item_craves_category);
		cravesCategoryAdapter.setCaptionLayoutId(R.layout.list_item_craves_category_caption);
		//cravesCategoryAdapter.setDropDownViewResource(R.layout.list_item_navigation);
		actionBar.setListNavigationCallbacks(cravesCategoryAdapter, this);
		if (savedState.isSaved()) {
			actionBar.setSelectedNavigationItem(savedState.selectedCategoryNavigationPosition);
		}
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
		controller.getCobrain().checkLogin();
		loaderUtils.dismiss();

		if (!setCategoryId(savedState.categoryId)) {
			craveFilterLoader.load(savedState.categoryId);
			craveLoader.loadPage(savedState.page);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		savedState.categoryId = craveLoader.getCategoryId();
		savedState.position = cravePager.getCurrentItem();
		savedState.page = getCurrentCravesPage(savedState.position);
		savedState.selectedCategoryNavigationPosition = actionBar.getSelectedNavigationIndex();
		savedState.save();
		
		craveAdapter.dispose();
		craveAdapter = null;

		cravePager.setAdapter(null);
		cravePager = null;

		if (craveFilterAdapter != null) {
			craveFilterAdapter.clear();
			craveFilterAdapter = null;
		}

		craveLoader.dispose();
		//craveLoader = null;
		craveFilterHeader.setOnClickListener(null);
		craveFilterHeader = null;
		craveFilterLoader.dispose();
		//craveFilterLoader = null;
		
		filterMenu.setContentView(null);
		filterMenu = null;

		cravesCategoryAdapter.clear();
		cravesCategoryAdapter = null;
		
		comingsoon = null;
		menu = null;
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setListNavigationCallbacks(null, null);
		super.onDestroyView();
	}

	@Override
	public void onLoadStarted() {
		loaderUtils.showLoading("Loading your craves...");
	}

	@Override
	public void onLoadCompleted(RecommendationsResults r) {
		if (r == null) {
			loaderUtils.showEmpty("We had a problem loading your craves. Click here to try loading them again.");
			loaderUtils.setOnClickListener(new OnClickListener () {
				public void onClick(View v) {
					update();
				}
			});
		}
		else if (r.getTotal() == 0)
			loaderUtils.showEmpty("Sorry we couldn't find any craves for you yet. Try training your Cobrain to get some craves.");
		else {
			loaderUtils.dismissLoading();

			if (savedState.isSaved()) {
				cravePager.setCurrentItem(savedState.position, false);
				savedState.restored();
			}
			
		}
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	
	}

	int getCurrentCravesPage(int position) {
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
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
	}

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
		else if (savedState.isInitial()) {
			setCategoryId(savedState.categoryId);
		}
		else
			setCategoryId((int)itemId);
		return true;
	}

	boolean filterOnSale() {
		NavigationMenuItem mi = getSelectedCategoryMenuItem();
		if (mi == null) return false;
		return mi.id < 0;
	}
	NavigationMenuItem getSelectedCategoryMenuItem() {
		if (cravesCategoryAdapter.getParent() == null) return null;
		return cravesCategoryAdapter.getItem(cravesCategoryAdapter.getParent().getSelectedItemPosition());
	}
	
	boolean setCategoryId(int id) {
		switch (id) {
		case -1:
			showFilterMenu(false);
			loaderUtils.dismiss();
			comingsoon.setVisibility(View.VISIBLE);
			break;
			
		default:
			//on sale is -6
			if (id == -6) id = -savedState.categoryId;
			//apparel is 6, tops = 5 so we default to tops.. never the top level apparel
			if (id == 6) id = 5;
			
			showFilterMenu(true);
			comingsoon.setVisibility(View.GONE);

			id = Math.abs(id);

			boolean onSale = filterOnSale();
			boolean restore = savedState.isSaved();
			
			if (craveLoader.setCategoryId(id) | craveLoader.setOnSaleRecommendationsOnly(onSale)) {
				savedState.categoryId = id;
				//lets only load the top level apparel: id = 6
				//craveFilterLoader.load(id);
				craveFilterLoader.load(6);
				if (restore)
					craveLoader.loadPage(savedState.page);
				else {
					savedState.clear();
					craveLoader.loadPage(1);
					cravePager.setCurrentItem(0, false);
				}
				return true;
			}
			break;
		}
		
		return false;
	}

	private void showFilterMenu(boolean show) {
		if (menu != null) {
			MenuItem item = menu.findItem(R.id.menu_filter);
			item.setVisible(show);
		}
		//getSherlockActivity().invalidateOptionsMenu();
	}

	public void showBrowser(String url, String merchant) {
		cravePager.setId(View.NO_ID); //so we don't save the pager state automatically; i want to do this myself
		controller.showBrowser(url, R.id.content_frame, merchant);
	}
	
}	