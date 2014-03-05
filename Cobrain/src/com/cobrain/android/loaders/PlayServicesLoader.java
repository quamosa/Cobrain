package com.cobrain.android.loaders;

import java.io.IOException;

import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainSharedPreferences;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

public class PlayServicesLoader {
	private static final String TAG = "PlayServicesLoader";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private final static String PROPERTY_REG_ID = "gcm.app.registration_id";
	private final static String PROPERTY_APP_VERSION = "gcm.app.version";
	private final static String SENDER_ID = "521768102800";
	//for reference:
	private final static String SERVER_API_KEY = "AIzaSyDopi6KJ86323elpFSlhtqF4JHWQ1D0mPE";
	private static final boolean CLOUD_MESSAGING_ENABLED = false;
	
	
    String regid;
	GoogleCloudMessaging gcm;
	private boolean playServicesNotAvailable;

	/**
	 * Check the device to make sure it has the Google Play Services APK. If
	 * it doesn't, display a dialog that allows users to download the APK from
	 * the Google Play Store or enable it in the device's system settings.
	 */
	
	public boolean checkGoogleCloudMessaging(Activity activity, boolean onResume) {
		if (!CLOUD_MESSAGING_ENABLED) return true;
		
        Context context = activity.getApplicationContext();

        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices(activity, onResume)) {
            gcm = GoogleCloudMessaging.getInstance(context);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground(context);
            }
            return true;
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }		
        
        return false;
	}
	
	public boolean checkPlayServices(Activity activity, boolean onResume) {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	        	if (!playServicesNotAvailable)
		            GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
		                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	            if (!onResume) playServicesNotAvailable = true;
	        } else {
	            Log.i(TAG, "This device is not supported.");
	        }
	        return false;
	    }
	    return true;
	}
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    CobrainSharedPreferences prefs = new Cobrain(context).getGlobalSharedPrefs();

	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}	
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    int appVersion = getAppVersion(context);

	    Log.i(TAG, "Saving regId on app version " + appVersion);

	    new Cobrain(context).getGlobalSharedPrefs().edit()
	    	.putString(PROPERTY_REG_ID, regId)
	    	.putInt(PROPERTY_APP_VERSION, appVersion)
	    	.commit();
	}
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground(final Context context) {
	    new AsyncTask<Void, Void, String>() {
	        @Override
	        protected String doInBackground(Void... params) {
	            String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(context);
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                sendRegistrationIdToBackend();

	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the
	                // message using the 'from' address in the message.

	                // Persist the regID - no need to register again.
	                storeRegistrationId(context, regid);
	                
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
	        }

	        @Override
	        protected void onPostExecute(String msg) {
	            Log.i(TAG, msg);
	        }
	    }.execute();
	}
	
	private void sendRegistrationIdToBackend() {
		
	}

}
