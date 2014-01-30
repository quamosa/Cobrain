package com.cobrain.android.fragments;

import com.cobrain.android.R;
import com.cobrain.android.service.Cobrain.CobrainController;
import com.cobrain.android.service.Cobrain.CobrainView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

public class AccountSaveFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "AccountSaveFragment";
	private Button saveButton;
	private Button verifyInviteButton;
	private EditText name;
	private EditText zipcode;
	private RadioGroup gender;
	private boolean loggingIn;
	private boolean doInviteValidation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.login_save_account, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		View v = getView();
		saveButton = (Button) v.findViewById(R.id.save_account_button);
		verifyInviteButton = (Button) v.findViewById(R.id.verify_invite_button);
		name = (EditText) v.findViewById(R.id.name);
		zipcode = (EditText) v.findViewById(R.id.zipcode);
		gender = (RadioGroup) v.findViewById(R.id.gender);
		
		saveButton.setOnClickListener(this);
		verifyInviteButton.setOnClickListener(this);

		controller.showOptionsMenu(false);
		actionBar.setCustomView(R.layout.actionbar_login_save_account_frame);

		//hideActionBar();

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		saveButton.setOnClickListener(null);
		saveButton = null;
		gender = null;
		name = null;
		zipcode = null;

		//restoreActionBar();

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		case R.id.verify_invite_button:
			doInviteValidation = true;
			saveButton.performClick();
			break;
		case R.id.save_account_button:
			if (!loggingIn) {
				loggingIn = true;

				final String gender;
				final String name = this.name.getText().toString();
				final String zipcode = this.zipcode.getText().toString();
				int genderId = this.gender.getCheckedRadioButtonId();

				switch (genderId) {
				case R.id.male: gender = "MALE"; break;
				case R.id.female: gender = "FEMALE"; break;
				default: gender = "BOTH";
				}

				if (validate(name, zipcode, gender)) {
					controller.showProgressDialog("Please wait ...", "Saving your profile ...");
					new AsyncTask<Void, Void, Boolean>() {

						@Override
						protected Boolean doInBackground(Void... params) {
							
							loggingIn = false;

							if (controller.getCobrain().getUserInfo().saveProfile(name, zipcode, gender)) {
								if (doInviteValidation) {
									controller.getCobrain().getUserInfo().validateInvitation();
								}
								return true;
							}
							return false;
						}

						@Override
						protected void onPostExecute(Boolean result) {
							controller.dismissDialog();
							if (result) {
								controller.showMain(CobrainController.VIEW_HOME);
							}
						}
					}.execute();
				}
				else {
					doInviteValidation = false;
					loggingIn = false;
				}
			}
			break;
		}
		
	}

	@Override
	public void onError(final String message) {
		//show dialog
		loggingIn = false;
		controller.showErrorDialog(message);
	}
	
	boolean validate(String name, String zipcode, String gender) {
		boolean nameok = name != null && name.length() > 0;
		boolean zipok = zipcode != null && zipcode.length() > 0;
		boolean genderok = gender != null && gender.length() > 0;
		
		if (!nameok || !zipok || !genderok) {
			if (!nameok && !zipok && !genderok) {
				onError("Please enter a name, zip code, and gender preference");
			}
			else if (!nameok) {
				onError("Please enter your name");
			}
			else if (!zipok) {
				onError("Please enter your zip code");
			}
			else {
				onError("Please enter you gender preference");
			}
			return false;
		}
		
		return true;
	}

	public void setSubTitle(CharSequence title) {
	}

	public void setTitle(CharSequence title) {
	}

	public CobrainController getCobrainController() {
		return null;
	}

}

