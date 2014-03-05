package com.cobrain.android.controllers;


import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.loaders.CraveStripLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.ScenarioStripLoader;
import com.cobrain.android.model.Scenario;

public class ScenarioCraveStrip extends CraveStrip<Scenario> {

	public static final int STRIP_TYPE_CRAVES = 0;
	public static final int STRIP_TYPE_CRAVES_NOT_AVAILABLE = 1;
	
	public int scenarioId;

	@Override
	void onLoadHasStarted() {
		if (loader.refresh) {
			setCaption("Refreshing...");
		}
		else setCaption("Loading...");
	}
	
	@Override
	void onLoadWasCompleted(Scenario r) {
		String whyText = null;

		if (r != null) {
			whyText = r.getWhyText();
		}
		
		if (r != null && r.getSkus().size() >= 20) {
			type = STRIP_TYPE_CRAVES;
		}
		else {
			//type = STRIP_TYPE_CRAVES_NOT_AVAILABLE;
			type = STRIP_TYPE_CRAVES;
			if (r == null) {
				whyText = "Was not able to find new Craves for you... please try refreshing your list.";
			}
			//whyText = "While your Cobrain finds new Craves for you, check out these popular items";
		}
		
		setCaption(whyText);

		adapter.setCraveStrip(ScenarioCraveStrip.this);

		list.smoothScrollToPosition(0);		
	}
	
	public ScenarioCraveStrip(CraveStripListAdapter allStripsAdapter, OnLoadListener<Scenario> listener) {
		super(allStripsAdapter, listener);
	}
	
	@Override
	protected void onLoad(CraveStripLoader<Scenario> loader) {
		((ScenarioStripLoader)loader).setCategoryId(scenarioId);
		loader.setOnLoadListener(_listener);
	}
	
}