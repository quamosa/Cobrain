package com.cobrain.android.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class FriendDeleteDialog extends DialogFragment implements OnClickListener {
	public static final String TAG = "FriendDeleteDialog";
	private OnClickListener listener;
	private String friendName;

	public FriendDeleteDialog(String name, DialogInterface.OnClickListener listener) {
		initialize(name, listener);
	}

	public void initialize(String friendName, DialogInterface.OnClickListener listener) {
		this.friendName = friendName;
		this.listener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), getTheme());
		b.setMessage("Are you sure you want to remove " + friendName + " from your friends list?");
		b.setPositiveButton("Yes", this);
		b.setNegativeButton("No", this);
		return b.create();
	}

	@Override
	public void onDestroyView() {
		listener = null;
		friendName = null;
		super.onDestroyView();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		listener.onClick(dialog, which);
		dismiss();
	}
}
