package com.cobrain.anroid.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class FriendAcceptDialog extends DialogFragment implements OnClickListener {
	public static final String TAG = "FriendAcceptDialog";
	private OnClickListener listener;
	private String friendName;

	public FriendAcceptDialog(String name, DialogInterface.OnClickListener listener) {
		initialize(name, listener);
	}

	public void initialize(String friendName, DialogInterface.OnClickListener listener) {
		this.friendName = friendName;
		this.listener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), getTheme());
		b.setMessage(friendName + " added you as a friend. Do you want to Accept or Reject the friend request?");
		b.setPositiveButton("Accept", this);
		b.setNegativeButton("Reject", this);
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
