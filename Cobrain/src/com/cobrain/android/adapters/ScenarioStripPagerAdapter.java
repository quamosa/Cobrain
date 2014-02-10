package com.cobrain.android.adapters;

import java.util.List;

import android.app.Activity;
import com.cobrain.android.MiniFragment;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.fragments.CraveStripsFragment;
import com.cobrain.android.minifragments.CraveStripFragment;
import com.cobrain.android.minifragments.CraveStripHeaderInfoFragment;
import com.cobrain.android.minifragments.CraveStripRefresherFragment;
import com.cobrain.android.model.Scenario;
import com.cobrain.android.model.Sku;

public class ScenarioStripPagerAdapter extends CraveStripPagerAdapter<Scenario> {
	
	public ScenarioStripPagerAdapter(Activity activity, CraveStrip strip, CraveStripsFragment cravesFragment) {
		super(activity, strip, cravesFragment);
	}

	@Override
	protected List<Sku> getSkus(Scenario r) {
		return r.getSkus();
	}
	
	@Override
	public MiniFragment getItem(int position) {

		int typ = 0;
		int offset = 0;
		MiniFragment f;

		if (position == (getCount()-1)) {
			typ = 2;
		}

		if (stripType == CraveStrip.STRIP_TYPE_CRAVES_NOT_AVAILABLE) {
			if (position == 0) {
				typ = 1;
			}
			offset = 1;
		}
		
		switch(typ) {
		case 1:
			f = new CraveStripHeaderInfoFragment(getActivity());
			break;

		case 2:
			f = new CraveStripRefresherFragment(getActivity(), strip);
			break;
			
		default:
			
			int pos = position - offset;
			CraveStripFragment<Scenario> csf = new CraveStripFragment<Scenario>(getActivity(),  parentFragment) {

				@Override
				protected void onShowZoomedCrave(Scenario skuParent, Sku s) {
					parentFragment.showCravesFragmentForScenario(skuParent, s);
				}
				
			};

			if (recommendations == null) 
				return csf;
			
			if (recommendations.size() <= pos)
				return csf;
			
			csf.setRecommendation(results, recommendations.get(pos));
			
			f = csf;
		}

		fragments.put(position, f);

		return f;
	}	

	@Override
	public int getCount() {
		//return count;

		int cnt = 0;

		if (recommendations != null) {
			cnt += ((page-1) * perPage) + countOnThisPage;

			cnt = Math.max(recommendations.size(), cnt);
			cnt = Math.min(cnt, count);
			if (cnt < 0) cnt = 0;
		}

		cnt++;
		if (stripType == CraveStrip.STRIP_TYPE_CRAVES_NOT_AVAILABLE) {
			cnt++;
		}

		return cnt;
	}
	
	@Override
	public Sku getRecommendation(int position) {
		if (stripType == CraveStrip.STRIP_TYPE_CRAVES_NOT_AVAILABLE) {
			position -= 1;
		}
		return super.getRecommendation(position);
	}

	@Override
	public int getTotalCraves(Scenario sc) {
		return sc.getSkus().size();
	}

	public Scenario getScenario() {
		return results;
	}

}