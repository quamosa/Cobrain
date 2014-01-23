package com.cobrain.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.cobrain.android.R;
import com.cobrain.android.service.Cobrain.CobrainView;

public class SignupFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "SignupFragment";
	private Button createButton;
	private Button cancelButton;
	private EditText email;
	private EditText password;
	private boolean loggingIn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.login_new_account, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		View v = getView();
		createButton = (Button) v.findViewById(R.id.create_account_button);
		cancelButton = (Button) v.findViewById(R.id.cancel_button);
		email = (EditText) v.findViewById(R.id.email);
		password = (EditText) v.findViewById(R.id.password);
		
		createButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

		hideActionBar();

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		createButton = null;
		cancelButton = null;
		email = null;
		password = null;

		restoreActionBar();

		super.onDestroyView();
	}

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
					controller.getCobrain().createAccount(email, password);
				}
				else loggingIn = false;
			}
			break;

		case R.id.cancel_button:
			getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new LoginFragment())
				.commit();
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

