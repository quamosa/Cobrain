package com.cobrain.android;

import com.cobrain.android.fragments.BaseCobrainFragment.StateSaver;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MiniFragment {

	View v;
	ViewGroup container;
	Context context;
	Activity activity;
	StateSaver state;

	public MiniFragment() {
	}

	public MiniFragment(Activity a) {
		activity = a;
		context = a.getApplicationContext();
	}

	public final void create(ViewGroup container) {
		this.container = container;
		Bundle bundle = (state != null) ? state.getBundle() : null;
		onCreate(bundle, container);
        LayoutInflater inflater = LayoutInflater.from(context);
		v = onCreateView(bundle, inflater, container);
		if (container != null) container.addView(v);
		onActivityCreated(bundle);
	}

	View onCreate(Bundle inState, ViewGroup container) {
		return null;
	}
	
	public View onCreateView(Bundle inState, LayoutInflater inflater, ViewGroup container) {
		return null;
	}

	public boolean isVisible() {
		if (v != null) return v.getVisibility() == View.VISIBLE;
		return false;
	}
	public View getView() {
		return v;
	}
	
	public void onActivityCreated(Bundle state) {}

	/*
	 * Must call super.onDestroy()!
	 */
	public final void destroy() {
		onDestroy();
	}

	public Activity getActivity() {
		return activity;
	}
	
	/*
	 * Must call super.onDestroy()!
	 */
	public void onDestroy() {
		onDestroyView();
		activity = null;
		context = null;
		if (v != null && container != null) {
			container.removeView(v);
		}
	}

	public void onDestroyView() {
	}

	public void onSaveInstanceState(Bundle outState) {
	}

}
