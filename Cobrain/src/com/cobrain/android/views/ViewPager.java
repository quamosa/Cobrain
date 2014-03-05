package com.cobrain.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.ViewParent;
import android.widget.Scroller;

public class ViewPager extends android.support.v4.view.ViewPager {
	private boolean enabled;

	public ViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	    this.enabled = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (this.enabled) {
	        return super.onTouchEvent(event);
	    }

	    return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
	    if (this.enabled) {
	        return super.onInterceptTouchEvent(event);
	    }

	    return false;
	}

	public void setPagingEnabled(boolean enabled) {
	    this.enabled = enabled;
	}
}
