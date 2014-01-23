package com.cobrain.android.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import com.cobrain.android.R;
import com.cobrain.android.fragments.SavedAndShareFragment;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.model.WishListItem;
import com.cobrain.android.model.Product;
import com.cobrain.android.model.Rave;
import com.cobrain.android.utils.LoaderUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SavedAndShareAdapter extends ArrayAdapter<WishListItem> {
	private static final int VIEWTYPE_SAVED_AND_SHARE = 0;
	private static final int VIEWTYPE_SAVED_AND_SHARE_WITH_RAVES = 1;
	private final String RAVE_INFO = "<font color='#9ec5e7'>%s</font> RAVED THIS";
	private final String RAVE_INFO_WITH_FRIENDS = "<font color='#9ec5e7'>%s</font> AND <font color='#9ec5e7'>%s FRIEND%s</font> RAVED THIS";
	
	ArrayList<WishListItem> items = new ArrayList<WishListItem>();
	LoaderUtils loader = new LoaderUtils();
	SavedAndShareFragment parent;

	public SavedAndShareAdapter(Context context, int resource, SavedAndShareFragment parent) {
		super(context, resource);
		setParent(parent);
	}

	public SavedAndShareAdapter(Context context, int resource,
			List<WishListItem> items, SavedAndShareFragment parent) {
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
	
	@Override
	public WishListItem getItem(int position) {
		if (items.size() > position) {
			return items.get(position);
		}
		return super.getItem(position);
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).getProduct().getId();
	}

	@Override
	public int getItemViewType(int position) {
		return (items.get(position).getRaves().size() > 0) ? VIEWTYPE_SAVED_AND_SHARE_WITH_RAVES :
			VIEWTYPE_SAVED_AND_SHARE;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public void notifyDataSetChanged() {
		//do sorting here I suppose
		doSort();
		super.notifyDataSetChanged();
	}
	
	static Comparator<WishListItem> comparer =
			new Comparator<WishListItem>() {
				@Override
				public int compare(WishListItem a, WishListItem b) {
					return Integer.compare( a.getPosition() , b.getPosition() );
				}
			};
	
	void doSort() {
		items.clear();

		for (int i = 0; i < getCount(); i++) {
			items.add(super.getItem(i));
		}

		Collections.sort(items, comparer);
	}

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

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.share_layout:
				parent.shareRecommendation(items.get(position), true, shareListener);
				break;
			case R.id.private_layout:
				parent.shareRecommendation(items.get(position), false, privateListener);
				break;
			case R.id.remove_layout:
				parent.saveRecommendation(items.get(position), false, removeListener);
				break;
			case R.id.raves_layout:
				parent.showRavesUserList(items.get(position).getId());
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
					shareLayout.setVisibility(View.GONE);
					privateLayout.setVisibility(View.VISIBLE);
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
					shareLayout.setVisibility(View.VISIBLE);
					privateLayout.setVisibility(View.GONE);
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
			vh.image = (ImageView) v.findViewById(R.id.item_image);
			vh.info = (TextView) v.findViewById(R.id.item_info);
			vh.merchant = (TextView) v.findViewById(R.id.item_retailer);
			vh.price = (TextView) v.findViewById(R.id.item_price);
			vh.rave = (TextView) v.findViewById(R.id.rave_info);
			vh.shareLayout = v.findViewById(R.id.share_layout);
			vh.privateLayout = v.findViewById(R.id.private_layout);
			vh.removeLayout = v.findViewById(R.id.remove_layout);
			vh.shareLayout.setOnClickListener(vh);
			vh.privateLayout.setOnClickListener(vh);
			vh.removeLayout.setOnClickListener(vh);
			v.setTag(vh);
		}
		else vh = (ViewHolder) v.getTag();

		//showProgress(true);
		
		WishListItem item = getItem(position);
		Product p = item.getProduct();
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
		
		if (item.isPublic()) {
			vh.shareLayout.setVisibility(View.GONE);
			vh.privateLayout.setVisibility(View.VISIBLE);
		}
		else {
			vh.shareLayout.setVisibility(View.VISIBLE);
			vh.privateLayout.setVisibility(View.GONE);
		}

		if (vtyp == VIEWTYPE_SAVED_AND_SHARE_WITH_RAVES) {
			CharSequence raveInfo = getRaveInfo(position);
			vh.rave.setText(raveInfo);
		}
		
		if (p.getImageURL() != null) {
			ImageLoader.load(p.getImageURL(), vh.image, new OnImageLoadListener() {
				@Override
				public void onLoad(String url, ImageView view, Bitmap b, boolean fromCache) {
					//showProgress(false);
					if (fromCache) {
						view.setVisibility(View.VISIBLE);
					}
					else loader.show(view);
				}
	
				@Override
				public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
					return b;
				}
			});
		}
		else vh.image.setVisibility(View.INVISIBLE);

		return v;
	}

	private CharSequence getRaveInfo(int position) {
		String raveInfo;
		ArrayList<Rave> raves = items.get(position).getRaves();
		String name = raves.get(0).getUser().getName().toUpperCase(Locale.US);
		int otherRaves = raves.size() - 1;
		
		if (otherRaves > 0) {
			raveInfo = String.format(RAVE_INFO_WITH_FRIENDS, name, otherRaves, (otherRaves > 1) ? "S" : "");
		}
		else
			raveInfo = String.format(RAVE_INFO, name);

		return Html.fromHtml(raveInfo);
	}

	public void remove(int position) {
		setNotifyOnChange(false);
		this.remove(items.get(position));
		setNotifyOnChange(true);
	}

}
