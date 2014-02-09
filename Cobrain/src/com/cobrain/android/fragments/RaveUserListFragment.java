package com.cobrain.android.fragments;

import com.cobrain.android.R;
import com.cobrain.android.adapters.RaveUserListAdapter;
import com.cobrain.android.loaders.RaveUserListLoader;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.v1.Rave;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class RaveUserListFragment extends BaseCobrainListFragment {

	public static final String TAG = "RaveUserListFragment";
	String itemId;
	RaveUserListAdapter adapter;
	RaveUserListLoader loader = new RaveUserListLoader();
	
	public RaveUserListFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		itemId = getArguments().getString("itemId");
		adapter = new RaveUserListAdapter(getActivity().getApplicationContext(), R.id.friend_name, loader.getItems(), this);
		loader.initialize(controller, adapter);
		setListAdapter(adapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Rave rave = adapter.getItem(position);

		loaderUtils.showLoading("Loading " + rave.getUser().getName() + "'s rave list...");
		
		new AsyncTask<Object, Void, Skus>() {
			@Override
			protected Skus doInBackground(Object... params) {

				return controller.getCobrain().getUserInfo().getSkus(rave.getUser().getId(), "shared", null, null);
			}

			@Override
			protected void onPostExecute(Skus result) {
				loaderUtils.dismiss();
				if (result != null) {
					controller.showWishList(result, false, true);
				}
			}
			
		}.execute();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		loaderUtils.initialize((ViewGroup)getView());
		update();
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onDestroyView() {
		setListAdapter(null);
		adapter.dispose();
		adapter = null;
		super.onDestroyView();
	}

	void update() {
		loader.loadFriendList(itemId);
	}

	public static Fragment newInstance(String userId) {
		Bundle args = new Bundle();
		args.putString("userId", userId);
		
		RaveUserListFragment f = new RaveUserListFragment();
		f.setArguments(args);

		return f;
	}
	
}
