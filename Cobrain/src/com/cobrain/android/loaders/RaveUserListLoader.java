package com.cobrain.android.loaders;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.cobrain.android.adapters.FriendsListAdapter;
import com.cobrain.android.adapters.RaveUserListAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.v1.Rave;
import com.cobrain.android.model.v1.WishList;
import com.cobrain.android.model.v1.WishListItem;

public class RaveUserListLoader {

	RaveUserListAdapter adapter;
	CobrainController controller;
	private AsyncTask<Void, Void, ArrayList<Rave>> currentRequest;
	private OnLoadListener<ArrayList<Rave>> onLoadListener;
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

	public void loadFriendList(final String itemId) {
		if (onLoadListener != null) onLoadListener.onLoadStarted();

		currentRequest = new AsyncTask<Void, Void, ArrayList<Rave>>() {

			@Override
			protected ArrayList<Rave> doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();

				if (u != null) {
					WishList list = u.getCachedWishList();

					if (list != null) {
						for (WishListItem item : list.getItems()) {
							if (item.getId().equals(itemId)) {
								return item.getRaves();
							}
						}
					}
				}

				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<Rave> result) {
				if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
				adapter.clear();
				adapter.addAll(result);
				
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
	
	public void setOnLoadListener(OnLoadListener<ArrayList<Rave>> listener) {
		onLoadListener = listener;
	}

}
