package com.cobrain.android;

import com.cobrain.android.controllers.Cobrain;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class SplashActivity extends Activity implements OnClickListener {
	TextView caption;
	TextView login;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.act_splash);

		login = (TextView) findViewById(R.id.login_button);
		login.setOnClickListener(this);
		
		CharSequence sc = Html.fromHtml( getString(R.string.splash_caption) );
		caption = (TextView) findViewById(R.id.splash_caption);
		caption.setText(sc);
		caption.setOnClickListener(this);

		checkStartCobrain();

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		caption.setOnClickListener(null);
		caption = null;
		login.setOnClickListener(null);
		login = null;
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_button:
			startCobrainActivity(MainActivity.ACTION_SIGNUP);
			break;
		case R.id.splash_caption:
			startCobrainActivity(null);
			break;
		}
	}
	
	void startCobrainActivity(String action) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(action);
		startActivity(intent);
		finish();
	}

	boolean checkStartCobrain() {
        Cobrain cobrain = new Cobrain(getApplicationContext());
		int firstRun = cobrain.getSharedPrefs().getInt("FirstRun", 0);
		if (firstRun == 0) {
			cobrain.getSharedPrefs().putInt("FirstRun", 1).commit();
			return false;
		}
		else {
			startCobrainActivity(null);
			return true;
		}
	}

}
