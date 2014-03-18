package com.cobrain.android.adapters;

import java.util.List;
import com.cobrain.android.R;
import com.cobrain.android.dialogs.FriendDeleteDialog;
import com.cobrain.android.fragments.BaseCobrainFragment;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.Badge;
import com.cobrain.android.model.Feed;
import com.cobrain.android.utils.LoaderUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedListAdapter extends ArrayAdapter<Feed> {
	private static final int ITEM_LAYOUT_ID = R.layout.list_item_feed;
	LoaderUtils loader = new LoaderUtils();
	FriendsListFragment parent;
	List<Feed> items;
	FriendDeleteDialog dialog;
	
	/*public FriendsListAdapter(Context context, int resource, TuneMenuFragment parent) {
		super(context, resource);
		setParent(parent);
	}*/

	ColorDrawable color = new ColorDrawable();
	private int avatarSize;
	private ImageLoader avatarLoader;

	public int getItemHeight() {
		View item = View.inflate(getContext(), ITEM_LAYOUT_ID, null);
		item.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		item.measure(0, 0);
		return item.getMeasuredHeight();
	}
	
	OnImageLoadListener listener = new OnImageLoadListener() {

		@Override
		public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
			return b;
		}

		@Override
		public void onLoad(String url, ImageView view, Bitmap b,
				int fromCache) {
			LoaderUtils.show(view, fromCache == ImageLoader.CACHE_NONE);
		}
		
	};

	public FeedListAdapter(Context context,
			List<Feed> items, FriendsListFragment parent) {
		super(context, 0, items);
		this.items = items;
		setParent(parent);
		color.setColor(context.getResources().getColor(R.color.FriendsColor));
		avatarSize = (int) context.getResources().getDimension(R.dimen.avatar_feeds_size);
		avatarSize = (int) context.getResources().getDimension(R.dimen.avatar_friends_size);
		avatarLoader = new ImageLoader("feed", (4*avatarSize*avatarSize) * 50);
	}
	
	public void setParent(FriendsListFragment parent) {
		this.parent = parent;
	}

	private class ViewHolder {
		int position;
		TextView feed;
		ImageView avatar;
		ImageView badge;
		int paddingTop;
		int paddingBottom;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder vh;

		if (v == null) {
			v = View.inflate(parent.getContext(), ITEM_LAYOUT_ID, null);

			vh = new ViewHolder();
			vh.feed = (TextView) v.findViewById(R.id.feed_entry);
			vh.avatar = (ImageView) v.findViewById(R.id.friend_avatar);
			vh.badge = (ImageView) v.findViewById(R.id.friend_badge);
			vh.paddingTop = v.getPaddingTop();
			vh.paddingBottom = v.getPaddingBottom();

			v.setTag(vh);
		}
		else vh = (ViewHolder) v.getTag();
		
		Feed feed = getItem(position);

		String ft = feed.getType();
		String message = null;
		
		//TODO: should cache these!
		Context c = getContext();
		int resId = c.getResources().getIdentifier(ft, "string", c.getPackageName());
		if (resId != 0) {
			message = c.getResources().getString(resId, feed.getUser().getName(), color.getColor() & 0xffffff);
		}

		if (feed.getUser().hasBadge(Badge.TASTEMAKER)) {
			v.setPadding(0, 0, 0, 2);
			vh.badge.setImageResource(R.drawable.ic_badge_tastemaker);
			vh.badge.setVisibility(View.VISIBLE);
		}
		else
			if (feed.getUser().hasBadge(Badge.TRENDSETTER)) {
				v.setPadding(0, 0, 0, 2);
				vh.badge.setImageResource(R.drawable.ic_badge_trendsetter);
				vh.badge.setVisibility(View.VISIBLE);
			}
			else {
				v.setPadding(0, vh.paddingTop, 0, vh.paddingBottom);
				vh.badge.setVisibility(View.GONE);
			}

		vh.avatar.setImageDrawable(color);
		avatarLoader.load(feed.getUser().getAvatarUrl(), vh.avatar, avatarSize, avatarSize, listener) ;
		vh.feed.setText(Html.fromHtml(message));
		vh.position = position;
		
		return v;
	}

}