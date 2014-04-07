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

public class SignupFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "SignupFragment";
	private Button createButton;
	private TextView cancelButton;
	private EditText email;
	private EditText password;
	private EditText phone;
	private boolean loggingIn;
	private String signupUrl;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_create_account, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		View v = getView();
		
		signupUrl = getArguments().getString("signupUrl");
		createButton = (Button) v.findViewById(R.id.create_account_button);
		cancelButton = (TextView) v.findViewById(R.id.login_link);
		email = (EditText) v.findViewById(R.id.email);
		password = (EditText) v.findViewById(R.id.password);
		//phone = (EditText) v.findViewById(R.id.phone);
		
		createButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

		if (signupUrl != null) {
			TextView message = (TextView) v.findViewById(R.id.message_label);
			message.setVisibility(View.VISIBLE);
			message.setText("A Cobrain member wants to share favorite craves with you! Please create your Cobrain account below to see them.");
		}

		controller.showOptionsMenu(false);

		/*String mobile = getPhoneNumber();
		phone.setText(mobile);
		phone.setEnabled(false);
		 */
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		signupUrl = null;
		createButton = null;
		cancelButton = null;
		email = null;
		password = null;
		phone = null;

		//restoreActionBar();

		super.onDestroyView();
	}

	/*String mobile = getPhoneNumber();
	phone.setText(mobile);
	phone.setEnabled(false);

	String getPhoneNumber() {
		TelephonyManager tm = (TelephonyManager)getActivity().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE); 
		return tm.getLine1Number();
	}
	 */
	
	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		case R.id.create_account_button:
			if (!loggingIn) {
				loggingIn = true;
				String email = this.email.getText().toString();
				String password = this.password.getText().toString();
				if (validate(email, password)) {
					controller.showProgressDialog("Please wait ...", "Creating your account ...");
					controller.getCobrain().createAccount(signupUrl, email, password);
				}
				else loggingIn = false;
			}
			break;

		case R.id.login_link:
			controller.showLogin(signupUrl);
			break;
		}
		
	}

	@Override
	public void onError(final CharSequence message) {
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
		
		if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			onError("Please enter a valid email address");
			return false;
		}
		
		if (password.length() < 8) {
			onError("Your password must be at least 8 characters");
			return false;
		}
		
		return true;
	}

}

