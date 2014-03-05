package com.cobrain.android.loaders;

import com.cobrain.android.model.Skus;
import com.cobrain.android.model.UserInfo;

public class SkuStripLoader extends CraveStripLoader<Skus> {

	int categoryId = 0;
	boolean onSale;
	Skus skus;

	public int getCategoryId() {
		return categoryId;
	}


	@Override
	public void dispose() {
		skus = null;
		super.dispose();
	}


	public boolean setCategoryId(int categoryId) {
		if (categoryId != this.categoryId) {
			this.categoryId = categoryId;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}
	public boolean setOnSaleRecommendationsOnly(boolean onSale) {
		if (this.onSale != onSale) {
			this.onSale = onSale;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}
	public boolean setSkus(Skus skus) {
		if (this.skus != skus) {
			this.skus = skus;
			pagesLoaded.clear();
			return true;
		}
		return false;
	}

	
	@Override
	void onInitialize() {
	}
	
	@Override
	void performLoad(int countPerPage, int page) {
		pagesLoaded.add(page);
		//lets not start a new asynctask, we already have our skus to load into the adapter;
		adapter.load(skus);
	}


	@Override
	protected Skus onPerformLoad(UserInfo u) {
		Skus r = null;
		
		if (u != null) {
			//r = u.getScenario( categoryId );
		}
		return r;
	}

	@Override
	protected void onLoadCompleted(Skus sc) {
		if (sc == null) {
			//SkuStripLoader.this.categoryId = 0;
		}
	}

}
