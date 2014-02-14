package com.cobrain.android.loaders;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.cobrain.android.adapters.CraveStripPagerAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.UserInfo;

public class CraveStripLoader<T> {
	//int page;
	int countPerPage = 25; //50 no longer returns results
	CraveStripPagerAdapter<T> adapter;
	CobrainController controller;
	OnLoadListener<T> onLoadListener;
	private AsyncTask<Void, Void, T> currentRequest;
	protected ArrayList<Integer> pagesLoaded = new ArrayList<Integer>();
	public boolean refresh;

	public boolean isPageLoaded(int page) {
		return pagesLoaded.contains(page);
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
	
	public void initialize(CobrainController controller, CraveStripPagerAdapter<T> adapter) {
		this.controller = controller;
		this.adapter = adapter;
		onInitialize();
	}
	
	void onInitialize() {}
	
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
				performLoad(countPerPage, page);
			}
		//}
	}
	
	void performLoad(final int countPerPage, final int page) {
		pagesLoaded.add(page);

		if (onLoadListener != null) onLoadListener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, T>() {
			@Override
			protected T doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				T t = onPerformLoad(u);
				refresh = false;
				return t;
			}

			@Override
			protected void onPostExecute(T result) {
				if (result != null) {
					//int pg = result.getPage();
					//CraveLoader.this.page = pg;
					//pagesLoaded.add(pg);
				}
				else {
					pagesLoaded.remove((Integer)page);
				}
				onLoadCompleted(result);
				adapter.load(result);
				if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
				currentRequest = null;
			}
			
		}.execute();			
	}

	protected T onPerformLoad(UserInfo u) { return null; }
	protected void onLoadCompleted(T result) {}

	public void cancel() {
		if (currentRequest != null) {
			currentRequest.cancel(true);
			currentRequest = null;
		}
	}
	
	public void setOnLoadListener(OnLoadListener<T> listener) {
		onLoadListener = listener;
	}

	public void clearPages() {
		pagesLoaded.clear();
	}

}
