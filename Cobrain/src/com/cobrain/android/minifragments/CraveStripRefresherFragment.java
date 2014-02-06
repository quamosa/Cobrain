package com.cobrain.android.minifragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.cobrain.android.MiniFragment;
import com.cobrain.android.R;
import com.cobrain.android.controllers.CraveStrip;

public class CraveStripRefresherFragment extends MiniFragment implements OnClickListener {
	
	CraveStrip strip;
	Button refresh;
	
	public CraveStripRefresherFragment(Activity a, CraveStrip strip) {
		super(a);
		this.strip = strip;
	}
	
	@Override
	public View onCreateView(Bundle inState, LayoutInflater inflater,
			ViewGroup container) {
		View v = inflater.inflate(R.layout.crave_strip_refresher_frame, null);
		
		refresh = (Button) v.findViewById(R.id.refresh);
		refresh.setOnClickListener(this);
		
		return v;
	}

	@Override
	public void onDestroyView() {
		strip = null;
		refresh.setOnClickListener(null);
		refresh = null;
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		strip.refresh();
	}

}
