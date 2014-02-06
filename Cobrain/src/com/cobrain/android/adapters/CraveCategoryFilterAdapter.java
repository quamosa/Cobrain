package com.cobrain.android.adapters;

import java.util.ArrayList;

import com.cobrain.android.R;
import com.cobrain.android.model.Category;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CraveCategoryFilterAdapter extends ArrayAdapter<Category> {

	int resId;
	int textResId;
	
	public CraveCategoryFilterAdapter(Context c, int res, int textRes) {
		super(c, res, textRes, new ArrayList<Category>());
		resId = res;
		textResId = textRes;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(getContext());
		tv.setText(getItem(position).getName());
		return tv;
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			v = View.inflate(getContext(), resId, null);
		}

		TextView tv = (TextView) v.findViewById(textResId);
		Category c = getItem(position);
		tv.setText(c.getName());

		return v;
	}

}
