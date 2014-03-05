package com.cobrain.android.loaders;

import android.os.AsyncTask;

import com.cobrain.android.adapters.FeedListAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.Feeds;
import com.cobrain.android.model.UserInfo;

public class FeedLoader {

	FeedListAdapter adapter;
	CobrainController controller;
	private AsyncTask<Void, Void, Feeds> currentRequest;
	private OnLoadListener<Feeds> onLoadListener;
	private Feeds feeds;
	public int itemHeight;
	private boolean pause;

	public void initialize(CobrainController controller,
			FeedListAdapter adapter) {
		this.controller = controller;
		this.adapter = adapter;
		itemHeight = adapter.getItemHeight();
	}

	public void dispose() {
		cancel();
		adapter = null;
		controller = null;
	}

	public void loadFeedList() {
		if (pause) return;
		if (onLoadListener != null) onLoadListener.onLoadStarted();

		currentRequest = new AsyncTask<Void, Void, Feeds>() {

			@Override
			protected Feeds doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();

				if (u != null) {
					//if (lists == null)
					feeds = u.getFeeds();
					return feeds;
				}

				return null;
			}

			@Override
			protected void onPostExecute(Feeds result) {
				if (!isCancelled() && !pause) {
					if (result != null) {
						adapter.clear();
						adapter.addAll(result.getFeeds());
					}
					if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
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
	
	public void setOnLoadListener(OnLoadListener<Feeds> listener) {
		onLoadListener = listener;
	}

	public void pauseLoad(boolean pause) {
		this.pause = pause;
		if (pause) cancel();
	}
}
