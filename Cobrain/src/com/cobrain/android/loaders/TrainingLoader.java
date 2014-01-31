package com.cobrain.android.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cobrain.android.R;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.Product;
import com.cobrain.android.model.Training;
import com.cobrain.android.model.TrainingResult;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.service.Cobrain;
import com.cobrain.android.service.Cobrain.CobrainController;
import com.cobrain.android.utils.LoaderUtils;

public class TrainingLoader {

	private CobrainController controller;
	private AsyncTask currentRequest;
	private Training training;
	public ArrayList<TrainingItem> trainingItems = new ArrayList<TrainingItem>();
	public boolean multiSelect = true;
	OnSelectedListener selectedListener;
	
	public interface OnSelectedListener {
		void onSelected(View v, boolean selected);
	}
	
	public class TrainingItem implements OnClickListener, OnImageLoadListener {
		ImageView image;
		ImageView checkbox;
		ProgressBar progress;
		boolean selected;
		int id;
		View parent;
		HashMap<Integer, View> viewCache = new HashMap<Integer, View>();
		
		public void add(View v, int id) {
			v = v.findViewById(id);
			parent = v;

			image = (ImageView) v.findViewById(R.id.training_image);
			image.setOnClickListener(this);
			
			checkbox = (ImageView) v.findViewById(R.id.training_checkbox_icon);

			progress = (ProgressBar) v.findViewById(R.id.training_progress);

			setText(R.id.training_description, null);
			setText(R.id.training_dollars, null);
			setText(R.id.training_cents, null);

			showProgress(true);
		}
		
		void showProgress(boolean show) {
			if (show) {
				image.setVisibility(View.GONE);
				progress.setVisibility(View.VISIBLE);
			}
			else {
				LoaderUtils.show(image);
				progress.setVisibility(View.GONE);
			}
		}

		public void setText(int id, String text) {
			TextView tv = (TextView) viewCache.get(id);
			if (tv == null) {
				tv = (TextView) parent.findViewById(id);
				viewCache.put(id, tv);
			}
			tv.setText(text);
		}

		public void setImageUrl(String url) {
			showProgress(true);
			ImageLoader.load(url, image, this);
		}
		
		public void setSelected(boolean selected) {
			if (this.selected != selected) {
				if (!multiSelect)
					for (TrainingItem ti : trainingItems)
						if (ti != this) ti.setSelected(false);
				this.selected = selected;
				//checkbox.setVisibility((selected) ? View.VISIBLE : View.GONE);
				checkbox.setImageResource((selected) ? R.drawable.ic_training_checkbox_selected : R.drawable.ic_training_checkbox);
				onSelected(image, selected);
			}
		}

		public boolean isSelected() {
			return selected;
		}
		
		public void dispose() {
			viewCache.clear();
			image.setOnClickListener(null);
			image = null;
			checkbox = null;
			parent = null;
		}

		@Override
		public void onClick(View v) {
			setSelected(!selected); //toggle it
		}

		@Override
		public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
			return b;
		}

		@Override
		public void onLoad(String url, ImageView view, Bitmap b, boolean fromCache) {
			showProgress(false);
		}

	}

	void onSelected(View v, boolean selected) {
		if (selectedListener != null) selectedListener.onSelected(v, selected);
	}
	
	public void setOnSelectedListener(OnSelectedListener listener) {
		selectedListener = listener;
	}

	public ArrayList<TrainingItem> getTrainingItems() {
		return trainingItems;
	}
	
	public void clearSelections() {
		for (TrainingItem ti : trainingItems)
			ti.setSelected(false);
	}

	public void clear() {
		clearSelections();
		for (TrainingItem ti : trainingItems)
			ti.showProgress(false);
	}

	public void saveChoices(final OnLoadListener<Boolean> listener) {
		if (listener != null) listener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				
				if (u != null) {
					ArrayList<Integer> ids = new ArrayList<Integer>();

					for (TrainingItem ti : trainingItems)
						if (ti.selected) ids.add(ti.id);

					if (ids.size() > 0) {
						return u.saveTrainingAnswers(training.getId(), ids);
					}
					return true;
				}
				
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (listener != null) listener.onLoadCompleted(result);
				currentRequest = null;
			}
			
		}.execute();
		
	}

	public void skipChoices(final OnLoadListener<Boolean> listener) {
		if (listener != null) listener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				
				if (u != null)
					return u.skipTraining(training.getId());
				
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (listener != null) listener.onLoadCompleted(result);
				currentRequest = null;
			}
			
		}.execute();
		
	}

	public void initialize(CobrainController controller) {
		this.controller = controller;
	}
	
	public void addTrainingItem(View v, int id) {
		TrainingItem ti = new TrainingItem();
		ti.add(v, id);
		trainingItems.add(ti);
	}

	public void loadTraining(final boolean refresh, final OnLoadListener<TrainingResult> listener) {
		if (listener != null) listener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, TrainingResult>() {
			@Override
			protected TrainingResult doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				TrainingResult tr = null;
				
				if (u != null) {
					tr = u.getTrainings(refresh);
					if (tr != null) {
						training = tr.getTraining();
					}
				}
				
				return tr;
			}

			@Override
			protected void onPostExecute(TrainingResult result) {
				if (listener != null) listener.onLoadCompleted(result);
				if (result != null) {
					int i = 0;
					for (Product p : result.getTraining().getChoices()) {
						String url = p.getImageURL();
						TrainingItem ti = trainingItems.get(i++);
						ti.id = p.getId();
						ti.setSelected(false);
						ti.setImageUrl(url);
						String[] price = p.getPriceLabel().split("\\.", 2);
						ti.setText(R.id.training_description, p.getName().toUpperCase(Locale.US));
						ti.setText(R.id.training_dollars, price[0] + ".");
						ti.setText(R.id.training_cents, price[1]);
					}
				}
				currentRequest = null;
			}
			
		}.execute();
	}

	public void dispose() {
		selectedListener = null;
		
		if (currentRequest != null) {
			currentRequest.cancel(true);
			currentRequest = null;
		}
		while (trainingItems.size() > 0)
			trainingItems.remove(0).dispose();
		
		controller = null;
	}

}