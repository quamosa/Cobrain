package com.cobrain.android.fragments;

import com.cobrain.android.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class InviteFragment extends BaseCobrainFragment {
	EditText invite;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main_nerve_center_invitation_frame, null);
		invite = (EditText) v.findViewById(R.id.invitation_url);
		loaderUtils.initialize((ViewGroup) v);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onDestroyView() {
		invite = null;
		super.onDestroyView();
	}

	public void update() {
		//loaderUtils.hide(invite);
		loaderUtils.showLoading(null);
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				return controller.getCobrain().getUserInfo().getInviteUrl();
			}

			@Override
			protected void onPostExecute(String result) {
				loaderUtils.dismissLoading();
				if (result == null) result = "You can't create a new invite url at this time... please try again later.";
				invite.setText(result);
				//loaderUtils.show(invite);
			}
			
		}.execute();
	}

}
