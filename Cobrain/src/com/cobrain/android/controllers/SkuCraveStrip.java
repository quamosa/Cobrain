package com.cobrain.android.controllers;

import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.loaders.CraveStripLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.SkuStripLoader;
import com.cobrain.android.model.Skus;

public class SkuCraveStrip extends CraveStrip<Skus> {

	public static final int STRIP_TYPE_CRAVES = 0;
	public static final int STRIP_TYPE_CRAVES_NOT_AVAILABLE = 1;
	
	boolean on = false;
	public Skus skus;

	@Override
	public void dispose() {
		skus = null;
		super.dispose();
	}

	@Override
	void onLoadWasCompleted(Skus r) {
		String whyText;
		
		/*
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
		}*/

		adapter.setCraveStrip(SkuCraveStrip.this);

		//list.smoothScrollToPosition(0);		
	}
	
	public SkuCraveStrip(CraveStripListAdapter<Skus> allStripsAdapter, OnLoadListener<Skus> listener) {
		super(allStripsAdapter, listener);
	}
	
	@Override
	protected void onLoad(CraveStripLoader<Skus> loader) {
		((SkuStripLoader)loader).setSkus(skus); //.setCategoryId(scenarioId);
		loader.setOnLoadListener(_listener);
	}
	
}