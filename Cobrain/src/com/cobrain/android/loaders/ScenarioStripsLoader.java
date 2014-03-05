package com.cobrain.android.loaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.sephiroth.android.library.widget.HListView;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cobrain.android.R;
import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.adapters.ScenarioStripPagerAdapter;
import com.cobrain.android.adapters.ScenarioStripPagerListAdapter;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.controllers.ScenarioCraveStrip;
import com.cobrain.android.fragments.CraveStripsFragment;
import com.cobrain.android.model.Scenario;
import com.cobrain.android.model.ScenarioItem;
import com.cobrain.android.model.Scenarios;
import com.cobrain.android.model.UserInfo;

public class ScenarioStripsLoader {
	CraveFilterLoader loader = new CraveFilterLoader();
	CraveStripsFragment parent;
	ListView craveStripList;
	CraveStripListAdapter craveStripListAdapter;
	public ArrayList<CraveStrip<Scenario>> craveStrips = new ArrayList<CraveStrip<Scenario>>();
	private AsyncTask<Void, Void, List<ScenarioItem>> currentRequest;
	private boolean onSale;
	
	public void initialize(CraveStripsFragment<?> parent, ListView list, boolean onSale) {
		this.parent = parent;
		craveStripList = list;
		this.onSale = onSale;
		setup();
	}
	
	public void dispose() {
		if (currentRequest != null) {
			currentRequest.cancel(true);
			currentRequest = null;
		}
		
		for (CraveStrip<Scenario> strip : craveStrips) {
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
		craveStripList.setAdapter(null);

		parent.loaderUtils.showLoading("Loading...");
		
		currentRequest = new AsyncTask<Void, Void, List<ScenarioItem>>() {

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
	
					scenariosToStrips( sc.getResults() );

					return sc.getResults();
				}

				return null;
			}

			@Override
			protected void onPostExecute(List<ScenarioItem> result) {
				loadStrips();
				parent.loaderUtils.dismissLoading();
			}
			
		}.execute();
	}
	
	void setup() {
		craveStripListAdapter = new CraveStripListAdapter(parent.getActivity().getApplicationContext(), R.id.caption, craveStrips, parent);
	}

	void loadStrips() {
		for (CraveStrip<Scenario> strip : craveStrips) {
			strip.load();
		}
		craveStripList.setAdapter(craveStripListAdapter);
	}

	public void clear() {
		craveStripListAdapter.clear();
		craveStrips.clear();
	}
	
	public void addHeaderStrip() {	
		CraveStrip<Scenario> strip = new ScenarioCraveStrip(craveStripListAdapter, parent);
		strip.type = CraveStrip.STRIP_TYPE_HEADER;
		strip.list = new HListView(parent.getActivity().getApplicationContext());
		strip.list.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		strip.list.setDivider(null);
		strip.list.setTag(strip);
		strip.list.setSelector(R.drawable.sel_transparent);
		strip.listAdapter = new ArrayAdapter<Object>(parent.getActivity().getApplicationContext(), 0) {

			@Override
			public int getCount() {
				return 1;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = convertView;
				if (v == null) {
					LayoutInflater inflater = LayoutInflater.from(getContext());
					v = inflater.inflate(R.layout.frg_home_rack_header, parent, false);
					int w = getContext().getResources().getDisplayMetrics().widthPixels;
					v.getLayoutParams().width = w;
				}
				return v;
			}
			
		};
		//strip.container = new RelativeLayout(parent.getActivity().getApplicationContext());
		//strip.container.addView(strip.list);
		
		craveStrips.add(strip);
	}
	
	void scenariosToStrips(List<ScenarioItem> scenarios) {
		if (scenarios != null) {
			for (ScenarioItem s : scenarios) {
				ScenarioCraveStrip strip = new ScenarioCraveStrip(craveStripListAdapter, parent);
	
				//CraveStripGroupedPagerAdapter adapter = new CraveStripGroupedPagerAdapter(getActivity(), CravesFragment.this);
				ScenarioStripPagerAdapter adapter = new ScenarioStripPagerAdapter(parent.getActivity(), strip, parent);
				ScenarioStripLoader loader = new ScenarioStripLoader();
				loader.initialize(parent.controller, adapter);
				loader.setOnSaleRecommendationsOnly(onSale);
	
				strip.scenarioId = s.getId();
				strip.adapter = adapter;
				strip.loader = loader;
				//strip.pager = new com.cobrain.android.views.ViewPager(getActivity().getApplicationContext());
	
				ScenarioStripPagerListAdapter a = new ScenarioStripPagerListAdapter(parent.getActivity().getApplicationContext(), adapter);
				strip.listAdapter = a;
	
				strip.list = new HListView(parent.getActivity().getApplicationContext());
				strip.list.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, a.getItemHeight()));
				strip.list.setDivider(null);
				strip.list.setTag(strip);
				strip.list.setSelector(R.drawable.sel_transparent);
				
				//FYI: to scroll and present views like Netflix android app:
				//strip.pager.setPageMargin(-80); to do like netflix android app
				//strip.adapter = new CraveStripGroupedPagerAdapter
				
				//strip.container = new RelativeLayout(parent.getActivity().getApplicationContext());
				/*
				strip.pager.setId(strip.categoryId);
				strip.container.setId(strip.categoryId + 0x1000);
				strip.container.addView(strip.pager);
				*/
				//strip.container.addView(strip.list);
	
				craveStrips.add(strip);
			}
		}
	}

}
