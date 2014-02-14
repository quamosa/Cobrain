package com.cobrain.android.fragments;

import java.util.ArrayList;

import com.cobrain.android.R;
import com.cobrain.android.drawables.EdgeFadeDrawable;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.HomePagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends BaseCobrainFragment implements OnPageChangeListener {

	public static final String TAG = "HomeFragment";
	ViewPager homePager;
	private HomePagerAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.frg_home, null);
		homePager = (ViewPager) v.findViewById(R.id.home_pager);
		HomePagerTabStrip titleStrip = (HomePagerTabStrip) v.findViewById(R.id.home_pager_tab_strip);
		
		homePager.setOnPageChangeListener(this);
		adapter = new HomePagerAdapter(getChildFragmentManager());
		
		titleStrip.setBackgroundColor(Color.BLACK);
		titleStrip.setTextColor(Color.WHITE);
		titleStrip.setTabIndicatorColor(Color.WHITE);
		homePager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.home_pager_margin_size));
		//homePager.setPageMarginDrawable(new ColorDrawable(getResources().getColor(R.color.CraveBorderColor)));
		homePager.setPageMarginDrawable(new EdgeFadeDrawable(getResources().getColor(R.color.CraveBorderColor)));
		adapter.add("HOME RACK", new CraveStripsFragment());
		adapter.add("SALE RACK", CraveStripsFragment.newInstance(true));
		adapter.add("MY PRIVATE RACK", new SavedAndShareFragment());
		adapter.add("MY SHARED RACK", WishListFragment.newInstance(controller.getCobrain().getUserInfo(), false));
		//homePager.setOffscreenPageLimit(4);
		homePager.setAdapter(adapter);
		homePager.post(new Runnable(){
			@Override
			    public void run() {
			        onPageSelected(0);
			    }
			});

		//showPersonalizationAnimation();

		return v;
	}

	void showPersonalizationAnimation() {
		PersonalizationAnimationFragment f = new PersonalizationAnimationFragment();
		getFragmentManager().beginTransaction()
			.add(R.id.overlay_layout, f, PersonalizationAnimationFragment.TAG)
			.commitAllowingStateLoss();
	}
	
	private class FragmentHolder {
		String title;
		Fragment fragment;
		boolean updated;
	}

	private class HomePagerAdapter extends FragmentPagerAdapter {
		ArrayList<FragmentHolder> fragmentHolder = new ArrayList<FragmentHolder>();
		
		public HomePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		FragmentHolder getFragmentHolder(int position) {
			return fragmentHolder.get(position);
		}
		
		public void clear() {
			
			FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			
			while (getCount() > 0) {
				Fragment f = getItem(0);
				destroyItem(null, 0, f);

				ft.remove(f);

				FragmentHolder fh = fragmentHolder.remove(0);
				fh.fragment = null;
				fh.title = null;
			}
			
			ft.commitAllowingStateLoss();
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			//FIXME: dont destroy the fragment once its been created.. lets see how we do on memory!
			//super.destroyItem(container, position, object);
		}

		public void add(String title, Fragment f) {
			FragmentHolder fh = new FragmentHolder();
			fh.title = title;
			fh.fragment = f;
			fragmentHolder.add(fh);
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return fragmentHolder.size();
		}

		@Override
		public Fragment getItem(int position) {
			Fragment f = fragmentHolder.get(position).fragment;
			return f;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return fragmentHolder.get(position).title;
		}
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		adapter.clear();
		adapter = null;
		homePager = null;
		super.onDestroyView();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		Fragment f = adapter.getItem(position);
		if (f instanceof BaseCobrainFragment) {
			BaseCobrainFragment bf = (BaseCobrainFragment) f;
			if (bf instanceof CraveStripsFragment) { 
				FragmentHolder fh = adapter.getFragmentHolder(position);
				if (!fh.updated) {
					fh.updated = true;
					bf.update();
				}
			}
			controller.setCurrentCobrainView(bf);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

}
