package com.cobrain.android.adapters;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.cobrain.android.R;
import com.cobrain.android.fragments.CraveFragment;
import com.cobrain.android.fragments.WishListFragment;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;

public class WishListPagerAdapter extends FragmentStatePagerAdapter {
	private int page = 1;
	private int perPage;
	private int countOnThisPage;
	private int count;
	private ArrayList<Sku> listItems;
	private WishListFragment parentFragment;
	private boolean destroyAll;
	//private RecommendationsResults results;
	private Skus results;

	public WishListPagerAdapter(FragmentManager fm, WishListFragment cravesFragment) {
		super(fm);
		parentFragment = cravesFragment;
	}

	public void dispose() {
		results = null;
		parentFragment = null;
		if (listItems != null) {
			listItems.clear();
			listItems = null;
		}
	}

	public void add(Sku item, boolean notify) {
		if (listItems == null) listItems = new ArrayList<Sku>();
		listItems.add(item);
		if (notify) notifyDataSetChanged();
	}
	public Sku get(int index) {
		if (listItems == null) return null;
		return listItems.get(index);
	}

	public void load(Skus r, List<Sku> items, boolean append) {
		load(r, items, append, true);
	}
	public void load(Skus r, List<Sku> items, boolean append, boolean notify) {
		//page = r.getPage();
		//perPage = r.getPerPage();
		//countOnThisPage = r.getCount();
		//count = r.getTotal();
		page = 1;
		perPage = items.size();
		countOnThisPage = items.size();
		count = items.size();

		if (append && listItems != null) {
			listItems.addAll(items);
		}
		else listItems = new ArrayList<Sku>(items);
		
		results = r;

		if (notify) {
			destroyAll = true;
			notifyDataSetChanged();
			destroyAll = false;
			notifyDataSetChanged();
		}
	}

	public void load(Skus r, boolean append) {
		if (r != null) {
			load(r, r.get(), append, false);
		}
		else {
			page = 1;
			count = 0;
			if (listItems != null) {
				listItems.clear();
				listItems = null;
			}
		}
		results = r;
		destroyAll = true;
		notifyDataSetChanged();
		destroyAll = false;
	}
	public int getMaxPages() {
		double d = Math.ceil(count/(double)perPage);
		return (int)d;
	}

	@Override
	public Fragment getItem(int position) {
		CraveFragment f = new CraveFragment(parentFragment);

		f.setWishListItem(results, listItems, listItems.get(position), position + 1, listItems.size());

		if (parentFragment.cravePager.getCurrentItem() == position) updateTitle(position);

		return f;
	}

	@Override
	public int getItemPosition(Object object) {
		if (destroyAll) return POSITION_NONE;
		return super.getItemPosition(object);
	}

	@Override
	public int getCount() {
		if (listItems == null) return 0;
		
		int cnt = ((page-1) * perPage) + countOnThisPage;
		cnt = listItems.size();

		if (cnt < 0) cnt = 0;
		return cnt;
	}

	public void updateTitle(int position) {
		if (listItems != null) {
			int totalCraves = listItems.size();
			
			String s;
			
			if (listItems.size() > position && count > position && listItems.get(position) != null) 
				s = parentFragment.getString(R.string.rank_for_you,
						position + 1, 
						totalCraves
						);
			else
				s = parentFragment.getString(R.string.rank_for_you_empty);

			parentFragment.setSubTitle(Html.fromHtml(s));
			//final TextView txt = parentFragment.getCobrainController().getSubTitleView();
			//txt.setVisibility(View.VISIBLE);
			//txt.setText(Html.fromHtml(s));
		}
	}
}
