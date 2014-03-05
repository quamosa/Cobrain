package com.cobrain.android.fragments;

import java.util.ArrayList;
import java.util.List;

import com.cobrain.android.R;
import com.cobrain.android.adapters.NavigationMenuItem;
import com.cobrain.android.adapters.SavedAndShareAdapter;
import com.cobrain.android.loaders.CraveFilterLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.SavedAndShareLoader;
import com.cobrain.android.model.Category;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.v1.CategoryTree;
import com.cobrain.android.utils.HelperUtils;
import com.cobrain.android.utils.LoaderUtils;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ToggleButton;

public class SavedAndShareFragment extends BaseCobrainFragment implements OnLoadListener<List<Sku>>, OnItemClickListener {
	public static final String TAG = "SavedAndShareFragment";
	private SwipeListView saves;
	private SavedAndShareAdapter adapter;
	SavedAndShareLoader loader = new SavedAndShareLoader();
	PopupWindow filterMenu = new PopupWindow();
	//Spinner priceFilter;
	//Spinner categoryFilter;
	ToggleButton categoryFilter;
	Button priceFilter;
	private PopupWindow categoryFilterMenu;
	private ListView filterMenuListView;
	ArrayAdapter<NavigationMenuItem> craveFilterAdapter;
	CraveFilterLoader craveFilterLoader = new CraveFilterLoader();
	int categoryId;
	String signal = "saved";
	
	public static SavedAndShareFragment newInstance(String signal) {
		SavedAndShareFragment f = new SavedAndShareFragment();
		f.signal = signal;
		return f;
	}
	
	public LoaderUtils getLoaderUtils() {
		return loaderUtils;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.frg_saved_and_shared_craves, null);
		saves = (SwipeListView) v.findViewById(R.id.saves_list);
		adapter = new SavedAndShareAdapter(container.getContext(), R.id.item_info, this);
		saves.setAdapter(adapter);
		loaderUtils.initialize((ViewGroup) v);
		loader.initialize(controller, adapter, signal);
		loader.setOnLoadListener(this);

		setTitle("My Private Rack");

		if (signal.equals("saved")) {
			priceFilter = setupPriceFilter(v);
		}
		else priceFilter = setupRaveAndNewFilter(v);
		categoryFilter = setupCategoryFilterMenu(v);
		categoryId = getResources().getInteger(R.integer.default_category_id); //apparel

        saves.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        saves.setSwipeActionRight(SwipeListView.SWIPE_ACTION_REVEAL);
        saves.setSwipeOpenOnLongPress(false);
 
        saves.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                int x = getResources().getDimensionPixelSize(R.dimen.saved_and_shared_item_height);
                int wx = saves.getWidth();
                saves.setOffsetLeft(wx-x);
                saves.setOffsetRight(wx-x);
            }

            @Override
            public void onStartClose(int position, boolean right) {
            }

            @Override
            public void onClickFrontView(int position) {
            }

            @Override
            public void onClickBackView(int position) {
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    adapter.remove(position);
                }
                adapter.notifyDataSetChanged();
                if (adapter.getCount() == 0) {
        			showEmpty();
                }
            }

        });
		
		
		return v;
	}
	
	@Override
	public void setTitle(CharSequence title) {
		if (!(getParentFragment() instanceof HomeFragment))
			super.setTitle(title);
	}
	
	/*private Spinner setupCategoryFilter(View v) {
		final Spinner s = (Spinner) v.findViewById(R.id.category_filter);
		Context c = getActivity().getApplicationContext();
		Resources res = c.getResources();
		TypedArray items = res.obtainTypedArray(R.array.sku_categories);
		ArrayList<NavigationMenuItem> menuItems = new ArrayList<NavigationMenuItem>();
		
		for (int i = 0; i < items.length();) {
			NavigationMenuItem nmi = new NavigationMenuItem();
			nmi.caption = Html.fromHtml(items.getString(i++));
			//nmi.icon = items.getDrawable(i++);
			i++;
			nmi.id = items.getInt(i++, 0);
			//nmi.label = items.getString(i++);
			nmi.label = nmi.caption;
			nmi.labelCopy = nmi.label;
			menuItems.add(nmi);
		}

		CravesCategoryAdapter cravesCategoryAdapter = new CravesCategoryAdapter(c, menuItems);
		cravesCategoryAdapter.setLayoutId(R.layout.list_item_craves_category);
		cravesCategoryAdapter.setCaptionLayoutId(R.layout.list_item_craves_category_caption);
		s.setAdapter(cravesCategoryAdapter);
		
		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SavedAndShareFragment.this.loader.applyCategoryFilter((int) id);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		
		return s;
	}*/

	private ToggleButton setupCategoryFilterMenu(View parent) {
		ToggleButton button = (ToggleButton) parent.findViewById(R.id.category_filter_button);
		
		button.setOnClickListener(this);
		View v = View.inflate(parent.getContext(), R.layout.menu_craves_filter, null);
		
		if (categoryFilterMenu == null) {
			categoryFilterMenu = new PopupWindow(v);
		}
		else categoryFilterMenu.setContentView(v);
		filterMenuListView = (ListView) v.findViewById(R.id.menu_category_filter_listview);
		filterMenuListView.setOnItemClickListener(this);

		craveFilterLoader.initialize(controller);
		craveFilterLoader.setOnLoadListener(new OnLoadListener<CategoryTree>() {

			@Override
			public void onLoadStarted() {
				loaderUtils.showLoading(null);
			}
			
			@Override
			public void onLoadCompleted(CategoryTree r) {
				loaderUtils.dismissLoading();

				if (r != null) {
					ArrayList<NavigationMenuItem> items = new ArrayList<NavigationMenuItem>();

					NavigationMenuItem item = new NavigationMenuItem();
					item.caption = "All " + HelperUtils.Strings.wordCase(r.getName());
					item.id = 0;
					item.label = item.caption;
					items.add(item);

					for (Category c : r.getChildren()) {
						item = new NavigationMenuItem();
						item.caption = HelperUtils.Strings.wordCase(c.getName());
						item.id = c.getId();
						item.label = item.caption;
						items.add(item);
					}
					
					craveFilterAdapter = new ArrayAdapter<NavigationMenuItem>(
							getActivity().getApplicationContext(), 
							R.layout.list_item_craves_filter,
							R.id.caption,
							items);
					
					filterMenuListView.setAdapter(craveFilterAdapter);
				}

				setItemCheckedForId(filterMenuListView, loader.categoryId);

				setupPopupMenu(categoryFilterMenu, filterMenuListView);
			}
			
		});
		
		return button;
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

	private Button setupPriceFilter(View v) {
		Button s = (Button) v.findViewById(R.id.price_filter_button);
		s.setOnClickListener(this);
		return s;
	}
	private Button setupRaveAndNewFilter(View v) {
		Button s = (Button) v.findViewById(R.id.price_filter_button);
		s.setOnClickListener(this);
		s.setText("Newest");
		loader.mostNew = true; //default
		return s;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {

		case R.id.category_filter_button:

			categoryFilter.setChecked(true);
			categoryFilterMenu.setOnDismissListener(new OnDismissListener () {

				@Override
				public void onDismiss() {
					categoryFilter.setChecked(false);
					categoryFilterMenu.setOnDismissListener(null);
				}
				
			});

			showPopupMenu(categoryFilterMenu, saves, Gravity.FILL, 100);

			break;
			
		case R.id.price_filter_button:
			if (signal.equals("saved")) {
				loader.applyPriceFilter(!loader.onSale);
				priceFilter.setText((loader.onSale) ? "On Sale" : "All Prices");
			}
			else if (signal.equals("shared")) {
				if (!loader.mostRaved) {
					loader.applyMostRavedFilter();
					priceFilter.setText("Most Raved");
				}
				else {
					loader.applyMostNewFilter();
					priceFilter.setText("Newest");
				}
			}
			update();
			break;
		}
		super.onClick(v);
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
				view.setLayoutParams(new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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

	public void removeCrave(int position) {
		saves.dismiss(position);
	}
	
	void showEmpty() {

		if (loader.isFiltered()) {
			loaderUtils.dismiss();
			return;
		}
		loaderUtils.showEmpty("Your 1st " + (signal.equals("saved") ? "Private" : "Shared") + " Crave will appear here. Save the Craves you love so you can find them later!");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		craveFilterLoader.load(categoryId);
		super.onActivityCreated(savedInstanceState);
	}

	public void update() {
		loader.loadUserList();
	}
	
	@Override
	public void onDestroyView() {
		filterMenuListView.setOnItemClickListener(null);
		filterMenuListView.setAdapter(null);
		filterMenuListView = null;
		categoryFilterMenu.setContentView(null);
		categoryFilterMenu = null;

		if (craveFilterAdapter != null) {
			craveFilterAdapter.clear();
			craveFilterAdapter = null;
		}
		craveFilterLoader.dispose();

		categoryFilter.setOnClickListener(null);
		priceFilter.setOnClickListener(null);
		categoryFilter = null;
		priceFilter = null;

		state.save(saves, "saves");
		adapter.dispose();
		adapter = null;
		saves = null;
		filterMenu.setContentView(null);
		super.onDestroyView();
	}

	@Override
	public void onLoadStarted() {
		saves.closeOpenedItems();
		loaderUtils.showLoading("Loading your " + (signal.equals("shared") ? "shared" : "private") + " craves...");
	}

	@Override
	public void onLoadCompleted(List<Sku> r) {
		if (r == null) {
			loaderUtils.showEmpty("We had a problem loading your private craves. Click here to try loading them again.");
			loaderUtils.setOnClickListener(new OnClickListener () {
				public void onClick(View v) {
					loaderUtils.dismissEmpty();
					update();
				}
			});
		}
		else if (r.size() == 0)
			showEmpty();
		else {
			loaderUtils.dismiss();
			state.restore(saves, "saves");
		}
	}
	
	
	public void saveRecommendation(final Sku savedCrave, boolean save, final OnLoadListener<Integer> listener) {
		listener.onLoadStarted();

		new AsyncTask<Object, Void, Integer>() {
			@Override
			protected Integer doInBackground(Object... params) {

				UserInfo ui = controller.getCobrain().getUserInfo();
				boolean save = (Boolean) params[0];
				
				if (save) {
					if (ui.addToPrivateRack(savedCrave)) {
						return 1;
					}
				}
				else {
					if (ui.removeProduct(savedCrave)) {
						return 2;
					}
				}
				
				return 0;
			}

			@Override
			protected void onPostExecute(Integer result) {
				listener.onLoadCompleted(result);
			}
			
		}.execute(save);
	}

	public void shareRecommendation(final Sku savedCrave, boolean share, final OnLoadListener<Integer> listener) {
		listener.onLoadStarted();

		new AsyncTask<Object, Void, Integer>() {
			@Override
			protected Integer doInBackground(Object... params) {

				UserInfo ui = controller.getCobrain().getUserInfo();
				Boolean isShared = (Boolean)params[0];
				
				if (isShared) {
					if (ui.addToSharedRack(savedCrave)) {
						return 1;
					}
				}
				else {
					if (ui.addToPrivateRack(savedCrave)) {
						return 2;
					}
				}

				return 0;
			}

			@Override
			protected void onPostExecute(Integer result) {
				//show result of is published change
				listener.onLoadCompleted(result);
			}
			
		}.execute(share);
	}

	public void showRavesUserList(String itemId) {
		controller.showRavesUserList(itemId);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		NavigationMenuItem mi = (NavigationMenuItem) parent.getAdapter().getItem(position);
		SavedAndShareFragment.this.loader.applyCategoryFilter(mi.id);
		categoryFilterMenu.dismiss();
	}	
	
}
