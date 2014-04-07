package com.cobrain.android;

import android.app.Application;

import com.cobrain.android.fragments.BaseCobrainFragment;
import com.cobrain.android.utils.Analytics;

public class ThisApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onTerminate() {
        BaseCobrainFragment.controller = null;
        MainActivity.environment = null;
        Analytics.dispose();
		super.onTerminate();
	}

}
