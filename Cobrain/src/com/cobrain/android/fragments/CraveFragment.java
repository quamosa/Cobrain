package com.cobrain.android.fragments;

import java.util.List;
import java.util.Locale;

import com.cobrain.android.R;
import com.cobrain.android.controllers.Cobrain.CobrainController;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.model.Rave;
import com.cobrain.android.utils.HelperUtils;
import com.cobrain.android.utils.LoaderUtils;
import com.cobrain.android.views.HttpImageView;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CraveFragment extends Fragment implements OnClickListener, OnTouchListener {

	private Sku recommendation;
	TextView itemRetailer;
	TextView itemDescription;
	TextView itemPrice;
	TextView rankForYouLabel;
	HttpImageView itemImage;
	ProgressBar progress;
	RelativeLayout craveInfoHeader;
	RelativeLayout itemInfoFooter;
	TextView cravePopupLabel;
	BaseCobrainFragment parent;
	Skus results;
	private LinearLayout bottomButtons;
	private ImageButton saveButton;
	private ImageButton shareButton;
	private ImageButton dislikeButton;
	boolean isSaved;
	boolean isPublished;
	private int itemId;
	private boolean isOnSale;
	private Integer salePrice;
	private RelativeLayout saleLayout;
	private TextView itemRegularPrice;
	private TextView itemSalePercent;
	private ImageView itemSaleIcon;
	private Skus wishList;
	private Sku wishListItem;
	private WishListFragment wishListParent;
	//private TextView craveIndexLabel;
	private ImageView raveIcon;
	private TextView raveInfoLabel;
	private Boolean _iRavedThis;
	private boolean isRaved;
	private String raveId;
	private int position;
	private int totalCraves;
	private List<Sku> wishListItems;
	private boolean isDisliked;

	public CraveFragment() {
	}
	
	public CraveFragment(BaseCobrainFragment parent) {
		this.parent = parent;
	}
	
	public CraveFragment(WishListFragment parent) {
		this.wishListParent = parent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	boolean isShowingWishList() {
		return wishListParent != null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v;
		
		if (isShowingWishList()) {
			v = inflater.inflate(R.layout.crave_wishlist_frame, null);
			//craveIndexLabel = (TextView) v.findViewById(R.id.shared_craves_index_label);
			raveIcon = (ImageView) v.findViewById(R.id.item_rave_icon);
			raveInfoLabel = (TextView) v.findViewById(R.id.rave_info);
			raveIcon.setOnClickListener(this);
			raveIcon.setOnTouchListener(this);
			if (wishList != null && wishList.getOwner().getId().equals(wishListParent.controller.getCobrain().getUserInfo().getUserId())) {
				raveIcon.setVisibility(View.INVISIBLE);
			}
		}
		else {
			v = inflater.inflate(R.layout.crave_frame_with_rank_header, null);
		}

		itemRetailer = (TextView) v.findViewById(R.id.item_retailer);
		itemDescription = (TextView) v.findViewById(R.id.item_description);
		itemPrice = (TextView) v.findViewById(R.id.item_price);
		rankForYouLabel = (TextView) v.findViewById(R.id.rank_for_you_label);
		itemImage = (HttpImageView) v.findViewById(R.id.item_image);
		progress = (ProgressBar) v.findViewById(R.id.progress);
		
		craveInfoHeader = (RelativeLayout) v.findViewById(R.id.crave_info_header);
		cravePopupLabel = (TextView) v.findViewById(R.id.crave_popup_label);
		itemInfoFooter = (RelativeLayout) v.findViewById(R.id.item_info_footer);
		bottomButtons = (LinearLayout) v.findViewById(R.id.bottom_buttons);
		saveButton = (ImageButton) v.findViewById(R.id.just_for_me_button);
		shareButton = (ImageButton) v.findViewById(R.id.friends_can_see_button);
		dislikeButton = (ImageButton) v.findViewById(R.id.dislike_button);
		
		saveButton.setOnClickListener(this);
		shareButton.setOnClickListener(this);
		if (dislikeButton != null) dislikeButton.setOnClickListener(this);
		
		HelperUtils.Graphics.setAlpha(craveInfoHeader, 0.5f);
		HelperUtils.Graphics.setAlpha(bottomButtons, 0.5f);

		itemImage.setOnClickListener(this);
		
		saleLayout = (RelativeLayout) v.findViewById(R.id.item_sale_layout);
		itemSaleIcon = (ImageView) v.findViewById(R.id.item_sale_icon);
		itemRegularPrice = (TextView) v.findViewById(R.id.item_sale_regular_price);
		itemRegularPrice.setPaintFlags(itemRegularPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		itemSalePercent = (TextView) v.findViewById(R.id.item_sale_pct);
		
		showCravePopup(false, null);
		showProgress(true, false);
		
		return v;
	}

	private void setAlphaOfViewCompat(View v, float alpha) {
		AlphaAnimation a = new AlphaAnimation(alpha, alpha);
		a.setFillAfter(true);
		v.startAnimation(a);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		update();
		//if (parent != null)
		//	parent.onCravePageLoaded(position);

		super.onActivityCreated(savedInstanceState);
	}
	
	public void setRecommendation(Skus results, Sku r) {
		this.results = results;
		recommendation = r;
		if (r != null) position = r.getRank();
		else position = 0;
		update();
	}

	public void setWishListItem(Skus results, List<Sku> listItems, Sku item, int position, int total) {
		wishList = results;
		wishListItem = item;
		wishListItems = listItems;
		this.position = position;
		this.totalCraves = total;
		recommendation = (Sku) item;
	}

	void updateSaveAndShareState(boolean animate) {
		itemId = 0;
		isSaved = false;
		isPublished = false;
		isDisliked = false;
		isOnSale = false;
		isRaved = false;

		int price = recommendation.getPrice();
		//recommendation.setSalePrice((int) (price * 0.15));
		salePrice = recommendation.getSalePrice();
		isOnSale = recommendation.isOnSale();

		if (wishListItem != null) {
			itemId = wishListItem.getId();
			isPublished = wishListItem.getOpinion().is("shared");
			isDisliked = wishListItem.getOpinion().is("disliked");
			isRaved = iRavedThis(wishListItem);
		}
		else {
			isSaved = recommendation.getOpinion().is("saved");
			isPublished = recommendation.getOpinion().is("shared");
			isDisliked = recommendation.getOpinion().is("disliked");

			//FIXME: uncomment when we have racks api endpoint available
			/*
			WishList list = getController().getCobrain().getUserInfo().getCachedWishList();
	
			//TODO: make asynchronous
			for (WishListItem item : list.getItems()) {
				if (item.getProduct().getId() == recommendation.getId()) {
					itemId = item.getId();
					isSaved = true;
					isPublished = item.isPublic();
					break;
				}
			}
			*/
		}
		
		updateFooterButtons(animate);
	}
	
	boolean update() {
		if (isVisible()) {
			
			if (recommendation != null) {
				if (recommendation.getMerchant() != null) {
					itemRetailer.setText(recommendation.getMerchant().getName().toUpperCase(Locale.US));
				}
				else itemRetailer.setText(null);
				itemDescription.setText(recommendation.getName());
				itemPrice.setText(recommendation.getPriceFormatted());

				/*
				if (raveIcon != null) raveIcon.setVisibility(View.VISIBLE);
				if (saveButton != null) saveButton.setVisibility(View.VISIBLE);
				if (shareButton != null) shareButton.setVisibility(View.VISIBLE);
				*/

				updateSaveAndShareState(false);
	
				showProgress(true, false);
				
				if (recommendation.getImageURL() != null) {
					itemImage.setImageUrl(recommendation.getImageURL(), new OnImageLoadListener() {
						@Override
						public void onLoad(String url, ImageView view, Bitmap b, int fromCache) {
							showProgress(false, fromCache == ImageLoader.CACHE_NONE);
						}
			
						@Override
						public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
							return b;
						}
					});
				}
				else itemImage.setVisibility(View.INVISIBLE);
			}
			/*else {
				if (raveIcon != null) raveIcon.setVisibility(View.INVISIBLE);
				if (saveButton != null) saveButton.setVisibility(View.INVISIBLE);
				if (shareButton != null) shareButton.setVisibility(View.INVISIBLE);
				if (progress != null) progress.setVisibility(View.INVISIBLE);

<<<<<<< HEAD
			updateSaveAndShareState(false);

			showProgress(true, false);
			
			if (recommendation.getImageURL() != null) {
				ImageLoader.load(recommendation.getImageURL(), itemImage, new OnImageLoadListener() {
					@Override
					public void onLoad(String url, ImageView view, Bitmap b, boolean fromCache) {
						showProgress(false, !fromCache);
					}
		
					@Override
					public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
						return b;
					}
				});
			}
			else itemImage.setVisibility(View.INVISIBLE);
=======
			}*/
			
			return true;
		}
		return false;
	}
	
	private CobrainController getController() {
		if (parent != null) 
			return parent.controller;
		else
			return wishListParent.controller;
	}
	
	private CharSequence getRaveInfoLabel(Sku item) {
		boolean iRaved = iRavedThis(item);
		int raves = item.getRaves().size();
		
		if (raves == 0) return null;
		
		String youAnd = null;
		
		if (iRaved) {
			youAnd = "YOU & ";
			raves--;
		}
		else youAnd = "";
		
		if (raves > 0) {
			final String raveInfo = "%s%d FRIEND%s RAVED THIS";
			return String.format(raveInfo, youAnd, raves, raves > 1 ? "S" : "");
		}
		else {
			if (iRaved) return "YOU RAVED THIS";
		}
		
		return null;
	}

	//TODO: make asynchronous
	private Boolean iRavedThis(Sku item) {
		if (_iRavedThis == null) {
			_iRavedThis = false;
			raveId = null;
			for (Rave r : item.getRaves())
				if (r.getUser().getId().equals(getController().getCobrain().getUserInfo().getUserId())) {
					raveId = r.getId(); 
					_iRavedThis = true;
					break;
				}
		}
		return _iRavedThis;
	}

	void showCravePopup(boolean show, String message) {
		craveInfoHeader.setVisibility((show) ? View.VISIBLE : View.GONE);
		cravePopupLabel.setText(message);
	}
	
	void showProgress(boolean show, boolean animate) {
		if (show) {
			progress.setVisibility(View.VISIBLE);
			itemImage.setVisibility(View.INVISIBLE);
		}
		else {
			progress.setVisibility(View.GONE);
			if (animate) LoaderUtils.show(itemImage);
			else itemImage.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroyView() {
		ImageLoader.get.cancel(itemImage);
		itemRetailer = null;
		itemDescription = null;
		itemPrice = null;
		rankForYouLabel = null;
		saveButton.setOnClickListener(null);
		shareButton.setOnClickListener(null);
		if (dislikeButton != null) dislikeButton.setOnClickListener(null);
		itemImage.setOnClickListener(null);
		itemImage.setImageDrawable(null);
		itemImage = null;
		recommendation = null;
		progress = null;
		cravePopupLabel = null;
		craveInfoHeader = null;
		itemInfoFooter = null;
		bottomButtons = null;
		saveButton = null;
		shareButton = null;
		dislikeButton = null;
		parent = null;
		results = null;
		
		wishList = null;
		wishListItem = null;
		wishListItems = null;
		wishListParent = null;
		//craveIndexLabel = null;
		if (raveIcon != null) {
			raveIcon.setOnClickListener(null);
			raveIcon = null;
		}
		raveInfoLabel = null;

		saleLayout = null;
		itemRegularPrice = null;
		itemSalePercent = null;
		itemSaleIcon = null;

		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.item_image:
			if (parent != null) {
				if (parent instanceof CravesFragment) {
					((CravesFragment)parent).showBrowser(recommendation.getBuyURL(), recommendation.getMerchant().getName());
				}
			}
			else if (wishListParent != null) {
				//wishListParent.showBrowser(recommendation.getBuyURL(), recommendation.getMerchant().getName());
			}
			break;
		case R.id.just_for_me_button: 
			saveRecommendation(recommendation, !isSaved);
			break;
		case R.id.friends_can_see_button: 
			shareRecommendation(recommendation, !isPublished);
			break;
		case R.id.dislike_button: 
			dislikeRecommendation(recommendation, !isDisliked);
			break;
		case R.id.item_rave_icon:
			//rave toggle
			raveItem(!isRaved, raveId);
		}
	}
	
	public void raveItem(final boolean rave, final String raveId) {
		wishListParent.loaderUtils.showLoading((rave) ? "Raving this crave..." : "Removing your rave...");

		raveIcon.setEnabled(false);
		
		new AsyncTask<Object, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Object... params) {
				UserInfo ui = wishListParent.controller.getCobrain().getUserInfo();
				boolean ok = false;
				
				if (rave) {
					ok = ui.raveProduct(wishList.getOwner(), wishListItem);
					refreshWishList();
					if (ok) {
						//isRaved = rave;
						return true;
					}
				}
				else {
					ok = ui.unraveProduct(wishList.getOwner(), wishListItem, raveId);
					refreshWishList();
					if (ok) {
						//isRaved = rave;
						return true;
					}
				}
				
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				wishListParent.loaderUtils.dismissLoading();
				if (raveIcon != null) {
					raveIcon.setEnabled(true);
					//if (result) {
						updateSaveAndShareState(true);
					//}
				}
			}
			
		}.execute();
	}

	void refreshWishList() {
		if (wishListItem != null) {
			int itemId = wishListItem.getId();
			String signal = wishListItem.getOpinion().getSignal();
			wishList = getController().getCobrain().getUserInfo().getSkus(wishList.getOwner(), wishList.getSignal(), null, null);
	
			if (wishList != null) {
				for (Sku item : wishList.get()) {
					if (item.getId() == itemId) {
						wishListItem = item;
						wishListItems.set(position-1, wishListItem);
						_iRavedThis = null;
						break;
					}
				}
			}
		}
	}

	public void saveRecommendation(Sku recommendation, boolean save) {
		parent.loaderUtils.showLoading((save) ? "Saving your crave..." : "Removing your crave...");

		new AsyncTask<Object, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Object... params) {
				
				//FIXME: **** this happened last night not sure why
				if (parent == null) {
					Log.e("SAVE_RECOMMENDATION", "parent is null");
					return false;
				}
				if (parent.controller == null) {
					Log.e("SAVE_RECOMMENDATION", "parent controller is null");
					return false;
				}
				if (parent.controller.getCobrain() == null) {
					Log.e("SAVE_RECOMMENDATION", "parent controller cobrain is null");
					return false;
				}
				// ******

				UserInfo ui = parent.controller.getCobrain().getUserInfo();
				boolean save = (Boolean) params[1];
				Sku recommendation = (Sku) params[0];
				
				if (save) {
					if (ui.addToPrivateRack(recommendation)) {
						return true;
					}
				}
				else {
					if (ui.removeProduct(recommendation)) {
						return true;
					}
				}
				
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				parent.loaderUtils.dismissLoading();
				if (result) updateSaveAndShareState(true);
			}
			
		}.execute(recommendation, save);
	}

	public void shareRecommendation(Sku recommendation, boolean share) {
		parent.loaderUtils.showLoading((share) ? "Sharing your crave..." : "Making your crave private...");

		new AsyncTask<Object, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Object... params) {

				UserInfo ui = parent.controller.getCobrain().getUserInfo();
				Sku recommendation = (Sku)params[0];
				Boolean isShared = (Boolean)params[1];
				
				if (isShared) {
					if (ui.addToSharedRack(recommendation)) {
						return true;
					}
				}
				else {
					if (ui.removeProduct(recommendation)) {
						return true;
					}
				}

				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				//show result of is published change
				parent.loaderUtils.dismissLoading();
				if (result) updateSaveAndShareState(true);
			}
			
		}.execute(recommendation, share);
	}

	public void dislikeRecommendation(Sku recommendation, boolean dislike) {
		parent.loaderUtils.showLoading((dislike) ? "Disliking your crave..." : "Removing your dislike...");

		new AsyncTask<Object, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Object... params) {

				UserInfo ui = parent.controller.getCobrain().getUserInfo();
				Sku recommendation = (Sku)params[0];
				Boolean isDisliked = (Boolean)params[1];
				
				if (isDisliked) {
					if (ui.dislikeProduct(recommendation)) {
						return true;
					}
				}
				else {
					if (ui.removeProduct(recommendation)) {
						return true;
					}
				}

				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				//show result of is published change
				parent.loaderUtils.dismissLoading();
				if (result) updateSaveAndShareState(true);
			}
			
		}.execute(recommendation, dislike);
	}

	void updateFooterButtons(boolean animate) {
		boolean saved = isSaved;
		boolean published = isPublished;
		boolean disliked = isDisliked;

		if (published) {
			saved = false;
		}
		
		int saveId = (!saved) ? R.drawable.crave_save_button : R.drawable.crave_save_button_pressed;
		int shareId = (!published) ? R.drawable.crave_share_button : R.drawable.crave_share_button_pressed;
		int dislikeId = (!disliked) ? R.drawable.crave_dislike_button : R.drawable.crave_dislike_button_pressed;
		Drawable save = getActivity().getResources().getDrawable(saveId);
		Drawable share = getActivity().getResources().getDrawable(shareId);
		Drawable dislike = getActivity().getResources().getDrawable(dislikeId);
		saveButton.setBackground(save);
		shareButton.setBackground(share);
		if (dislikeButton != null) dislikeButton.setBackground(dislike);

		if (isOnSale) {
			if (!isShowingWishList()) {
				itemSaleIcon.setVisibility(View.VISIBLE);
				itemSalePercent.setVisibility(View.VISIBLE);
			}
			itemRegularPrice.setVisibility(View.VISIBLE);
			//saleLayout.setVisibility(View.VISIBLE);
			//itemPrice.setVisibility(View.GONE);
			itemPrice.setTextColor(0xfff4a427);
			itemRegularPrice.setText(recommendation.getPriceFormatted());
			itemPrice.setText(recommendation.getSalePriceFormatted());
			float pct = 1-((float)recommendation.getSalePrice() / recommendation.getPrice());
			CharSequence sPct = Html.fromHtml("<b>" + (int)(pct * 100) + "%</b><br><small>OFF</small>");
			itemSalePercent.setText(sPct);
		}
		else {
			itemSaleIcon.setVisibility(View.GONE);
			itemSalePercent.setVisibility(View.GONE);
			itemRegularPrice.setVisibility(View.INVISIBLE);
			itemPrice.setTextColor(0xff000000);
			//saleLayout.setVisibility(View.GONE);
			//itemPrice.setVisibility(View.VISIBLE);
		}

		if (isShowingWishList()) {
			
			raveIcon.setImageResource(iRavedThis(wishListItem) ? R.drawable.crave_rave_button_pressed : R.drawable.crave_rave_button);

			CharSequence raveInfo = getRaveInfoLabel(wishListItem);
			if (raveInfo != null) {
				raveInfoLabel.setText(raveInfo);
				//raveInfoLabel.setVisibility(View.VISIBLE);
				LoaderUtils.show(raveInfoLabel, animate);
			}
			else
				LoaderUtils.hide(raveInfoLabel, animate, true);
				//raveInfoLabel.setVisibility(View.GONE);
		}
	
	}

	public CharSequence getTitle() {
		String sharedOrSaved = (isPublished) ? "Shared" : "Saved";
		
		CharSequence cs = Html.fromHtml("<b>%1 of %2</b> %3 Craves");
		cs = TextUtils.replace(cs, new String[] {"%1", "%2", "%3"}, new CharSequence[] {String.valueOf(position), String.valueOf(totalCraves), sharedOrSaved });
		return cs;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			raveIcon.setImageResource(!isRaved ? R.drawable.ic_wishlist_raved : R.drawable.ic_wishlist_unraved);
		}
		return false;
	}

}
