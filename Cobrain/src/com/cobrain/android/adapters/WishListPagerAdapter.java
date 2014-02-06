package com.cobrain.android.adapters;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.cobrain.android.fragments.CraveFragment;
import com.cobrain.android.fragments.WishListFragment;
import com.cobrain.android.model.v1.WishList;
import com.cobrain.android.model.v1.WishListItem;

public class WishListPagerAdapter extends FragmentStatePagerAdapter {
	private int page = 1;
	private int perPage;
	private int countOnThisPage;
	private int count;
	private List<WishListItem> listItems;
	private WishListFragment parentFragment;
	private boolean destroyAll;
	//private RecommendationsResults results;
	private WishList results;

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

	public void add(WishListItem item, boolean notify) {
		if (listItems == null) listItems = new ArrayList<WishListItem>();
		listItems.add(item);
		if (notify) notifyDataSetChanged();
	}

	public void load(WishList r, ArrayList<WishListItem> items, boolean append) {
		load(r, items, append, true);
	}
	public void load(WishList r, ArrayList<WishListItem> items, boolean append, boolean notify) {
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
		else listItems = items;
		
		results = r;

		if (notify) {
			destroyAll = true;
			notifyDataSetChanged();
			destroyAll = false;
			notifyDataSetChanged();
		}
	}

	public void load(WishList r, boolean append) {
		if (r != null) {
			load(r, r.getItems(), append, false);
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

}