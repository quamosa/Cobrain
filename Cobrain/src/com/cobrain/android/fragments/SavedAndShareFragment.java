package com.cobrain.android.fragments;

import java.util.List;

import com.cobrain.android.R;
import com.cobrain.android.adapters.CraveCategoryFilterAdapter;
import com.cobrain.android.adapters.CravePriceFilterAdapter;
import com.cobrain.android.adapters.NavigationMenuItem;
import com.cobrain.android.adapters.SavedAndShareAdapter;
import com.cobrain.android.loaders.CraveFilterLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.SavedAndShareLoader;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.v1.CategoryTree;
import com.cobrain.android.utils.LoaderUtils;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.PopupWindow;
import android.widget.Spinner;

public class SavedAndShareFragment extends BaseCobrainFragment implements OnLoadListener<List<Sku>> {
	public static final String TAG = "SavedAndShareFragment";
	private SwipeListView saves;
	private SavedAndShareAdapter adapter;
	SavedAndShareLoader loader = new SavedAndShareLoader();
	PopupWindow filterMenu = new PopupWindow();
	Spinner priceFilter;
	Spinner categoryFilter;
	
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
		
		View v = inflater.inflate(R.layout.main_saved_craves_frame, null);
		saves = (SwipeListView) v.findViewById(R.id.saves_list);
		adapter = new SavedAndShareAdapter(container.getContext(), R.id.item_info, this);
		saves.setAdapter(adapter);
		loaderUtils.initialize((ViewGroup) v);
		loader.initialize(controller, adapter);
		loader.setOnLoadListener(this);

		setTitle("My Private Craves");
		
		priceFilter = setupPriceFilter(v);
		categoryFilter = setupCategoryFilter(v);

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
	
	private Spinner setupCategoryFilter(View v) {
		final Spinner s = (Spinner) v.findViewById(R.id.category_filter);
		final CraveFilterLoader loader = new CraveFilterLoader();

		loader.initialize(controller);
		loader.setOnLoadListener(new OnLoadListener<CategoryTree>() {
	
			@Override
			public void onLoadStarted() {
			}
	
			@Override
			public void onLoadCompleted(CategoryTree r) {
				loader.dispose();

				CraveCategoryFilterAdapter adapter = new CraveCategoryFilterAdapter(getActivity().getApplicationContext(), R.layout.list_item_craves_filter, R.id.caption);
				if (r != null)
					adapter.addAll(r.getChildren());
				s.setAdapter(adapter);
			}
			
		});
		
		int categoryId = getResources().getInteger(R.integer.default_category_id); // apparel (6)
		loader.load(categoryId);

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
	}
	
	private Spinner setupPriceFilter(View v) {
		Spinner s = (Spinner) v.findViewById(R.id.price_filter);
		CravePriceFilterAdapter adapter = new CravePriceFilterAdapter(getActivity().getApplicationContext(), R.layout.list_item_craves_filter, R.id.caption);
		NavigationMenuItem mi = new NavigationMenuItem();

		mi.id = 0;
		mi.caption = mi.label = mi.labelCopy = "All Prices";
		adapter.add(mi);

		mi = new NavigationMenuItem();
		mi.id = 1;
		mi.caption = mi.label = mi.labelCopy = "On Sale";
		adapter.add(mi);

		s.setAdapter(adapter);
		s.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				loader.applyPriceFilter(id == 1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		return s;
	}

	
	public void removeCrave(int position) {
		saves.dismiss(position);
	}
	
	void showEmpty() {
		loaderUtils.showEmpty("Your 1st Saved Crave will appear here. Save the Craves you love so you can find them later!");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		update();
		super.onActivityCreated(savedInstanceState);
	}

	public void update() {
		loader.loadUserList();
	}
	
	@Override
	public void onDestroyView() {
		CravePriceFilterAdapter a = (CravePriceFilterAdapter) priceFilter.getAdapter();
		priceFilter.setAdapter(null);
		a.clear();
		priceFilter = null;

		CraveCategoryFilterAdapter b = (CraveCategoryFilterAdapter) categoryFilter.getAdapter();
		categoryFilter.setAdapter(null);
		b.clear();
		categoryFilter = null;
		
		state.save(saves, "saves");
		adapter.dispose();
		adapter = null;
		saves = null;
		filterMenu.setContentView(null);
		super.onDestroyView();
	}

	@Override
	public void onLoadStarted() {
		loaderUtils.showLoading("Loading your private craves...");
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
	
}
