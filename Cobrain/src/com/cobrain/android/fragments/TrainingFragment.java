package com.cobrain.android.fragments;

import java.util.ArrayList;

import com.cobrain.android.R;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.TrainingLoader;
import com.cobrain.android.loaders.TrainingLoader.OnSelectedListener;
import com.cobrain.android.loaders.TrainingLoader.TrainingItem;
import com.cobrain.android.model.v1.TrainingResult;
import com.cobrain.android.model.v1.Training.Experiment;
import com.cobrain.android.model.v1.Training.Member;
import com.cobrain.android.utils.LoaderUtils;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TrainingFragment extends BaseCobrainFragment implements OnLoadListener<TrainingResult>, OnSelectedListener {
	public static final String TAG = "TrainingFragment";
	private static final String SELECT_ANY = "SELECT ANY";

	TrainingLoader trainingLoader = new TrainingLoader();
	TextView cravesFound;
	TextView question;
	Button save;
	boolean skipTraining;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.training_frame, null);
		loaderUtils.initialize((ViewGroup)v);
		trainingLoader.initialize(controller);
		trainingLoader.setOnSelectedListener(this);
		
		question = (TextView) v.findViewById(R.id.training_question);
		cravesFound = (TextView) v.findViewById(R.id.craves_found_info);
		save = (Button) v.findViewById(R.id.training_save_button);
		save.setOnClickListener(this);
		
		String craves = getResources().getString(R.string.craves_found, 0);
		cravesFound.setText(Html.fromHtml(craves));

		LoaderUtils.hide(question, false, false);
		//LoaderUtils.hide(cravesFound, false, false);
		
		setTitle("Teach My Cobrain");
		
		addTrainingItem(v, R.id.training_image_1);
		addTrainingItem(v, R.id.training_image_2);
		addTrainingItem(v, R.id.training_image_3);
		addTrainingItem(v, R.id.training_image_4);

		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		update(false);
		super.onActivityCreated(savedInstanceState);
	}

	public void update(boolean refreshTrainings) {
		controller.getCobrain().checkLogin();
		loadTrainings(refreshTrainings);
	}

	void addTrainingItem(View v, int id) {
		trainingLoader.addTrainingItem(v, id);
	}

	void loadTrainings(boolean refresh) {
		trainingLoader.loadTraining(refresh, this);
	}
	
	@Override
	public void onDestroyView() {
		save = null;
		cravesFound = null;
		question = null;
		trainingLoader.dispose();

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch(v.getId()) {
		case R.id.training_skip_button:
			trainingLoader.skipChoices(new OnLoadListener<Boolean>() {

				@Override
				public void onLoadStarted() {
					loaderUtils.showLoading(null);
				}

				@Override
				public void onLoadCompleted(Boolean r) {
					if (r != null && r == true) {
						//we skipped our choices load new ones now!
						loaderUtils.dismissLoading();
						update(true);
					}
				}
				
			});
			break;
			
		case R.id.training_save_button:
			trainingLoader.saveChoices(new OnLoadListener<Boolean>() {

				@Override
				public void onLoadStarted() {
					loaderUtils.showLoading(null);
				}

				@Override
				public void onLoadCompleted(Boolean r) {
					//if (r != null && r == true) {
						//we ASSUME we saved our choices load new ones now!
						loaderUtils.dismissLoading();
						update(true);
					//}
				}
				
			});
			break;
		}
	}

	@Override
	public void onLoadStarted() {
		//loaderUtils.showLoading("Loading your training items...");
	}

	@Override
	public void onLoadCompleted(TrainingResult tr) {
		if (tr == null) {
			trainingLoader.clear();
			loaderUtils.showEmpty("We had a problem loading your training choices. Click here to try loading them again.");
			loaderUtils.setOnClickListener(new OnClickListener () {
				public void onClick(View v) {
					loaderUtils.dismissEmpty();
					update(false);
				}
			});
		}
		else {
			Experiment e = tr.getTraining().getExperiment();
			Member m = tr.getTraining().getMember();
			Integer score = m.getScore();
			
			if (score == null) score = 0;

			loaderUtils.dismiss();
			trainingLoader.multiSelect = e.getActionType().equals(SELECT_ANY);
			question.setText(e.getQuestionText());
			
			String craves = getResources().getString(R.string.craves_found, score);
			cravesFound.setText(Html.fromHtml(craves));
			loaderUtils.show(question);
			loaderUtils.show(cravesFound);
		}
	}

	@Override
	public void onSelected(View v, boolean selected) {
		ArrayList<TrainingItem> items = trainingLoader.getTrainingItems();
		boolean isSelected = false;
		
		for (TrainingItem item : items) {
			if (item.isSelected()) {
				isSelected = true;
				break;
			}
		}
		
		if (isSelected) {
			save.setText("NEXT");
		}
		else {
			save.setText("NONE OF THESE");
		}
	}

}
