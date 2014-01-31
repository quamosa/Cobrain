package com.cobrain.android.loaders;

import java.util.ArrayList;

import android.os.AsyncTask;
import com.cobrain.android.adapters.CravePagerAdapter;
import com.cobrain.android.model.RecommendationsResults;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.service.Cobrain;
import com.cobrain.android.service.Cobrain.CobrainController;

public class CraveLoader {
	//int page;
	int categoryId = 0;
	int countPerPage = 25; //50 no longer returns results
	CravePagerAdapter adapter;
	CobrainController controller;
	OnLoadListener<RecommendationsResults> onLoadListener;
	private AsyncTask<Void, Void, RecommendationsResults> currentRequest;
	private ArrayList<Integer> pagesLoaded = new ArrayList<Integer>();
	boolean onSale;

	public int getCategoryId() {
		return categoryId;
	}

	public boolean isPageLoaded(int page) {
		return pagesLoaded.contains(page);
	}
	
	public boolean setCategoryId(int categoryId) {
		if (categoryId != this.categoryId) {
			this.categoryId = categoryId;
			//page = 0;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}
	public boolean setOnSaleRecommendationsOnly(boolean onSale) {
		if (this.onSale != onSale) {
			this.onSale = onSale;
			//page = 0;
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
	
	public void initialize(CobrainController controller, CravePagerAdapter adapter) {
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
				loadRecommendations(categoryId, countPerPage, page);
			}
		//}
	}
	
	void loadRecommendations(final int categoryId, final int countPerPage, final int page) {
		pagesLoaded.add(page);

		if (onLoadListener != null) onLoadListener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, RecommendationsResults>() {
			@Override
			protected RecommendationsResults doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				RecommendationsResults r = null;
				
				if (u != null) {
					r = u.getRecommendations(categoryId, countPerPage, page, onSale);
				}
				//else Debug.waitForDebugger();
				
				return r;
			}

			@Override
			protected void onPostExecute(RecommendationsResults result) {
				if (result != null) {
					//int pg = result.getPage();
					//CraveLoader.this.page = pg;
					//pagesLoaded.add(pg);
				}
				else {
					pagesLoaded.remove((Integer)page);
					CraveLoader.this.categoryId = 0;
				}
				adapter.load(result);
				if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
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
	
	public void setOnLoadListener(OnLoadListener<RecommendationsResults> listener) {
		onLoadListener = listener;
	}

}
