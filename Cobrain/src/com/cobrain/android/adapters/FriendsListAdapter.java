package com.cobrain.android.adapters;

import java.util.List;
import com.cobrain.android.R;
import com.cobrain.android.fragments.FriendsListFragment;
import com.cobrain.android.model.WishList;
import com.cobrain.android.utils.LoaderUtils;
import com.cobrain.anroid.dialogs.FriendDeleteDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendsListAdapter extends ArrayAdapter<WishList> implements DialogInterface.OnClickListener {
	LoaderUtils loader = new LoaderUtils();
	FriendsListFragment parent;
	List<WishList> items;
	FriendDeleteDialog dialog;
	
	boolean editMode;

	/*public FriendsListAdapter(Context context, int resource, TuneMenuFragment parent) {
		super(context, resource);
		setParent(parent);
	}*/

	public FriendsListAdapter(Context context, int resource,
			List<WishList> items, FriendsListFragment parent) {
		super(context, resource, items);
		this.items = items;
		setParent(parent);
	}

	public boolean inEditMode() {
		return editMode;
	}
	
	/**
	for now it only works if the row is visible
	 */
	public void deleteFriend(int position) {
		WishList item = getItem(position);
		String name = item.getOwner().getName();
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
			vh.friend = (TextView) v.findViewById(R.id.friend_name);
			vh.delete = (ImageView) v.findViewById(R.id.friend_delete);
			vh.delete.setOnClickListener(vh);
			vh.updates = (TextView) v.findViewById(R.id.friend_updates);
			vh.updatesLayout = (View) vh.updates.getParent();

			v.setTag(vh);
		}
		else vh = (ViewHolder) v.getTag();
		
		WishList list = getItem(position);
		int updates = list.getUpdates();

		vh.friend.setTypeface(null, (!list.wasAccepted()) ? Typeface.ITALIC : Typeface.NORMAL);
		vh.friend.setText(list.getOwner().getName());
		vh.position = position;
		vh.delete.setVisibility(editMode ? View.VISIBLE : View.INVISIBLE);
		vh.updatesLayout.setVisibility((updates > 0) ? View.VISIBLE : View.INVISIBLE);
		if (updates > 0) vh.updates.setText(String.valueOf(updates));
		
		return v;
	}

	public void remove(int position) {
		setNotifyOnChange(false);
		this.remove(items.get(position));
		setNotifyOnChange(true);
	}

}