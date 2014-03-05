package com.cobrain.android.adapters;

import java.util.List;
import com.cobrain.android.R;
import com.cobrain.android.fragments.BaseCobrainFragment;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.Badge;
import com.cobrain.android.model.Friendship;
import com.cobrain.android.utils.LoaderUtils;
import com.cobrain.anroid.dialogs.FriendDeleteDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsListAdapter extends ArrayAdapter<Friendship> implements DialogInterface.OnClickListener {
	LoaderUtils loader = new LoaderUtils();
	FriendsListFragment parent;
	List<Friendship> items;
	FriendDeleteDialog dialog;

	//4 bytes per pixel * 60 pixels * 60 pixels tall
	public ImageLoader avatarLoader;
	
	boolean editMode;

	ColorDrawable color = new ColorDrawable();
	private int avatarSize;

	/*public FriendsListAdapter(Context context, int resource, TuneMenuFragment parent) {
		super(context, resource);
		setParent(parent);
	}*/

	public FriendsListAdapter(Context context, int resource,
			List<Friendship> items, FriendsListFragment parent) {
		super(context, resource, items);
		this.items = items;
		setParent(parent);
		color.setColor(context.getResources().getColor(R.color.FeedsColor));
		avatarSize = (int) context.getResources().getDimension(R.dimen.avatar_friends_size);
		avatarLoader = new ImageLoader("friend", (4*avatarSize*avatarSize) * 50);
	}

	public boolean inEditMode() {
		return editMode;
	}

	/**
	for now it only works if the row is visible
	 */
	public void deleteFriend(int position) {
		Friendship item = getItem(position);
		String name = item.getUser().getName();
		dialog = new FriendDeleteDialog(name, this);
		Bundle args = new Bundle();
		args.putInt("position", position);
		dialog.setArguments(args);
		dialog.show(parent.getFragmentManager(), FriendDeleteDialog.TAG);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			int position = FriendsListAdapter.this.dialog.getArguments().getInt("position");
			parent.removeFriend(position);
			FriendsListAdapter.this.dialog = null;
		}
	}

	public void toggleEditMode() {
		editMode = !editMode;
		notifyDataSetChanged();
	}
	
	public void setParent(FriendsListFragment parent) {
		this.parent = parent;
	}

	private class ViewHolder implements OnClickListener {
		int position;
		ImageView delete;
		TextView friend;
		TextView updates;
		public View updatesLayout;
		public ImageView avatar;
		public ImageView badge;
		int paddingTop;
		int paddingBottom;

		@Override
		public void onClick(View v) {
			deleteFriend(position);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder vh;

		if (v == null) {
			v = View.inflate(parent.getContext(), R.layout.list_item_friend, null);

			vh = new ViewHolder();
			vh.avatar = (ImageView) v.findViewById(R.id.friend_avatar);
			vh.badge = (ImageView) v.findViewById(R.id.friend_badge);
			vh.friend = (TextView) v.findViewById(R.id.friend_name);
			vh.delete = (ImageView) v.findViewById(R.id.friend_delete);
			vh.delete.setOnClickListener(vh);
			vh.updates = (TextView) v.findViewById(R.id.friend_updates);
			vh.updatesLayout = (View) vh.updates.getParent();
			vh.paddingTop = v.getPaddingTop();
			vh.paddingBottom = v.getPaddingBottom();
			
			v.setTag(vh);
		}
		else vh = (ViewHolder) v.getTag();
		
		Friendship friend = getItem(position);
		int updates = 0 ;//list.getUpdates();

		vh.avatar.setImageDrawable(color);
		avatarLoader.load(friend.getUser().getAvatarUrl(), vh.avatar, avatarSize, avatarSize, listener);

		if (friend.getUser().hasBadge(Badge.TASTEMAKER)) {
			v.setPadding(0, 0, 0, 2);
			vh.badge.setImageResource(R.drawable.ic_badge_tastemaker);
			vh.badge.setVisibility(View.VISIBLE);
		}
		else
			if (friend.getUser().hasBadge(Badge.TRENDSETTER)) {
				v.setPadding(0, 0, 0, 2);
				vh.badge.setImageResource(R.drawable.ic_badge_trendsetter);
				vh.badge.setVisibility(View.VISIBLE);
			}
			else {
				v.setPadding(0, vh.paddingTop, 0, vh.paddingBottom);
				vh.badge.setVisibility(View.GONE);
			}
		
		vh.friend.setTypeface(null, (!friend.isAccepted()) ? Typeface.ITALIC : Typeface.NORMAL);
		vh.friend.setText(friend.getUser().getName());
		vh.position = position;
		vh.delete.setVisibility(editMode ? View.VISIBLE : View.INVISIBLE);
		vh.updatesLayout.setVisibility((updates > 0) ? View.VISIBLE : View.INVISIBLE);
		if (updates > 0) vh.updates.setText(String.valueOf(updates));
		
		return v;
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
	
	public void remove(int position) {
		setNotifyOnChange(false);
		this.remove(items.get(position));
		setNotifyOnChange(true);
	}

}