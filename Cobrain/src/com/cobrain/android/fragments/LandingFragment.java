package com.cobrain.android.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.cobrain.android.MainActivity;
import com.cobrain.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class LandingFragment extends Fragment implements View.OnClickListener, OnPageChangeListener {

	public static final String TAG = "LandingFragment";

	ViewPager pager;

	public static LandingFragment newInstance() {
		LandingFragment f = new LandingFragment();
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_landing, null);
		pager = (ViewPager) v.findViewById(R.id.pager);
		pager.setOnPageChangeListener(this);
        HomePagerAdapter adapter = new HomePagerAdapter(getChildFragmentManager());
		pager.setAdapter(adapter);

        Button b = (Button) v.findViewById(R.id.signup_button);
        b.setOnClickListener(this);
        b = (Button) v.findViewById(R.id.login_button);
        b.setOnClickListener(this);

		return v;
	}

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.signup_button:
                MainActivity.start(getActivity(), MainActivity.ACTION_SIGNUP);
                break;
            case R.id.login_button:
                MainActivity.start(getActivity(), null);
                break;
        }
        getActivity().finish();
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
	private class FragmentHolder {
		String title;
		Fragment fragment;
		boolean updated;
		public Bitmap bitmap;
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
		public int getCount() {
			return 2;
		}

        Fragment getFragmentFromHolder(int position) {
            Fragment f = null;

            if (fragmentHolder.size() > position) {
                f = fragmentHolder.get(position).fragment;
            }

            if (f == null) {
                f = getFragment(position);
                FragmentHolder fh = new FragmentHolder();
                fh.fragment = f;
                fragmentHolder.add(position, fh);
            }

            return f;
        }

        Fragment getFragment(int position) {
            switch (position) {
                case 0: return new LandingFragment1();
                case 1: return new LandingFragment2();
            }
            return null;
        }

		@Override
		public Fragment getItem(int position) {
            Fragment f = getFragmentFromHolder(position);
			if (!clearing && position == pager.getCurrentItem())
				onPageSelected(position);
			return f;
		}
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
        pager.setAdapter(null);
		pager = null;
		super.onDestroyView();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
        ImageView iv = (ImageView) getView().findViewById(R.id.pager_indicator);
        switch (position) {
            case 0:
                iv.setImageResource(R.drawable.ic_landing_indicator_1);
                break;
            case 1:
                iv.setImageResource(R.drawable.ic_landing_indicator_2);
                break;
        }
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}
}
