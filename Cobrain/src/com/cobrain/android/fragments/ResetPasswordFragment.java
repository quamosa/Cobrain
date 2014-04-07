package com.cobrain.android.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cobrain.android.R;

public class ResetPasswordFragment extends BaseCobrainFragment {
	public static final String TAG = "ResetPasswordFragment";
	Button reset;
	TextView email;
    TextView signupButton;
    TextView loginButton;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frg_forgot_password, null);
		
		email = (TextView) v.findViewById(R.id.email);
		reset = (Button) v.findViewById(R.id.reset_button);
        signupButton = (TextView) v.findViewById(R.id.signup_link);
        loginButton = (TextView) v.findViewById(R.id.cancel_link);
        signupButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
		reset.setOnClickListener(this);
		loaderUtils.initialize((ViewGroup)(getActivity().findViewById(android.R.id.content)));
		
		Bundle args = getArguments();
		if (args != null) {
			email.setText(args.getString("email"));
		}
		
		controller.showOptionsMenu(false);
		//hideActionBar();
		
		return v;
	}

	@Override
	public void onDestroyView() {
		
		//restoreActionBar();
		reset.setOnClickListener(null);
		reset = null;
		email = null;
        signupButton.setOnClickListener(null);
        signupButton = null;
        loginButton.setOnClickListener(null);
        loginButton = null;

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch(v.getId()) {
        case R.id.cancel_link:
            controller.showLogin(null);
            break;
		case R.id.signup_link:
			controller.showSignup(null);
			break;
		case R.id.reset_button:

			if (email.getText() != null && email.getText().length() > 0) {
				loaderUtils.showLoading(null);
				new AsyncTask<Void, Void, Boolean>() {
	
					@Override
					protected Boolean doInBackground(Void... params) {
						return controller.getCobrain().resetPassword(email.getText().toString());
					}
	
					@Override
					protected void onPostExecute(Boolean result) {
						loaderUtils.dismissLoading();
						if (result) {
							AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
							builder1.setTitle("Successful");
					        builder1.setMessage("Please check your email for instructions to complete resetting your password.");
					        builder1.setCancelable(true);
					        builder1.setPositiveButton("Done",
					                new DialogInterface.OnClickListener() {
					            public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					                controller.showLogin(null);
					            }
					        });
					
					        AlertDialog alert11 = builder1.create();
					        alert11.show();		
						}
					}
					
				}.execute();
			}
			else controller.showErrorDialog("Please enter your email address");
			
		}
	}

}
