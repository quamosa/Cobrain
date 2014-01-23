package com.cobrain.android.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.widget.Button;

public class TransitionButton extends Button {
	private TransitionDrawable mTransition = null;
	private Context mContext;

	public TransitionButton(Context context) {
		super(context);
		mContext = context;
	}

	public TransitionButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initTransition();
	}

	public TransitionButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initTransition();
	}
	
	void initTransition() {
		Drawable d = getBackground();
		if (d instanceof StateListDrawable) {
			StateListDrawable sd = (StateListDrawable) d;
			sd.setState(new int[] {android.R.attr.state_pressed});
			d = sd.getCurrent();
		}
		if (d instanceof TransitionDrawable) {
			mTransition = (TransitionDrawable) d;
		}
	}

	public void setTransition(TransitionDrawable transition) {
		mTransition = transition;
		setBackground(mTransition);
	}

	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		if (pressed && mTransition != null) {
			mTransition.startTransition(1000);
		} else if (!pressed && mTransition != null) {
			mTransition.resetTransition();
		}
	}
}
