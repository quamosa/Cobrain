package com.cobrain.android.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
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
import com.cobrain.android.R;
import com.cobrain.android.adapters.CravesCategoryAdapter;
import com.cobrain.android.adapters.NavigationMenuItem;
import com.cobrain.android.adapters.WishListPagerAdapter;
import com.cobrain.android.loaders.CraveFilterLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.WishListLoader;
import com.cobrain.android.model.Category;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.v1.CategoryTree;
import com.cobrain.android.utils.HelperUtils;

public class WishListFragment extends BaseCobrainFragment implements OnLoadListener<List<Sku>>, OnPageChangeListener, OnItemClickListener, OnNavigationListener {
	public static final String TAG = "WishListFragment";

	public ViewPager cravePager;
	WishListPagerAdapter craveAdapter;
	WishListLoader craveLoader = new WishListLoader();
	CraveFilterLoader craveFilterLoader = new CraveFilterLoader();
	TextView craveFilterHeader;
	CravesCategoryAdapter cravesCategoryAdapter;
	ArrayAdapter<NavigationMenuItem> craveFilterAdapter;
	private int currentPage = 1;
	PopupWindow filterMenu = new PopupWindow();
	ListView filterMenuListView;
	int categoryId;
	Skus wishList;
	boolean showMyPrivateWishList;
	boolean thisIsMyList;
	private User wishListOwner;
	private List<Integer> skuIds;
	private AsyncTask<Void, Void, Skus> addTask;
	boolean useWishList;
	int initialRavePosition = -1;

	public void initialize(Skus skus, Sku sku, boolean showMyPrivateWishList) {
		wishList = skus;
		useWishList = true;
		this.showMyPrivateWishList = showMyPrivateWishList;
		if (sku != null) initialRavePosition = skus.get().indexOf(sku);
	}
	public void initialize(User owner, boolean showMyPrivateWishList) {
		wishListOwner = owner;
		this.showMyPrivateWishList = showMyPrivateWishList;
	}
	public void initialize(User owner, boolean showMyPrivateWishList, List<Integer> skuIds) {
		wishListOwner = owner;
		this.showMyPrivateWishList = showMyPrivateWishList;
		this.skuIds = skuIds;
	}

	public WishListFragment() {
	}

	public static WishListFragment newInstance(User owner, boolean showMyPrivateWishList) {
		WishListFragment f = new WishListFragment();
		f.initialize(owner, showMyPrivateWishList);
		return f;
	}
	public static WishListFragment newInstance(Skus skus, Sku sku, boolean showMyPrivateWishList) {
		WishListFragment f = new WishListFragment();
		f.initialize(skus, sku, showMyPrivateWishList);
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.main_wishlist_frame, null);
		loaderUtils.initialize((ViewGroup) v);
		cravePager = (ViewPager) v.findViewById(R.id.crave_pager);
		craveAdapter = new WishListPagerAdapter(getChildFragmentManager(), this);
		cravePager.setAdapter(craveAdapter);
		craveLoader.initialize(controller, craveAdapter);
		craveLoader.setOnLoadListener(this);
		cravePager.setOnPageChangeListener(this);

		setTitle("Loading Rack...");
		
		//setupCategoryNavigationMenu();
		//setupFilterMenu(inflater);

		return v;
	}

	
	@Override
	public void setTitle(CharSequence title) {
		if (!(getParentFragment() instanceof HomeFragment))
			super.setTitle(title);
	}
	
	private void setupFilterMenu(LayoutInflater inflater) {
		View v = inflater.inflate(R.layout.menu_craves_filter, null);
		
		filterMenu.setContentView(v);
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

				if (r != null) {
					ArrayList<NavigationMenuItem> items = new ArrayList<NavigationMenuItem>();

					for (Category c : r.getChildren()) {
						NavigationMenuItem item = new NavigationMenuItem();
						item.caption = c.getName();
						item.id = c.getId();
						item.label = item.caption;
						items.add(item);
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
					
					show = (craveFilterLoader.getParentCategoryId() != r.getId());
					rGetName = r.getName();
				}
				else rGetName = pCatName;

				craveFilterHeader.setText(rGetName);

				if (show) {
					CharSequence label = Html.fromHtml(rGetName + " " + ((show) ? "<small> / " + pCatName + "</small>" : ""));
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
		categoryId = getResources().getInteger(R.integer.default_category_id);

		ArrayList<NavigationMenuItem> menuItems = new ArrayList<NavigationMenuItem>();
		Context c = getActivity().getApplicationContext(); // actionBar.getThemedContext();// 
		Resources res = c.getResources();
		TypedArray items = res.obtainTypedArray(R.array.craves_category_menu_items);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		for (int i = 0; i < items.length(); i+=4) {
			NavigationMenuItem nmi = new NavigationMenuItem();
			nmi.caption = items.getText(i);
			nmi.icon = items.getDrawable(i+1);
			nmi.id = items.getInt(i+2, 0);
			nmi.label = items.getString(i+3);
			nmi.labelCopy = nmi.label;
			menuItems.add(nmi);
		}

		cravesCategoryAdapter = new CravesCategoryAdapter(c, menuItems);
		cravesCategoryAdapter.setLayoutId(R.layout.list_item_craves_category);
		cravesCategoryAdapter.setCaptionLayoutId(R.layout.list_item_craves_category_caption);
		//cravesCategoryAdapter.setDropDownViewResource(R.layout.list_item_navigation);
		actionBar.setListNavigationCallbacks(cravesCategoryAdapter, this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@SuppressWarnings("unchecked")
	public void update() {
		if (controller == null) return;
		
		controller.getCobrain().checkLogin();
		loaderUtils.dismiss();
		
		if (wishList != null) {
			if (wishList.getOwner().getId().equals(controller.getCobrain().getUserInfo().getUserId())) {
				setTitle("My " + ((showMyPrivateWishList) ? "Private" : "Shared") + " Rack");			
			}
			else setTitle(wishList.getOwner().getName() + "'s Rack");

			if (useWishList) {
				craveAdapter.load(wishList, false);
				if (initialRavePosition != -1) {
					cravePager.setCurrentItem(initialRavePosition, false);
					initialRavePosition = -1;
				}
			}
			else {
				if (!setWishListId(wishList.getOwner())) {
					craveLoader.clearPagesLoaded(); //so we can force a refresh of the current page
					craveLoader.setPage(currentPage);
				}
			}
		}
		else {
			if (wishListOwner != null) {
				addAsyncTask("wishList", new AsyncTask<Object, Void, Skus>() {

					@Override
					protected Skus doInBackground(Object... params) {
						if (HelperUtils.Tasks.asyncTaskCancel(this, controller == null)) return null;
						
						UserInfo ui = controller.getCobrain().getUserInfo();
						return ui.getSkus(wishListOwner, "shared", null, null);
					}

					@Override
					protected void onPostExecute(Skus result) {
						if (result != null) {
							wishList = result;
							update();
						}
						else {
							if (wishListOwner.getId().equals(controller.getCobrain().getUserInfo().getUserId())) {
								onError("Could not load your craves");
							}
							else onError("Could not load " + wishListOwner.getName() + "'s craves");
						}
					}
				}).execute();
			}
		}
		
	}
	
	@Override
	public void onDestroyView() {
		setSubTitle(null);

		parent = null;
		skuIds = null;
		
		wishList = null;
		wishListOwner = null;
		
		cravePager = null;

		craveAdapter.dispose();
		craveAdapter = null;
		//craveFilterAdapter.clear();
		//craveFilterAdapter = null;

		craveLoader.dispose();
		craveLoader = null;
		//craveFilterHeader.setOnClickListener(null);
		//craveFilterHeader = null;
		//craveFilterLoader.dispose();
		//craveFilterLoader = null;
		
		filterMenu.setContentView(null);
		filterMenu = null;

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setListNavigationCallbacks(null, null);
		//cravesCategoryAdapter.clear();
		//cravesCategoryAdapter = null;
		
		super.onDestroyView();
	}

	@Override
	public void onLoadStarted() {
		loaderUtils.showLoading("Loading " + ((wishList.getOwner().getId() == controller.getCobrain().getUserInfo().getUserId()) ? "your" : (wishList.getOwner().getName() + "'s")) + " rack...");
	}

	@Override
	public void onLoadCompleted(List<Sku> r) {
		if (r == null) {
			loaderUtils.showEmpty("We had a problem loading this Wish List. Click here to try loading it again.");
			loaderUtils.setOnClickListener(new OnClickListener () {
				public void onClick(View v) {
					loaderUtils.dismissEmpty();
					update();
				}
			});
		}
		else if (r.size() == 0) {
			craveAdapter.updateTitle(0);
			if (wishList.getOwner().getId().equals(controller.getCobrain().getUserInfo().getUserId())) {
				loaderUtils.showEmpty("Your 1st Shared Crave will appear here. Share your favorite Craves so friends can Rave about them.");
			}
			else {
				loaderUtils.showEmpty(wishList.getOwner().getName() + " hasn't shared any Craves. Remind friends to share their favorite Craves so you can Rave about them.");
			}
		}
		else {
			findSkuIds();
			loaderUtils.dismiss();
		}
	}

	void findSkuIds() {
		int i = findIndexOfSkuId();
		if (i != -1) {
			cravePager.setCurrentItem(i, true);
		}
	}
	int findIndexOfSkuId() {
		if (skuIds != null) {
			for (Integer skuId : skuIds) {
				for (int i = 0; i < craveAdapter.getCount(); i++) {
					if (skuId == craveAdapter.get(i).getId() ) {
						return i;
					}
				}
			}
		}
		return -1;
	}
	
	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		craveAdapter.updateTitle(position);
		final int LOAD_ITEMS_AHEAD = 0;
		int page = (int) Math.floor((position + 1 + LOAD_ITEMS_AHEAD) / (double)craveLoader.getCountPerPage()) + 1;

		if (!useWishList)
			craveLoader.setPage(page);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void showFilterMenu(View menuItemView) {
		//filterMenu.showAsDropDown(menuItemView, 0, 0, Gravity.RIGHT);
		
		View view = filterMenu.getContentView();

		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
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
	
	boolean setWishListId(User owner) {
		if (craveLoader.setMyListId(owner, showMyPrivateWishList)) {
			craveLoader.setPage(1);
			cravePager.setCurrentItem(0, false);
			return true;
		}
		return false;
	}
	
	boolean setCategoryId(int id) {
		id = Math.abs(id);
		
		boolean onSale = filterOnSale();
		
		if (craveLoader.setCategoryId(id) | craveLoader.setOnSaleRecommendationsOnly(onSale)) {
			categoryId = id;
			craveFilterLoader.load(id);
			craveLoader.setPage(1);
			cravePager.setCurrentItem(0, false);
			return true;
		}
		return false;
	}
}	
