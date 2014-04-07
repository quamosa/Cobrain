package com.cobrain.android.fragments;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cobrain.android.R;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.controllers.Cobrain.CobrainView;

public class AccountSaveFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "AccountSaveFragment";
	private Button saveButton;
	private EditText name;
	private EditText zipcode;
	private Spinner gender;
	private boolean loggingIn;
	private boolean doInviteValidation;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_signup_profile, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		View v = getView();
		saveButton = (Button) v.findViewById(R.id.save_account_button);
		name = (EditText) v.findViewById(R.id.name);
		zipcode = (EditText) v.findViewById(R.id.zipcode);
		gender = (Spinner) v.findViewById(R.id.gender);
		
		TextView tv = (TextView) v.findViewById(R.id.zipcode_label);
		tv.setText(Html.fromHtml(getString(R.string.profile_zipcode_label)));
		
		saveButton.setOnClickListener(this);

		/*ArrayAdapter<CharSequence> a = (ArrayAdapter<CharSequence>) gender.getAdapter();
		CharSequence cs = (CharSequence) a.getItem(0);
		a.remove(cs);
		a.insert(Html.fromHtml(String.format("<font color=\"#%x\">%s</font>", getActivity().getResources().getColor(R.color.GreySelectedItem) & 0xffffff, cs.toString())), 0);
		*/
		
		controller.showOptionsMenu(false);
		actionBar.setCustomView(R.layout.ab_signup);

		setTitle("Step 2 of 3");

		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		controller.hideSoftKeyBoard();

		saveButton.setOnClickListener(null);
		saveButton = null;
		gender = null;
		name = null;
		zipcode = null;

		//restoreActionBar();

		super.onDestroyView();
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

				final String gender = getGenderFromSpinner(this.gender);
				final String name = this.name.getText().toString();
				final String zipcode = this.zipcode.getText().toString();

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
								controller.showContentPage(new SignupMobileNumberFragment(), SignupMobileNumberFragment.TAG, false);
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
	public void onError(final CharSequence message) {
		//show dialog
		loggingIn = false;
		controller.showErrorDialog("Unable to save your information", message);
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
			else if (zipcode.length() != 5 || !TextUtils.isDigitsOnly(zipcode)) {
				onError("Please enter your 5 digit zip code");
			}
			else {
				onError("Please enter your gender preference");
			}
			return false;
		}
		
		return true;
	}

	public void setSubTitle(CharSequence title) {
	}

	public void setTitle(CharSequence title) {
		TextView t = (TextView) actionBar.getCustomView().findViewById(R.id.title);
		t.setText(title);
	}

	public CobrainController getCobrainController() {
		return null;
	}

}

