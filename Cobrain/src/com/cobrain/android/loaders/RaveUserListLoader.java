package com.cobrain.android.loaders;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.cobrain.android.adapters.RaveUserListAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.Rave;

public class RaveUserListLoader {

	RaveUserListAdapter adapter;
	CobrainController controller;
	private AsyncTask<Void, Void, List<Rave>> currentRequest;
	private OnLoadListener<List<Rave>> onLoadListener;
	ArrayList<Rave> items = new ArrayList<Rave>();

	public void initialize(CobrainController controller,
			RaveUserListAdapter adapter) {
		this.controller = controller;
		this.adapter = adapter;
	}

	public ArrayList<Rave> getItems() {
		return items;
	}

	public void dispose() {
		items.clear();
		adapter = null;
		controller = null;
	}

	public void loadFriendList(final int itemId) {
		if (onLoadListener != null) onLoadListener.onLoadStarted();

		currentRequest = new AsyncTask<Void, Void, List<Rave>>() {

			@Override
			protected List<Rave> doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();

				if (u != null) {
					Skus skus = u.getSkus(u, "shared", null, null);

					if (skus != null) {
						for (Sku item : skus.get()) {
							if (item.getId() == itemId) {
								return item.getRaves();
							}
						}
					}
				}

				return null;
			}

			@Override
			protected void onPostExecute(List<Rave> result) {
				if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
				if (result != null) {
					adapter.clear();
					adapter.addAll(result);
				}
				
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
	
	public void setOnLoadListener(OnLoadListener<List<Rave>> listener) {
		onLoadListener = listener;
	}

}
