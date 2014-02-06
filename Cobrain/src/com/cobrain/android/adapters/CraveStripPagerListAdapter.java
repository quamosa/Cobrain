package com.cobrain.android.adapters;

import it.sephiroth.android.library.widget.AbsHListView;

import com.cobrain.android.MiniFragment;
import com.cobrain.android.R;
import com.cobrain.android.minifragments.CraveStripFragment;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class CraveStripPagerListAdapter extends ArrayAdapter<MiniFragment> {

	private static final int ITEM_TYPE_CRAVE = 0;
	private static final int ITEM_TYPE_HEADER_INFO = 1;
	private static final int ITEM_TYPE_REFRESHER = 2;

	private CraveStripPagerAdapter pagerAdapter;
	private int dimHeight;
	private int dimWidth;

	public CraveStripPagerListAdapter(Context context, CraveStripPagerAdapter pagerAdapter) {
		super(context, 0);
		setPagerAdapter(pagerAdapter);
		dimWidth = context.getResources().getDisplayMetrics().widthPixels / 2;
		dimWidth -= dimWidth / 10f;
		dimHeight = context.getResources().getDimensionPixelSize(R.dimen.crave_strip_item_height);
	}
	
	public CraveStripPagerListAdapter(Context context, int resource) {
		super(context, resource);
	}

	void setPagerAdapter(CraveStripPagerAdapter pagerAdapter) {
		this.pagerAdapter = pagerAdapter;
		pagerAdapter.registerDataSetObserver(observer);
	}

	public void dispose() {
		pagerAdapter = null;
	}
	
	@Override
	public int getCount() {
		return pagerAdapter.getCount();
	}
	
	@Override
	public int getViewTypeCount() {
		return 3;
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
	public MiniFragment getItem(int position) {
		return pagerAdapter.getItem(position);
	}

	private class ViewHolder {
		int position;
		int type;
		MiniFragment f;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			int type = getItemViewType(position);
			MiniFragment f = getItem(position);
			if (f != null) {
				f.create(null);
				v = f.getView();
			}
			ViewHolder vh = new ViewHolder();
			vh.type = type;
			vh.f = f;
			vh.position = position;
			v.setTag(vh);
			int width = dimWidth;
			int height = dimHeight;
			if (v.getLayoutParams() == null) {
				v.setLayoutParams(new AbsHListView.LayoutParams(width, height));
			}
			else {
				AbsHListView.LayoutParams lp = (AbsHListView.LayoutParams) v.getLayoutParams();
				lp.width = width;
				lp.height = height;
			}
		}
		else {
			ViewHolder vh = (ViewHolder) v.getTag();
			vh.position = position;
			
			switch (vh.type) {
			case ITEM_TYPE_CRAVE:
				CraveStripFragment csf = (CraveStripFragment) vh.f;
				csf.setRecommendation(null, pagerAdapter.getRecommendation(position));
			}
		}

		return v;
	}

	DataSetObserver observer = new DataSetObserver() {

		@Override
		public void onChanged() {
			notifyDataSetChanged();
		}
		
	};

	public int getItemHeight() {
		return dimHeight;
	}
}