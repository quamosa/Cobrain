package com.cobrain.android.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import com.cobrain.android.R;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.views.RepeatingTabHost;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.HomePagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;

public class HomeFragment extends BaseCobrainFragment implements OnPageChangeListener {

	public static final String TAG = "HomeFragment";
	public static final int TAB_HOME_RACK = 0;
	public static final int TAB_SALE_RACK = 1;
	public static final int TAB_PRIVATE_RACK = 2;
	public static final int TAB_SHARED_RACK = 3;
	
	ViewPager homePager;
	private HomePagerAdapter adapter;
	int initialTab = -1;
	private boolean loading;
	private RepeatingTabHost tabHost;
	private static final boolean useCustomTabs = true;

	public static HomeFragment newInstance(int showTab, boolean showPersonalizationAnimation) {
		HomeFragment f = new HomeFragment();
		f.initialTab = showTab;
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = null;
		
		if (!useCustomTabs) {
			v = inflater.inflate(R.layout.frg_home, null);
			HomePagerTabStrip titleStrip = (HomePagerTabStrip) v.findViewById(R.id.home_pager_tab_strip);
			titleStrip.setBackgroundColor(Color.BLACK);
			titleStrip.setTextColor(Color.WHITE);
			titleStrip.setTabIndicatorColor(Color.WHITE);
		}
		else {
			v = inflater.inflate(R.layout.frg_home_w_repeating_tabs, null);
			tabHost = (RepeatingTabHost) v.findViewById(R.id.repeating_tabs);
			tabHost.setOnTabChangedListener(new OnTabChangeListener() {
				@Override
				public void onTabChanged(String tabId) {
					int i = Integer.parseInt(tabId);
					if (homePager.getCurrentItem() != i) {
						homePager.setCurrentItem(i, false);
					}
				}
			});
			
		}

		homePager = (ViewPager) v.findViewById(R.id.home_pager);
		homePager.setOnPageChangeListener(this);
		adapter = new HomePagerAdapter(getChildFragmentManager());
		
		if (useCustomTabs) {
			((com.cobrain.android.views.ViewPager) homePager).setPagingEnabled(false);
			tabHost.setAdapter(adapter);
		}
		
		homePager.setPageMargin(getResources().getDimensionPixelOffset(R.dimen.home_pager_margin_size));
		homePager.setPageMarginDrawable(new ColorDrawable(getResources().getColor(R.color.CraveBorderColor)));
		//homePager.setPageMarginDrawable(new EdgeFadeDrawable(getResources().getColor(R.color.CraveBorderColor)));

		setupAdapter();
		homePager.setAdapter(adapter);

		setTitle("Cobrain");
		
		autoUpdateOnlyFor(Math.max(initialTab, 0));

		if (initialTab >= 0) {
			//turn off autoupdate for all tabs except the initial one!
			loading = true;
			homePager.setCurrentItem(initialTab, false);
			loading = false;
		}
		
		/*if (initialTab <= 0)
			homePager.post(new Runnable(){
				@Override
				    public void run() {
						onPageSelected(0);
				    }
				});*/
		
		return v;
	}

	private void setupAdapter() {
		adapter.add("Home Rack", R.drawable.ic_navigation_menu_home, new CraveStripsFragment());

		adapter.add("Sale Rack", R.drawable.ic_navigation_menu_sale_rack, CraveStripsFragment.newInstance(true));

		adapter.add("My Private Rack", R.drawable.ic_navigation_menu_private_rack, new SavedAndShareFragment());

		adapter.add("My Shared Rack", R.drawable.ic_navigation_menu_shared_rack, SavedAndShareFragment.newInstance("shared"));
	}

/*	@Override
	public void onSaveInstanceState(Bundle outState) {
		adapter.clear();
		adapter.notifyDataSetChanged();
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		setupAdapter();
		super.onResume();
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}
*/
	void autoUpdateOnlyFor(int position) {
		for (int i = 0; i < adapter.getCount(); i++) {
			FragmentHolder fh = adapter.getFragmentHolder(i);
			BaseCobrainFragment f = (BaseCobrainFragment) fh.fragment;
			if (f != null) f.autoUpdate = (i == position);
		}
	}

	private class FragmentHolder {
		String title;
		Fragment fragment;
		boolean updated;
		public Bitmap bitmap;
	}

	@Override
	public void setSubTitle(BaseCobrainFragment child, CharSequence title) {
		if (adapter.getFragmentHolder( homePager.getCurrentItem() ).fragment == child) {
			setSubTitle(title);
		}
	}

	private class HomePagerAdapter extends FragmentPagerAdapter {
		ArrayList<FragmentHolder> fragmentHolder = new ArrayList<FragmentHolder>();
		HashMap<String, FragmentHolder> fragmentHolderMap = new HashMap<String, FragmentHolder>();
		private boolean clearing;
		
		public HomePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		FragmentHolder getFragmentHolder(int position) {
			return fragmentHolder.get(position);
		}
		
		public void clear() {
			//FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
			clearing = true;
			
			while (getCount() > 0) {
				Fragment f = getItem(0);
				destroyItem(null, 0, f);

				//ft.remove(f);

				FragmentHolder fh = fragmentHolder.remove(0);
				fragmentHolderMap.remove(fh.title);
				fh.fragment = null;
				fh.title = null;
				if (fh.bitmap != null) {
					fh.bitmap.recycle();
					fh.bitmap = null;
				}
			}
			
			//ft.commitAllowingStateLoss();
			
			clearing = false;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			//FIXME: dont destroy the fragment once its been created.. lets see how we do on memory!
			//super.destroyItem(container, position, object);
		}

		public void add(String title, int iconId, Fragment f) {
			FragmentHolder fh = fragmentHolderMap.get(title);
			if (fh == null) fh = new FragmentHolder();
			if (iconId != 0) {
				fh.bitmap = BitmapFactory.decodeResource(getActivity().getResources(), iconId);
			}
			if (f != null) {
				Bundle args = f.getArguments();
				if (args == null) {
					args = new Bundle();
					f.setArguments(args);
				}
				args.putString("pagerTitle", title);
				args.putInt("pagerIconId", iconId);
			}
			fh.title = title;
			fh.fragment = f;
			if (!fragmentHolder.contains(fh)) {
				fragmentHolder.add(fh);
				fragmentHolderMap.put(fh.title, fh);
				notifyDataSetChanged();
			}
		}
		
		@Override
		public int getCount() {
			return fragmentHolder.size();
		}

		@Override
		public Fragment getItem(int position) {
			Fragment f = fragmentHolder.get(position).fragment;
			if (!clearing && position == homePager.getCurrentItem()) 
				onPageSelected(position);
			return f;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment f = (Fragment) super.instantiateItem(container, position);
			if (f != null) {
				Bundle args = f.getArguments();
				if (args != null) {
					String title = args.getString("pagerTitle");
					FragmentHolder fh = fragmentHolderMap.get(title);
					if (fh != null) {
						if (fh.fragment != f) {
							fh.fragment = f;
						}
					}
				}
			}
			return f;
		}

		@Override
		public CharSequence getPageTitle(int position) {

			FragmentHolder fh = fragmentHolder.get(position);
			if (fh.bitmap != null) {
			    SpannableStringBuilder sb = new SpannableStringBuilder("   " + fh.title);
			    ImageSpan span = new ImageSpan(getActivity().getApplicationContext(), fh.bitmap, ImageSpan.ALIGN_BASELINE); 
			    sb.setSpan(span, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
			    return sb;
			}

		    return fh.title;
		}
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		setSubTitle(null);
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
		setSubTitle(null);
		tabHost.setCurrentTab(position);
		if (loading) homePager.post(updateRunnable);
		else updateRunnable.run();
	}

	void selectNavigationMenuItem(int position) {
		String id = null;
		
		switch(position) {
			case 0: id = "0"; break;
			case 1: id = "7"; break;
			case 2: id = "2"; break;
			case 3: id = "3"; break;
		}
		
		if (id != null) controller.setMenuItemSelected(id);
	}
	
	Runnable updateRunnable = new Runnable() {
		public void run() {
			int position = homePager.getCurrentItem();
			FragmentHolder fh = adapter.getFragmentHolder(position);
			Fragment f = fh.fragment;
			if (f instanceof BaseCobrainFragment) {
				fh.updated = true;
				((BaseCobrainFragment) f).update();
				selectNavigationMenuItem(position);
			}
			
/*			Fragment f = adapter.getItem(position);
			if (f instanceof BaseCobrainFragment) {
				BaseCobrainFragment bf = (BaseCobrainFragment) f;
				bf.update();
				if (bf instanceof CraveStripsFragment) { 
					FragmentHolder fh = adapter.getFragmentHolder(position);
					if (!fh.updated) {
						fh.updated = true;
						bf.update();
					}
				}
				else {
					bf.update();
				}
				controller.setCurrentCobrainView(bf);
			}*/
		}
	};

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	public void setTab(int tab) {
		//homePager.setCurrentItem(tab, false);
		tabHost.setCurrentTab(tab);
	}

	void setupCustomTabs(View v) {
		
	}
}
