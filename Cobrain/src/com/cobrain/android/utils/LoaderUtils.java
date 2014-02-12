package com.cobrain.android.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

import com.cobrain.android.R;

public class LoaderUtils {
	private View loadingFrame;
	private TextView loadingText;
	private View emptyFrame;
	private TextView emptyText;
	private int loading;
	private int empty;

	public void initialize(ViewGroup v) {
		LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View lv = inflater.inflate(R.layout.loading_frame, null);
		View ev = inflater.inflate(R.layout.empty_frame, null); 

		lv.setVisibility(View.GONE);
		ev.setVisibility(View.GONE);
		v.addView(lv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		v.addView(ev, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		loadingFrame = v.findViewById(R.id.loading_panel);
		loadingText = (TextView) loadingFrame.findViewById(R.id.loading_text);
		emptyFrame = v.findViewById(R.id.empty_panel);
		emptyText = (TextView) emptyFrame.findViewById(R.id.empty_text);
		loadingFrame.setClickable(true);
		emptyFrame.setClickable(true);
	}
	
	public void showLoading(CharSequence message) {
		showLoading(message, true);
	}
	public void showLoading(final CharSequence message, final boolean pushToStack) {
		HelperUtils.runOnUiThread(new Runnable() {
			public void run() {
				if (pushToStack) loading++;
				dismissEmpty();
				loadingFrame.setVisibility(View.VISIBLE);
				String msg = null;
				if (message != null) {
					msg = message.toString().toUpperCase();
				}
				loadingText.setText(msg);
			}
		});
	}

	public void showEmpty(CharSequence message) {
		empty++;
		dismissLoading();
		emptyFrame.setVisibility(View.VISIBLE);
		emptyText.setText(message);
	}

	public void dismissLoading() {
		loading--;
		if (loading <= 0) {
			loading = 0;
			if (loadingFrame != null) 
				HelperUtils.runOnUiThread(new Runnable() {
					public void run() {
						loadingFrame.setVisibility(View.GONE);				
					}
				});
		}
	}

	public void dismissEmpty() {
		empty--;
		if (empty <= 0) {
			empty = 0;
			if (emptyFrame != null)
				HelperUtils.runOnUiThread(new Runnable() {
					public void run() {
						emptyFrame.setVisibility(View.GONE);
					}
				});
		}
	}

	public void dispose() {
		setOnClickListener(null);
		loadingFrame = null;
		loadingText = null;
		emptyFrame = null;
		emptyText = null;
	}

	public void dismiss() {
		loading = 0;
		empty = 0;
		dismissLoading();
		dismissEmpty();
	}

	public void setOnClickListener(OnClickListener l) {
		emptyText.setOnClickListener(l);
		loadingText.setOnClickListener(l);
	}

	public static void show(final View v) {
		show(v, true);
	}
	public static void show(final View v, boolean animate) {
		if (v.getVisibility() == View.VISIBLE) return;
		if (!animate) {
			v.setVisibility(View.VISIBLE);
			return;
		}
		if (v.getVisibility() == View.GONE) v.setVisibility(View.INVISIBLE);
		
		Animation ca = v.getAnimation();
		if (ca != null) ca.cancel();
		AlphaAnimation a = new AlphaAnimation(0, 1);
		a.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				v.setVisibility(View.VISIBLE);
			}
		});
		a.setDuration(500);
		v.startAnimation(a);
	}

	public static void hide(View v, AnimationListener listener) {
		hide(v, true, false, listener);
	}
	public static void hide(View v) {
		hide(v, true, false, null);
	}
	public static void hide(final View v, boolean animate, final boolean gone) {
		hide(v, animate, gone, null);
	}
	public static void hide(final View v, boolean animate, final boolean gone, final AnimationListener listener) {
		if (v.getVisibility() != View.VISIBLE) return;
		
		Animation ca = v.getAnimation();
		if (ca != null) ca.cancel();
		
		if (!animate) {
			v.setVisibility((gone) ? View.GONE : View.INVISIBLE);
			return;
		}
		
		AlphaAnimation a = new AlphaAnimation(1, 0);
		a.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				v.setVisibility((gone) ? View.GONE : View.INVISIBLE);
				if (listener != null) listener.onAnimationEnd(animation);
			}
		});
		a.setDuration(250);
		v.startAnimation(a);
	}

}
