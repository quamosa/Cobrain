package com.cobrain.android.loaders;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.cobrain.android.adapters.FriendsListAdapter;
import com.cobrain.android.model.WishList;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.service.Cobrain;
import com.cobrain.android.service.Cobrain.CobrainController;

public class FriendsListLoader {

	FriendsListAdapter adapter;
	CobrainController controller;
	ArrayList<WishList> items = new ArrayList<WishList>();
	private AsyncTask<Void, Void, ArrayList<WishList>> currentRequest;
	private OnLoadListener<ArrayList<WishList>> onLoadListener;
	private ArrayList<WishList> lists;

	public void initialize(CobrainController controller,
			FriendsListAdapter adapter) {
		this.controller = controller;
		this.adapter = adapter;
	}

	public ArrayList<WishList> getItems() {
		return items;
	}

	public void dispose() {
		cancel();
		adapter = null;
		controller = null;
		items.clear();
	}

	public void loadFriendList() {
		if (onLoadListener != null) onLoadListener.onLoadStarted();

		currentRequest = new AsyncTask<Void, Void, ArrayList<WishList>>() {

			@Override
			protected ArrayList<WishList> doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();

				if (u != null) {
					//if (lists == null)
						lists = u.getLists();

					ArrayList<WishList> items = new ArrayList<WishList>();

					if (lists != null)
						for (WishList lr : lists)
							if (!lr.getOwner().getId().equals(u.getUserId()))
								items.add(lr);
					
					return items;
				}

				return null;
			}

			@Override
			protected void onPostExecute(ArrayList<WishList> result) {
				if (!isCancelled()) {
					if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
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
	
	public void setOnLoadListener(OnLoadListener<ArrayList<WishList>> listener) {
		onLoadListener = listener;
	}
}
