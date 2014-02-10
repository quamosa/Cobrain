package com.cobrain.android.adapters;

import java.util.List;
import com.cobrain.android.R;
import com.cobrain.android.fragments.RaveUserListFragment;
import com.cobrain.android.model.Rave;
import com.cobrain.android.utils.LoaderUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RaveUserListAdapter extends ArrayAdapter<Rave> {
	LoaderUtils loader = new LoaderUtils();
	RaveUserListFragment parent;
	List<Rave> items;
	
	boolean editMode;

	/*public FriendsListAdapter(Context context, int resource, TuneMenuFragment parent) {
		super(context, resource);
		setParent(parent);
	}*/

	public RaveUserListAdapter(Context context, int resource,
			List<Rave> items, RaveUserListFragment parent) {
		super(context, resource, items);
		this.items = items;
		setParent(parent);
	}

	public void dispose() {
		clear();
		items = null;
		parent = null;
	}
	
	public void toggleEditMode() {
		editMode = !editMode;
		notifyDataSetChanged();
	}
	
	public void setParent(RaveUserListFragment parent) {
		this.parent = parent;
	}

	private class ViewHolder implements OnClickListener, DialogInterface.OnClickListener {
		int position;
		ImageView delete;
		TextView friend;
		TextView updates;
		public View updatesLayout;

		@Override
		public void onClick(View v) {
			/*
			String name = friend.getText().toString();
			FriendDeleteDialog dialog = new FriendDeleteDialog(name, this);
			dialog.show(parent.getFragmentManager(), FriendDeleteDialog.TAG);
			*/
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			/*if (which == DialogInterface.BUTTON_POSITIVE) {
				parent.removeFriend(position);
			}*/
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder vh;

		if (v == null) {
			v = View.inflate(parent.getContext(), R.layout.list_item_rave, null);

			vh = new ViewHolder();
			vh.friend = (TextView) v.findViewById(R.id.friend_name);
			//vh.delete = (ImageView) v.findViewById(R.id.friend_delete);
			//vh.delete.setOnClickListener(vh);
			//vh.updates = (TextView) v.findViewById(R.id.friend_updates);
			//vh.updatesLayout = (View) vh.updates.getParent();

			v.setTag(vh);
		}
		else vh = (ViewHolder) v.getTag();
		
		Rave rave = getItem(position);
		//int updates = list.getUpdates();

		vh.friend.setText(rave.getUser().getName());
		//vh.position = position;
		//vh.delete.setVisibility(editMode ? View.VISIBLE : View.INVISIBLE);
		//vh.updatesLayout.setVisibility((updates > 0) ? View.VISIBLE : View.INVISIBLE);
		//if (updates > 0) vh.updates.setText(String.valueOf(updates));
		
		return v;
	}

	public void remove(int position) {
		setNotifyOnChange(false);
		this.remove(items.get(position));
		setNotifyOnChange(true);
	}

	@Override
	public void notifyDataSetChanged() {
		int count = getCount();
		parent.setTitle(count + " Rave" + ((count != 1) ? "s" : ""));
		super.notifyDataSetChanged();
	}

}