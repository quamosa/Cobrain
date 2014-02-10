package com.cobrain.android.adapters;

import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
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
import com.cobrain.android.loaders.FontLoader;
import com.cobrain.android.utils.LoaderUtils;

public class CraveStripListAdapter extends ArrayAdapter<CraveStrip> implements DialogInterface.OnClickListener {
	LoaderUtils loader = new LoaderUtils();
	List<CraveStrip> items;
	
	public CraveStripListAdapter(Context context, int resource,
			List<CraveStrip> items, CraveStripsFragment parent) {
		super(context, resource, items);
		this.items = items;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
		}
	}

	private class ViewHolder implements OnClickListener {
		int position;
		RelativeLayout layout;
		TextView caption;
		
		@Override
		public void onClick(View v) {
		}
	}

	LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder vh;

		if (v == null) {
			v = View.inflate(parent.getContext(), R.layout.list_item_crave_strip, null);

			vh = new ViewHolder();
			vh.caption = (TextView) v.findViewById(R.id.caption);
			vh.layout = (RelativeLayout) v.findViewById(R.id.layout);
			vh.caption.setTypeface(FontLoader.load(getContext(), "Doppio One.ttf"));

			v.setTag(vh);
		}
		else vh = (ViewHolder) v.getTag();
		
		CraveStrip strip = getItem(position);

		vh.caption.setText(strip.caption);
		removeAllViews(vh.layout);
		addView(vh.layout, strip.container, lp);

		return v;
	}
	
	void removeAllViews(ViewGroup v) {
		for (int i = 0; i < v.getChildCount(); i++) {
			View c = v.getChildAt(i);
			c.setVisibility(View.GONE);
		}
	}
	
	void addView(ViewGroup parent, View v, LayoutParams lp) {
		ViewGroup vp = (ViewGroup) v.getParent();
		if (vp != parent) {
			if (vp != null) vp.removeView(v);
			parent.addView(v, lp);
		}
		v.setVisibility(View.VISIBLE);
	}

}