package com.cobrain.android.fragments;

import java.util.ArrayList;

import com.cobrain.android.R;
import com.cobrain.android.adapters.SavedAndShareAdapter;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.SavedAndShareLoader;
import com.cobrain.android.model.WishListItem;
import com.cobrain.android.model.Product;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.LoaderUtils;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.PopupWindow;

public class SavedAndShareFragment extends BaseCobrainFragment implements OnLoadListener<ArrayList<WishListItem>> {
	public static final String TAG = "SavedAndShareFragment";
	private SwipeListView saves;
	private SavedAndShareAdapter adapter;
	SavedAndShareLoader loader = new SavedAndShareLoader();
	PopupWindow filterMenu = new PopupWindow();
	ArrayList<WishListItem> sharedCraves = new ArrayList<WishListItem>();
	
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
		adapter = new SavedAndShareAdapter(container.getContext(), R.id.item_info, sharedCraves, this);
		saves.setAdapter(adapter);
		loaderUtils.initialize((ViewGroup) v);
		loader.initialize(controller, adapter);
		loader.setOnLoadListener(this);

		setTitle("My Private Craves");

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
	public void onLoadCompleted(ArrayList<WishListItem> r) {
		if (r == null) {
			loaderUtils.showEmpty("We had a problem loading your private craves. Click here to try loading them again.");
			loaderUtils.setOnClickListener(new OnClickListener () {
				public void onClick(View v) {
					update();
				}
			});
		}
		else if (r.size() == 0)
			showEmpty();
		else {
			loaderUtils.dismissLoading();
			state.restore(saves, "saves");
		}
	}
	
	
	public void saveRecommendation(final WishListItem savedCrave, boolean save, final OnLoadListener<Integer> listener) {
		listener.onLoadStarted();

		new AsyncTask<Object, Void, Integer>() {
			@Override
			protected Integer doInBackground(Object... params) {

				UserInfo ui = controller.getCobrain().getUserInfo();
				String wishListId = ui.getWishListId();
				Product p = ((WishListItem) params[0]).getProduct();
				boolean save = (Boolean) params[1];
				
				if (save) {
					if (ui.addToList(wishListId, p.getId(), false)) {
						return 1;
					}
				}
				else {
					if (ui.removeFromList(wishListId, savedCrave.getId())) {
						return 2;
					}
				}
				
				return 0;
			}

			@Override
			protected void onPostExecute(Integer result) {
				listener.onLoadCompleted(result);
			}
			
		}.execute(savedCrave, save);
	}

	public void shareRecommendation(final WishListItem savedCrave, boolean share, final OnLoadListener<Integer> listener) {
		listener.onLoadStarted();

		new AsyncTask<Object, Void, Integer>() {
			@Override
			protected Integer doInBackground(Object... params) {

				UserInfo ui = controller.getCobrain().getUserInfo();
				String wishListId = ui.getWishListId();
				String itemId = ((WishListItem)params[0]).getId();
				Boolean isShared = (Boolean)params[1];
				
				if (itemId == null) {
					if (ui.addToList(wishListId, savedCrave.getProduct().getId(), true)) {
						return 1;
					}
				}
				else {
					if (ui.publishListItem(wishListId, itemId, isShared)) { 
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
			
		}.execute(savedCrave, share);
	}

	public void showRavesUserList(String itemId) {
		controller.showRavesUserList(itemId);
	}	
	
}
