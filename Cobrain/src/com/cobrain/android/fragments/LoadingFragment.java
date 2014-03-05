package com.cobrain.android.fragments;

import com.cobrain.android.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LoadingFragment extends BaseCobrainFragment {
	public static final String TAG = "LoadingFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.pop_loading, null);

		setTitle("Loading...");
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

}
