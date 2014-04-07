package com.cobrain.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cobrain.android.R;
import com.cobrain.android.controllers.Cobrain.CobrainView;

public class LoginFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "LoginFragment";
	private Button loginButton;
	private TextView signupButton;
	private EditText email;
	private EditText password;
	private boolean loggingIn;
	private TextView forgotPassword;
	private String loginUrl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_login, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		View v = getView();
		
		loginUrl = getArguments().getString("loginUrl");
		
		loginButton = (Button) v.findViewById(R.id.login_button);
		signupButton = (TextView) v.findViewById(R.id.signup_link);
		email = (EditText) v.findViewById(R.id.email);
		password = (EditText) v.findViewById(R.id.password);
		forgotPassword = (TextView) v.findViewById(R.id.forgot_password);
		
		email.setText(this.getResources().getString(R.string.username));
		password.setText(this.getResources().getString(R.string.password));
		
        forgotPassword.setOnClickListener(this);
		loginButton.setOnClickListener(this);
		signupButton.setOnClickListener(this);

		if (loginUrl != null) {
			TextView message = (TextView) v.findViewById(R.id.message_label);
			message.setVisibility(View.VISIBLE);
			message.setText("A Cobrain member wants to share favorite craves with you! Please log in to your Cobrain account below to see them.");
		}

		//*** for custom action bar view
		/*actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		controller.showOptionsMenu(false);
		*/
		//hideActionBar();
		controller.showOptionsMenu(false);

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		controller.hideSoftKeyBoard();

        loginButton.setOnClickListener(null);
        signupButton.setOnClickListener(null);
		loginButton = null;
		signupButton = null;
		email = null;
		password = null;
        forgotPassword.setOnClickListener(null);
		forgotPassword.setText(null);
		forgotPassword = null;

		//restoreActionBar();
		
		/*actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayShowCustomEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setCustomView(null);
		controller.showOptionsMenu(true);
		*/

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		case R.id.login_button:
			if (!loggingIn) {
				loggingIn = true;
				String email = this.email.getText().toString();
				String password = this.password.getText().toString();
				if (validate(email, password)) {
					controller.showProgressDialog("Please wait ...", "Logging you in ...");
					controller.getCobrain().login(loginUrl, email, password);
				}
				loggingIn = false;
			}
			break;
        case R.id.forgot_password:
            controller.showForgotPassword(email.getText().toString());
            break;
		case R.id.signup_link:
			controller.showSignup(loginUrl);
			break;
		}
		
	}

	@Override
	public void onError(final CharSequence message) {
		//show dialog
		loggingIn = false;
		if (controller != null) controller.showErrorDialog(message);
	}
	
	boolean validate(String email, String password) {
		boolean emailok = email.length() > 0;
		boolean pwdok = password.length() > 0;
		
		if (!emailok || !pwdok) {
			if (!emailok && !pwdok) {
				onError("Please enter a username and password");
			}
			else if (!emailok) {
				onError("Please enter your username or email address");				
			}
			else
				onError("Please enter your password");					
			return false;
		}
		
		return true;
	}

}

