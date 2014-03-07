package com.cobrain.android.fragments;

import com.cobrain.android.R;
import com.cobrain.android.controllers.Cobrain.CobrainView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class AccountFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "AccountFragment";
	private Button saveButton;
	private EditText currentPassword, newPassword, confirmPassword;
	private boolean loggingIn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_account, null);
		saveButton = (Button) v.findViewById(R.id.save_account_button);
		currentPassword = (EditText) v.findViewById(R.id.current_password);
		newPassword = (EditText) v.findViewById(R.id.new_password);
		confirmPassword = (EditText) v.findViewById(R.id.confirm_password);
		saveButton.setOnClickListener(this);
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		//controller.showOptionsMenu(false);
		//actionBar.setCustomView(R.layout.actionbar_login_save_account_frame);

		//hideActionBar();

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		saveButton.setOnClickListener(null);
		saveButton = null;
		currentPassword = null;
		newPassword = null;
		confirmPassword = null;

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		case R.id.save_account_button:
			if (!loggingIn) {
				loggingIn = true;

				final String currentPassword = this.currentPassword.getText().toString();
				final String confirmPassword = this.confirmPassword.getText().toString();
				final String password = this.newPassword.getText().toString();

				if (validate(currentPassword, password, confirmPassword)) {
					controller.showProgressDialog("Please wait ...", "Changing your password ...");
					new AsyncTask<Void, Void, Boolean>() {

						@Override
						protected Boolean doInBackground(Void... params) {
							
							loggingIn = false;

							String email = controller.getCobrain().getUserInfo().getEmail();
							if (controller.getCobrain().changePassword(email, currentPassword, password, confirmPassword)) {
								return true;
							}
							return false;
						}

						@Override
						protected void onPostExecute(Boolean result) {
							controller.dismissDialog();
							if (result) {
								controller.showDialog("Your password was changed.");
							}
							else onError("We had a problem changing your password. Please check your current password and try again.");
						}
					}.execute();
				}
				else {
					loggingIn = false;
				}
			}
			break;
		}
		
	}

	@Override
	public void onError(final CharSequence message) {
		//show dialog
		loggingIn = false;
		controller.showErrorDialog(message);
	}
	
	boolean validate(String currentPassword, String newPassword, String confirmPassword) {
		boolean passok = newPassword != null && newPassword.length() > 0;
		boolean cok = currentPassword != null && currentPassword.length() > 0;
		boolean cfok = confirmPassword != null && confirmPassword.length() > 0;
		
		if (!passok) {
			onError("Please enter your new password");
			return false;
		}
		else if (!cok) {
			onError("Please enter your current password");
			return false;			
		}
		else if (newPassword.length() < 8) {
			onError("Your password must be at least 8 characters");
			return false;
		}
		else if (!cfok) {
			onError("Please confirm your new password");
			return false;
		}
		else if (!newPassword.equals(confirmPassword)) {
			onError("Your passwords don't match. Please confirm your new password");
			return false;
		}
		
		return true;
	}

}

