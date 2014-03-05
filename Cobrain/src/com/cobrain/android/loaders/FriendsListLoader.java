package com.cobrain.android.loaders;

import java.util.ArrayList;

import android.os.AsyncTask;

import com.cobrain.android.adapters.FriendsListAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.Friendships;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.Friendship;

public class FriendsListLoader {

	FriendsListAdapter adapter;
	CobrainController controller;
	ArrayList<Friendship> items = new ArrayList<Friendship>();
	private AsyncTask<Void, Void, Friendships> currentRequest;
	private OnLoadListener<Friendships> onLoadListener;
	private Friendships friends;
	private boolean pause;

	public void initialize(CobrainController controller,
			FriendsListAdapter adapter) {
		this.controller = controller;
		this.adapter = adapter;
	}

	public ArrayList<Friendship> getItems() {
		return items;
	}

	public void dispose() {
		cancel();
		adapter = null;
		controller = null;
		items.clear();
	}

	public void loadFriendList() {
		if (pause) return;
		if (onLoadListener != null) onLoadListener.onLoadStarted();

		currentRequest = new AsyncTask<Void, Void, Friendships>() {

			@Override
			protected Friendships doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();

				if (u != null) {
					//if (lists == null)
					friends = u.getFriendships();
					return friends;
				}

				return null;
			}

			@Override
			protected void onPostExecute(Friendships result) {
				if (!isCancelled() && !pause) {
					if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
					if (result != null) {
						adapter.clear();
						adapter.addAll(result.getFriendships());
					}
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
	
	public void setOnLoadListener(OnLoadListener<Friendships> listener) {
		onLoadListener = listener;
	}

	public void pauseLoad(boolean pause) {
		this.pause = pause;
		if (pause) cancel();
	}
}
