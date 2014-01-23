package com.cobrain.android.fragments;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.cobrain.android.R;
import com.cobrain.android.adapters.NerveCenterPagerAdapter;

public class NerveCenterFragment extends BaseCobrainFragment implements OnPageChangeListener, TabListener {
	public static final String TAG = "NerveCenterFragment";
	private ViewPager pager;
	private NerveCenterPagerAdapter adapter;
	private int oldActionBarNavMode;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.main_nerve_center_frame, null);
		loaderUtils.initialize((ViewGroup) v);

		pager = (ViewPager) v.findViewById(R.id.nerve_center_pager);
		adapter = new NerveCenterPagerAdapter(getChildFragmentManager());
		pager.setAdapter(adapter);
		pager.setOffscreenPageLimit(3);
		pager.setOnPageChangeListener(this);
		
		oldActionBarNavMode = actionBar.getNavigationMode();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab().setText("INVITATIONS").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("LOGIN INFO").setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText("FORGET ME").setTabListener(this));
		
    	setTitle("Nerve Center");
    	
        return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onPageSelected(int position) {
		actionBar.selectTab( actionBar.getTabAt(position) );
	}

	@Override
	public void onDestroyView() {
		actionBar.removeAllTabs();
		actionBar.setNavigationMode(oldActionBarNavMode);
		pager.setAdapter(null);
		pager.setOnPageChangeListener(null);
		pager = null;
		adapter = null;
		super.onDestroyView();
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		pager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

}
