package com.cobrain.android.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cobrain.android.R;
import com.cobrain.android.controllers.Cobrain;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.Opinion;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.LoaderUtils;

public class TrainingLoader {

	private static final int CRAVES_NEEDED = 5;
	private CobrainController controller;
	private AsyncTask<?, ?, ?> currentRequest;
	public ArrayList<TrainingItem> trainingItems = new ArrayList<TrainingItem>();
	public boolean multiSelect = true;
	OnSelectedListener selectedListener;
	int cravesCompleted;
	int currentSelects;
	public boolean inLoading;
	
	public interface OnSelectedListener {
		void onSelected(View v, int selected);
	}
	
	public class TrainingItem implements OnClickListener, OnImageLoadListener {
		ImageView image;
		ImageView checkboxLiked;
		ImageView checkboxDisliked;
		View progress;
		int selected;
		int id;
		View parent;
		HashMap<Integer, View> viewCache = new HashMap<Integer, View>();
		Opinion opinion;
		
		public void add(View v, int id) {
			v = v.findViewById(id);
			parent = v;

			image = (ImageView) v.findViewById(R.id.training_image);
			
			checkboxLiked = (ImageView) v.findViewById(R.id.training_checkbox_liked_icon);
			checkboxDisliked = (ImageView) v.findViewById(R.id.training_checkbox_disliked_icon);
			checkboxLiked.setOnClickListener(this);
			checkboxDisliked.setOnClickListener(this);

			progress = v.findViewById(R.id.training_progress);

			setText(R.id.training_description, null);
			setText(R.id.training_dollars, null);
			setText(R.id.training_cents, null);

			showProgress(true);
		}
		
		void showProgress(boolean show) {
			if (show) {
				image.setVisibility(View.GONE);
				progress.setVisibility(View.GONE);
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
			ImageLoader.get.load(url, image, this);
		}
		
		public void setSelected(int selected) {
			if (this.selected != selected) {
				if (!multiSelect)
					for (TrainingItem ti : trainingItems)
						if (ti != this) ti.setSelected(0);
				this.selected = selected;
				//checkbox.setVisibility((selected) ? View.VISIBLE : View.GONE);
				switch(selected) {
				case 0:
					checkboxLiked.setImageResource(R.drawable.ic_training_checkbox_liked);
					checkboxDisliked.setImageResource(R.drawable.ic_training_checkbox_disliked);
					break;
				case 1:
					checkboxLiked.setImageResource(R.drawable.ic_training_checkbox_liked_selected);
					checkboxDisliked.setImageResource(R.drawable.ic_training_checkbox_disliked);
					break;
				case 2:
					checkboxLiked.setImageResource(R.drawable.ic_training_checkbox_liked);
					checkboxDisliked.setImageResource(R.drawable.ic_training_checkbox_disliked_selected);
					break;
				}
				if (!inLoading) onSelected(image, selected);
			}
		}

		/*public boolean isSelected() {
			return selected;
		}*/
		
		public void dispose() {
			ImageLoader.get.cancel(image);
			opinion = null;
			viewCache.clear();
			image = null;
			checkboxLiked.setOnClickListener(null);
			checkboxDisliked.setOnClickListener(null);
			checkboxLiked = null;
			checkboxDisliked = null;
			parent = null;
		}

		@Override
		public void onClick(View v) {
			int sel = selected;
			
			switch (v.getId()) {
			case R.id.training_checkbox_liked_icon:
				if (sel == 1) sel = 0;
				else sel = 1;
				break;
			case R.id.training_checkbox_disliked_icon:
				if (sel == 2) sel = 0;
				else sel = 2;
				break;
			}
			setSelected(sel); //toggle it
		}

		@Override
		public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
			return b;
		}

		@Override
		public void onLoad(String url, ImageView view, Bitmap b, int fromCache) {
			if (image != null) showProgress(false);
		}

	}

	void onSelected(View v, int selected) {
		ArrayList<TrainingItem> items = getTrainingItems();

		int selects = 0;
		
		for (TrainingItem item : items) {
			if (item.selected == 1) {
				selects++;
			}
		}
		currentSelects = selects;
		
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
			ti.setSelected(0);
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
				if (controller == null) return false;
				
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				String signal;
				
				if (u != null) {
					for (TrainingItem ti : trainingItems) {
						switch(ti.selected){
						case 1: signal = "liked"; break;
						case 2: signal = "disliked"; break;
						default: signal = "null";
						}
						if (ti.opinion != null) u.saveOpinion(ti.opinion, signal);
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

	public void initialize(CobrainController controller) {
		this.controller = controller;
	}
	
	public void addTrainingItem(View v, int id) {
		TrainingItem ti = new TrainingItem();
		ti.add(v, id);
		trainingItems.add(ti);
	}

	public int getCravesRemaining() {
		int remaining = CRAVES_NEEDED - getCravesLiked();
		if (remaining < 0) remaining = 0;
		return remaining;
	}
	public int getCravesLiked() {
		return cravesCompleted + currentSelects;
	}

	
	public void loadTraining(final boolean refresh, final OnLoadListener<Skus> listener) {
		if (listener != null) listener.onLoadStarted();
		
		currentRequest = new AsyncTask<Void, Void, Skus>() {
			@Override
			protected Skus doInBackground(Void... params) {
				Cobrain c = controller.getCobrain();
				UserInfo u = c.getUserInfo();
				Skus tr = null;
				
				if (u != null && controller.getShown() != null) {
					boolean silent = controller.getShown().getSilentMode();
					controller.getShown().setSilentMode(true);
					tr = u.getSkus(u, "training", null, null, 4, 1 /*(!refresh) ? 1 : 2*/);
					Skus liked = u.getSkus(u, "liked", null, null);
					if (liked != null) {
						cravesCompleted = liked.get().size();
						currentSelects = 0;
					}
					controller.getShown().setSilentMode(silent);
				}
				
				return tr;
			}

			@Override
			protected void onPostExecute(Skus result) {
				if (listener != null) listener.onLoadCompleted(result);
				if (result != null) {
					int i = 0;
					inLoading = true;
					for (Sku p : result.get()) {
						if (isCancelled()) return;
						String url = p.getImageURL();
						TrainingItem ti = trainingItems.get(i++);
						ti.opinion = p.getOpinion();
						ti.id = p.getId();
						if (ti.opinion.is("liked")) 							
							ti.setSelected(1);
						else if (ti.opinion.is("disliked")) 
							ti.setSelected(2);
						else ti.setSelected(0);
						ti.setImageUrl(url);
						String[] price = p.getPriceLabel().split("\\.", 2);
						ti.setText(R.id.training_description, p.getName().toUpperCase(Locale.US));
						ti.setText(R.id.training_dollars, price[0] + ".");
						ti.setText(R.id.training_cents, price[1]);
					}
					inLoading = false;
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
