package com.cobrain.android.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cobrain.android.R;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.controllers.Cobrain.CobrainView;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.HelperUtils;
import com.cobrain.android.model.Error;

public class SignupMobileNumberFragment extends BaseCobrainFragment implements OnClickListener, CobrainView {

	public static final String TAG = "SignupMobileNumberFragment";
	private Button next;
	private EditText phone;
	private boolean loggingIn;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_signup_mobile_number, null);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		View v = getView();
		
		next = (Button) v.findViewById(R.id.next);
		phone = (EditText) v.findViewById(R.id.phone);
		next.setOnClickListener(this);

		controller.showOptionsMenu(false);
		actionBar.setCustomView(R.layout.ab_signup);

		setTitle("Step 3 of 3");
		
		String mobile = HelperUtils.SMS.getPhoneNumber(getActivity());
		phone.setText(mobile);
		
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		next = null;
		phone = null;

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()) {
		case R.id.next:
			if (!loggingIn) {
				loggingIn = true;
				
				final String phone = this.phone.getText().toString();

				if (validate(phone)) {
					controller.showProgressDialog("Please wait ...", "Saving your profile");
					
					if (!TextUtils.isEmpty(phone)) {
						new AsyncTask<Void, Void, Boolean>() {

							@Override
							protected Boolean doInBackground(Void... params) {
								UserInfo ui = controller.getCobrain().getUserInfo();
                                if (TextUtils.isEmpty(phone)) return true;
                                return(ui.savePhoneNumber(phone));
							}

							@Override
							protected void onPostExecute(Boolean result) {
                                if (!result) {
                                    UserInfo ui = controller.getCobrain().getUserInfo();
                                    Error error = ui.getLastError();
                                    if (Error.Codes.INVALID_PHONE_NUMBER.equals(error.code)) {
                                        controller.showErrorDialog("Unable to save your information", "Please enter a valid phone number");
                                    }
                                    else controller.showErrorDialog("Unable to save your information", error.message);
                                    loggingIn = false;
                                }
                                else showMain();
							}

						}.execute();
					}
                    else {
                        showMain();
                    }
				}
				else loggingIn = false;
			}
			break;
		}
		
	}

    void showMain() {
        controller.dismissDialog();
        controller.showMain(CobrainController.VIEW_HOME);
    }

	@Override
	public void setTitle(CharSequence title) {
		TextView t = (TextView) actionBar.getCustomView().findViewById(R.id.title);
		t.setText(title);
	}

	@Override
	public void onError(final CharSequence message) {
		loggingIn = false;
		controller.showErrorDialog(message);
	}
	
	boolean validate(String phone) {
		boolean phoneok = phone.length() > 0;
		
		if (phoneok) {
			if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
				onError("Please enter a valid phone number");
				return false;
			}
		}

		return true;
	}

}

