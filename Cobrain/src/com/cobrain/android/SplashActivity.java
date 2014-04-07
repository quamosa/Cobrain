package com.cobrain.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.fragments.LandingFragment;

public class SplashActivity extends FragmentActivity implements OnClickListener {
	TextView caption;
	TextView login;
    private static final boolean debug = true;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(android.R.id.content, new LandingFragment()).commitAllowingStateLoss();

		checkStartCobrain();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_button:
			//startCobrainActivity(MainActivity.ACTION_SIGNUP);
            startLoginActivity(LoginActivity.ACTION_SIGNUP);
			break;
		case R.id.splash_caption:
			//startCobrainActivity(null);
            startLoginActivity(null);
			break;
		}
	}
	
	void startLoginActivity(String action) {
		Intent intent = new Intent(this, LoginActivity.class);
		intent.setAction(action);
		startActivity(intent);
		finish();
	}
    void startCobrainActivity(String action) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(action);
        startActivity(intent);
        finish();
    }

	boolean checkStartCobrain() {
        Cobrain cobrain = new Cobrain(getApplicationContext());
		int firstRun = cobrain.getGlobalSharedPrefs().getInt("FirstRun", 0);
		if (firstRun == 0 || debug) {
			cobrain.getGlobalSharedPrefs().putInt("FirstRun", 1).commit();
			return false;
		}
		else {
			//startCobrainActivity(null);
            startLoginActivity(null);
			return true;
		}
	}

}
