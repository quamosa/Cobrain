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

public class CraveStripPagerListAdapter<T> extends ArrayAdapter<MiniFragment> {

	protected CraveStripPagerAdapter<T> pagerAdapter;
	private int dimHeight;
	private int dimWidth;

	public CraveStripPagerListAdapter(Context context, CraveStripPagerAdapter<T> pagerAdapter) {
		super(context, 0);
		setPagerAdapter(pagerAdapter);
		dimWidth = context.getResources().getDisplayMetrics().widthPixels / 2;
		dimWidth -= dimWidth / 10f;
		dimHeight = context.getResources().getDimensionPixelSize(R.dimen.crave_strip_item_height);
	}
	
	public CraveStripPagerListAdapter(Context context, int resource) {
		super(context, resource);
	}

	void setPagerAdapter(CraveStripPagerAdapter<T> pagerAdapter) {
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
	public MiniFragment getItem(int position) {
		return pagerAdapter.getItem(position);
	}

	protected class ViewHolder {
		int position;
		int type;
		MiniFragment f;
	}
	
	@SuppressWarnings("unchecked")
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
			
			onUpdateView(vh);
		}

		return v;
	}

	@SuppressWarnings("unchecked")
	protected void onUpdateView(ViewHolder vh) {
		CraveStripFragment<T> csf = (CraveStripFragment<T>) vh.f;
		csf.setRecommendation(pagerAdapter.getParentObject(), pagerAdapter.getRecommendation(vh.position));
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