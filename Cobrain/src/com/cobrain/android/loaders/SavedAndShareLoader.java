package com.cobrain.android.loaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.os.AsyncTask;
import com.cobrain.android.adapters.SavedAndShareAdapter;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.HelperUtils;

public class SavedAndShareLoader {
	SavedAndShareAdapter adapter;
	CobrainController controller;
	OnLoadListener<List<Sku>> onLoadListener;
	private AsyncTask<Void, Void, List<Sku>> currentRequest;
	public int categoryId = 0;
	int priceId = 0;
	public boolean onSale;
	public String signal;
	public boolean mostRaved;
	public boolean mostNew;

	public void initialize(CobrainController controller, SavedAndShareAdapter adapter, String signal) {
		this.controller = controller;
		this.signal = signal;
		setAdapter(adapter);
	}

	public void setAdapter(SavedAndShareAdapter adapter) {
		this.adapter = adapter;
		adapter.isShared = signal.equals("shared");
	}

	public void dispose() {
		cancel();
		controller = null;
		adapter = null;
		onLoadListener = null;
	}

	public boolean isFiltered() {
		if (signal.equals("saved")) return categoryId != 0 || onSale;
		else return mostRaved || mostNew;
	}
	
	public void applyCategoryFilter(int id) {
		if (categoryId != id) {
			categoryId = id;
			loadUserList();
		}
	}
	
	public void applyPriceFilter(boolean onSale) {
		if (this.onSale != onSale) {
			this.onSale = onSale;
			loadUserList();
		}
	}
	
	public void loadUserList() {
		if (onLoadListener != null) onLoadListener.onLoadStarted();

		currentRequest = new AsyncTask<Void, Void, List<Sku>>() {
			@Override
			protected List<Sku> doInBackground(Void... params) {
				if (HelperUtils.Tasks.asyncTaskCancel(this, controller == null)) return null;

				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();

				if (u != null) {
					Skus s = u.getSkus(null, signal, (categoryId > 0) ? categoryId : null, (onSale) ? onSale : null);
					if (s != null) {
						if (mostNew) doMostNewFilter(s.get());
						if (mostRaved) doMostRavedFilter(s.get());
						return s.get();
					}
				}

				return null;
			}

			@Override
			protected void onPostExecute(List<Sku> result) {
				if (result != null) {
					adapter.clear();
					adapter.addAll(result);
				}
				if (onLoadListener != null) onLoadListener.onLoadCompleted(result);
				currentRequest = null;
			}

		}.execute();		
	}

	public void cancel() {
		if (currentRequest != null) {
			currentRequest.cancel(true);
			currentRequest = null;
		}
	}
	
	public void setOnLoadListener(OnLoadListener<List<Sku>> listener) {
		onLoadListener = listener;
	}

	public void applyMostRavedFilter() {
		if (!mostRaved) {
			mostRaved = true;
			mostNew = false;
			loadUserList();
		}
	}

	public void applyMostNewFilter() {
		if (!mostNew) {
			mostNew = true;
			mostRaved = false;
			loadUserList();
		}
	}

	void doMostRavedFilter(List<Sku> sku) {
		int i = 0;
		while (i < sku.size()) {
			if (sku.get(i).getRaves().size() == 0) {
				sku.remove(i);
			}
			else i++;
		}
		Collections.sort(sku, new Comparator<Sku>() {
	
			@Override
			public int compare(Sku lhs, Sku rhs) {
				if (lhs.getRaves().size() > rhs.getRaves().size()) return -1;
				else if (lhs.getRaves().size() < rhs.getRaves().size()) return 1;
				return 0;
			}
		});
	}

	void doMostNewFilter(List<Sku> sku) {
		Collections.sort(sku, new Comparator<Sku>() {
			
			@Override
			public int compare(Sku lhs, Sku rhs) {
				String lu = lhs.getOpinion().updatedAt();
				String ru = rhs.getOpinion().updatedAt();

				try {
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:s");
					Date ld = (Date) dateFormat.parse(lu);
					Date rd = (Date) dateFormat.parse(ru);
					
					if (ld.after(rd)) return -1;
					else if (ld.before(rd)) return 1;
					
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				return 0;
			}
		});
	}
}
