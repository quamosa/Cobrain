package com.cobrain.android.adapters;

import java.util.Comparator;
import java.util.List;
import com.cobrain.android.R;
import com.cobrain.android.fragments.SavedAndShareFragment;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.model.Rave;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.v1.WishListItem;
import com.cobrain.android.utils.LoaderUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SavedAndShareAdapter extends ArrayAdapter<Sku> {
	private static final int VIEWTYPE_SAVED_AND_SHARE = 0;
	private static final int VIEWTYPE_SAVED_AND_SHARE_WITH_RAVES = 1;
	private static final boolean debug = false;
	private final String RAVE_INFO = "<font color='#9ec5e7'>%s</font> RAVED THIS";
	private final String RAVE_INFO_WITH_FRIENDS = "<font color='#9ec5e7'>%s</font> AND <font color='#9ec5e7'>%s FRIEND%s</font> RAVED THIS";
	private boolean flinging = false;
	
	LoaderUtils loader = new LoaderUtils();
	SavedAndShareFragment parent;
	public boolean isShared;

	public SavedAndShareAdapter(Context context, int resource, SavedAndShareFragment parent) {
		super(context, resource);
		setParent(parent);
	}

	public SavedAndShareAdapter(Context context, int resource,
			List<Sku> items, SavedAndShareFragment parent) {
		super(context, resource, items);
		setParent(parent);
	}

	public void setParent(SavedAndShareFragment parent) {
		this.parent = parent;
	}

	public void dispose() {
		clear();
		parent = null;
	}
	
	/*@Override
	public WishListItem getItem(int position) {
		if (items.size() > position) {
			return items.get(position);
		}
		return super.getItem(position);
	}*/
	
	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}
	
	@Override
	public int getItemViewType(int position) {
		//Sku item = getItem(position);
		//int typ = (item.getRaves().size() > 0) ? VIEWTYPE_SAVED_AND_SHARE_WITH_RAVES : VIEWTYPE_SAVED_AND_SHARE;
		//return typ;
		return VIEWTYPE_SAVED_AND_SHARE_WITH_RAVES;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	/*@Override
	public void notifyDataSetChanged() {
		//do sorting here I suppose
		//doSort();
		super.notifyDataSetChanged();
	}*/
	
	static Comparator<WishListItem> comparer =
			new Comparator<WishListItem>() {
				@Override
				public int compare(WishListItem a, WishListItem b) {
					return Integer.compare( a.getPosition() , b.getPosition() );
				}
			};
	
	/*void doSort() {
		items.clear();

		for (int i = 0; i < getCount(); i++) {
			items.add(super.getItem(i));
		}

		Collections.sort(items, comparer);
	}*/

	private class ViewHolder implements OnClickListener {
		int position;
		ImageView image;
		TextView merchant;
		TextView info;
		TextView price;
		TextView rave;
		View shareLayout;
		View privateLayout;
		View removeLayout;
		View ravesLayout;
		TextView salePrice;

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.share_layout:
				parent.shareRecommendation(getItem(position), true, shareListener);
				break;
			case R.id.private_layout:
				parent.shareRecommendation(getItem(position), false, privateListener);
				break;
			case R.id.remove_layout:
				parent.saveRecommendation(getItem(position), false, removeListener);
				break;
			case R.id.raves_layout:
				//parent.showRavesUserList(getItem(position).getId());
			}
		}

		OnLoadListener<Integer> removeListener = new OnLoadListener<Integer>() {

			@Override
			public void onLoadStarted() {
				parent.getLoaderUtils().showLoading("Removing your crave...");
			}

			@Override
			public void onLoadCompleted(Integer r) {
				parent.getLoaderUtils().dismissLoading();
				if (r > 0) {
					//SavedAndShareAdapter.this.remove(items.get(position));
					parent.removeCrave(position);
				}
			}
			
		};

		OnLoadListener<Integer> shareListener = new OnLoadListener<Integer>() {

			@Override
			public void onLoadStarted() {
				parent.getLoaderUtils().showLoading("Sharing your crave...");
			}

			@Override
			public void onLoadCompleted(Integer r) {
				parent.getLoaderUtils().dismissLoading();
				if (r > 0) {
					if (!isShared && r == 1) {
						parent.removeCrave(position);
					}
					else {
						shareLayout.setVisibility(View.GONE);
						privateLayout.setVisibility(View.VISIBLE);
					}
				}
			}
			
		};
		
		OnLoadListener<Integer> privateListener = new OnLoadListener<Integer>() {

			@Override
			public void onLoadStarted() {
				parent.getLoaderUtils().showLoading("Making your crave private...");
			}

			@Override
			public void onLoadCompleted(Integer r) {
				parent.getLoaderUtils().dismissLoading();
				if (r > 0) {
					if (isShared) {
						parent.removeCrave(position);
					}
					else {
						shareLayout.setVisibility(View.VISIBLE);
						privateLayout.setVisibility(View.GONE);
					}
				}
			}
			
		};

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder vh;
		int vtyp = getItemViewType(position);

		if (v == null) {
			vh = new ViewHolder();
			switch(vtyp) {
			case VIEWTYPE_SAVED_AND_SHARE:
				v = View.inflate(parent.getContext(), R.layout.list_item_saved_and_share, null);
				break;
			case VIEWTYPE_SAVED_AND_SHARE_WITH_RAVES:
				v = View.inflate(parent.getContext(), R.layout.list_item_saved_and_share_with_raves, null);
				vh.ravesLayout = v.findViewById(R.id.raves_layout);
				vh.ravesLayout.setOnClickListener(vh);
				break;
			}
			v.setId(vtyp);
			vh.image = (ImageView) v.findViewById(R.id.item_image);
			vh.info = (TextView) v.findViewById(R.id.item_info);
			vh.merchant = (TextView) v.findViewById(R.id.item_retailer);
			vh.price = (TextView) v.findViewById(R.id.item_price);
			vh.salePrice = (TextView) v.findViewById(R.id.item_sale_price);
			vh.rave = (TextView) v.findViewById(R.id.rave_count);
			vh.shareLayout = v.findViewById(R.id.share_layout);
			vh.privateLayout = v.findViewById(R.id.private_layout);
			vh.removeLayout = v.findViewById(R.id.remove_layout);
			vh.shareLayout.setOnClickListener(vh);
			vh.privateLayout.setOnClickListener(vh);
			vh.removeLayout.setOnClickListener(vh);
			v.setTag(vh);

			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			v.setLayoutParams(lp);
			v.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		}
		else vh = (ViewHolder) v.getTag();


		//showProgress(true);
		
		Sku p = getItem(position);
		vh.position = position;
		if (p.getMerchant() != null) {
			vh.merchant.setText(p.getMerchant().getName().toUpperCase());
		}
		else vh.merchant.setText(null);
		CharSequence price = null;
		price = p.getPriceFormatted();
		vh.price.setText(price);
		if (p.getName() != null) {
			vh.info.setText(p.getName().toUpperCase());
		}
		else vh.info.setText(null);
		vh.image.setVisibility(View.INVISIBLE);
		
		if (p.getOpinion().is("shared")) {
			vh.shareLayout.setVisibility(View.GONE);
			vh.privateLayout.setVisibility(View.VISIBLE);
		}
		else {
			vh.shareLayout.setVisibility(View.VISIBLE);
			vh.privateLayout.setVisibility(View.GONE);
		}

		if (vtyp == VIEWTYPE_SAVED_AND_SHARE_WITH_RAVES) {
			if (p.getRaves().size() > 0 || debug) {
				vh.ravesLayout.setVisibility(View.VISIBLE);
				CharSequence raveInfo = getRaveInfo(position);
				vh.rave.setText(raveInfo);
			}
			else vh.ravesLayout.setVisibility(View.GONE);
		}

		if (p.isOnSale()) {
			vh.price.setPaintFlags(vh.price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			vh.salePrice.setVisibility(View.VISIBLE);
			vh.salePrice.setText(p.getSalePriceFormatted());
		}
		else {
			vh.price.setPaintFlags(vh.price.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
			vh.salePrice.setVisibility(View.INVISIBLE);
		}
		
		LoaderUtils.hide(vh.image, false, false);
		if (p.getImageURL() != null) { 
			int w = vh.image.getMeasuredWidth();
			int h = vh.image.getMeasuredHeight();
			ImageLoader.get.load(p.getImageURL(), vh.image, w, h, new OnImageLoadListener() {

				@Override
				public void onLoad(String url, ImageView view, Bitmap b, int fromCache) {
					//showProgress(false);
					LoaderUtils.show(view, fromCache == ImageLoader.CACHE_NONE && !flinging);
				}
	
				@Override
				public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
					return b;
				}
			});
		}

		return v;
	}

	private CharSequence getRaveInfo(int position) {
		String raveInfo;
		List<Rave> raves = getItem(position).getRaves();
		if (debug) {
			raveInfo = String.valueOf(Math.random() * 24);
		}
		else raveInfo = String.valueOf(raves.size());
		return raveInfo;
	}

	public void remove(int position) {
		setNotifyOnChange(false);
		this.remove(getItem(position));
		setNotifyOnChange(true);
	}

}
