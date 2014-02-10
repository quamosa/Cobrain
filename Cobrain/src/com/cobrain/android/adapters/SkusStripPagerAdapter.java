package com.cobrain.android.adapters;

import java.util.List;

import android.app.Activity;
import com.cobrain.android.MiniFragment;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.fragments.CraveStripsFragment;
import com.cobrain.android.minifragments.CraveStripFragment;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;

public class SkusStripPagerAdapter extends CraveStripPagerAdapter<Skus> {

	public SkusStripPagerAdapter(Activity activity, CraveStrip strip, CraveStripsFragment cravesFragment) {
		super(activity, strip, cravesFragment);
	}
	
	@Override
	protected List<Sku> getSkus(Skus r) {
		return r.get();
	}

	@Override
	public MiniFragment getItem(int position) {

		MiniFragment f;

		CraveStripFragment<Skus> csf = new CraveStripFragment<Skus>(getActivity(),  parentFragment);

		if (recommendations == null) 
			return csf;
		
		if (recommendations.size() <= position)
			return csf;
		
		csf.setRecommendation(results, recommendations.get(position));
		
		f = csf;

		fragments.put(position, f);

		return f;
	}	

	@Override
	public int getTotalCraves(Skus sk) {
		return sk.get().size();
	}

	public Skus getSkus() {
		return results;
	}

}