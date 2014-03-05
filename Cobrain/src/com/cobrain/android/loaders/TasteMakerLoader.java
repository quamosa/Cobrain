package com.cobrain.android.loaders;

import java.util.Calendar;

import com.actionbarsherlock.app.SherlockFragment;
import com.cobrain.android.R;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.controllers.Cobrain.CobrainSharedPreferences;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.HelperUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
	public final static String PREF_LAST_SHOWN = "TasteMaker.lastShown";
	public final static int TRENDSETTER_WIN = 5;
	public final static int TASTEMAKER_WIN = 4;
	public final static int TRENDSETTER_CLOSE = 3;
	public final static int TASTEMAKER_CLOSE = 2;
	public final static int TASTEMAKER_BETA = 1;
	public final static int MAX_TIMES_TO_SHOW = 3;
	final static boolean DEBUG = false;
	
	static String rulesUrl;
	
	public TasteMakerLoader() {
	}
	
	public static long getLastTimeShown(Cobrain c) {
		return c.getSharedPrefs().getLong(PREF_LAST_TIME_SHOWN, 0);
	}
	public static int getLastShown(Cobrain c) {
		return c.getSharedPrefs().getInt(PREF_LAST_SHOWN, 0);
	}
	private static void setLastTimeShown(Cobrain c, int lastShown, Long timeInMillis, int times) {
		if (times < 0) {
			if (times == -1) times = c.getSharedPrefs().getInt(PREF_NUM_TIMES_SHOWN, 0); 
			else if (times == -2) times = -1;
		}
		
		CobrainSharedPreferences prefs = c.getSharedPrefs();
		prefs.edit();
		prefs.putLong(PREF_LAST_TIME_SHOWN, timeInMillis);
		prefs.putInt(PREF_NUM_TIMES_SHOWN, times + 1);
		if (lastShown >= 0) prefs.putInt(PREF_LAST_SHOWN, lastShown);
		prefs.commit();
	}
	
	private static void resetTimesShown(Cobrain c) {
		setLastTimeShown(c, -1, 0L, -2);//resetTimesShown(c.getCobrain())
	}
	
	public static int getMaxTimesToShow(Cobrain c, int shown) {
		switch(shown) {
		case TASTEMAKER_WIN:
			return 1;
		case TRENDSETTER_WIN:
			return 1;
		default:
			return MAX_TIMES_TO_SHOW;
		}
	}
	
	static TasteMakerInfo checkNotificationType(CobrainController controller) {
		Cobrain c = controller.getCobrain();
		UserInfo ui = c.getUserInfo();
		ui.fetchUserInfo(); //refresh user notifications and all

		TasteMakerInfo info = new TasteMakerInfo();
		info.shown = getLastShown(c);

		if (ui.hasNotification(User.NOTIFICATION_TRENDSETTER_WIN)) {
			info.notification = User.NOTIFICATION_TRENDSETTER_WIN;
			if (DEBUG || info.shown < TRENDSETTER_WIN) {
				resetTimesShown(c);
				info.shown = TRENDSETTER_WIN;
			}
		}
		else 
			if (ui.hasNotification(User.NOTIFICATION_TASTEMAKER_WIN)) {
				info.notification = User.NOTIFICATION_TASTEMAKER_WIN;
				info.remainingInvites = 5; //TODO: since we don't get this from API yet!
				if (DEBUG || info.shown < TASTEMAKER_WIN) {
					resetTimesShown(c);
					info.shown = TASTEMAKER_WIN;
				}
			}
			else 
				if (ui.hasNotification(User.NOTIFICATION_TASTEMAKER)) {
					info.notification = User.NOTIFICATION_TASTEMAKER;
					if (DEBUG || info.shown < TASTEMAKER_BETA) {
						resetTimesShown(c);
						info.shown = TASTEMAKER_BETA;
					}
				}

		info.timesShown = c.getSharedPrefs().getInt(PREF_NUM_TIMES_SHOWN, 0);

		return info;
	}
	
	static class TasteMakerInfo {
		public String notification;
		int shown = 0;
		int timesShown = 0;
		int remainingInvites = 0;
		protected boolean active;
	}

	public static void show(final CobrainController controller) {
		Resources res = controller.getCobrain().getContext().getResources();
		rulesUrl = res.getString(R.string.url_tastemaker_beta_program_rules, res.getString(R.string.url_cobrain_app));
		 
		new AsyncTask<Void, Void, TasteMakerInfo>() {
			Calendar showDate = null;

			@Override
			protected TasteMakerInfo doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo ui = c.getUserInfo();
				Calendar today = Calendar.getInstance();
				TasteMakerInfo info = checkNotificationType(controller);

				if (info.notification != null) {
					
					int maxTimesToShow = getMaxTimesToShow(c, info.shown);
					
					if (info.timesShown < maxTimesToShow) {
						long lastTimeShown = getLastTimeShown(c);
						Calendar lastRun = Calendar.getInstance();
						lastRun.setTimeInMillis(lastTimeShown);
						
						//if today is more than 24 hours than last run time then we try to show it
						if (today.getTimeInMillis() - lastRun.getTimeInMillis() > (24 * 60 * 60 * 1000)) {
							if ((info.timesShown+1) == maxTimesToShow) {
								ui.removeNotification(info.notification);
							}
							info.active = true;
						}
						else info.active = false;
					}
					else {
						info.active = false;
					}
				}
				
				if (info.active) {
					showDate = today;
				}

				return info;
			}

			@Override
			protected void onPostExecute(TasteMakerInfo result) {
				if (!result.active) return;
				
				setLastTimeShown(controller.getCobrain(), result.shown, showDate.getTimeInMillis(), result.timesShown);

				switch(result.shown) {
				case TASTEMAKER_CLOSE:
					showTasteMakerClose(controller, result);
					break;
				case TRENDSETTER_CLOSE:
					showTrendsetterClose(controller, result);
					break;
				case TASTEMAKER_WIN:
					showTastemakerWin(controller, result);
					break;
				case TRENDSETTER_WIN:
					showTrendsetterWin(controller, result);
					break;
				default:
					showTasteMaker(controller);
				}
			}
		}.execute();
	}
	
	
	static View inflateView(Context c, int id) {
		LayoutInflater inflater = (LayoutInflater)
		        c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(id, null);
	}
	
	static AlertDialog createDialog(Context c, View v) {
		AlertDialog.Builder builder;

		builder = new AlertDialog.Builder(c);
		builder.setView(v).setCancelable(false);
		return builder.create();
	}
	
	static void setupClickToContactList(final CobrainController controller, final AlertDialog alertDialog, View v, int id) {
		View button = v.findViewById(id);
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
	}

	static void setupClickToDismiss(final AlertDialog alertDialog, View v, int id) {
		View button = v.findViewById(id);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.setOnClickListener(null);
				alertDialog.dismiss();
			}

		});
	}

	static void setupClickToDontShowAgain(final CobrainController c, final AlertDialog alertDialog, View v, int id) {
		TextView button = (TextView) v.findViewById(id);
		button.setText(
				Html.fromHtml( v.getContext().getString(R.string.tastemaker_dont_show_again_link) )
			);

		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.setOnClickListener(null);
				setLastTimeShown(c.getCobrain(), -1, 0L, MAX_TIMES_TO_SHOW);
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						TasteMakerInfo info = checkNotificationType(c);
						c.getCobrain().getUserInfo().removeNotification(info.notification);
						return null;
					}
					
				}.execute();
				alertDialog.dismiss();
			}

		});
	}

	static void showTasteMaker(CobrainController controller) {
		Context c = (Activity)controller;
		View v = inflateView(c, R.layout.dlg_tastemaker);
		final AlertDialog alertDialog = createDialog(c, v);
		
		TextView tv = (TextView) v.findViewById(R.id.tastemaker_rules_link);
		tv.setMovementMethod(LinkMovementMethod.getInstance());

		//String link = c.getResources().getString(R.string.tastemaker_beta_program_rules_link, rulesUrl);
		String link = c.getResources().getString(R.string.tastemaker_beta_link, rulesUrl);
		tv.setText(Html.fromHtml(link));

		setupClickToContactList(controller, alertDialog, v, R.id.tastemaker_next);
		setupClickToDontShowAgain(controller, alertDialog, v, R.id.tastemaker_dont_show_again);
		
		alertDialog.show();
	}

	static void showTasteMakerClose(CobrainController controller, TasteMakerInfo info) {
		Context c = (Activity)controller;
		View v = inflateView(c, R.layout.dlg_tastemaker_close);
		final AlertDialog alertDialog = createDialog(c, v);

		TextView tv = (TextView) v.findViewById(R.id.badge_count_message);
		tv.setText("Only " + info.remainingInvites + " more " + HelperUtils.Strings.plural(info.remainingInvites, "friend") + " to become a");
		
		tv = (TextView) v.findViewById(R.id.badge_name);
		tv.setText("Tastemaker");

		tv = (TextView) v.findViewById(R.id.badge_earnings);
		tv.setText("$5");

		String link = c.getResources().getString(R.string.tastemaker_beta_program_rules_link, rulesUrl);
		tv.setText(Html.fromHtml(link));

		setupClickToContactList(controller, alertDialog, v, R.id.invite_button);
		setupClickToDismiss(alertDialog, v, R.id.tastemaker_close);
		setupClickToDontShowAgain(controller, alertDialog, v, R.id.tastemaker_dont_show_again);
		
		alertDialog.show();
	}

	static void showTrendsetterClose(CobrainController controller, TasteMakerInfo info) {
		Context c = (Activity)controller;
		View v = inflateView(c, R.layout.dlg_tastemaker_close);
		final AlertDialog alertDialog = createDialog(c, v);

		TextView tv = (TextView) v.findViewById(R.id.badge_count_message);
		tv.setText("Only " + info.remainingInvites + " more " + HelperUtils.Strings.plural(info.remainingInvites, "friend") + " to become a");
		
		tv = (TextView) v.findViewById(R.id.badge_name);
		tv.setText("Trendsetter\nTastemaker");

		tv = (TextView) v.findViewById(R.id.badge_earnings);
		tv.setText("$12");

		String link = c.getResources().getString(R.string.tastemaker_beta_program_rules_link, rulesUrl);
		tv.setText(Html.fromHtml(link));

		setupClickToContactList(controller, alertDialog, v, R.id.invite_button);
		setupClickToDismiss(alertDialog, v, R.id.tastemaker_close);
		setupClickToDontShowAgain(controller, alertDialog, v, R.id.tastemaker_dont_show_again);
		
		alertDialog.show();
	}
	
	static void showTastemakerWin(CobrainController controller, TasteMakerInfo info) {
		Context c = (Activity)controller;
		View v = inflateView(c, R.layout.dlg_tastemaker_win);
		final AlertDialog alertDialog = createDialog(c, v);
		
		TextView tv = (TextView) v.findViewById(R.id.badge_count_message);
		tv.setText(
				Html.fromHtml(
					c.getResources().getString(R.string.tastemaker_status, info.remainingInvites)
					)
				);

		setupClickToContactList(controller, alertDialog, v, R.id.invite_button);
		setupClickToDismiss(alertDialog, v, R.id.tastemaker_close);
		alertDialog.show();
	}

	static void showTrendsetterWin(CobrainController controller, TasteMakerInfo info) {
		Context c = (Activity)controller;
		View v = inflateView(c, R.layout.dlg_trendsetter_win);
		final AlertDialog alertDialog = createDialog(c, v);
		
		setupClickToContactList(controller, alertDialog, v, R.id.invite_button);
		setupClickToDismiss(alertDialog, v, R.id.tastemaker_close);
		alertDialog.show();
	}

}
