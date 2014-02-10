package com.cobrain.android.controllers;

import java.util.List;

import it.sephiroth.android.library.widget.HListView;
import android.support.v4.view.ViewPager;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.adapters.CraveStripPagerAdapter;
import com.cobrain.android.loaders.CraveStripLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.model.Scenario;
import com.cobrain.android.model.Sku;

public class CraveStrip<T> {

	public static final int STRIP_TYPE_CRAVES = 0;
	public static final int STRIP_TYPE_CRAVES_NOT_AVAILABLE = 1;
	
	public String caption;
	public CraveStripListAdapter allStripsAdapter;
	public CraveStripPagerAdapter<T> adapter;
	public CraveStripLoader<T> loader;
	public ViewPager pager;
	public RelativeLayout container;
	public HListView list;
	public Adapter listAdapter;
	public int type;

	OnLoadListener<T> listener;
	OnLoadListener<T> _listener = new OnLoadListener<T>() {

		@Override
		public void onLoadStarted() {
			listener.onLoadStarted();
		}
		
		@Override
		public void onLoadCompleted(T r) {
			listener.onLoadCompleted(r);
			onLoadWasCompleted(r);
		}
	};

	void onLoadWasCompleted(T r) {}
	
	public CraveStrip(CraveStripListAdapter allStripsAdapter, OnLoadListener<T> listener) {
		this.allStripsAdapter = allStripsAdapter;
		this.listener = listener;
	}
	
	public void dispose() {
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
			onLoad(loader);
			loader.loadPage(1);

			list.setAdapter((ListAdapter) listAdapter);
			//pager.setAdapter(adapter);
		}
	}

	public void refresh() {
		loader.clearPages();
		loader.loadPage(1);
	}

	public List<Sku> getRecommendations() {
		return adapter.getRecommendations();
	}

}