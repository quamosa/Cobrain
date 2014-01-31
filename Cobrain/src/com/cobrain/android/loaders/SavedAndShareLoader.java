package com.cobrain.android.loaders;

import java.util.ArrayList;

import android.os.AsyncTask;
import com.cobrain.android.adapters.SavedAndShareAdapter;
import com.cobrain.android.model.WishListItem;
import com.cobrain.android.model.WishList;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.service.Cobrain;
import com.cobrain.android.service.Cobrain.CobrainController;

public class SavedAndShareLoader {
	SavedAndShareAdapter adapter;
	CobrainController controller;
	OnLoadListener<ArrayList<WishListItem>> onLoadListener;
	private AsyncTask<Void, Void, ArrayList<WishListItem>> currentRequest;
	ArrayList<WishList> lists = null;

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
	
	public void loadUserList() {
		if (onLoadListener != null) onLoadListener.onLoadStarted();

		currentRequest = new AsyncTask<Void, Void, ArrayList<WishListItem>>() {
			@Override
			protected ArrayList<WishListItem> doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();

				if (u != null) {
					if (lists == null)
						lists = u.getLists();

					ArrayList<WishListItem> items = new ArrayList<WishListItem>();
					
					for (WishList lr : lists) {
						//FIXME: Question?: what about shared lists.. ? im only showing my own!
						if (lr.getOwner().getId().equals(u.getUserId())) {
							WishList ml = u.getList(lr.getId());
							for (WishListItem item : ml.getItems())
								if (!item.isPublic()) items.add(item);
						}
					}
					
					return items;
				}

				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<WishListItem> result) {
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
	
	public void setOnLoadListener(OnLoadListener<ArrayList<WishListItem>> listener) {
		onLoadListener = listener;
	}

}
