package com.cobrain.android.adapters;

import com.cobrain.android.MiniFragment;
import com.cobrain.android.model.Scenario;

import android.content.Context;

public class ScenarioStripPagerListAdapter extends CraveStripPagerListAdapter<Scenario> {

	private static final int ITEM_TYPE_CRAVE = 0;
	private static final int ITEM_TYPE_HEADER_INFO = 1;
	private static final int ITEM_TYPE_REFRESHER = 2;

	public ScenarioStripPagerListAdapter(Context context, ScenarioStripPagerAdapter pagerAdapter) {
		super(context, pagerAdapter);
	}
	
	@Override
	public int getViewTypeCount() {
		return 4;
	}
	
	@Override
	public int getItemViewType(int position) {
		MiniFragment f = pagerAdapter.getItem(position);

		if (f instanceof com.cobrain.android.minifragments.CraveStripHeaderInfoFragment) {
			return ITEM_TYPE_HEADER_INFO;
		}

		if (f instanceof com.cobrain.android.minifragments.CraveStripRefresherFragment) {
			return ITEM_TYPE_REFRESHER;
		}

		return ITEM_TYPE_CRAVE;

	}

	@Override
	protected void onUpdateView(ViewHolder vh) {
		switch (vh.type) {
		case ITEM_TYPE_CRAVE:
			super.onUpdateView(vh);
		}
	}

}