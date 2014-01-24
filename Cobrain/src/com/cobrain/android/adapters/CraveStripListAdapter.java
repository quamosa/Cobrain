package com.cobrain.android.adapters;

import java.util.List;
import com.cobrain.android.R;
import com.cobrain.android.fragments.CravesFragment;
import com.cobrain.android.model.CraveStrip;
import com.cobrain.android.utils.LoaderUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CraveStripListAdapter extends ArrayAdapter<CraveStrip> implements DialogInterface.OnClickListener {
	LoaderUtils loader = new LoaderUtils();
	CravesFragment parent;
	List<CraveStrip> items;
	
	public CraveStripListAdapter(Context context, int resource,
			List<CraveStrip> items, CravesFragment parent) {
		super(context, resource, items);
		this.items = items;
		setParent(parent);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
		}
	}

	public void setParent(CravesFragment parent) {
		this.parent = parent;
	}

	private class ViewHolder implements OnClickListener {
		int position;
		RelativeLayout layout;
		TextView caption;
		ViewPager pager;
		
		@Override
		public void onClick(View v) {
		}
	}

	LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder vh;

		if (v == null) {
			v = View.inflate(parent.getContext(), R.layout.list_item_crave_strip, null);

			vh = new ViewHolder();
			vh.caption = (TextView) v.findViewById(R.id.caption);
			//vh.pager = (ViewPager) v.findViewById(R.id.pager);
			vh.layout = (RelativeLayout) v.findViewById(R.id.layout);

			v.setTag(vh);
		}
		else vh = (ViewHolder) v.getTag();
		
		CraveStrip strip = getItem(position);

		vh.caption.setText(strip.caption);
		removeAllViews(vh.layout);
		addView(vh.layout, strip.container, lp);
		
		if (strip.pager.getAdapter() != strip.adapter) {
			strip.loader.setCategoryId(strip.categoryId);
			strip.loader.loadPage(1);
			strip.pager.setAdapter(strip.adapter);
		}

		return v;
	}
	
	void removeAllViews(ViewGroup v) {
		for (int i = 0; i < v.getChildCount(); i++) {
			View c = v.getChildAt(i);
			c.setVisibility(View.GONE);
		}
	}
	
	void addView(ViewGroup parent, View v, LayoutParams lp) {
		if (v.getParent() != parent) {
			parent.removeView(v);
			parent.addView(v, lp);
		}
		v.setVisibility(View.VISIBLE);
	}
	
}