package com.cobrain.android.minifragments;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.cobrain.android.MiniFragment;
import com.cobrain.android.R;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.controllers.CraveStrip.OnRefreshCompletedListener;

public class CraveStripRefresherFragment extends MiniFragment implements OnClickListener, OnRefreshCompletedListener, OnTouchListener {
	
	CraveStrip strip;
	Button refresh;
	Rect r = new Rect();
	
	public CraveStripRefresherFragment(Activity a, CraveStrip strip) {
		super(a);
		this.strip = strip;
	}
	
	@Override
	public View onCreateView(Bundle inState, LayoutInflater inflater,
			ViewGroup container) {
		View v = inflater.inflate(R.layout.frg_crave_strip_refresher_frame, null);
		
		refresh = (Button) v.findViewById(R.id.refresh);
		refresh.setOnClickListener(this);
		refresh.setOnTouchListener(this);
		
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
		v.setPressed(true);
		v.setEnabled(false);
		strip.refresh(this);
	}

	@Override
	public void onRefreshCompleted() {
		refresh.setEnabled(true);
		refresh.setPressed(false);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				v.getParent().requestDisallowInterceptTouchEvent(true);
		        v.setPressed(true);
		        return true;

			case MotionEvent.ACTION_UP:
				v.getHitRect(r);
				if (r.contains( (int)event.getX(), (int)event.getY() )) {
					v.getParent().requestDisallowInterceptTouchEvent(false);
					if (v.isPressed()) {
						v.performClick();
				        return true;
					}
				}
		}
		
		return false;
	}

}
