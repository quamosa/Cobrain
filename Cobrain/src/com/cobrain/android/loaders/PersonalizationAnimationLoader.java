package com.cobrain.android.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cobrain.android.R;
import com.cobrain.android.controllers.AnimationStepper;
import com.cobrain.android.controllers.AnimationStepper.Timer;
import com.cobrain.android.fragments.PersonalizationAnimationFragment;
import com.cobrain.android.utils.LoaderUtils;

public class PersonalizationAnimationLoader {
	String[] logoPaths;
	ArrayList<Bitmap> logos = new ArrayList<Bitmap>();
	int logoPosition = 0;
	private long lastTime;
	private long lastMerchantFlipTime;
	PersonalizationAnimationFragment parent;
	AccelerateInterpolator acceleration = new AccelerateInterpolator(1);
	ArrayList<View> cravesFoundViews = new ArrayList<View>();
	int categoryCountViewIndex;

	public PersonalizationAnimationLoader(PersonalizationAnimationFragment f) {
		parent = f;
	}

	private void disposeLogos() {
		parent.cancelAsyncTask("cacheLogos");
		for (Bitmap logo : logos)
			logo.recycle();
		logos.clear();
	}
	
	@SuppressWarnings("unchecked")
	private void cacheLogos(final Context c, final AnimationStepper stepper) {
		parent.addAsyncTask( "cacheLogos", new AsyncTask<Object, Void, Void>() {

			@Override
			protected Void doInBackground(Object... params) {
				try {
					String[] logoPaths;

					logoPaths = c.getAssets().list("merchant_logos");
				
					for (String logo : logoPaths) {
						InputStream is = c.getAssets().open("merchant_logos/" + logo);
						Bitmap bmp = BitmapFactory.decodeStream(is);
						is.close();
						logos.add(bmp);
					}
					
					stepper.nextState();

				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return null;
			}
			
		}).execute();
	}
	
	public void flipMerchants(final AnimationStepper stepper, int step, int counter) {
		if (counter == 0) {
			cacheLogos(parent.getActivity(), stepper);
		}
		else if (counter > 50 && stepper.inState(0)) {
			stepper.nextStep();
		}
		
		//start flipping when all logos have been cached
		//every 5 secs increase merchant logo flipping speed
		if (stepper.getState() > 0) {
			if (stepper.inState(1)) {
				logoPosition = 0;

				View pv = parent.getView();
				pv.findViewById(R.id.merchant_logo_layout).setVisibility(View.VISIBLE);
				LinearLayout container = (LinearLayout) pv.findViewById(R.id.category_counts_container);
				container.setVisibility(View.VISIBLE);
				parent.logo.setImageBitmap(logos.get(logoPosition));
				
				LoaderUtils.show(parent.logo);
				lastTime = lastMerchantFlipTime = stepper.getStepTime();
				stepper.nextState();
			}
		
			if (stepper.inState(2)) {
				//wait 3 seconds
				if (stepper.timeHasPassed(lastTime, 2*1000)) {
					lastTime = stepper.getStepTime();
					stepper.nextState();
				}
			}
			if (stepper.getState() > 2) {
				//we will fully accelerate in 15 seconds;
				float pos = (stepper.getStepTime() - lastTime) / (15 * 1000f);
				
				if (pos > 1) {
					if (stepper.inState(3)) {
						lastTime = stepper.getStepTime();
						stepper.nextState();
					}
				}
	
				if (stepper.inState(4)) {
					pos = 1;
					if (stepper.timeHasPassed(lastTime, 5*1000)) {
						LoaderUtils.hide(parent.logo, true, false, new AnimationListener() {
							
							@Override
							public void onAnimationStart(Animation animation) {}
							
							@Override
							public void onAnimationRepeat(Animation animation) {}
							
							@Override
							public void onAnimationEnd(Animation animation) {
								parent.logo.post(new Runnable() {
									public void run() {
										stepper.nextStep();
									}
								});
							}
						});
						stepper.nextState();
					}
				}
	
				parent.logo.setImageBitmap(logos.get(logoPosition));
	
				float speed = 1 - acceleration.getInterpolation(pos);
				int duration = (int) (0.5f*1000 * speed);
				
				if (stepper.timeHasPassed(lastMerchantFlipTime, duration)) {
					lastMerchantFlipTime = stepper.getStepTime();
					if (++logoPosition >= logos.size()) logoPosition = 0;
				}
			}

		}
		
	}

	public void flipMerchantsEnd(AnimationStepper stepper, ImageView logo) {
		disposeLogos();
		logo.setVisibility(View.GONE);
	}
	
	public void cravesFoundEnd(AnimationStepper stepper) {
		while (cravesFoundViews.size() > 0) {
			View v = cravesFoundViews.remove(0);
			ViewGroup vg = (ViewGroup) v.getParent();
			vg.removeView(v);
		}
	}
	
	public void dispose() {
		disposeLogos();
		cravesFoundViews.clear();
		parent = null;
	}

	@SuppressWarnings("unchecked")
	public void cravesFound(final AnimationStepper stepper, int step, int counter) {

		switch(stepper.getState()) {
		case 0:
			categoryCountViewIndex = 0;
			stepper.nextState();
			addCategoryCounts(stepper);
			
			/*
			parent.addAsyncTask("cravesFound", 
				new AsyncTask<Object, Void, CategoryTree>() {

					@Override
					protected CategoryTree doInBackground(Object... params) {
						Cobrain c = parent.controller.getCobrain();
						UserInfo u = c.getUserInfo();
						CategoryTree r = null;
						
						if (u != null) {
							r = u.getCategories(6); //6 for Apparel
						}
						
						return r;
					}

					@Override
					protected void onPostExecute(CategoryTree result) {

						View pv = parent.getView();
						TextView caption = (TextView) pv.findViewById(R.id.caption);
						caption.setText("Searching through millions of products...");

						Context con = parent.getActivity().getApplicationContext();
						LinearLayout container = (LinearLayout) pv.findViewById(R.id.category_counts_container);
						LayoutInflater inflater = parent.getActivity().getLayoutInflater();
						//pv.findViewById(R.id.merchant_logo_layout).setVisibility(View.GONE);

						DecimalFormat formatter = new DecimalFormat("#,###,###");
						
						for (Category c : result.getChildren()) {
							View v = inflater.inflate(R.layout.inc_category_counts_layout, container, false);
							TextView tv = (TextView) v.findViewById(R.id.category_count);
							tv.setText( formatter.format( c.getTotalProducts() ) );
							tv = (TextView) v.findViewById(R.id.category_name);
							tv.setText( c.getName() );
							v.setVisibility(View.GONE);
							cravesFoundViews.add(v);
							container.addView(v);
						}
						
						stepper.nextState();
					}
				}
			).execute();
			*/
			
			break;
		case 1:
			break;
		case 2:

			Timer timer = stepper.timer("cravesFound.2");
			
			if (timer.expired(3*1000)) {
				if (categoryCountViewIndex < cravesFoundViews.size()) {
					timer.reset();
					View v = cravesFoundViews.get(categoryCountViewIndex++);
					LoaderUtils.show(v);
				}
				else {
					stepper.nextState();
				}
			}
			
			break;
			
		case 3:

			View v = (View) cravesFoundViews.get(0).getParent();
			LoaderUtils.hide(v, new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					stepper.nextState();
				}
			});

			stepper.nextState();
			break;

		case 4:
			break;
		case 5:
			stepper.nextStep();
		}
	}
	
	void addCategoryCounts(AnimationStepper stepper) {
		View pv = parent.getView();
		TextView caption = (TextView) pv.findViewById(R.id.caption);
		caption.setText("Searching through millions of products...");

		Context con = parent.getActivity().getApplicationContext();
		LinearLayout container = (LinearLayout) pv.findViewById(R.id.category_counts_container);
		LayoutInflater inflater = parent.getActivity().getLayoutInflater();
		pv.findViewById(R.id.merchant_logo_layout).setVisibility(View.GONE);

		DecimalFormat formatter = new DecimalFormat("#,###,###");

		Resources res = con.getResources();
		TypedArray items = res.obtainTypedArray(R.array.sku_categories);

		for (int i = 0; i < items.length();) {
			if (items.getInt(i + 2, 0) != 0) {
				int total = (int) ((Math.random() * (200 - 40)) + 40);
				View v = inflater.inflate(R.layout.inc_category_counts_layout, container, false);
				TextView tv = (TextView) v.findViewById(R.id.category_count);
				tv.setText( formatter.format( total ) );
				tv = (TextView) v.findViewById(R.id.category_name);
				tv.setText( items.getString(i + 0) );
				v.setVisibility(View.GONE);
				cravesFoundViews.add(v);
				container.addView(v);
			}
			i += 3;
		}

		stepper.nextState();
		
	}

}
