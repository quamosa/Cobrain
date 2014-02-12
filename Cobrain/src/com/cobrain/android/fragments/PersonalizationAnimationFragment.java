package com.cobrain.android.fragments;

import com.cobrain.android.R;
import com.cobrain.android.controllers.AnimationStepper;
import com.cobrain.android.controllers.AnimationStepper.OnAnimationStep;
import com.cobrain.android.loaders.PersonalizationAnimationLoader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PersonalizationAnimationFragment extends BaseCobrainFragment implements OnAnimationStep {
	
	private LinearLayout containerLayout;
	AnimationStepper anim;
	private TextView caption;
	PersonalizationAnimationLoader loader;
	public ImageView logo;
	
	//public PersonalizationAnimationFragment() {super();}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.frg_training_animation, null);
		containerLayout = (LinearLayout) v.findViewById(R.id.category_counts_container);
		caption = (TextView) v.findViewById(R.id.caption);
		logo = (ImageView) v.findViewById(R.id.merchant_logo);
		logo.setVisibility(View.INVISIBLE);
		anim = new AnimationStepper(this);
		anim.setStepCount(5);
		
		v.findViewById(R.id.progress).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				anim.stop();
				anim.start();
			}
		});
		
		loader = new PersonalizationAnimationLoader(this);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		anim.start();
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		loader.dispose();
		loader = null;
		logo = null;
		caption = null;
		containerLayout = null;
		anim = null;
		super.onDestroyView();
	}

	@Override
	public void onAnimationStepStart(AnimationStepper stepper, int step) {
	}
	

	@Override
	public void onAnimationStepEnd(AnimationStepper stepper, int step) {
		switch(step) {
		case 2:
			loader.flipMerchantsEnd(stepper, logo);
			break;
		case 4:
			loader.cravesFoundEnd(stepper);
			break;
		}
	}

	@Override
	public void onAnimationStep(AnimationStepper stepper, int step, int counter) {
		switch(step) {
		case 1: //
			caption.setText("Personalization is starting...");
			stepper.nextStep(5 * 1000);
			break;
		case 2:
			loader.flipMerchants(stepper, step, counter);
			if (stepper.getState() >= 4) {
				caption.setText("Scanning 1,323,432 products...");
			}
			else
				caption.setText("Discovering products at every merchant");
			break;
		case 3:
			stepper.nextStep(1*1000);
			break;
		case 4:
			loader.cravesFound(stepper, step, counter);
			break;
		case 5:
			caption.setText("Ranking your Craves...");
			if (stepper.nextStep(5*1000)) {
				caption.setText("done for now..");
			}
			break;
		}
	}

}