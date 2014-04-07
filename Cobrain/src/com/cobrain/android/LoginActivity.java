package com.cobrain.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.cobrain.android.model.UserInfo;

public class LoginActivity extends MainActivity {

    static final String TAG = LoginActivity.class.toString();
	public static final String ACTION_SIGNUP = "com.cobrain.android.signup";
    public static final String ACTION_LOGIN = "com.cobrain.android.login";

    public static void start(Context c, String action) {
        Intent intent = new Intent(c, LoginActivity.class);
        intent.setAction(action);
        c.startActivity(intent);
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        showOptionsMenu = false;
        logOutOfCobrainOnDestroy = false;
        super.onCreate(savedInstanceState);
    }

	@Override
	public void showLogin(String loginUrl) {
        //show login fragment
        showLoginOld(loginUrl);
 	}

/*	@Override
	public void onBackPressed() {
		if (cobrainView != null) {
			if (cobrainView.onBackPressed()) return;
		}

		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			if (!getSlidingMenu().isMenuShowing()) {
				showNavigationMenu();
				return;
			}
			if (getSlidingMenu().isSecondaryMenuShowing()) {
				showContent();
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
						finish();
					}
				});
				AlertDialog alert = b.create();
				alert.show();
				return;
			}
		}

		super.onBackPressed();
	}*/

	@Override
	public void showDefaultActionBar() {
        //show no actionbar
	}

	@Override
	public void setTitle(CharSequence title) {
	}
	@Override
	public void setSubTitle(CharSequence title) {
	}

	@Override
	public void onAccountCreated(UserInfo userInfo) {
		dismissDialog();
        showHome();
		//showAccountSave();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

    @Override
    public void showMain(int defaultView) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

	@Override
	public void showHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
	}
}
