package com.cobrain.android.fragments;

import com.cobrain.android.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ForgetMeFragment extends BaseCobrainFragment {
	Button forget;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.main_nerve_center_forget_me_frame, null);
		
		forget = (Button) v.findViewById(R.id.forgetme);
		forget.setOnClickListener(this);
		loaderUtils.initialize((ViewGroup)v);
		
		return v;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch(v.getId()) {
		case R.id.forgetme:

			AlertDialog.Builder builder;
			AlertDialog alertDialog;

			Context c = getActivity();
			//LayoutInflater inflater = (LayoutInflater)
			//        c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			builder = new AlertDialog.Builder(c);

			View vw = View.inflate(c, R.layout.dlg_error, null);
			vw.findViewById(R.id.uh_oh).setVisibility(View.GONE);
			TextView tv = (TextView) vw.findViewById(R.id.error_message);
			String mymessage = "Are you sure you want to delete your Cobrain account?\n\nAll your information and recommendations will be forgotten. This action cannot be undone.";
			tv.setText(mymessage);
			builder.setView(vw);
			
			builder.setPositiveButton("Forget Me", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					loaderUtils.showLoading(null);
					new AsyncTask<Void, Void, Boolean>() {

						@Override
						protected Boolean doInBackground(Void... params) {
							return controller.getCobrain().getUserInfo().forgetMe();
						}

						@Override
						protected void onPostExecute(Boolean result) {
							loaderUtils.dismissLoading();
							if (result) 
								controller.getCobrain().logout();
						}
						
					}.execute();
				}
			});
			
			builder.setNegativeButton("Cancel", null);
			//builder.setTitle("FORGET ME");
			alertDialog = builder.create();
			alertDialog.show();
		}
	}

}
