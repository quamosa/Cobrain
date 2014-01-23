package com.cobrain.android.loaders;

import android.os.AsyncTask;

import com.cobrain.android.model.Category;
import com.cobrain.android.model.CategoryTree;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.service.Cobrain;
import com.cobrain.android.service.Cobrain.CobrainController;

public class CraveFilterLoader {
	CobrainController controller;
	OnLoadListener<CategoryTree> onLoadListener;
	private AsyncTask<Void, Void, CategoryTree> currentRequest;
	int parentCategoryId;
	String parentCategoryName;
	
	public void initialize(CobrainController controller) {
		this.controller = controller;
	}

	public void dispose() {
		cancel();
		controller = null;
		onLoadListener = null;
	}
	
	public void cancel() {
		if (currentRequest != null) {
			currentRequest.cancel(true);
			currentRequest = null;
		}
	}

	public int getParentCategoryId() {
		return parentCategoryId;
	}

	public String getParentCategoryName() {
		return parentCategoryName;
	}

	public void setOnLoadListener(OnLoadListener<CategoryTree> listener) {
		onLoadListener = listener;
	}

	public void load(final int categoryId) {
		if (onLoadListener != null) onLoadListener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, CategoryTree>() {

			@Override
			protected CategoryTree doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				CategoryTree r = null;
				
				if (u != null) {
					r = u.getCategories(categoryId);
				}
				
				if (r == null) {
					parentCategoryId = 0;
					parentCategoryName = null;
				}
				else {
					int depth = r.getDepth();
					if (depth > 1) {
						for (Category ca : r.getAncestors()) {
							if (ca.getDepth() == (depth - 1)) {
								parentCategoryId = ca.getId();
								parentCategoryName = ca.getName();
								break;
							}
						}
					}
					else {
						parentCategoryName = r.getName();
						parentCategoryId = r.getId();
					}
				}
				
				return r;
			}

			@Override
			protected void onPostExecute(CategoryTree result) {
				currentRequest = null;
				if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
			}
			
		}.execute();			
	}
}
