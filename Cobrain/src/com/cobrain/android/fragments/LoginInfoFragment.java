package com.cobrain.android.fragments;

import com.cobrain.android.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LoginInfoFragment extends BaseCobrainFragment {
	TextView login;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main_nerve_center_login_info_frame, null);
		login = (TextView) v.findViewById(R.id.login);
		login.setText(controller.getCobrain().getUserInfo().getEmail());
		return v;
	}
	
	@Override
	public void onDestroyView() {
		login = null;
		super.onDestroyView();
	}

	public void update() {
		login.setText(controller.getCobrain().getEmail());
	}

}
