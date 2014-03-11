package com.cobrain.android.fragments;

import com.cobrain.android.R;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.loaders.TrainingLoader;
import com.cobrain.android.loaders.TrainingLoader.OnSelectedListener;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.utils.HelperUtils;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class TrainingFragment extends BaseCobrainFragment implements OnLoadListener<Skus>, OnSelectedListener {
	public static final String TAG = "TrainingFragment";
	private static final String SELECT_ANY = "SELECT ANY";

	TrainingLoader trainingLoader = new TrainingLoader();
	TextView question;
	Button save;
	Button skip;
	boolean saveChoicesThenGoHome;
	AlertDialog dialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_training, null);
		loaderUtils.initialize((ViewGroup)v);
		trainingLoader.initialize(controller);
		trainingLoader.setOnSelectedListener(this);
		
		question = (TextView) v.findViewById(R.id.training_question);
		save = (Button) v.findViewById(R.id.training_save_button);
		save.setOnClickListener(this);
		skip = (Button) v.findViewById(R.id.training_skip_button);
		skip.setOnClickListener(this);
		question.setText("Loading...");
		question.setGravity(Gravity.CENTER);
		
		//LoaderUtils.hide(question, false, false);
		//LoaderUtils.hide(cravesFound, false, false);
		
		setTitle("Teach My Cobrain");
		
		addTrainingItem(v, R.id.training_image_1);
		addTrainingItem(v, R.id.training_image_2);
		addTrainingItem(v, R.id.training_image_3);
		addTrainingItem(v, R.id.training_image_4);

		return v;
	}
	
	@Override
	public int getMenuItemId() {
		return 1;
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

	public void showPersonalizationAnimation() {
		PersonalizationAnimationFragment f = new PersonalizationAnimationFragment();
		getFragmentManager().beginTransaction()
			.replace(R.id.slidingmenumain, f, PersonalizationAnimationFragment.TAG)
			.addToBackStack(null)
			.commitAllowingStateLoss();
	}
	
	void addTrainingItem(View v, int id) {
		trainingLoader.addTrainingItem(v, id);
	}

	void loadTrainings(boolean refresh) {
		trainingLoader.loadTraining(refresh, this);
	}
	
	@Override
	public void onDestroyView() {
		dialog = null;
		trainingLoader.dispose();
		setSubTitle(null);
		skip.setOnClickListener(null);
		save = null;
		question = null;

		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch(v.getId()) {
		case R.id.training_skip_button:
			saveChoicesThenGoHome = true;
			saveChoices();
			break;
			
		case R.id.training_save_button:
			saveChoices();
			break;
		}
	}

	void saveChoices() {
		trainingLoader.saveChoices(new OnLoadListener<Boolean>() {

			@Override
			public void onLoadStarted() {
				loaderUtils.showLoading(null);
			}

			@Override
			public void onLoadCompleted(Boolean r) {
				//if (r != null && r == true) {
					//we ASSUME we saved our choices load new ones now!

					if (saveChoicesThenGoHome || trainingLoader.getCravesRemaining() == 0) {
						new AsyncTask<Void, Void, Void>() {
			
							@Override
							protected Void doInBackground(Void... params) {
								UserInfo ui = controller.getCobrain().getUserInfo();
								if (ui != null) {
									if (!ui.getChecklist().hasInitialTraining()) {
										ui.updateProfile("{\"checklist\": {\"initial_training\": true}}");
									}
								}
								return null;
							}

							@Override
							protected void onPostExecute(Void result) {
								if (saveChoicesThenGoHome) {
									showPersonalizationAnimation();
									save.postDelayed(new Runnable() {
										public void run() {
											controller.showHome(HomeFragment.TAB_HOME_RACK, false);
										}
									}, 5 * 1000); 
								}
							}
							
						}.execute();
					}

					if (!saveChoicesThenGoHome) {
						//loaderUtils.dismissLoading();
						update(true);
					}
				//}
			}
			
		});
	}
	
	@Override
	public void onLoadStarted() {
		//loaderUtils.showLoading("Loading your training items...");
	}

	@Override
	public void onLoadCompleted(Skus tr) {
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

			loaderUtils.dismiss();
			updateUI();
		}
	}

	void updateUI() {
		if (detached) return;
		
		trainingLoader.multiSelect = true;
		String response;
		if (trainingLoader.getCravesRemaining() == 0) {
			response = "Your Cobrain has Craves just for you! Keep teaching or View your Craves";
		}
		else {
			response = "Like " + trainingLoader.getCravesRemaining() + " more apparel " + HelperUtils.Strings.plural(trainingLoader.getCravesRemaining(), "item") + " to make your Cobrain smarter!";
		}
		setSubTitle("You've liked " + trainingLoader.getCravesLiked() + " " + HelperUtils.Strings.plural(trainingLoader.getCravesLiked(), "item"));
		question.setGravity(Gravity.LEFT);
		question.setText(response);
		
		if (trainingLoader.getCravesRemaining() == 0) {
			showTeachingCompleteDialog();
		}
		//String craves = getResources().getString(R.string.craves_found, score);
		//cravesFound.setText(Html.fromHtml(craves));
		//loaderUtils.show(question);
	}
	
	@Override
	public void onSelected(View v, int selected) {
		updateUI();
	}


	void showTeachingCompleteDialog() {
		boolean completed = controller.getCobrain().getSharedPrefs().getBoolean("trainingCompletedShown", false);
		
		if (!completed) {
			OnClickListener click = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					switch(v.getId()) {
					case R.id.keep_teaching:
						dialog.dismiss();
						break;
					case R.id.done:
						dialog.dismiss();
						TrainingFragment.this.onClick(skip);
					}
				}
			};
			
			AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
			View v = View.inflate(getActivity().getApplicationContext(), R.layout.dlg_teaching_complete, null);
			v.findViewById(R.id.keep_teaching).setOnClickListener(click);
			v.findViewById(R.id.done).setOnClickListener(click);
			b.setView(v);
			dialog = b.show();
			controller.getCobrain().getSharedPrefs().edit().putBoolean("trainingCompletedShown", true).commit();
		}
	}

	@Override
	public boolean onBackPressed() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(PersonalizationAnimationFragment.TAG) != null) {
			return fm.findFragmentByTag(HomeFragment.TAG) != null;
		}
		return super.onBackPressed();
	}


}
