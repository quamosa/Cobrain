package com.cobrain.android.controllers;

import java.util.List;

import it.sephiroth.android.library.widget.HListView;
import android.support.v4.view.ViewPager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;

import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.adapters.CraveStripPagerAdapter;
import com.cobrain.android.loaders.CraveStripLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.model.Scenario;
import com.cobrain.android.model.Sku;

public class CraveStrip {

	public static final int STRIP_TYPE_CRAVES = 0;
	public static final int STRIP_TYPE_CRAVES_NOT_AVAILABLE = 1;
	
	public String caption;
	public CraveStripListAdapter allStripsAdapter;
	public CraveStripPagerAdapter adapter;
	public CraveStripLoader loader;
	public ViewPager pager;
	public RelativeLayout container;
	public HListView list;
	public Adapter listAdapter;
	public int type;
	public int scenarioId;
	OnLoadListener<Scenario> listener;
	OnLoadListener<Scenario> _listener = new OnLoadListener<Scenario>() {

		@Override
		public void onLoadStarted() {
			listener.onLoadStarted();
		}

		boolean on = false;
		
		@Override
		public void onLoadCompleted(Scenario r) {
			listener.onLoadCompleted(r);

			boolean changed = false;
			String whyText;
			
			on = !on;

			//if (r.getSkus().size() >= 20) {
			if (on) {
				type = STRIP_TYPE_CRAVES;
				if (r != null) whyText = r.getWhyText();
				else whyText = "Was not able to get Cobrain loves for you... please try refreshing your list.";
			}
			else {
				type = STRIP_TYPE_CRAVES_NOT_AVAILABLE;
				whyText = "Here are some recommendations just for you!";
			}
			
			if (!whyText.equals(caption)) {
				caption = whyText;
				allStripsAdapter.notifyDataSetChanged();
			}

			adapter.setCraveStrip(CraveStrip.this);

			list.smoothScrollToPosition(0);
		}
	};

	
	public CraveStrip(CraveStripListAdapter allStripsAdapter, OnLoadListener<Scenario> listener) {
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
	
	public void load() {
		//if (pager.getAdapter() != adapter) {
		if (list.getAdapter() != listAdapter) {
			loader.setCategoryId(scenarioId);
			loader.setOnLoadListener(_listener);
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