package com.cobrain.android.loaders;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import com.cobrain.android.adapters.WishListPagerAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.HelperUtils;

public class WishListLoader {
	User owner;
	int page;
	int categoryId = 1;
	int countPerPage = 5; //50 no longer returns results
	WishListPagerAdapter adapter;
	CobrainController controller;
	OnLoadListener<List<Sku>> onLoadListener;
	private AsyncTask<Void, Void, List<Sku>> currentRequest;
	private ArrayList<Integer> pagesLoaded = new ArrayList<Integer>();
	boolean onSale;
	boolean showPrivateOrPublic; //private is true

	public User getListId() {
		return owner;
	}

	public boolean setMyListId(User owner, boolean showPrivateOrPublic) {
		if (owner != this.owner || showPrivateOrPublic != this.showPrivateOrPublic) {
			this.showPrivateOrPublic = showPrivateOrPublic;
			this.owner = owner;
			page = 0;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}
	
	public boolean setListId(User owner) {
		return setMyListId(owner, false);
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void clearPagesLoaded() {
		page = 0;
		pagesLoaded.clear();
	}
	
	public boolean setCategoryId(int categoryId) {
		if (categoryId != this.categoryId) {
			this.categoryId = categoryId;
			clearPagesLoaded();
			return true;
		}
		return false;
	}
	public boolean setOnSaleRecommendationsOnly(boolean onSale) {
		if (this.onSale != onSale) {
			this.onSale = onSale;
			clearPagesLoaded();
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
		owner = null;
		adapter = null;
		onLoadListener = null;
	}

	public void setPage(int page) {
		if (page < 1) page = 1;
		else if (page > 1 && page > adapter.getMaxPages()) page = adapter.getMaxPages();
		
		if (this.page != page) {
			if (!pagesLoaded.contains(page)) {
				loadWishList(countPerPage, page);
			}
		}
	}
	
	void loadWishList(final int countPerPage, final int page) {
		if (onLoadListener != null) onLoadListener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, List<Sku>>() {

			Skus r = null;
			
			@Override
			protected List<Sku> doInBackground(Void... params) {
				if (HelperUtils.Tasks.asyncTaskCancel(this, controller == null)) return null;
				
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				List<Sku> items = null;
				
				if (u != null) {
					r = u.getSkus(owner, showPrivateOrPublic ? "saved" : "shared", null, null);
					if (r != null) items = r.get();
				}
				
				return items;
			}

			@Override
			protected void onCancelled() {
				r = null;
				super.onCancelled();
			}

			@Override
			protected void onPostExecute(List<Sku> result) {
				if (adapter != null) {
					if (result != null) {
						int pg = 1; //result.getPage();
						WishListLoader.this.page = pg;
						pagesLoaded.add(pg);
					}
					adapter.load(r, result, (page > 1));
					if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
				}
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
	
	public void setOnLoadListener(OnLoadListener<List<Sku>> listener) {
		onLoadListener = listener;
	}
}
