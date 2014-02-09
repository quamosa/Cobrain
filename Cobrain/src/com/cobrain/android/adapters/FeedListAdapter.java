package com.cobrain.android.adapters;

import java.util.List;
import com.cobrain.android.R;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.Feed;
import com.cobrain.android.utils.LoaderUtils;
import com.cobrain.anroid.dialogs.FriendDeleteDialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedListAdapter extends ArrayAdapter<Feed> {
	LoaderUtils loader = new LoaderUtils();
	FriendsListFragment parent;
	List<Feed> items;
	FriendDeleteDialog dialog;
	
	/*public FriendsListAdapter(Context context, int resource, TuneMenuFragment parent) {
		super(context, resource);
		setParent(parent);
	}*/

	ColorDrawable color = new ColorDrawable();

	OnImageLoadListener listener = new OnImageLoadListener() {

		@Override
		public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
			return b;
		}

		@Override
		public void onLoad(String url, ImageView view, Bitmap b,
				boolean fromCache) {
			LoaderUtils.show(view, !fromCache);
		}
		
	};

	public FeedListAdapter(Context context,
			List<Feed> items, FriendsListFragment parent) {
		super(context, 0, items);
		this.items = items;
		setParent(parent);
		color.setColor(context.getResources().getColor(R.color.FriendsColor));
	}
	
	public void setParent(FriendsListFragment parent) {
		this.parent = parent;
	}

	private class ViewHolder {
		int position;
		TextView feed;
		ImageView avatar;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder vh;

		if (v == null) {
			v = View.inflate(parent.getContext(), R.layout.list_item_feed, null);

			vh = new ViewHolder();
			vh.feed = (TextView) v.findViewById(R.id.feed_entry);
			vh.avatar = (ImageView) v.findViewById(R.id.friend_avatar);

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
		
		vh.avatar.setImageDrawable(color);
		ImageLoader.load(feed.getUser().getAvatarUrl(), vh.avatar, listener) ;
		vh.feed.setText(Html.fromHtml(message));
		vh.position = position;
		
		return v;
	}

}