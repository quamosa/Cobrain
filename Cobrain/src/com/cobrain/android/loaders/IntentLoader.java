package com.cobrain.android.loaders;

import com.cobrain.android.MainActivity;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.Mobile;
import com.cobrain.android.service.web.WebRequest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

public class IntentLoader {

	private CobrainController controller;

	public IntentLoader() {
	}

	public void initialize(CobrainController controller) {
		this.controller = controller;
	}
	
	public void dispose() {
		controller = null;
	}
	
	public boolean processIntent(Intent i) {
		if (i != null) {
			String action = i.getAction();
			
			if (processNotifications(action)) return true;
			
			if (MainActivity.ACTION_SIGNUP.equals(action)) {
				controller.showSignup(null);
				return true;
			}
			
			Uri uri = i.getData();
			if (uri != null && uri.getHost().contains("cobrain.com")) {
				if (uri.getPath().equals("/mobile/download")) {
					//String phone = uri.getQueryParameter("phone");
					return processMobileDownloadIntent(uri.toString());
				}
				else if (uri.getPath().contains("/favorites/exchange/")) {
					return processEmailIntent(uri.toString());
				}
			}
		}
		return false;
	}
	
	private boolean processNotifications(String code) {
		if (Mobile.FRIENDSHIP_ACCEPTED.equals(code)) {
			//go to most recent friend's shared page
		}
		else if (Mobile.RAVED.equals(code)) {
			//go to users shared rack. If friends have only raved Cobrain recommended items, it should just go to the user�s home rack.
		}
		else if (Mobile.TASTEMAKER_PROGRES.equals(code)) {
			//Go to home rack with Tastemaker popup opened.  If user has jumped directly to Trendsetter Tastemaker, show just the Trendsetter popup.
		}
		else if (Mobile.CRAVES_ON_SALE.equals(code)) {
			//go to sale rack ???
		}
		else if (Mobile.APP_UPDATED.equals(code)) {
			//go to play store???
		}
		else return false;
			
		return true;
	}
	
	public boolean processAnyIntents(Activity activity) {
		Intent i = activity.getIntent();
		return processIntent(i);
	}

	boolean processEmailIntent(String url) {
		controller.showLogin(url);
		return true;
	}

	boolean processMobileDownloadIntent(String url) {
		//if (!processedMobileDownload) {
			new AsyncTask<String, Void, Boolean>() {
	
				@Override
				protected Boolean doInBackground(String... params) {
					WebRequest wr = new WebRequest().get(params[0]);
					if (wr.go() == 200) {
						return true;
					}
					return false;
				}

				boolean result;
				Runnable runWhenLoggedIn = new Runnable() {
					public void run() {
						if (result) {
							//processedMobileDownload = true;
							controller.showMain(CobrainController.VIEW_FRIENDS_MENU);
						}
						else controller.showMain(CobrainController.VIEW_HOME);					
					}
				};
				
				@Override
				protected void onPostExecute(Boolean result) {
					if (!controller.getCobrain().isLoggedIn()) {
						this.result = result;
						if (!controller.getCobrain().restoreLogin(runWhenLoggedIn)) {
							controller.showLogin(null);
						}
					}
				}
				
			}.execute(url);
		//}
		return true;
	}
}
