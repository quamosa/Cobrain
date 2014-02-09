package com.cobrain.android.loaders;

import java.util.List;

import android.os.AsyncTask;
import com.cobrain.android.adapters.SavedAndShareAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.UserInfo;

public class SavedAndShareLoader {
	SavedAndShareAdapter adapter;
	CobrainController controller;
	OnLoadListener<List<Sku>> onLoadListener;
	private AsyncTask<Void, Void, List<Sku>> currentRequest;
	int categoryId = 0;
	int priceId = 0;
	private boolean onSale;

	public void initialize(CobrainController controller, SavedAndShareAdapter adapter) {
		this.controller = controller;
		this.adapter = adapter;
	}

	public void setAdapter(SavedAndShareAdapter adapter) {
		this.adapter = adapter;
	}

	public void dispose() {
		cancel();
		controller = null;
		adapter = null;
		onLoadListener = null;
	}

	public void applyCategoryFilter(int id) {
		if (categoryId != id) {
			categoryId = id;
			loadUserList();
		}
	}
	
	public void applyPriceFilter(boolean onSale) {
		if (this.onSale != onSale) {
			this.onSale = onSale;
			loadUserList();
		}
	}
	
	public void loadUserList() {
		if (onLoadListener != null) onLoadListener.onLoadStarted();

		currentRequest = new AsyncTask<Void, Void, List<Sku>>() {
			@Override
			protected List<Sku> doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();

				if (u != null) {
					Skus s = u.getSkus(null, "saved", categoryId, onSale);
					if (s != null) return s.get();
				}

				return null;
			}

			@Override
			protected void onPostExecute(List<Sku> result) {
				adapter.clear();
				adapter.addAll(result);
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
	
	public void setOnLoadListener(OnLoadListener<List<Sku>> listener) {
		onLoadListener = listener;
	}

}
