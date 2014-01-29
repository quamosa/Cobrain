package com.cobrain.android.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
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
	PopupWindow categoryFilterMenu = (android.os.Build.VERSION.SDK_INT > 10) ? new PopupWindow() : null;
	PopupWindow priceFilterMenu = (android.os.Build.VERSION.SDK_INT > 10) ? new PopupWindow() : null;
	ListView filterMenuListView, priceFilterMenuListView;
	//int categoryId;	
	//private int currentPage = 1;
	SavedState savedState = new SavedState();
	View comingsoon;
	Menu menu;
	ToggleButton categoryFilter;
	Button priceFilter;
	private boolean onSale;
	private TextView actionBarTitle;
	public TextView actionBarSubTitle;

	public class SavedState {
		boolean saved;
		int selectedCategoryNavigationPosition;
		int categoryId;
		int position;
		int page;
		
		boolean initial = true;
		public boolean onSale;

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

/*	@Override
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
			showPopupMenu(filterMenu, v);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/

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

		categoryFilter = (ToggleButton) v.findViewById(R.id.category_filter_button);
		priceFilter = (Button) v.findViewById(R.id.price_filter_button);
		categoryFilter.setOnClickListener(this);
		priceFilter.setOnClickListener(this);
		
		setTitle(null);
		actionBar.setDisplayShowCustomEnabled(true);
		View abv = inflater.inflate(R.layout.actionbar_crave_frame, null);
		actionBarTitle = (TextView) abv.findViewById(R.id.title);
		actionBarSubTitle = (TextView) abv.findViewById(R.id.subtitle);
		ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
		abv.setLayoutParams(params);
		actionBar.setCustomView(abv);
		
		setupCategoryNavigationMenu();
		setupFilterMenu(inflater);
		setupComingSoonView(v);

		return v;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {

		case R.id.category_filter_button:

			View image = craveAdapter.getPage(cravePager.getCurrentItem()).itemImage;

			categoryFilter.setChecked(true);
			categoryFilterMenu.setOnDismissListener(new OnDismissListener () {

				@Override
				public void onDismiss() {
					categoryFilter.setChecked(false);
					categoryFilterMenu.setOnDismissListener(null);
				}
				
			});

			showPopupMenu(categoryFilterMenu, image, Gravity.FILL, 1);

			break;
			
		case R.id.price_filter_button:
			onSale = !onSale;
			//priceFilter.setCompoundDrawablesRelativeWithIntrinsicBounds(((onSale) ? R.drawable.ic_category_menu_onsale : 0), 0, 0, 0);
			update();
			//showPopupMenu(priceFilterMenu, v, Gravity.LEFT);
			break;
		}
		super.onClick(v);
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
		
		if (categoryFilterMenu == null) {
			categoryFilterMenu = new PopupWindow(v);
		}
		else categoryFilterMenu.setContentView(v);
		filterMenuListView = (ListView) v.findViewById(R.id.menu_category_filter_listview);
		filterMenuListView.setOnItemClickListener(this);

		craveFilterHeader = (TextView) v.findViewById(R.id.category);
		craveFilterHeader.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (craveFilterLoader.getParentCategoryId() > 0)
					setCategoryId(craveFilterLoader.getParentCategoryId());
				categoryFilterMenu.dismiss();
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
				//craveFilterHeader.setCompoundDrawablesWithIntrinsicBounds(mi.icon, null, null, null);

				if (show) {
					CharSequence label = Html.fromHtml(rGetName + " " + ((show) ? "<small><font color='#a0a0a0'> in " + pCatName + "</font></small>" : ""));
					mi.label = label;
				}
				else mi.label = mi.labelCopy;
				
				actionBarTitle.setText(mi.label);
				
				cravesCategoryAdapter.notifyDataSetChanged();

				setItemCheckedForId(filterMenuListView, savedState.categoryId);

				setupPopupMenu(categoryFilterMenu, filterMenuListView);
			}
			
		});
	}
	
	void setupPopupMenu(PopupWindow popup, ListView list) {
		View v = popup.getContentView();
		
		v.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		DisplayMetrics displayMetrics = controller.getSupportActionBar().getThemedContext().
				getResources().getDisplayMetrics();
		int widthMeasureSpec = MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels , MeasureSpec.AT_MOST);
		int heightMeasureSpec = MeasureSpec.makeMeasureSpec(displayMetrics.heightPixels, MeasureSpec.AT_MOST);

		list.measure(widthMeasureSpec, heightMeasureSpec);
		try {
			v.measure(0, heightMeasureSpec);
		}
		catch (NullPointerException e) {
			e.printStackTrace();
		}

		int wi = v.getMeasuredWidth();
		int h = v.getMeasuredHeight();
		
		for (int i = 0; i < list.getCount(); i++) {
			View vi = list.getAdapter().getView(i, null, list);
			if (vi.getLayoutParams() == null) 
				vi.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			vi.measure(0, 0);
			wi = Math.max(wi, vi.getMeasuredWidth());
		}
		
	    popup.setWidth(wi);
	    popup.setHeight(h);
	    popup.setBackgroundDrawable(new ColorDrawable());
	    popup.setFocusable(true);
	    popup.setOutsideTouchable(true);
	    //popup.setWindowLayoutMode(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}


	void setupCategoryNavigationMenu() {
		if (!savedState.isSaved())
			savedState.categoryId = getResources().getInteger(R.integer.default_category_id);

		ArrayList<NavigationMenuItem> menuItems = new ArrayList<NavigationMenuItem>();
		Context c = getActivity().getApplicationContext(); // actionBar.getThemedContext();// 
		Resources res = c.getResources();
		TypedArray items = res.obtainTypedArray(R.array.craves_category_menu_items);

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
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		//actionBar.setListNavigationCallbacks(cravesCategoryAdapter, this);

		ListView list = new ListView(getActivity().getApplicationContext());
		//list.setBackgroundColor(Color.WHITE);
		list.setOnItemClickListener(this);
		priceFilterMenuListView = list;
		
		if (priceFilterMenu == null) {
			priceFilterMenu = new PopupWindow(list);
		}
		else priceFilterMenu.setContentView(list);
		
		list.setAdapter(cravesCategoryAdapter);
		cravesCategoryAdapter.notifyDataSetChanged();
		setupPopupMenu(priceFilterMenu, list);
		
		new Handler().post(new Runnable() {
			public void run() {
				int pos = 0;
				
				if (savedState.isSaved()) {
					pos = savedState.selectedCategoryNavigationPosition;
					onSale = savedState.onSale;
				}
				
				setSelectedNavigationItem(pos);
			}
		});
	}

	private void setSelectedNavigationItem(
			int selectedCategoryNavigationPosition) {
		//actionBar.setSelectedNavigationItem(savedState.selectedCategoryNavigationPosition);
		if (selectedCategoryNavigationPosition == -1) selectedCategoryNavigationPosition = 0;
		NavigationMenuItem mi = cravesCategoryAdapter.getItem(selectedCategoryNavigationPosition);
		cravesCategoryAdapter.getParent().performItemClick(null, selectedCategoryNavigationPosition, mi.id);
	}
	private int getSelectedNavigationIndex() {
		//return actionBar.getSelectedNavigationIndex();
		Integer i = (Integer) cravesCategoryAdapter.getParent().getTag();//getSelectedItemPosition();
		if (i == null) return i = -1;
		return i;
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
		actionBarTitle = null;
		actionBarSubTitle = null;
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setCustomView(null);
		
		savedState.categoryId = craveLoader.getCategoryId();
		savedState.position = cravePager.getCurrentItem();
		savedState.page = getCurrentCravesPage(savedState.position);
		savedState.selectedCategoryNavigationPosition = getSelectedNavigationIndex();
		savedState.onSale = onSale;
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
		
		priceFilterMenuListView.setOnItemClickListener(null);
		priceFilterMenuListView.setAdapter(null);
		priceFilterMenuListView = null;
		filterMenuListView.setOnItemClickListener(null);
		filterMenuListView.setAdapter(null);
		filterMenuListView = null;
		priceFilterMenu.setContentView(null);
		priceFilterMenu = null;
		categoryFilterMenu.setContentView(null);
		categoryFilterMenu = null;

		cravesCategoryAdapter.clear();
		cravesCategoryAdapter = null;
		
		comingsoon = null;
		menu = null;

		categoryFilter.setOnClickListener(null);
		priceFilter.setOnClickListener(null);
		categoryFilter = null;
		priceFilter = null;

		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		//actionBar.setListNavigationCallbacks(null, null);
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
		craveAdapter.updateTitle(position);
		int page = getCurrentCravesPage(position);
		if (page > 1 && !craveLoader.isPageLoaded(page - 1))
			craveLoader.loadPage(page - 1);
		
		craveLoader.loadPage(page);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
	}

	public class PopupMenuLayoutParams {
		public int gravity;
		public int paddingLeft;
		public int paddingRight;
		public int paddingTop;
		public int paddingBottom;
	}
	public void showPopupMenu(PopupWindow popup, View menuItemView, int gravity, int padding) {
		//popup.showAsDropDown(menuItemView, 0, 0, Gravity.RIGHT);
		
		View view = popup.getContentView();

		int x = 0, y = 0;
		
		switch(gravity) {
		case Gravity.FILL:
			int w = menuItemView.getWidth();
			int h = menuItemView.getHeight();
			y = -h;
			
			x += padding;
			y += padding;
			w -= padding*2;
			h -= padding*2;

			popup.setWidth(w);
			popup.setHeight(h);
			break;
			
		case Gravity.RIGHT:
			try {
				view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			}
			catch(NullPointerException e) {
				e.printStackTrace();
			}
			
			x = menuItemView.getWidth() - view.getMeasuredWidth();
			break;
		}

		popup.showAsDropDown(menuItemView, x, y);
		
		//popup.showAsDropDown(menuItemView, 0, 0);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
		if (arg0 == filterMenuListView) {
			NavigationMenuItem item = craveFilterAdapter.getItem(pos);
			setCategoryId(item.id);
			categoryFilterMenu.dismiss();
		}
		else if (arg0 == priceFilterMenuListView) {
			arg0.setTag(pos);
			onNavigationItemSelected(pos, id);
			priceFilterMenu.dismiss();
		}
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
		//NavigationMenuItem mi = getSelectedCategoryMenuItem();
		//if (mi == null) return false;
		//return mi.id < 0;
		return onSale;
	}
	NavigationMenuItem getSelectedCategoryMenuItem() {
		if (cravesCategoryAdapter.getParent() == null) return null;
		//return cravesCategoryAdapter.getItem(cravesCategoryAdapter.getParent().getSelectedItemPosition());

		int i = getSelectedNavigationIndex();
		return cravesCategoryAdapter.getItem(i);
	}
	
	boolean setCategoryId(int id) {
		switch (id) {
		case -1:
			showFilterMenu(false);
			loaderUtils.dismiss();
			comingsoon.setVisibility(View.VISIBLE);
			break;
			
		default:
			////on sale is -6
			////if (id == -6) id = -savedState.categoryId;
			//apparel is 6, tops = 5 so we default to tops.. never the top level apparel
			if (id == 6) id = 5;
			
			showFilterMenu(true);
			comingsoon.setVisibility(View.GONE);

			//id = Math.abs(id);

			boolean onSale = filterOnSale();
			boolean restore = savedState.isSaved();

			if (craveLoader.setCategoryId(id) | craveLoader.setOnSaleRecommendationsOnly(onSale)) {
				priceFilter.setText((onSale) ? "On Sale" : "All Prices");
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

	private void setItemCheckedForId(ListView list, int id) {
		list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		Adapter a = list.getAdapter();

		if (a != null) {
			for (int i = 0; i < a.getCount(); i++) {
				NavigationMenuItem mi = (NavigationMenuItem)a.getItem(i);
				if (id == mi.id) {
					list.setItemChecked(i, true);
					break;
				}
			}
		}
	}

	private void showFilterMenu(boolean show) {
		if (menu != null) {
			MenuItem item = menu.findItem(R.id.menu_filter);
			item.setVisible(show);
		}
		//getSherlockActivity().invalidateOptionsMenu();
	}

	public void showTeachMyCobrain() {
		cravePager.setId(View.NO_ID); //so we don't save the pager state automatically; i want to do this myself
		controller.showTeachMyCobrain(true);
	}
	public void showBrowser(String url, String merchant) {
		cravePager.setId(View.NO_ID); //so we don't save the pager state automatically; i want to do this myself
		controller.showBrowser(url, R.id.content_frame, merchant, true);
	}
	
}	