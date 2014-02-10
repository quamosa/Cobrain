package com.cobrain.android.controllers;

import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.loaders.CraveStripLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.ScenarioStripLoader;
import com.cobrain.android.model.Scenario;

public class ScenarioCraveStrip extends CraveStrip<Scenario> {

	public static final int STRIP_TYPE_CRAVES = 0;
	public static final int STRIP_TYPE_CRAVES_NOT_AVAILABLE = 1;
	
	boolean on = false;
	public int scenarioId;

	@Override
	void onLoadWasCompleted(Scenario r) {
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