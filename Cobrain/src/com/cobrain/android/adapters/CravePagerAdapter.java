package com.cobrain.android.adapters;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cobrain.android.fragments.CraveFragment;
import com.cobrain.android.fragments.CravesFragment;
import com.cobrain.android.model.Product;
import com.cobrain.android.model.RecommendationsResults;

public class CravePagerAdapter extends FragmentStatePagerAdapter {
	private int page = 1;
	private int perPage;
	private int countOnThisPage;
	private int count;
	private List<Product> recommendations;
	private CravesFragment parentFragment;
	private boolean destroyAll;
	private RecommendationsResults results;

	public CravePagerAdapter(FragmentManager fm, CravesFragment cravesFragment) {
		super(fm);
		parentFragment = cravesFragment;
	}

	public void clear() {
		destroyAll = true;
		notifyDataSetChanged();
		destroyAll = false;
	}
	
	public void dispose() {
		if (recommendations != null) {
			recommendations.clear();
			recommendations = null;
		}
		clear();
		results = null;
		parentFragment = null;
	}
	
	public void load(RecommendationsResults r) {
		if (r != null) {
			page = r.getPage();
			perPage = r.getPerPage();
			countOnThisPage = r.getCount();
			count = r.getTotal();
			countOnThisPage = Math.min(countOnThisPage, count);

			List<Product> products = r.getProducts();
			int position = (page - 1) * perPage;

			if (recommendations == null) 
				recommendations = new ArrayList<Product>(position + products.size());

			if (recommendations.size() == 0 && position == 0) {
				recommendations.addAll(products);
			}
			else if (recommendations.size() <= position) {
				while (recommendations.size() < position)
					recommendations.add(null);

				recommendations.addAll(position, products);
			}
			else {
				while (recommendations.size() < countOnThisPage)
					recommendations.add(null);

				for (int i = position, in = 0; in < countOnThisPage; in++) {
					recommendations.set(i++, products.get(in));
				}
			}
		}
		else {
			page = 1;
			count = 0;
			if (recommendations != null) {
				recommendations.clear();
				recommendations = null;
			}
		}
		results = r;
		clear();
	}
	
	public int getMaxPages() {
		double d = Math.ceil(count/(double)perPage);
		return (int)d;
	}

	@Override
	public Fragment getItem(int position) {
		CraveFragment f = new CraveFragment(parentFragment);

		if (recommendations == null) 
			return f;
		
		if (recommendations.size() <= position)
			return f;
		
		f.setRecommendation(results, recommendations.get(position));
		
		return f;
	}

	@Override
	public int getItemPosition(Object object) {
		if (destroyAll) return POSITION_NONE;
		return super.getItemPosition(object);
	}

	@Override
	public int getCount() {
		//return count;
		
		if (recommendations == null) return 0;
		
		int cnt = ((page-1) * perPage) + countOnThisPage;

		cnt = Math.max(recommendations.size(), cnt);
		cnt = Math.min(cnt, count);
		if (cnt < 0) cnt = 0;
		return cnt;
	}

}