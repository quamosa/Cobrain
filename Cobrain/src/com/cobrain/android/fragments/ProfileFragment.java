package com.cobrain.android.fragments;

import com.cobrain.android.R;
import com.cobrain.android.controllers.Cobrain.CobrainView;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.HelperUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class ProfileFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "ProfileFragment";
	private Button saveButton;
	private EditText name;
	private EditText zipcode;
	private Spinner gender;
	private boolean loggingIn;
	private boolean doInviteValidation;
	private EditText phone;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_profile, null);
		saveButton = (Button) v.findViewById(R.id.save_account_button);
		name = (EditText) v.findViewById(R.id.name);
		zipcode = (EditText) v.findViewById(R.id.zipcode);
		gender = (Spinner) v.findViewById(R.id.gender);
		phone = (EditText) v.findViewById(R.id.phone);

		saveButton.setOnClickListener(this);

		/*String mobile = HelperUtils.SMS.getPhoneNumber(getActivity().getApplicationContext());
		phone.setText(mobile);
		phone.setEnabled(false);*/

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//controller.showOptionsMenu(false);
		//actionBar.setCustomView(R.layout.actionbar_login_save_account_frame);

		//hideActionBar();

		super.onActivityCreated(savedInstanceState);
	}

	public void update() {
		UserInfo ui = controller.getCobrain().getUserInfo();
		name.setText(ui.getName());
		zipcode.setText(ui.getZipcode());
		String gen = ui.getGenderPreference();

		int i = getIndexOfGender(gen);
		if (i != -1) gender.setSelection(i);
		
	}
	
	@Override
	public void onDestroyView() {
		saveButton.setOnClickListener(null);
		saveButton = null;
		gender = null;
		name = null;
		zipcode = null;
		phone = null;
		
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		case R.id.save_account_button:
			AlertDialog.Builder builder;
			AlertDialog alertDialog;

			Context c = getActivity();
			builder = new AlertDialog.Builder(c);

			View vw = View.inflate(c, R.layout.dlg_error, null);
			TextView tv = (TextView) vw.findViewById(R.id.error_message);
			String mymessage = "Are you sure you want to save the changes to your Cobrain account?\n\nThis action cannot be undone.";
			tv.setText(mymessage);
			builder.setView(vw);
			
			builder.setPositiveButton("Save", new Dialog.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					final String name = ProfileFragment.this.name.getText().toString();
					final String zipcode = ProfileFragment.this.zipcode.getText().toString();
					final String gender = getGenderFromSpinner(ProfileFragment.this.gender);

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
									NavigationMenuFragment f = (NavigationMenuFragment) getActivity().getSupportFragmentManager().findFragmentByTag(NavigationMenuFragment.TAG);
									if (f != null) f.update();
									controller.showDialog(null, "Your profile was saved.");
								}
								else onError("We had a problem updating your profile. Please try again later.");
							}
						}.execute();
					}
					else {
						doInviteValidation = false;
						loggingIn = false;
					}
				}
			});
			
			builder.setNegativeButton("Cancel", null);
			alertDialog = builder.create();
			alertDialog.show();

			break;
		}
		
	}

	@Override
	public void onError(final CharSequence message) {
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

	String getGenderFromSpinner(Spinner spinner) {
		Resources res = getResources();
		final TypedArray selectedValues = res
		        .obtainTypedArray(R.array.profile_gender_values);
		int i = spinner.getSelectedItemPosition();
		if (i >= 0)
			return selectedValues.getString(i);
		return null;
	}
	int getIndexOfGender(String gender) {
		Resources res = getResources();
		final TypedArray selectedValues = res
		        .obtainTypedArray(R.array.profile_gender_values);
		for (int i = 0; i < selectedValues.length(); i++) {
			if (selectedValues.getString(i).equals(gender))
				return i;
		}
		return -1;
	}
	
	public void setSubTitle(CharSequence title) {
	}

	public void setTitle(CharSequence title) {
	}

}

