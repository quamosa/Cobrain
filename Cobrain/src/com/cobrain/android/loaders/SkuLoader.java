package com.cobrain.android.loaders;

import java.util.ArrayList;
import android.os.AsyncTask;
import com.cobrain.android.adapters.SkuPagerAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;

public class SkuLoader {
	int categoryId = 0;
	int countPerPage = 25;
	SkuPagerAdapter adapter;
	CobrainController controller;
	OnLoadListener<Skus> onLoadListener;
	private AsyncTask<Void, Void, Skus> currentRequest;
	private ArrayList<Integer> pagesLoaded = new ArrayList<Integer>();
	boolean onSale;
	private User owner;
	private String signal;

	public int getCategoryId() {
		return categoryId;
	}

	public boolean isPageLoaded(int page) {
		return pagesLoaded.contains(page);
	}
	
	public boolean setCategoryId(int categoryId) {
		if (categoryId != this.categoryId) {
			this.categoryId = categoryId;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}
	public boolean setOnSaleRecommendationsOnly(boolean onSale) {
		if (this.onSale != onSale) {
			this.onSale = onSale;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}

	public boolean setOwner(User owner) {
		if (this.owner != owner) {
			this.owner = owner;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}

	public boolean setSignal(String signal) {
		if (this.signal != signal) {
			this.signal = signal;
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

	//public int getPage() {
	//	return page;
	//}
	
	public void initialize(CobrainController controller, SkuPagerAdapter adapter) {
		this.controller = controller;
		this.adapter = adapter;
		//page = 0;
		categoryId = 0;
	}
	
	public void dispose() {
		cancel();
		controller = null;
		adapter = null;
		onLoadListener = null;
	}
	
	public void loadPage(int page) {
		if (page < 1) page = 1;
		else if (page > 1 && adapter.getMaxPages() > 0 && page > adapter.getMaxPages()) page = adapter.getMaxPages();
		
		//if (this.page != page) {
			if (!pagesLoaded.contains(page)) {
				loadRecommendations(owner, signal, countPerPage, page);
			}
		//}
	}
	
	void loadRecommendations(final User owner, final String signal, final int countPerPage, final int page) {
		pagesLoaded.add(page);

		if (onLoadListener != null) onLoadListener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, Skus>() {
			@Override
			protected Skus doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				
				if (u != null) {
					return u.getSkus(owner, signal, null, null);
				}
				
				return null;
			}

			@Override
			protected void onPostExecute(Skus result) {
				if (result == null) {
					pagesLoaded.remove((Integer)page);
					SkuLoader.this.categoryId = 0;
				}
				loadSkus(result);
				currentRequest = null;
			}
			
		}.execute();			
	}

	public void loadSkus(Skus skus) {
		if (skus != null && !pagesLoaded.contains(1)) {
			pagesLoaded.add(1);
		}
		adapter.load(skus.get());
		if (onLoadListener != null) onLoadListener.onLoadCompleted(skus);
	}
	
	public void cancel() {
		if (currentRequest != null) {
			currentRequest.cancel(true);
			currentRequest = null;
		}
	}
	
	public void setOnLoadListener(OnLoadListener<Skus> listener) {
		onLoadListener = listener;
	}

}
