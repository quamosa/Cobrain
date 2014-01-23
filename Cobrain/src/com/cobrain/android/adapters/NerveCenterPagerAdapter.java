package com.cobrain.android.adapters;

import com.cobrain.android.fragments.ForgetMeFragment;
import com.cobrain.android.fragments.InviteFragment;
import com.cobrain.android.fragments.LoginInfoFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class NerveCenterPagerAdapter extends FragmentPagerAdapter {
	 
    public NerveCenterPagerAdapter(FragmentManager fm) {
        super(fm);
    }
 
    @Override
	public CharSequence getPageTitle(int position) {
        switch (position) {
        case 0:
            return "INVITATIONS";
        case 1:
        	return "LOGIN INFO";
        case 2:
        	return "FORGET ME";
        }
		return null;
	}

	@Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            return new InviteFragment();
        case 1:
            return new LoginInfoFragment();
        case 2:
            return new ForgetMeFragment();
        }
 
        return null;
    }
 
    @Override
    public int getCount() {
        return 3;
    }
 
}
