package com.cobrain.android.loaders;

import java.util.ArrayList;
import it.sephiroth.android.library.widget.HListView;
import android.os.AsyncTask;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cobrain.android.R;
import com.cobrain.android.adapters.CraveStripListAdapter;
import com.cobrain.android.adapters.SkusStripPagerAdapter;
import com.cobrain.android.adapters.SkusStripPagerListAdapter;
import com.cobrain.android.controllers.CraveStrip;
import com.cobrain.android.controllers.SkuCraveStrip;
import com.cobrain.android.fragments.BaseCobrainFragment;
import com.cobrain.android.fragments.FriendCraveStripsFragment;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;

public class SkuStripsLoader {
	CraveFilterLoader loader = new CraveFilterLoader();
	public FriendCraveStripsFragment parent;
	ListView craveStripList;
	CraveStripListAdapter<Skus> craveStripListAdapter;
	public ArrayList<CraveStrip<Skus>> craveStrips = new ArrayList<CraveStrip<Skus>>();
	private OnLoadListener<Skus> listener;
	
	public void initialize(FriendCraveStripsFragment parent, ListView list, OnLoadListener<Skus> listener) {
		this.listener = listener;
		this.parent = parent;
		craveStripList = list;
		setup();
	}
	
	public void dispose() {
		listener = null;
		
		for (CraveStrip<Skus> strip : craveStrips) {
			strip.dispose();
		}
		craveStrips.clear();
		//craveStrips = null;
		
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
	
	@SuppressWarnings("unchecked")
	public void load(final User owner, final String caption[], final String signal[] ) {
		if (listener != null) listener.onLoadStarted();
		
		parent.addAsyncTask("skuStripsLoader", new AsyncTask<Object, Void, Skus>() {

			@Override
			protected Skus doInBackground(Object... params) {
				UserInfo u = BaseCobrainFragment.controller.getCobrain().getUserInfo();
				Skus s;
				
				for (int i = 0; i < signal.length; i++) {
					s = u.getSkus(owner, signal[i], null, null);
					if (!addSkuStrip( caption[i], s )) break;
				}
				
				return null;
			}

			@Override
			protected void onPostExecute(Skus result) {
				loadStrips();
				if (listener != null) listener.onLoadCompleted(result);
			}
			
		}).execute();
	}
	
	void setup() {
		craveStripListAdapter = new CraveStripListAdapter<Skus>(parent.getActivity().getApplicationContext(), R.id.caption, craveStrips, null);
	}

	void loadStrips() {
		for (CraveStrip<Skus> strip : craveStrips) {
			strip.load();
		}
		if (craveStripList != null)
			craveStripList.setAdapter(craveStripListAdapter);
	}

	boolean addSkuStrip(String caption, Skus skus) {
		if (parent == null) return false;
		
		SkuCraveStrip strip = new SkuCraveStrip(craveStripListAdapter, parent);
		strip.skus = skus;
		strip.caption = caption;
		
		SkusStripPagerAdapter adapter = new SkusStripPagerAdapter(parent.getActivity(), strip, parent);
		SkuStripLoader loader = new SkuStripLoader();
		loader.initialize(BaseCobrainFragment.controller, adapter);

		strip.adapter = adapter;
		strip.loader = loader;

		SkusStripPagerListAdapter a = new SkusStripPagerListAdapter(parent.getActivity().getApplicationContext(), adapter);
		strip.listAdapter = a;

		strip.list = new HListView(parent.getActivity().getApplicationContext());
		strip.list.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, a.getItemHeight()));
		strip.list.setDivider(null);
		strip.list.setTag(strip);
		strip.list.setSelector(R.drawable.sel_transparent);
		
		//strip.container = new RelativeLayout(parent.getActivity().getApplicationContext());
		//strip.container.addView(strip.list);

		craveStrips.add(strip);
		return true;
	}

}
