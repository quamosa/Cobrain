package com.cobrain.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class StateFullView extends RelativeLayout implements Checkable {

	public static final int STATE_PRESSED = 1;
	public static final int STATE_CHECKED = 2;
	private OnStateChangedListener mStateChangedListener;
	boolean selected;

	private static final int[] CheckedStateSet = {
	    android.R.attr.state_checked,
	};

	public interface OnStateChangedListener {
		public void onStateChanged(View v, int state, boolean enabled);
	}
	
	public StateFullView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public StateFullView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public StateFullView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
		onStateChanged(STATE_PRESSED, pressed);
	}

	@Override
	public void setChecked(boolean checked) {
		selected = checked;
		onStateChanged(STATE_CHECKED, checked);
		refreshDrawableState();
	}

	void onStateChanged(int state, boolean enabled) {
		if (mStateChangedListener != null) {
			mStateChangedListener.onStateChanged(this, state, enabled);
		}
	}

	public void setOnStateChangeListener(OnStateChangedListener listener) {
		mStateChangedListener = listener;
	}

	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
	    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
	    if (isChecked()) {
	        mergeDrawableStates(drawableState, CheckedStateSet);
	    }
	    return drawableState;
	}
	
	@Override
	public boolean isChecked() {
		return selected;
	}

	@Override
	public void toggle() {
		setChecked(!selected);
	}
}
