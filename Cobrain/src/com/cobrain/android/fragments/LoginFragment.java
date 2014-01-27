package com.cobrain.android.fragments;

import com.cobrain.android.R;
import com.cobrain.android.service.Cobrain.CobrainView;

import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "LoginFragment";
	private Button loginButton;
	private Button signupButton;
	private EditText email;
	private EditText password;
	private boolean loggingIn;
	private TextView forgotPassword;
	private String loginUrl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.login, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		View v = getView();
		
		loginUrl = getArguments().getString("loginUrl");
		
		loginButton = (Button) v.findViewById(R.id.login_button);
		signupButton = (Button) v.findViewById(R.id.signup_button);
		email = (EditText) v.findViewById(R.id.email);
		password = (EditText) v.findViewById(R.id.password);
		forgotPassword = (TextView) v.findViewById(R.id.forgot_password);
		
		email.setText(this.getResources().getString(R.string.username));
		password.setText(this.getResources().getString(R.string.password));
		
		forgotPassword.setMovementMethod(LinkMovementMethod.getInstance());
		  Spannable spans = (Spannable) forgotPassword.getText();
		  ClickableSpan clickSpan = new ClickableSpan() {

		     @Override
		     public void onClick(View widget)
		     {
		    	 controller.showForgotPassword(email.getText().toString());
		     }
		  };
		  spans.setSpan(clickSpan, 0, spans.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);		
		
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
		actionBar.setCustomView(R.layout.actionbar_login_frame);
		*/
		hideActionBar();

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		controller.hideSoftKeyBoard();
		
		loginButton = null;
		signupButton = null;
		email = null;
		password = null;
		forgotPassword.setText(null);
		forgotPassword = null;

		restoreActionBar();
		
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

		case R.id.signup_button:
			controller.showSignup(loginUrl);
			break;
		}
		
	}

	@Override
	public void onError(final String message) {
		//show dialog
		loggingIn = false;
		controller.showErrorDialog(message);
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

