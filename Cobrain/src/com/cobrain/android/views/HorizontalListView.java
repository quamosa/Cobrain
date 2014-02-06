package com.cobrain.android.views;

import java.util.HashMap;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class HorizontalListView extends HorizontalScrollView {

	int x = 0;
	HashMap<Integer, View> views = new HashMap<Integer, View>();
	Adapter adapter;
	int currentPosition = 0;

	public HorizontalListView(Context context) {
		super(context);
		init();
	}

	public HorizontalListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HorizontalListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	LinearLayout parent;
	
	void init() {
		parent = new LinearLayout(getContext()) {
		};
		parent.setOrientation(LinearLayout.HORIZONTAL);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(345, LayoutParams.MATCH_PARENT);
		addView(parent, lp);
	}
	
	public void setAdapter(Adapter a) {
		adapter = a;
		a.registerDataSetObserver(new DataSetObserver() {

			@Override
			public void onChanged() {
				requestLayout();
			}
			
		});
		requestLayout();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(ev);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		layThemOut(r-l);
		super.onLayout(changed, l, t, r, b);
	}

	void addNewView(View v) {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		v.setLayoutParams(lp);
		v.measure(0, 0);
		int w = v.getMeasuredWidth();
		//lp.leftMargin = x;
		parent.addView(v, lp);
		x += w;
	}

	void layThemOut(int width) {
		x = 0;
		int i = currentPosition;
		int overFlow = 2;
		int cnt = adapter.getCount();

		//i -= overFlow;
		//if (i < 0) i = 0;
		
		while (i < cnt && x < width) {
			View vw = views.get(i);
			if (vw == null) {
				vw = (View) adapter.getView(i++, vw, parent);
				addNewView(vw);
			}
		}
		
		while (i < cnt && --overFlow > 0) {
			View vw = views.get(i);
			if (vw == null) {
				vw = (View) adapter.getView(i++, vw, parent);
				addNewView(vw);
			}
		}
		
		
	}
	
	void recycleOldViews() {
		View v = getChildAt(0);
		int l = v.getLeft();
		int w = v.getWidth();
		int pw = this.getWidth();
		
		int sx = getScrollX();
		
		if (((l + w) < 0) || (l > pw)) {
			removeView(v);
		}
	}

	public Adapter getAdapter() {
		return adapter;
	}

}
