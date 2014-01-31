package com.cobrain.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class MyRelativeLayout extends RelativeLayout {

	public MyRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MyRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public MyRelativeLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		try {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
		catch(NullPointerException e) {
			e.printStackTrace();
			//FIXME: setMeasuredDimension(measuredWidth, measuredHeight);
		}
	}

	
}
