package com.cobrain.anroid.dialogs;

import com.cobrain.android.R;
import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import com.cobrain.android.model.User;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FriendAcceptDialog extends DialogFragment implements OnClickListener {
	public static final String TAG = "FriendAcceptDialog";
	private OnClickListener listener;
	private User user;

	public FriendAcceptDialog(User user, DialogInterface.OnClickListener listener) {
		initialize(user, listener);
	}

	public void initialize(User user, DialogInterface.OnClickListener listener) {
		this.user = user;
		this.listener = listener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder b = new AlertDialog.Builder(getActivity(), getTheme());
		View v = View.inflate(b.getContext(), R.layout.dlg_friend_accept, null);

		View.OnClickListener onclick = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FriendAcceptDialog d = FriendAcceptDialog.this;
				
				switch(v.getId()) {
				case R.id.accept_user:
					d.onClick(d.getDialog(), DialogInterface.BUTTON_POSITIVE);
					break;
				case R.id.dismiss_user:
					d.onClick(d.getDialog(), DialogInterface.BUTTON_NEGATIVE);
				}
			}
		};
		
		TextView tv = (TextView) v.findViewById(R.id.user_name);
		tv.setText(getString(R.string.friend_accept_caption, user.getName()));
		
		tv = (TextView) v.findViewById(R.id.accept_user);
		tv.setOnClickListener(onclick);
		
		tv = (TextView) v.findViewById(R.id.dismiss_user);
		tv.setOnClickListener(onclick);
		
		ImageView avatar = (ImageView) v.findViewById(R.id.user_avatar);
		ImageLoader.get.load(user.getAvatarUrl(), avatar, new OnImageLoadListener() {
			
			@Override
			public void onLoad(String url, ImageView view, Bitmap b, int fromCache) {
			}
			
			@Override
			public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
				return b;
			}
		});
		
		b.setView(v);
		return b.create();
	}

	@Override
	public void onDestroyView() {
		listener = null;
		user = null;
		super.onDestroyView();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		listener.onClick(dialog, which);
		dismiss();
	}
}
