package com.cobrain.android;

import com.cobrain.android.utils.Analytics;

import android.app.Application;

public class ThisApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		Analytics.dispose();
		super.onTerminate();
	}

}
