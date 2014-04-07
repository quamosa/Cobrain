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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cobrain.android.R;

import java.util.ArrayList;
import java.util.HashMap;

public class LandingFragment2 extends Fragment implements OnPageChangeListener {

	public static final String TAG = "LandingFragment2";

	ViewPager pager;
    ImageView indicator;
    Animation in, out;
    RelativeLayout header;

	public static LandingFragment2 newInstance() {
		LandingFragment2 f = new LandingFragment2();
		return f;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);

        out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                for (View v : headers)
                    if (v.getAnimation() == animation) {
                        headers.remove(v);
                        ViewGroup vg = (ViewGroup) v.getParent();
                        vg.removeView(v);
                        break;
                    }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

		View v = inflater.inflate(R.layout.frg_tutorial, null);
		pager = (ViewPager) v.findViewById(R.id.pager);
		pager.setOnPageChangeListener(this);
        HomePagerAdapter adapter = new HomePagerAdapter(getChildFragmentManager());
		pager.setAdapter(adapter);

		return v;
	}

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        header = (RelativeLayout) v.getRootView().findViewById(R.id.header);
        indicator = (ImageView) v.getRootView().findViewById(R.id.pager_indicator);

        setHeaderForPage(0);

        super.onViewCreated(v, savedInstanceState);
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
			return 4;
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
            return TutorialFragment.newInstance(position + 1);
        }

        @Override
		public Fragment getItem(int position) {
			Fragment f = getFragmentFromHolder(position);
			//if (!clearing && position == pager.getCurrentItem())
			//	onPageSelected(position);
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
        indicator = null;
        header = null;
		super.onDestroyView();
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
        int did = 0;

        setHeaderForPage(position);

        switch(position + 1) {
            case 1:
                did = R.drawable.ic_landing_indicator_2;
                break;
            case 2:
                did = R.drawable.ic_landing_indicator_3;
                break;
            case 3:
                did = R.drawable.ic_landing_indicator_4;
                break;
            case 4:
                did = R.drawable.ic_landing_indicator_5;
                break;
        }

        indicator.setImageResource(did);
	}

    ArrayList<View> headers = new ArrayList<View>();

    void setHeaderForPage(int position) {
        String caption = null;
        String header = null;

        switch(position + 1) {
            case 1:
                header = "Teach";
                caption = "Tell your Cobrain what you love so it can find things you’ll want to buy";
                break;
            case 2:
                header = "Invite";
                caption = "Invite friends to give your opinion on what they’re sharing";
                break;
            case 3:
                header = "Share";
                caption = "Share the things you love to get opinions from friends before you buy";
                break;
            case 4:
                header = "Buy";
                caption = "Buy the things you love through the app directly from over 300 online stores";
                break;
        }

        setHeader(header, caption);

    }

    void setHeader(String header, String caption) {
        View v = View.inflate(getActivity(), R.layout.frg_tutorial_header, null);
        boolean firstRun = headers.size() == 0;
        headers.add(v);

        TextView tv = (TextView) v.findViewById(R.id.header);
        tv.setText(header);
        tv = (TextView) v.findViewById(R.id.caption);
        tv.setText(caption);

        this.header.addView(v);

        if (!firstRun)
            v.startAnimation(in);

        int last = headers.size() - 1 - 1;
        if (last >= 0)
            headers.get(last).startAnimation(out);

    }

	@Override
	public void onPageScrollStateChanged(int state) {
	}
}
