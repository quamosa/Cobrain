package com.cobrain.android.adapters;

import com.cobrain.android.fragments.AccountFragment;
import com.cobrain.android.fragments.ForgetMeFragment;
import com.cobrain.android.fragments.ProfileFragment;

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
            return "Profile";
        case 1:
        	return "Account";
        case 2:
        	return "Forget Me";
        }
		return null;
	}

	@Override
    public Fragment getItem(int index) {
 
        switch (index) {
        case 0:
            return new ProfileFragment();
        case 1:
            return new AccountFragment();
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
