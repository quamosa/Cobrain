package com.cobrain.android.minifragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cobrain.android.MiniFragment;
import com.cobrain.android.R;

public class HomeRackHeaderFragment extends MiniFragment {

	public HomeRackHeaderFragment(Activity a) {
		super(a);
	}

	@Override
	public View onCreateView(Bundle inState, LayoutInflater inflater,
			ViewGroup container) {
		View v = inflater.inflate(R.layout.frg_home_rack_header, null);
		return v;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

}
