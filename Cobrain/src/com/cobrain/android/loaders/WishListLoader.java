package com.cobrain.android.loaders;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.os.Debug;
import com.cobrain.android.adapters.WishListPagerAdapter;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.WishList;
import com.cobrain.android.model.WishListItem;
import com.cobrain.android.service.Cobrain;
import com.cobrain.android.service.Cobrain.CobrainController;

public class WishListLoader {
	String listId;
	int page;
	int categoryId = 1;
	int countPerPage = 5; //50 no longer returns results
	WishListPagerAdapter adapter;
	CobrainController controller;
	OnLoadListener<ArrayList<WishListItem>> onLoadListener;
	private AsyncTask<Void, Void, ArrayList<WishListItem>> currentRequest;
	private ArrayList<Integer> pagesLoaded = new ArrayList<Integer>();
	boolean onSale;
	boolean showPrivateOrPublic; //private is true

	public String getListId() {
		return listId;
	}

	public boolean setMyListId(String listId, boolean showPrivateOrPublic) {
		if (listId != this.listId || showPrivateOrPublic != this.showPrivateOrPublic) {
			this.showPrivateOrPublic = showPrivateOrPublic;
			this.listId = listId;
			page = 0;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}
	
	public boolean setListId(String listId) {
		return setMyListId(listId, false);
	}

	public int getCategoryId() {
		return categoryId;
	}

	public boolean setCategoryId(int categoryId) {
		if (categoryId != this.categoryId) {
			this.categoryId = categoryId;
			page = 0;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}
	public boolean setOnSaleRecommendationsOnly(boolean onSale) {
		if (this.onSale != onSale) {
			this.onSale = onSale;
			page = 0;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}

	public int getCountPerPage() {
		return countPerPage;
	}

	public void setCountPerPage(int countPerPage) {
		this.countPerPage = countPerPage;
	}

	public int getPage() {
		return page;
	}
	
	public void initialize(CobrainController controller, WishListPagerAdapter adapter) {
		this.controller = controller;
		this.adapter = adapter;
	}
	
	public void dispose() {
		cancel();
		controller = null;
		adapter = null;
		onLoadListener = null;
	}

	public void setPage(int page) {
		if (page < 1) page = 1;
		else if (page > 1 && page > adapter.getMaxPages()) page = adapter.getMaxPages();
		
		if (this.page != page) {
			if (!pagesLoaded.contains(page)) {
				loadWishList(listId, countPerPage, page);
			}
		}
	}
	
	void loadWishList(final String listId, final int countPerPage, final int page) {
		if (onLoadListener != null) onLoadListener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, ArrayList<WishListItem>>() {
			WishList r = null;

			@Override
			protected ArrayList<WishListItem> doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				ArrayList<WishListItem> items = new ArrayList<WishListItem>();
				
				if (u != null) {
					r = u.getList(listId);
					if (r != null) {
						for (WishListItem item : r.getItems()) {
							if (!item.isPublic() == showPrivateOrPublic) {
								items.add(item);
							}
						}
					}
				}
				
				return items;
			}

			@Override
			protected void onPostExecute(ArrayList<WishListItem> result) {
				if (result != null) {
					int pg = 1; //result.getPage();
					WishListLoader.this.page = pg;
					pagesLoaded.add(pg);
				}
				if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
				adapter.load(r, result, (page > 1));
				r = null;
				currentRequest = null;
			}
			
		}.execute();			
	}

	public void cancel() {
		if (currentRequest != null) {
			currentRequest.cancel(true);
			currentRequest = null;
		}
	}
	
	public void setOnLoadListener(OnLoadListener<ArrayList<WishListItem>> listener) {
		onLoadListener = listener;
	}
}
