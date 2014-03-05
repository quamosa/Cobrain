package com.cobrain.android.controllers;

import java.util.List;

import it.sephiroth.android.library.widget.HListView;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.adapters.CraveStripPagerAdapter;
import com.cobrain.android.loaders.CraveStripLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.model.Sku;

public class CraveStrip<T> {

	public static final int STRIP_TYPE_CRAVES = 0;
	public static final int STRIP_TYPE_CRAVES_NOT_AVAILABLE = 1;
	public static final int STRIP_TYPE_HEADER = 2;
	
	public String caption;
	public CraveStripListAdapter<T> allStripsAdapter;
	public CraveStripPagerAdapter<T> adapter;
	public CraveStripLoader<T> loader;
	public ViewPager pager;
	public RelativeLayout container;
	public HListView list;
	public Adapter listAdapter;
	public int type;

	public interface OnRefreshCompletedListener {
		public void onRefreshCompleted();
	}
	
	OnRefreshCompletedListener refreshListener;
	OnLoadListener<T> listener;
	OnLoadListener<T> _listener = new OnLoadListener<T>() {

		@Override
		public void onLoadStarted() {
			onLoadHasStarted();
			listener.onLoadStarted();
		}
		
		@Override
		public void onLoadCompleted(T r) {
			if (refreshListener != null) refreshListener.onRefreshCompleted();
			listener.onLoadCompleted(r);
			onLoadWasCompleted(r);
		}
	};

	void onLoadHasStarted() {}
	void onLoadWasCompleted(T r) {}
	
	public CraveStrip(CraveStripListAdapter<T> allStripsAdapter, OnLoadListener<T> listener) {
		this.allStripsAdapter = allStripsAdapter;
		this.listener = listener;
	}
	
	public void dispose() {
		refreshListener = null;
		
		list.setTag(null);
		list.setAdapter(null);
		
		allStripsAdapter = null;
		
		if (adapter != null) {
			adapter.dispose();
			adapter = null;
		}
		listAdapter = null;
		if (loader != null) {
			loader.dispose();
			loader = null;
		}
		pager = null;
		container = null;
		listener = null;
		_listener = null;
	}
	
	void onLoadCompleted(T r) {}
	protected void onLoad(CraveStripLoader<T> loader) {}
	
	public void load() {
		//if (pager.getAdapter() != adapter) {
		if (list.getAdapter() != listAdapter) {
			if (loader != null) {
				onLoad(loader);
				loader.loadPage(1);
			}

			list.setAdapter((ListAdapter) listAdapter);
			//pager.setAdapter(adapter);
		}
	}

	public void refresh() {
		refresh(null);
	}
	public void refresh(OnRefreshCompletedListener listener) {
		refreshListener = listener;
		loader.clearPages();
		loader.refresh = true;
		loader.loadPage(1);
	}

	public List<Sku> getSkus() {
		return adapter.getRecommendations();
	}
	public void setCaption(String text) {
		if (!TextUtils.equals(caption, text)) {
			caption = text;
			allStripsAdapter.notifyDataSetChanged();
		}
	}

}