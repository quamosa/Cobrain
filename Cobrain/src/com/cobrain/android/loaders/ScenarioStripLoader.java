package com.cobrain.android.loaders;

import com.cobrain.android.model.Scenario;
import com.cobrain.android.model.UserInfo;

public class ScenarioStripLoader extends CraveStripLoader<Scenario> {

	int categoryId = 0;
	boolean onSale;

	public int getCategoryId() {
		return categoryId;
	}


	public boolean setCategoryId(int categoryId) {
		if (categoryId != this.categoryId) {
			this.categoryId = categoryId;
			//page = 0;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}
	public boolean setOnSaleRecommendationsOnly(boolean onSale) {
		if (this.onSale != onSale) {
			this.onSale = onSale;
			//page = 0;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}

	@Override
	void onInitialize() {
		categoryId = 0;
	}
	
	@Override
	protected Scenario onPerformLoad(UserInfo u) {
		Scenario r = null;
		
		if (u != null) {
			r = u.getScenario( categoryId , onSale, refresh );
		}
		return r;
	}

	@Override
	protected void onLoadCompleted(Scenario sc) {
		if (sc == null) {
			ScenarioStripLoader.this.categoryId = 0;
		}
	}

}
