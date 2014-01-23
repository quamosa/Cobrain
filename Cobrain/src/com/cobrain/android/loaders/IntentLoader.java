package com.cobrain.android.loaders;

import com.cobrain.android.service.Cobrain.CobrainController;
import com.cobrain.android.service.web.WebRequest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

public class IntentLoader {

	private boolean processedMobileDownload;
	private CobrainController controller;

	public IntentLoader() {
	}

	public void initialize(CobrainController controller) {
		this.controller = controller;
	}
	
	public void dispose() {
		controller = null;
	}
	
	public void processAnyIntents(Activity activity) {
		Intent i = activity.getIntent();
		if (i != null) {
			Uri uri = i.getData();
			if (uri != null && uri.getHost().contains("cobrain.com") && uri.getPath().equals("/mobile/download")) {
				//String phone = uri.getQueryParameter("phone");
				processMobileDownloadIntent(uri.toString());
			}
		}
	}

	void processMobileDownloadIntent(String url) {
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
	
				@Override
				protected void onPostExecute(Boolean result) {
					if (result) {
						//processedMobileDownload = true;
						controller.showFriendsMenu();
					}
				}
				
			}.execute(url);
		//}
	}
}
