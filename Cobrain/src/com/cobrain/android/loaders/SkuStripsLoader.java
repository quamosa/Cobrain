package com.cobrain.android.loaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.sephiroth.android.library.widget.HListView;
import android.os.AsyncTask;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cobrain.android.R;
import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.adapters.ScenarioStripPagerAdapter;
import com.cobrain.android.adapters.ScenarioStripPagerListAdapter;
import com.cobrain.android.adapters.SkusStripPagerAdapter;
import com.cobrain.android.adapters.SkusStripPagerListAdapter;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.fragments.BaseCobrainFragment;
import com.cobrain.android.fragments.CraveStripsFragment;
import com.cobrain.android.fragments.FriendCraveStripsFragment;
import com.cobrain.android.model.ScenarioItem;
import com.cobrain.android.model.Scenarios;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;

public class SkuStripsLoader {
	CraveFilterLoader loader = new CraveFilterLoader();
	FriendCraveStripsFragment parent;
	ListView craveStripList;
	CraveStripListAdapter craveStripListAdapter;
	public ArrayList<CraveStrip> craveStrips = new ArrayList<CraveStrip>();
	
	public void initialize(FriendCraveStripsFragment parent, ListView list) {
		this.parent = parent;
		craveStripList = list;
		setup();
	}
	
	public void dispose() {
		for (CraveStrip strip : craveStrips) {
			strip.dispose();
		}
		craveStrips.clear();
		craveStrips = null;
		
		if (craveStripList != null) {
			craveStripList.setAdapter(null);
			craveStripList = null;
		}
		if (craveStripListAdapter != null) {
			craveStripListAdapter.clear();
			craveStripListAdapter = null;
		}
		craveStripList = null;
		parent = null;
		if (loader != null) {
			loader.dispose();
			loader = null;
		}
	}
	
	public void load(final User owner, final String ... signal ) {
		new AsyncTask<Void, Void, Skus>() {

			@Override
			protected Skus doInBackground(Void... params) {
				UserInfo u = parent.controller.getCobrain().getUserInfo();
				Skus s;
				
				for (String sig : signal) {
					s = u.getSkus(owner, sig, null, null);
					addSkuStrip( s );
				}
				
				return null;
			}

			@Override
			protected void onPostExecute(Skus result) {
				loadStrips();
			}
			
		}.execute();
	}
	
	void setup() {
		craveStripListAdapter = new CraveStripListAdapter(parent.getActivity().getApplicationContext(), R.id.caption, craveStrips, null);
	}

	void loadStrips() {
		for (CraveStrip strip : craveStrips) {
			strip.load();
		}
		craveStripList.setAdapter(craveStripListAdapter);
	}

	void addSkuStrip(Skus skus) {
		CraveStrip strip = new CraveStrip(craveStripListAdapter, parent);

		SkusStripPagerAdapter adapter = new SkusStripPagerAdapter(parent.getActivity(), strip, parent);
		CraveStripLoader loader = new CraveStripLoader();
		loader.initialize(parent.controller, adapter);

		strip.adapter = adapter;
		strip.loader = loader;

		SkusStripPagerListAdapter a = new SkusStripPagerListAdapter(parent.getActivity().getApplicationContext(), adapter);
		strip.listAdapter = a;

		strip.list = new HListView(parent.getActivity().getApplicationContext());
		strip.list.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, a.getItemHeight()));
		strip.list.setDivider(null);
		strip.list.setTag(strip);
		strip.list.setSelector(R.drawable.sel_transparent);
		
		strip.container = new RelativeLayout(parent.getActivity().getApplicationContext());
		strip.container.addView(strip.list);

		craveStrips.add(strip);
	}

}
