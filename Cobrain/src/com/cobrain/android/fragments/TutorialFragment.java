package com.cobrain.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cobrain.android.R;

public class TutorialFragment extends Fragment {

	public static final String TAG = "TutorialFragment";

	ViewPager pager;
    int page;

	public static TutorialFragment newInstance(int page) {
		TutorialFragment f = new TutorialFragment();
        f.page = page;
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v;
        int lid = 0;

        switch(page) {
            case 0:
                lid = R.layout.frg_tutorial;
                break;
            case 1:
                lid = R.layout.frg_tutorial_1;
                break;
            case 2:
                lid = R.layout.frg_tutorial_2;
                break;
            case 3:
                lid = R.layout.frg_tutorial_3;
                break;
            case 4:
                lid = R.layout.frg_tutorial_4;
                break;
        }
        v = inflater.inflate(lid, null);

		return v;
	}

/*	@Override
	public void onSaveInstanceState(Bundle outState) {
		adapter.clear();
		adapter.notifyDataSetChanged();
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		setupAdapter();
		super.onResume();
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}
*/
}
