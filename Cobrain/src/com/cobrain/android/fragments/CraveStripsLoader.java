package com.cobrain.android.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.HListView;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cobrain.android.R;
import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.adapters.CraveStripPagerAdapter;
import com.cobrain.android.adapters.CraveStripPagerListAdapter;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.loaders.CraveFilterLoader;
import com.cobrain.android.loaders.CraveStripLoader;
import com.cobrain.android.model.ScenarioItem;
import com.cobrain.android.model.Scenarios;
import com.cobrain.android.model.UserInfo;

public class CraveStripsLoader {
	CraveFilterLoader loader = new CraveFilterLoader();
	CraveStripsFragment parent;
	ListView craveStripList;
	CraveStripListAdapter craveStripListAdapter;
	ArrayList<CraveStrip> craveStrips = new ArrayList<CraveStrip>();

	public void initialize(CraveStripsFragment parent, ListView list) {
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
	
	public void load() {
		loadScenarios();
	}

	void loadScenarios() {
		new AsyncTask<Void, Void, List<ScenarioItem>>() {

			@Override
			protected List<ScenarioItem> doInBackground(Void... params) {
				UserInfo u = parent.controller.getCobrain().getUserInfo();
				Scenarios sc = u.getScenarios();

				if (sc != null) {
					Comparator<ScenarioItem> c = new Comparator<ScenarioItem>() {
	
						@Override
						public int compare(ScenarioItem lhs, ScenarioItem rhs) {
							int l = lhs.getOrder();
							int r = rhs.getOrder();
							if (l > r) return 1;
							else if (r < l) return -1;
							return 0;
						}
					};
					
					Collections.sort(sc.getResults(), c);
	
					return sc.getResults();
				}

				return null;
			}

			@Override
			protected void onPostExecute(List<ScenarioItem> result) {
				scenariosToStrips(result);
			}
			
		}.execute();
	}
	
	void setup() {
		craveStripListAdapter = new CraveStripListAdapter(parent.getActivity().getApplicationContext(), R.id.caption, craveStrips, parent);
	}
	
	void scenariosToStrips(List<ScenarioItem> scenarios) {
		for (ScenarioItem s : scenarios) {
			CraveStrip strip = new CraveStrip(craveStripListAdapter, parent);

			//CraveStripGroupedPagerAdapter adapter = new CraveStripGroupedPagerAdapter(getActivity(), CravesFragment.this);
			CraveStripPagerAdapter adapter = new CraveStripPagerAdapter(parent.getActivity(), strip, parent);
			CraveStripLoader loader = new CraveStripLoader();
			loader.initialize(parent.controller, adapter);

			strip.scenarioId = s.getId();
			strip.adapter = adapter;
			strip.loader = loader;
			//strip.pager = new com.cobrain.android.views.ViewPager(getActivity().getApplicationContext());

			CraveStripPagerListAdapter a = new CraveStripPagerListAdapter(parent.getActivity().getApplicationContext(), adapter);
			strip.listAdapter = a;

			strip.list = new HListView(parent.getActivity().getApplicationContext());
			strip.list.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, a.getItemHeight()));
			strip.list.setDivider(null);
			strip.list.setTag(strip);
			strip.list.setSelector(R.drawable.sel_transparent);
			
			//FYI: to scroll and present views like Netflix android app:
			//strip.pager.setPageMargin(-80); to do like netflix android app
			//strip.adapter = new CraveStripGroupedPagerAdapter
			
			
			strip.container = new RelativeLayout(parent.getActivity().getApplicationContext());
			/*
			strip.pager.setId(strip.categoryId);
			strip.container.setId(strip.categoryId + 0x1000);
			strip.container.addView(strip.pager);
			*/
			strip.container.addView(strip.list);

			strip.load();

			craveStrips.add(strip);
		}
		
		craveStripList.setAdapter(craveStripListAdapter);
		
	}

}
