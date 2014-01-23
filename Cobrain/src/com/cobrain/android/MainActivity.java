package com.cobrain.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cobrain.android.fragments.AccountSaveFragment;
import com.cobrain.android.fragments.BrowserFragment;
import com.cobrain.android.fragments.ContactListFragment.ContactSelectedListener;
import com.cobrain.android.fragments.ContactListFragment;
import com.cobrain.android.fragments.CravesFragment;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.fragments.MainFragment;
import com.cobrain.android.fragments.LoginFragment;
import com.cobrain.android.fragments.NerveCenterFragment;
import com.cobrain.android.fragments.RaveUserListFragment;
import com.cobrain.android.fragments.ResetPasswordFragment;
import com.cobrain.android.fragments.SavedAndShareFragment;
import com.cobrain.android.fragments.SignupFragment;
import com.cobrain.android.fragments.TrainingFragment;
import com.cobrain.android.fragments.WishListFragment;
import com.cobrain.android.loaders.IntentLoader;
import com.cobrain.android.loaders.TasteMakerLoader;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.WishList;
import com.cobrain.android.service.Cobrain;
import com.cobrain.android.service.Cobrain.CobrainController;
import com.cobrain.android.service.Cobrain.CobrainView;
import com.cobrain.android.service.Cobrain.OnLoggedInListener;
import com.cobrain.android.utils.HelperUtils;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends SlidingSherlockFragmentActivity implements OnLoggedInListener, CobrainController {

    static final String TAG = MainActivity.class.toString();
	Cobrain cobrain;
	CobrainView cobrainView;
	ProgressDialog progressDialog;
	MainFragment homeFragment;
	boolean showOptionsMenu = true;
    IntentLoader intentLoader = new IntentLoader();
	View menuSelected;
	int menuItemSelected = -1;
	
	@Override
	public void showLogin() {
		getSupportActionBar().hide();
		
        LoginFragment login = new LoginFragment();
        //int id = (homeFragment == null) ? android.R.id.content : R.id.content_frame;
        int id = android.R.id.content;
        cobrainView = login;

        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(id, login, LoginFragment.TAG)
        	.commitAllowingStateLoss();
	}

	boolean letMeLeave;
	private boolean isDestroying;
	
	@Override
	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
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
	public void showMain() {
        MainFragment main = new MainFragment();
        cobrainView = main;
        homeFragment = main;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, main, MainFragment.TAG)
        	.commitAllowingStateLoss();
	}
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        cobrain = new Cobrain(getApplicationContext());
		cobrain.setOnLoggedInListener(this);
		
		intentLoader.initialize(this);
		
		ActionBar ab = (ActionBar) getSupportActionBar();
		ab.setIcon(getResources().getDrawable(R.drawable.ic_ab_cobrain_logo));
		//ab.setLogo(getResources().getDrawable(R.drawable.ic_ab_cobrain_logo));
		try {
		ab.setHomeAsUpIndicator(R.drawable.ic_slideout_menu_up_indicator);
		}
		catch (NoSuchMethodError e) {
			//FIXME: for andy's phone Ice cream sandwich? 
		}
		ab.setDisplayShowTitleEnabled(true);
		ab.setDisplayHomeAsUpEnabled(true);

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
		sm.setBehindOffsetRes(R.dimen.button_size);
		sm.setAboveOffsetRes(R.dimen.button_size);
		sm.setFadeEnabled(true);
		sm.setFadeDegree(.75f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

		//TODO: we need to do this because status bar height is not being accounted for
		//by slidingmenu .. shame on you..
		int pad = HelperUtils.getStatusBarHeight(this);
		getSlidingMenu().getMenu().setPadding(0, pad, 0, 0);
		getSlidingMenu().getSecondaryMenu().setPadding(0, pad, 0, 0);
		// ***************
	    
		cobrain.restoreLogin();
		
		if (!cobrain.isLoggedIn())
        	showLogin();

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.base, menu);
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
		case android.R.id.home:
			showNavigationMenu();
			break;
		case R.id.menu_friends:
			TasteMakerLoader.show(this);
			showFriendsMenu();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLoggedIn(UserInfo ui) {
		dismissDialog(); //dismiss an indeterminate progress dialog if we have one open
		showMain();
	}

	@Override
	public void onLoggedOut(UserInfo ui) {
		if (!checkForDestroy()) {
			dismissDialog(); //dismiss an indeterminate progress dialog if we have one open
			showLogin();
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
		intentLoader.dispose();
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
	    EasyTracker.getInstance(this).activityStart(this);
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
		if (homeFragment != null)
			homeFragment.showNavigationMenu();
	}
	@Override
	public void showFriendsMenu() {
		if (homeFragment != null) {
			FriendsListFragment f = homeFragment.showFriendsMenu();
			cobrainView = f;
		}
	}
	
	@Override
	public void closeMenu(boolean animate) {
		getSlidingMenu().showContent(animate);
	}
	
	@Override
	public void showHome() {
        CravesFragment craves = new CravesFragment();
        cobrainView = craves;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, craves, CravesFragment.TAG)
        	.commitAllowingStateLoss();
        
        //getSupportFragmentManager().executePendingTransactions();
		closeMenu(true);
	}

	@Override
	public void showSavedAndShare() {
		SavedAndShareFragment f = new SavedAndShareFragment();
        cobrainView = f;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, f, SavedAndShareFragment.TAG)
        	.commitAllowingStateLoss();

		closeMenu(true);
	}

	@Override
	public void showTeachMyCobrain() {
		//this displays within MainFragment in the main content area
		
        TrainingFragment training = new TrainingFragment();
        cobrainView = training;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, training, TrainingFragment.TAG)
        	.commitAllowingStateLoss();
        
        //getSupportFragmentManager().executePendingTransactions();
		closeMenu(true);
	}

	@Override
	public void showNerveCenter() {

		NerveCenterFragment training = new NerveCenterFragment();
        cobrainView = training;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(R.id.content_frame, training, NerveCenterFragment.TAG)
        	.commitAllowingStateLoss();

        closeMenu(true);
	}

	@Override
	public void showSignup() {
		SignupFragment training = new SignupFragment();
        cobrainView = training;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, training, SignupFragment.TAG)
        	.commitAllowingStateLoss();
	}
	
	public void showAccountSave() {
		AccountSaveFragment training = new AccountSaveFragment();
        cobrainView = training;
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
        cobrainView = training;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(android.R.id.content, training, ResetPasswordFragment.TAG)
        	.commitAllowingStateLoss();
	}

	@Override
	public void showWishList(WishList list, boolean showMyPrivateItems, boolean addToStack) {
		WishListFragment training = new WishListFragment();
		training.initialize(list, showMyPrivateItems);
        cobrainView = training;

        FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        if (addToStack) tr.addToBackStack(WishListFragment.TAG);
        tr.replace(R.id.content_frame, training, WishListFragment.TAG)
        	.commitAllowingStateLoss();
	}

	@Override
	public void showBrowser(String url, int containerId, String merchant) {
		BrowserFragment browser = new BrowserFragment();
		Bundle args = new Bundle();
		args.putString("merchant", merchant);
		args.putString("url", url);
		browser.setArguments(args);
        cobrainView = browser;
        getSupportFragmentManager()
        	.beginTransaction()
        	.replace(containerId, browser, BrowserFragment.TAG)
        	.addToBackStack(null)
        	.commitAllowingStateLoss();
	}

	@Override
	public void showRavesUserList(String itemId) {
		RaveUserListFragment raves = new RaveUserListFragment();
		Bundle args = new Bundle();
		args.putString("itemId", itemId);
		raves.setArguments(args);
        cobrainView = raves;
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
        cobrainView = f;
        getSupportFragmentManager()
        	.beginTransaction()
        	.addToBackStack(null)
        	.add(f, ContactListFragment.TAG)
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
	public void processIntents() {
		intentLoader.processAnyIntents(this);
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

}