package com.cobrain.android;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.controllers.Cobrain.CobrainView;
import com.cobrain.android.controllers.Cobrain.OnLoggedInListener;
import com.cobrain.android.fragments.AccountSaveFragment;
import com.cobrain.android.fragments.BaseCobrainFragment;
import com.cobrain.android.fragments.BrowserFragment;
import com.cobrain.android.fragments.ContactListFragment.ContactSelectedListener;
import com.cobrain.android.fragments.ContactListFragment;
import com.cobrain.android.fragments.CravesFragment;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.fragments.HomeFragment;
import com.cobrain.android.fragments.MainFragment;
import com.cobrain.android.fragments.LoginFragment;
import com.cobrain.android.fragments.NavigationMenuFragment;
import com.cobrain.android.fragments.RaveUserListFragment;
import com.cobrain.android.fragments.ResetPasswordFragment;
import com.cobrain.android.fragments.SavedAndShareFragment;
import com.cobrain.android.fragments.SettingsFragment;
import com.cobrain.android.fragments.SignupFragment;
import com.cobrain.android.fragments.TrainingFragment;
import com.cobrain.android.fragments.WishListFragment;
import com.cobrain.android.loaders.IntentLoader;
import com.cobrain.android.loaders.TasteMakerLoader;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.HelperUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionParser;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;

public class MainActivity extends SlidingSherlockFragmentActivity implements OnLoggedInListener, CobrainController, OnOpenedListener, OnClosedListener, View.OnClickListener {

    static final String TAG = MainActivity.class.toString();
	Cobrain cobrain;
	CobrainView cobrainView, cobrainMainView, cobrainMenuView;
	Runnable showView;
	ProgressDialog progressDialog;
	MainFragment homeFragment;
	boolean showOptionsMenu = true;
    IntentLoader intentLoader = new IntentLoader();
	View menuSelected;
	int menuItemSelected = -1;
	boolean letMeLeave;
	private boolean isDestroying;
	Handler handler = new Handler();
	private TextView actionBarTitle;
	private TextView actionBarSubTitle;
	private View actionBarView;
	private View actionBarMenuOpenerView;

	@Override
	public void showLogin(String loginUrl) {
		//getSupportActionBar().hide();

		
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		/*
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		if (fragments != null)
			for (Fragment f : fragments)
				if (!(f instanceof MainFragment)) 
					t.remove(f);
		*/
		
		if (cobrainMainView != null) {
			t.remove((Fragment) cobrainMainView);
		}
		
		
		Bundle args = new Bundle();
		args.putString("loginUrl", loginUrl);
		
        LoginFragment login = new LoginFragment();
        login.setArguments(args);
        
        //int id = (homeFragment == null) ? android.R.id.content : R.id.content_frame;
        int id = android.R.id.content;
        setCurrentCobrainView(login);

        t.replace(id, login, LoginFragment.TAG)
        	.commitAllowingStateLoss();
	}

	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			if (!isMenuOpen()) {
				showNavigationMenu();
				return;
			}
			if (!letMeLeave) {
				AlertDialog.Builder b = new AlertDialog.Builder(this);
				b.setMessage("Are you sure you want to leave Cobrain?");
				b.setNegativeButton("Cancel", null);
				b.setPositiveButton("Yes", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						letMeLeave = true;
						dialog.dismiss();
					}
				});
				AlertDialog alert = b.create();
				alert.show();
				return;
			}
		}
		
		super.onBackPressed();
	}

	@Override
	public void hideSoftKeyBoard() {
	    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	    if(imm.isAcceptingText() && getCurrentFocus() != null)// verify if the soft keyboard is open                         
	    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
	
	@Override
	public void showMain(int defaultView) {
        
        //we only ever show main on log in and sign up
        if (defaultView != VIEW_FRIENDS_MENU && cobrain.getUserInfo().getSignInCount() <= 1)
        	defaultView = VIEW_TEACH;

        homeFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
        if (homeFragment != null) {
        	switch(defaultView) {
        	case VIEW_FRIENDS_MENU:
        		showFriendsMenu();
        		break;
        	case VIEW_TEACH:
        		showTeachMyCobrain(false);
        		break;
        	case VIEW_HOME:
        		showHome();
        		break;
        	}
        	return;
        }
        
        MainFragment main = new MainFragment();
        Bundle args = new Bundle();
        args.putInt("defaultView", defaultView);
        main.setArguments(args);
		setCurrentCobrainView(main);
        homeFragment = main;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, main, MainFragment.TAG)
        	.commitAllowingStateLoss();
	}
	
	@Override
	public void showDefaultActionBar() {
		ActionBar ab = (ActionBar) getSupportActionBar();
		
		if (ab.getCustomView() == actionBarView && actionBarView != null) return;

		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowHomeEnabled(true);
		ab.setDisplayHomeAsUpEnabled(false);

		View homeIcon = findViewById(
		        Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? 
		        android.R.id.home : R.id.abs__home);

		//set up default custom view
		ab.setDisplayShowCustomEnabled(true);
		
		if (homeIcon != null) {
			View homeParent = ((View) homeIcon.getParent());
			ViewGroup vg = (ViewGroup) homeParent;
			if (vg != null) {
				vg.setVisibility(View.GONE);
			}
		}
		
		if (actionBarView == null) {
			actionBarView = View.inflate(getApplicationContext(), R.layout.ab_main_frame, null);
			actionBarTitle = (TextView) actionBarView.findViewById(R.id.title);
			actionBarSubTitle = (TextView) actionBarView.findViewById(R.id.subtitle);
			ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
			actionBarView.setLayoutParams(params);
			actionBarMenuOpenerView = actionBarView.findViewById(R.id.app_icon_layout);
			actionBarMenuOpenerView.setOnClickListener(this);
		}
		
		ab.setCustomView(actionBarView);
		
	}
	
	@Override
	public void setTitle(CharSequence title) {
		actionBarTitle.setText(title);
	}
	@Override
	public void setSubTitle(CharSequence title) {
		actionBarSubTitle.setVisibility((title == null) ? View.GONE : View.VISIBLE);
		actionBarSubTitle.setText(title);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        cobrain = new Cobrain(getApplicationContext());
		cobrain.setOnLoggedInListener(this);
		
		intentLoader.initialize(this);
		
		showDefaultActionBar();
		
		/*ActionBar ab = controller.getSupportActionBar();
		ab.setCustomView(R.layout.actionbar_training_frame);
		ImageButton ib = (ImageButton) controller.getSupportActionBar().getCustomView().findViewById(R.id.navigation_button);
		ib.setOnClickListener(this);
		skip = (Button) controller.getSupportActionBar().getCustomView().findViewById(R.id.training_skip_button);
		skip.setOnClickListener(this);

		ActionBar ab = controller.getSupportActionBar();
		ab.setCustomView(R.layout.actionbar_crave_frame);
		ImageButton ib = (ImageButton) controller.getSupportActionBar().getCustomView().findViewById(R.id.navigation_button);
		ib.setOnClickListener(this);
		ib = (ImageButton) ab.getCustomView().findViewById(R.id.filter_button);
		ib.setOnClickListener(this);
		*/
		
		getSlidingMenu().setMode(SlidingMenu.LEFT_RIGHT);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

		// set the Behind View
		setContentView(R.layout.content_frame);
		setBehindContentView(R.layout.menu_frame);
		getSlidingMenu().setSecondaryMenu(R.layout.menu_frame_two);
		getSlidingMenu().setSecondaryShadowDrawable(R.drawable.shadowright);

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.menu_shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.navigation_menu_hide_width);
		sm.setAboveOffsetRes(R.dimen.friends_menu_hide_width);
		sm.setFadeEnabled(true);
		sm.setFadeDegree(.75f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

		//TODO: we need to do this because status bar height is not being accounted for
		//by slidingmenu .. shame on you..
		int pad = HelperUtils.getStatusBarHeight(this);
		getSlidingMenu().getMenu().setPadding(0, pad, 0, 0);
		getSlidingMenu().getSecondaryMenu().setPadding(0, pad, 0, 0);
		// ***************

        getSlidingMenu().setOnOpenedListener(this);
        getSlidingMenu().setOnClosedListener(this);

		if (!processIntents()) {
			if (!cobrain.restoreLogin(new Runnable() {
				public void run() {
					if (!cobrain.isLoggedIn())
			        	showLogin(null);
					else showMain(CobrainController.VIEW_HOME);
				}
			}))
				showLogin(null);
		}

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (showOptionsMenu) {
			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.base, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (showOptionsMenu) {
		   //MenuInflater inflater = getSupportMenuInflater();
		   //inflater.inflate(R.menu.base, menu);
		   return super.onPrepareOptionsMenu(menu);
		}
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		//case android.R.id.home:
		//	showNavigationMenu();
		//	break;
		case R.id.menu_friends:
			TasteMakerLoader.show(MainActivity.this);
			showFriendsMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLoggedIn(UserInfo ui) {
		dismissDialog(); //dismiss an indeterminate progress dialog if we have one open
		showMain(VIEW_HOME);
	}

	@Override
	public void onLoggedOut(UserInfo ui) {
		if (!checkForDestroy()) {
			dismissDialog(); //dismiss an indeterminate progress dialog if we have one open
			showLogin(null);
			closeMenu(true);
		}
	}

	@Override
	public void onAccountCreated(UserInfo userInfo) {
		dismissDialog();
		showAccountSave();
	}


	@Override
	public void onFailure(String message) {
		if (cobrainView != null) cobrainView.onError(message);
		checkForDestroy();
		//show a message asking user to login or request new password
	}

	private boolean checkForDestroy() {
		if (isDestroying) {
			dispose();
			isDestroying = false;
			return true;
		}
		return false;
	}
	private void dispose() {
		menuSelected = null;
		cobrain.dispose();
		cobrain = null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getSupportActionBar().setCustomView(null);
		actionBarView = null;
		actionBarTitle = null;
		actionBarSubTitle = null;
		getSlidingMenu().setOnClosedListener(null);
        getSlidingMenu().setOnOpenedListener(null);
		intentLoader.dispose();
		cobrainMenuView = null;
		cobrainMainView = null;
		cobrainView = null;
		homeFragment = null;
		isDestroying = true;
		cobrain.logout(); //TODO: do we wait for this to finish or block and remove strict rules for network on ui thread
	}

	@Override
	public Cobrain getCobrain() {
		return cobrain;
	}

	@Override
	public void showDialog(final String message) {
		runOnUiThread(new Runnable () {
			public void run() {
				//show dialog
				
				dismissDialog();
				
				AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
		        builder1.setMessage(message);
		        builder1.setCancelable(true);
		        builder1.setPositiveButton("OK",
		                new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		            }
		        });
		        /*builder1.setNegativeButton("No",
		                new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		            }
		        });*/
		
		        AlertDialog alert11 = builder1.create();
		        alert11.show();		
			}
		});
	}


	@Override
	public void showErrorDialog(final String message) {
		
		runOnUiThread(new Runnable () {
			public void run() {
				//show dialog
				
				dismissDialog();
				
				AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
				View v = View.inflate(getApplicationContext(), R.layout.dlg_error, null);
				TextView tv = (TextView) v.findViewById(R.id.error_message);
				String mymessage = "Your Cobrain synapses aren't firing properly. " + message;
				tv.setText(mymessage);
				//builder1.setIcon(R.drawable.ic_error);
				builder1.setView(v);
				//builder1.setTitle(Html.fromHtml("<b><font color='#000'>Uh oh!</font></b>"));
		        //builder1.setMessage(message);
		        builder1.setCancelable(true);
		        builder1.setPositiveButton("OK",
		                new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		            }
		        });
		        /*builder1.setNegativeButton("No",
		                new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		            }
		        });*/
		
		        AlertDialog alert11 = builder1.create();
		        alert11.show();		
			}
		});
	}

	@Override
	protected void onStart() {
	    super.onStart();
	    startGoogleAnalytics();
	}

	void startGoogleAnalytics() {
	    EasyTracker.getInstance(this).activityStart(this);

	    // Change uncaught exception parser...
	    // Note: Checking uncaughtExceptionHandler type can be useful if clearing ga_trackingId during development to disable analytics - avoid NullPointerException.
	    Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
	    if (uncaughtExceptionHandler instanceof ExceptionReporter) {
	      ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
	      exceptionReporter.setExceptionParser(new ExceptionParser() {
			
			@Override
			public String getDescription(String arg0, Throwable arg1) {
				return "Thread: " + arg0 + ", Exception: " + Log.getStackTraceString(arg1);
			}
	      });
	    }
	}
	
	@Override
	protected void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		cobrain.onStop();
		super.onPause();
	}

	@Override
	public void showProgressDialog(String title, String message) {
		dismissDialog();
		progressDialog = ProgressDialog.show(this, title, message, true);
	}

	@Override
	public void dismissDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	@Override
	public void showNavigationMenu() {
		if (homeFragment != null) {
			NavigationMenuFragment f = homeFragment.showNavigationMenu();
			setCurrentCobrainView(f);
		}
	}
	@Override
	public void showFriendsMenu() {
		if (homeFragment != null) {
			FriendsListFragment f = homeFragment.showFriendsMenu();
			setCurrentCobrainView(f);
		}
	}
	
	@Override
	public void closeMenu(boolean animate) {
		getSlidingMenu().showContent(animate);
	}
	
	@Override
	public void showHome() {
		if (getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG) == null) {
			HomeFragment home = new HomeFragment();
			setCurrentCobrainView(home);
	        getSupportFragmentManager()
	        	.beginTransaction()
	        	.replace(R.id.content_frame, home, HomeFragment.TAG)
	        	.commitAllowingStateLoss();
		}
		closeMenu(true);
	}

	@Override
	public void showSavedAndShare() {
		SavedAndShareFragment f = new SavedAndShareFragment();
        setCurrentCobrainView(f);
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, f, SavedAndShareFragment.TAG)
        	.commitAllowingStateLoss();

		closeMenu(true);
	}

	@Override
	public void showTeachMyCobrain(boolean addToBackStack) {
		//this displays within MainFragment in the main content area
		//showView = new Runnable() {
		//	public void run() {
				if (getSupportFragmentManager().findFragmentByTag(TrainingFragment.TAG) == null) {
					
			        TrainingFragment training = new TrainingFragment();
					setCurrentCobrainView(training);
			        FragmentTransaction t = getSupportFragmentManager()
			        	.beginTransaction();

			        if (addToBackStack) t.addToBackStack(null);
			        
			        t.replace(R.id.content_frame, training, TrainingFragment.TAG)
			        	.commitAllowingStateLoss();
			        
			        //getSupportFragmentManager().executePendingTransactions();
		        
				}
		//	}
		//};
		
		//if (isMenuOpen()) {
			closeMenu(true);
		//}
		//else {
		//	showView.run();
		//	showView = null;
		//}
	}
	
	public boolean isMenuOpen() {
        return (getSlidingMenu().isMenuShowing() ||
               getSlidingMenu().isSecondaryMenuShowing());
	}
	
	@Override
	public void showNerveCenter() {

		SettingsFragment training = new SettingsFragment();
		setCurrentCobrainView(training);
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, training, SettingsFragment.TAG)
        	.commitAllowingStateLoss();

        closeMenu(true);
	}

	@Override
	public void showSignup(String signupUrl) {
		Bundle args = new Bundle();
		args.putString("signupUrl", signupUrl);
		
		SignupFragment training = new SignupFragment();
		training.setArguments(args);
		
		setCurrentCobrainView(training);
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, training, SignupFragment.TAG)
        	.commitAllowingStateLoss();
	}
	
	public void showAccountSave() {
		AccountSaveFragment training = new AccountSaveFragment();
		setCurrentCobrainView(training);
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, training, AccountSaveFragment.TAG)
        	.commitAllowingStateLoss();
	}

	@Override
	public void showForgotPassword(String email) {
		ResetPasswordFragment training = new ResetPasswordFragment();
		Bundle b = new Bundle();
		b.putString("email", email);
		training.setArguments(b);
		setCurrentCobrainView(training);
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, training, ResetPasswordFragment.TAG)
        	.commitAllowingStateLoss();
	}

	@Override
	public void showWishList(Skus list, boolean showMyPrivateItems, boolean addToStack) {
		WishListFragment training = new WishListFragment();
		training.initialize(list, showMyPrivateItems);
		setCurrentCobrainView(training);

        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        if (addToStack) tr.addToBackStack(WishListFragment.TAG);
        tr.replace(R.id.content_frame, training, WishListFragment.TAG)
        	.commitAllowingStateLoss();
	}

	@Override
	public void showWishList(User owner, boolean showMyPrivateItems, boolean addToStack) {
		WishListFragment training = new WishListFragment();
		training.initialize(owner, showMyPrivateItems);
		setCurrentCobrainView(training);

        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        if (addToStack) tr.addToBackStack(WishListFragment.TAG);
        tr.replace(R.id.content_frame, training, WishListFragment.TAG)
        	.commitAllowingStateLoss();
	}
	
	@Override
	public void showBrowser(String url, int containerId, String merchant, boolean addToBackStack) {
		BrowserFragment browser = new BrowserFragment();
		Bundle args = new Bundle();
		
		args.putString("merchant", merchant);
		args.putString("url", url);
		browser.setArguments(args);
		setCurrentCobrainView(browser);

        FragmentTransaction t = getSupportFragmentManager()
        	.beginTransaction();
        
        t.replace(containerId, browser, BrowserFragment.TAG);
        if (addToBackStack) t.addToBackStack(null);
        t.commitAllowingStateLoss();
	}

	@Override
	public void showRavesUserList(String itemId) {
		RaveUserListFragment raves = new RaveUserListFragment();
		Bundle args = new Bundle();
		args.putString("itemId", itemId);
		raves.setArguments(args);
		setCurrentCobrainView(raves);
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, raves, RaveUserListFragment.TAG)
        	.addToBackStack(null)
        	.commitAllowingStateLoss();
	}

	@Override
	public void showContactList(ContactSelectedListener listener) {
		ContactListFragment f = new ContactListFragment();
		f.setContactSelectedListener(listener);
		setCurrentCobrainView(f);
        getSupportFragmentManager()
        	.beginTransaction()
        	.addToBackStack(null)
        	.add(f, ContactListFragment.TAG)
        	.commitAllowingStateLoss();
	}

	@Override
	public void showCraves(CraveStrip strip, Sku sku, int containerId, boolean addToBackStack) {
		CravesFragment f = CravesFragment.newInstance(strip, sku);
		setCurrentCobrainView(f);
        FragmentTransaction t = getSupportFragmentManager()
        	.beginTransaction();
        if (addToBackStack) t.addToBackStack(null);
        t.replace(containerId, f, CravesFragment.TAG)
        	.commitAllowingStateLoss();
	}

	@Override
	public void disableSlidingMenu() {
		// add a dummy view
		View v = new View(getApplicationContext());
		setBehindContentView(v);
		getSlidingMenu().setSlidingEnabled(false);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);		
		getSlidingMenu().showContent();
	}

	@Override
	public void showOptionsMenu(boolean show) {
		if (showOptionsMenu != show) {
			showOptionsMenu = show;
			invalidateOptionsMenu();
		}
	}

	@Override
	public boolean processIntents() {
		return intentLoader.processAnyIntents(this);
	}

	@Override
	public CobrainView getShown() {
		return cobrainView;
	}

	@Override
	public void setMenuItemSelected(View menu, int position,
			boolean selected) {
		
		if (menu instanceof ListView)	
			((ListView)menu).setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		if (selected) {
			if (menuSelected != null && menuSelected != menu) {
				if (menuSelected instanceof ListView) {
					((ListView)menuSelected).setItemChecked(menuItemSelected, false);
				}
				else if (menuSelected instanceof Checkable) {
					((Checkable)menuSelected).setChecked(false);
				}
			}
			menuItemSelected = position;
		}
		else menuItemSelected = -1;
		
		if (menu instanceof ListView)
			((ListView)menu).setItemChecked(position, selected);
		else if (menu instanceof Checkable) {
			((Checkable) menu).setChecked(selected);
		}
		
		menuSelected = menu;
	}

	@Override
	public void onOpened() {
		if (cobrainView != null) cobrainView.onSlidingMenuOpened();
	}

	@Override
	public void onClosed() {
		if (showView != null) {
			showView.run();
			showView = null;
		}
		if (cobrainMenuView != null) cobrainMenuView.onSlidingMenuClosed();
	}

	@Override
	public void setCurrentCobrainView(CobrainView cv) {
		if (!isCobrainViewMenu(cv)) cobrainMainView = cv;
		else cobrainMenuView = cv;
		cobrainView = cv;
	}
	
	boolean isCobrainViewMenu(CobrainView cv) {
		return (cv instanceof FriendsListFragment ||
				cv instanceof NavigationMenuFragment);
	}

	@Override
	public TextView getSubTitleView() {
		return actionBarSubTitle;
	}

	@Override
	public TextView getTitleView() {
		return actionBarTitle;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.app_icon_layout:
			showNavigationMenu();
		}
	}

	@Override
	public void dispatchOnFragmentAttached(BaseCobrainFragment f) {
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				if (f != fragment && fragment instanceof BaseCobrainFragment) {
					BaseCobrainFragment bf = (BaseCobrainFragment)fragment;
					bf.onFragmentAttached(f);
				}
			}
		}
	}

	@Override
	public void dispatchOnFragmentDetached(BaseCobrainFragment f) {
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		if (fragments != null) {
			for (Fragment fragment : fragments) {
				if (f != fragment && fragment instanceof BaseCobrainFragment) {
					BaseCobrainFragment bf = (BaseCobrainFragment)fragment;
					bf.onFragmentDetached(f);
				}
			}
		}
	}
}