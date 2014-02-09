package com.cobrain.android.adapters;

import java.util.List;
import com.cobrain.android.R;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
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
	
	boolean editMode;

	ColorDrawable color = new ColorDrawable();

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
			vh.friend = (TextView) v.findViewById(R.id.friend_name);
			vh.delete = (ImageView) v.findViewById(R.id.friend_delete);
			vh.delete.setOnClickListener(vh);
			vh.updates = (TextView) v.findViewById(R.id.friend_updates);
			vh.updatesLayout = (View) vh.updates.getParent();

			v.setTag(vh);
		}
		else vh = (ViewHolder) v.getTag();
		
		Friendship friend = getItem(position);
		int updates = 0 ;//list.getUpdates();

		vh.avatar.setImageDrawable(color);
		ImageLoader.load(friend.getUser().getAvatarUrl(), vh.avatar, listener);

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
				boolean fromCache) {
			LoaderUtils.show(view, !fromCache);
		}
		
	};
	
	public void remove(int position) {
		setNotifyOnChange(false);
		this.remove(items.get(position));
		setNotifyOnChange(true);
	}

}