package com.cobrain.android.model;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import com.cobrain.android.adapters.CravePagerAdapter;
import com.cobrain.android.loaders.CraveLoader;

public class CraveStrip {
	public String caption;
	public int categoryId;
	public CravePagerAdapter adapter;
	public CraveLoader loader;
	public ViewPager pager;
	public RelativeLayout container;
}