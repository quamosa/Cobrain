package com.cobrain.android.loaders;

import java.util.Calendar;

import com.actionbarsherlock.app.SherlockFragment;
import com.cobrain.android.R;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.service.Cobrain;
import com.cobrain.android.service.Cobrain.CobrainController;
import com.cobrain.android.service.web.WebRequest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class TasteMakerLoader {
	public final static String PREF_NUM_TIMES_SHOWN = "TasteMaker.timesShown";
	public final static String PREF_LAST_TIME_SHOWN = "TasteMaker.lastTimeShown";
	
	static String rulesUrl;
	
	public TasteMakerLoader() {
	}
	
	public static long getLastTimeShown(Cobrain c) {
		return c.getSharedPrefs().getLong(PREF_LAST_TIME_SHOWN, 0);
	}
	private static void setLastTimeShown(Cobrain c, Long timeInMillis, int times) {
		if (times == 0) times = c.getSharedPrefs().getInt(PREF_NUM_TIMES_SHOWN, 0); 
		Editor edit = c.getEditableSharedPrefs();
		edit.putLong(PREF_LAST_TIME_SHOWN, timeInMillis);
		edit.putInt(PREF_NUM_TIMES_SHOWN, times + 1);
		edit.commit();
	}
	
	private static void resetTimesShown(Cobrain c) {
		setLastTimeShown(c, 0L, -1);
	}
	
	public static void show(CobrainController c) {
		int timesShown = c.getCobrain().getSharedPrefs().getInt(PREF_NUM_TIMES_SHOWN, 0);
		if (timesShown < 3) {
			long lastTimeShown = getLastTimeShown(c.getCobrain());
			Calendar today = Calendar.getInstance();
			Calendar lastRun = Calendar.getInstance();
			lastRun.setTimeInMillis(lastTimeShown);
			
			//if today is more than 24 hours than last run time then we try to show it
			if (today.getTimeInMillis() - lastRun.getTimeInMillis() > (24 * 60 * 60 * 1000)) {
				_show(c, today);
			}
		}
	}
	
	private static void _show(final CobrainController controller, final Calendar showDate) {
		Resources res = controller.getCobrain().getContext().getResources();
		rulesUrl = res.getString(R.string.url_tastemaker_beta_program_rules, res.getString(R.string.url_cobrain_app));
		
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				String s = controller.getCobrain().getContext().getString(R.string.url_cobrain_api);
				/*if (s.indexOf("qa.") >= 0) {
					//resetTimesShown(controller.getCobrain());
					return true;
				}
				else {*/
					WebRequest wr = new WebRequest().get(rulesUrl);
					return wr.go() == 200;
				//}
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					AlertDialog.Builder builder;
					final AlertDialog alertDialog;
					Context c = (Activity)controller;
	
					LayoutInflater inflater = (LayoutInflater)
					        c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View v = inflater.inflate(R.layout.dlg_tastemaker, null);
	
					builder = new AlertDialog.Builder(c);
					builder.setView(v).setCancelable(false);
					alertDialog = builder.create();
					
					TextView tv = (TextView) v.findViewById(R.id.tastemaker_rules_link);
					tv.setMovementMethod(LinkMovementMethod.getInstance());

					String link = c.getResources().getString(R.string.tastemaker_beta_program_rules_link, rulesUrl);
					tv.setText(Html.fromHtml(link));
					
					View button = v.findViewById(R.id.tastemaker_next);
					button.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							v.setOnClickListener(null);
							v.post(r);
							alertDialog.dismiss();
						}

						Runnable r = new Runnable() {
							public void run() {
								controller.showFriendsMenu();
								SherlockFragment f = (SherlockFragment) controller.getShown();
								if (f instanceof FriendsListFragment) {
									((FriendsListFragment) f).showContactList();
								}
							}
						};
						
					});
					
					setLastTimeShown(controller.getCobrain(), showDate.getTimeInMillis(), 0);
					alertDialog.show();
				}
			}
			
		}.execute();
	}
}
