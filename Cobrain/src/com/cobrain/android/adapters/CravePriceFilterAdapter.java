package com.cobrain.android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CravePriceFilterAdapter extends ArrayAdapter<NavigationMenuItem> {

	int resId;
	int textResId;
	
	public CravePriceFilterAdapter(Context c, int res, int textRes) {
		super(c, res, textRes, new ArrayList<NavigationMenuItem>());
		resId = res;
		textResId = textRes;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(getContext());
		tv.setText(getItem(position).caption);
		return tv;
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).id;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			v = View.inflate(getContext(), resId, null);
		}

		TextView tv = (TextView) v.findViewById(textResId);
		NavigationMenuItem mi = getItem(position);
		tv.setText(mi.label);

		return v;
	}

}
