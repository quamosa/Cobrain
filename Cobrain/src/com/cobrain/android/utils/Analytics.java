package com.cobrain.android.utils;

import android.app.Activity;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class Analytics {
	static EasyTracker et;
	
	public static void start(Activity a) {
		et = EasyTracker.getInstance(a.getApplicationContext());
		et.activityStart(a);
	}
	public static void sendEvent(String category, String action, String label, Long value) {
		et.send(MapBuilder
			      .createEvent(category,    // Event category (required)
			                   action,  	// Event action (required)
			                   label,   	// Event label
			                   value)       // Event value
			      .build()
			 );
	}
	public static void stop(Activity a) {
		et.activityStop(a);
	}
	public static void dispose() {
		et = null;
	}
}
