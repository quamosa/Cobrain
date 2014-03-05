package com.cobrain.android.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.cobrain.android.MiniFragment;

public class MiniFragmentPagerAdapter extends PagerAdapter {

	Activity a;
	HashMap<MiniFragment, ArrayList<MiniFragment>> subFragments = new HashMap<MiniFragment, ArrayList<MiniFragment>>();
	
	public MiniFragmentPagerAdapter() {
		
	}
	
	public MiniFragmentPagerAdapter(Activity a) {
		this.a = a;
	}
	
	public void dispose() {
		a = null;
	}
	
	public Activity getActivity() {
		return a;
	}
	
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		MiniFragment f = (MiniFragment) object;
		ArrayList<MiniFragment> subs = subFragments.get(f);
		if (subs != null) {
			for (MiniFragment sub : subs) {
				sub.destroy();
			}
			subs.clear();
		}
		f.destroy();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		MiniFragment f = (MiniFragment) arg1;
		return f.getView() == arg0;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		MiniFragment f = getItem(position);
		f.create(container);
		
		
		for (int i = 0; i < getSubItemCount(position); i++) {
			int pos = position * getCountPerPage();

			MiniFragment fsub = getSubItem(f, pos + i);
			//fsub.create(container);
			ArrayList<MiniFragment> sub = subFragments.get(f);
			if (sub == null) {
				sub = new ArrayList<MiniFragment>();
				subFragments.put(f, sub);
			}
			sub.add(fsub);
		}
		
		return f;
	}

	public MiniFragment getItem(int position) {
		return null;
	}

	public int getTotalCount() {
		return 0;
	}

	@Override
	public int getCount() {
		int cnt = getTotalCount();
		int scnt = getCountPerPage();
		if (scnt > 0) {
			cnt = (int) Math.ceil(cnt / (float)scnt);
		}
		return cnt;
	}

	public int getCountPerPage() {
		return 0;
	}

	public MiniFragment getSubItem(MiniFragment parent, int position) {
		return null;
	}	

	private int getSubItemCount(int position) {
		int cntpg = getCountPerPage(); 
		int cnt = getTotalCount() - (position * cntpg);
		cnt = Math.min(cntpg, cnt);
		return cnt;
	}

}
