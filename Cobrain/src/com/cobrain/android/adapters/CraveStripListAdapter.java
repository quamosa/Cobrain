package com.cobrain.android.adapters;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cobrain.android.R;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.fragments.CraveStripsFragment;
import com.cobrain.android.utils.LoaderUtils;

public class CraveStripListAdapter<T> extends ArrayAdapter<CraveStrip<T>> implements DialogInterface.OnClickListener {
	LoaderUtils loader = new LoaderUtils();
	List<CraveStrip<T>> items;
	
	public CraveStripListAdapter(Context context, int resource,
			List<CraveStrip<T>> items, CraveStripsFragment<T> parent) {
		super(context, resource, items);
		this.items = items;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
		}
	}

	private class ViewHolder implements OnClickListener {
		int position = -1;
		RelativeLayout layout;
		TextView caption;
		
		@Override
		public void onClick(View v) {
		}
	}

	@Override
	public void clear() {
		views.clear();
		super.clear();
	}

	LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	SparseArray<View> views = new SparseArray<View>();
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder vh;

		//v = views.get(position);
		
		if (v == null) {
			v = View.inflate(parent.getContext(), R.layout.list_item_crave_strip, null);

			vh = new ViewHolder();
			vh.caption = (TextView) v.findViewById(R.id.caption);
			vh.layout = (RelativeLayout) v.findViewById(R.id.layout);

			v.setTag(vh);
			
			views.put(position, v);
		}
		else vh = (ViewHolder) v.getTag();
		
		CraveStrip<T> strip = getItem(position);

		if (strip.caption != null) {
			vh.caption.setVisibility(View.VISIBLE);
			vh.caption.setText(strip.caption);
		}
		else vh.caption.setVisibility(View.GONE);
		
		if (vh.position != position) {
			if (!removeAllViews(vh.layout, strip.list)) {
				addView(vh.layout, strip.list, lp);
			}
			vh.position = position;
		}
		
		return v;
	}

	boolean removeAllViews(ViewGroup v, View keep) {
		boolean found = false;
		
		for (int i = 0; i < v.getChildCount(); i++) {
			View c = v.getChildAt(i);
			if (keep != c) {
				c.setVisibility(View.GONE);
			}
			else {
				c.setVisibility(View.VISIBLE);
				found = true;
			}
		}
		return found;
	}
	
	void addView(ViewGroup parent, View v, LayoutParams lp) {
		ViewGroup vp = (ViewGroup) v.getParent();
		//if (vp != parent) {
			if (vp != null) vp.removeView(v);
			parent.addView(v, lp);
		//}
		v.setVisibility(View.VISIBLE);
	}

}