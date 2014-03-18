package com.cobrain.android.fragments;

import java.util.ArrayList;

import com.cobrain.android.R;
import com.cobrain.android.model.Mobile;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class PushNotificationsFragment extends BaseCobrainFragment {
	ListView list;
	ArrayAdapter<PushNotification> adapter;
	ArrayList<PushNotification> pushNotifications = new ArrayList<PushNotification>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.frg_settings_push_notifications, null);
		
		list = (ListView) v.findViewById(R.id.push_notifications_settings);
		loaderUtils.initialize((ViewGroup)v);
		
		setupList();
		
		return v;
	}

	class PushNotification {
		String caption;
		boolean enabled;
		public String code;
	}
	
	void setupList() {
		adapter = new ArrayAdapter<PushNotification>(getActivity().getApplicationContext(), R.layout.list_item_push_notifications, R.id.caption, pushNotifications) {

			class ViewHolder implements OnClickListener {
				ToggleButton enabled;
				int position;
				TextView caption;

				public void setEnabled( ToggleButton button) {
					enabled = button;
					enabled.setOnClickListener(this);
				}
				
				@Override
				public void onClick(View v) {
					PushNotification n = getItem(position);
					n.enabled = ((ToggleButton)v).isChecked();
					saveNotifications();
				}
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				
				ViewHolder vh = (ViewHolder) v.getTag();
				if (vh == null) {
					vh = new ViewHolder();
					vh.setEnabled( (ToggleButton) v.findViewById(R.id.enabled) );
					vh.caption = (TextView) v.findViewById(R.id.caption);
					v.setTag(vh);
				}
				
				PushNotification n = getItem(position);
				vh.position = position;
				vh.enabled.setChecked(n.enabled);
				vh.caption.setText(n.caption);
				
				return v;
			}
			
		};
		
		Resources res = getActivity().getApplicationContext().getResources();
		TypedArray captions = res.obtainTypedArray(R.array.push_notifications_caption);
		TypedArray codes = res.obtainTypedArray(R.array.push_notifications_code);

		pushNotifications.clear();
		
		for (int i = 0; i < captions.length(); i++) {
			PushNotification n = new PushNotification();
			n.caption = captions.getString(i);
			n.code = codes.getString(i);
			n.enabled = controller.getCobrain().getUserInfo().getPreferences().getNotification().getMobile().get(n.code);
			pushNotifications.add(n);
		}
		
		list.setAdapter(adapter);
	}
	
	void saveNotifications() {
		Mobile m = controller.getCobrain().getUserInfo().getPreferences().getNotification().getMobile();
		for (PushNotification n : pushNotifications) {
			m.set(n.code, n.enabled);
		}

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				controller.getCobrain().getUserInfo().savePreferences();
				return null;
			}
			
		}.execute();
	}
	
	@Override
	public void onDestroyView() {
		adapter.clear();
		adapter = null;
		list = null;
		super.onDestroyView();
	}
}
