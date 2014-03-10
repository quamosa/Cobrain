package com.cobrain.android.fragments;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.cobrain.android.R;
import com.cobrain.android.adapters.FeedListAdapter;
import com.cobrain.android.adapters.FriendsListAdapter;
import com.cobrain.android.loaders.ContactLoader;
import com.cobrain.android.loaders.ContactLoader.ContactInfo;
import com.cobrain.android.loaders.FeedLoader;
import com.cobrain.android.loaders.FriendsListLoader;
import com.cobrain.android.loaders.OnLoadListener;
import com.cobrain.android.model.Feed;
import com.cobrain.android.model.Feeds;
import com.cobrain.android.model.Friendship;
import com.cobrain.android.model.Friendships;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.service.web.WebRequest;
import com.cobrain.android.service.web.WebRequest.OnResponseListener;
import com.cobrain.android.utils.HelperUtils.Storage.TempStore;
import com.cobrain.android.utils.HelperUtils.Timing;
import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FriendsListFragment extends BaseCobrainFragment implements OnItemClickListener, OnLoadListener<Friendships>, OnScrollListener {
	public static final String TAG = FriendsListFragment.class.toString();
	private SwipeListView friends;
	private ListView feeds;
	private FriendsListAdapter adapter;
	private FeedListAdapter feedsAdapter;
	FriendsListLoader loader = new FriendsListLoader();
	FeedLoader feedLoader = new FeedLoader();
	private ImageView edit;
	ProgressBar progress;
	View invite;
	Button verify;
	ContactLoader contacts = new ContactLoader();
	Timing.Timer timer = new Timing.Timer();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.frg_friends_list_fragment, null);
		edit = (ImageView) v.findViewById(R.id.friend_edit);
		friends = (SwipeListView) v.findViewById(R.id.friends_list);
		progress = (ProgressBar) v.findViewById(R.id.friends_list_progress);
		feeds = (ListView) v.findViewById(R.id.feeds_list);
		verify = (Button) v.findViewById(R.id.verify_invite_button);
		verify.setOnClickListener(this);
		adapter = new FriendsListAdapter(container.getContext(), R.id.friend_name, loader.getItems(), this);
		friends.setAdapter(adapter);
		
		feeds.setOnScrollListener(this);
		friends.setOnScrollListener(this);
		
		TextView tv = (TextView) v.findViewById(R.id.friends_list_empty);
		tv.setText("Invite friends to see their Shared Craves and Rave about the ones you think are best for them. The more you interact with your friends, the smarter your Cobrain becomes!");
		friends.setEmptyView(tv);
		
		loaderUtils.initialize((ViewGroup) v);
		loader.initialize(controller, adapter);
		loader.setOnLoadListener(this);

		contacts.setContext(getActivity().getApplicationContext());
		
		edit.setOnClickListener(this);

		invite = v.findViewById(R.id.invite_button_layout);
		invite.setOnClickListener(this);
		
		//v.findViewById(R.id.my_saved_craves).setOnClickListener(this);
		//v.findViewById(R.id.my_shared_craves).setOnClickListener(this);
		
        friends.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_NONE);
        friends.setSwipeActionRight(SwipeListView.SWIPE_ACTION_NONE);
        friends.setSwipeOpenOnLongPress(false);
 
        friends.setOnItemClickListener(this);
        friends.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override 
            public void onOpened(int position, boolean toRight) {
            }

            @Override
            public void onClosed(int position, boolean fromRight) {
            }

            @Override
            public void onListChanged() {
            }

            @Override
            public void onMove(int position, float x) {
            }

            @Override
            public void onStartOpen(int position, int action, boolean right) {
                //int x = getResources().getDimensionPixelSize(R.dimen.saved_and_shared_item_height);
                //int wx = saves.getWidth();
                //saves.setOffsetLeft(wx-x);
                //saves.setOffsetRight(wx-x);
            }

            @Override
            public void onStartClose(int position, boolean right) {
            }

            @Override
            public void onClickFrontView(int position) {
            }

            @Override
            public void onClickBackView(int position) {
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    adapter.remove(position);
                }
                adapter.notifyDataSetChanged();
            }

        });

        setupMyFeedsList(feeds);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public void update() {
		if (controller != null && controller.getCobrain().getUserInfo() != null) {
			verify.setVisibility((!controller.getCobrain().getUserInfo().areInvitesVerified()) ? View.VISIBLE : View.GONE);
			loader.loadFriendList();
			feedLoader.loadFeedList();
		}
	}
	
	void setupMyFeedsList(final ListView menu) {
		ArrayList<Feed> menuItems = new ArrayList<Feed>();
		feedsAdapter = new FeedListAdapter(getActivity().getApplicationContext(), menuItems, this);
		feedLoader.initialize(controller, feedsAdapter);
		feedLoader.setOnLoadListener(new OnLoadListener<Feeds>() {

			@Override
			public void onLoadStarted() {
			}

			int getFeedHeight(float items) {
				final int itemHeight = feedLoader.itemHeight;
				float height = (itemHeight * items) + (feeds.getDividerHeight() * (items - 1));
				return (int) height;
			}
			
			@Override
			public void onLoadCompleted(Feeds r) {
				final float MIN_ITEMS = 3f;
				final float ITEM_OVERFLOW = 0.25f;

				float items = (r == null) ? 0 : r.getFeeds().size();
				
				if (items <= MIN_ITEMS) items = MIN_ITEMS;
				else items = MIN_ITEMS + ITEM_OVERFLOW;

				int height = getFeedHeight(items);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) menu.getLayoutParams();
				params.height = height;
	            menu.setLayoutParams(params);
			}

		});
		
		menu.setAdapter(feedsAdapter);
		menu.setOnItemClickListener(this);
	}
	
	@Override
	public void onDestroyView() {
		timer.dispose();
		feedLoader.dispose();
		loader.dispose();
		verify.setOnClickListener(null);
		verify = null;
		contacts.dispose();
		invite.setOnClickListener(null);
		invite = null;
		edit.setOnClickListener(null);
		edit = null;
		progress = null;
		feeds.setAdapter(null);
		feeds = null;
		feedsAdapter.clear();
		feedsAdapter = null;
		adapter.clear();
		adapter = null;
		//FIXME: friends.setAdapter(null);
		friends = null;
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.friend_edit:
			adapter.toggleEditMode();
			break;
		case R.id.verify_invite_button:
			verifyInvites();
			break;
		case R.id.invite_button_layout:
			showContactList();
		}
	}

	void verifyInvites() {
		verify.setEnabled(false);
		verify.setPressed(true);
		
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				controller.getCobrain().getUserInfo().validateInvitation();
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				verify.setEnabled(true);
				verify.setPressed(false);
				update();
			}
			
		}.execute();
	}
	
	public void showContactList() {
		//controller.showContactList(this);
		contacts.showContactPicker(this);
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		ContactInfo contact = contacts.processActivityResult(reqCode, resultCode, data);
		if (contact != null) generateInvitationSMS(contact);
	}

	
	//@Override
	//public void onContactSelected(ContactInfo contact) {
	//	generateInvitationSMS(contact);
	//}
	
	void generateInvitationSMS(final ContactInfo contact) {
		final int SEND_SMS = 1;
		final int SEND_EMAIL = 2;

		int send = 0;
		
		if (contact.number != null) send = SEND_SMS;
		//else if (contact.email != null) send = SEND_EMAIL;

		switch(send) {
		case SEND_SMS:

			String phone = contact.number;

			phone = phone.replaceAll("[^0-9]", "");
			if (phone.getBytes()[0] == '1') {
				phone = "+" + phone;
			}
			else phone = "+1" + phone;
			
			byte[] hash = getHash(phone);
			String hashedPhone = bin2hex(hash);

			final String link = getString(R.string.url_invite,
					getString(R.string.url_cobrain_app),
					hashedPhone, controller.getCobrain().getUserInfo().getUserId());
			
			String encodedLink = link;
			
			try {
				encodedLink = URLEncoder.encode(encodedLink, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			String bitly = String.format("https://api-ssl.bit.ly/v3/shorten?access_token=%s&longUrl=%s&format=txt", 
					getActivity().getString(R.string.bit_ly_access_token), encodedLink);
			
			//loaderUtils.showLoading("Generating your invite...");
			invite.setPressed(true);
			invite.setEnabled(false);

			new WebRequest().get(bitly)
				.setTimeout(10*1000)
				.setOnResponseListener(new OnResponseListener() {

					String cobrainUrl;
					
					@Override
					public void onResponseInBackground(int responseCode, String response,
							HashMap<String, String> headers) {
						
						if (responseCode == 200) {
							String shortUrl = response;
							//cobrainUrl = shortUrl.replace("http", "cobrain");
							cobrainUrl = shortUrl.replace("http", "https");
							cobrainUrl = cobrainUrl.replaceAll("[\\s\\n]", "");
						}
						else {
							//controller.getCobrain().getUserInfo().reportError("We had a problem creating your text message. Please try again.");
							cobrainUrl = link;
						}
						cobrainUrl = link;
					}
					
					@Override
					public void onResponse(int responseCode, String response,
							HashMap<String, String> headers) {

						//loaderUtils.dismiss();
						invite.setPressed(false);
						invite.setEnabled(true);
						
						if (cobrainUrl != null) {
							sendSMSInvite(contact, cobrainUrl);
						}
					}
				}).go(true);

			break;

		case SEND_EMAIL:
			invite.setPressed(true);
			invite.setEnabled(false);

			final String emailLink = getString(R.string.url_invite_email,
					getString(R.string.url_cobrain_app),
					controller.getCobrain().getUserInfo().getWishListId());
			String message = String.format("I sent you a Cobrain Crave. Click here to view it: %s", emailLink);
			startEmailIntent(contact.email, "Join me at Cobrain!", message);

			invite.setPressed(false);
			invite.setEnabled(true);

			break;
			
		default:
			reportErrorNonSilently("The contact you selected from your contact list doesn't appear to have a mobile phone number.\n" +
				"Please update your contact's information then try inviting them again.");
		}
	}

	void sendSMSInvite(ContactInfo contact, String url) {
		String subject = getString(R.string.invite_sms_subject);
		String message = getString(R.string.invite_sms_body, url);
		startSMSIntent(contact.number, subject, message);
	}
	
	public byte[] getHash(String password) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		digest.reset();
		return digest.digest(password.getBytes());
	}
	
	static String bin2hex(byte[] data) {
		  return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
		}

	void sendSMS(String toPhoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(toPhoneNumber, null, message, null, null);
	}
	
	void startSMSIntent(String number, String subject, String message) {
	    Intent intent = new Intent(Intent.ACTION_SEND); 

	    //intent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
	    intent.setType("image/png");
        intent.putExtra("address", number);
        intent.putExtra("subject", subject);
        intent.putExtra("sms_body", message);
        
        try {
        	startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
        	reportErrorNonSilently("We weren't able to find a supported text messaging application on your phone. Please try to send your invite again once you get one installed.");
        }
	}

	void reportErrorNonSilently(String message) {
    	TempStore.push("silentMode", silentMode);
    	silentMode = false;
    	controller.getCobrain().getUserInfo().reportError(message);
    	silentMode = (Boolean) TempStore.pull("silentMode");
	}
	
	void startEmailIntent(String email, String subject, String message) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
	            "mailto", email, null));
		
		emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
		emailIntent.putExtra(Intent.EXTRA_TEXT, message);
	
        try {
    		startActivity(Intent.createChooser(emailIntent, "Send email..."));
        }
        catch (ActivityNotFoundException e) {
        	reportErrorNonSilently("We weren't able to find a supported email application on your phone. Please try to send your invite again once you get one installed.");
        }
	}

	public void removeFriend(final int position/*, final OnLoadListener<Boolean> listener*/) {
		//loaderUtils.showLoading("Unsubscribing to your friend's craves...");
		//listener.onLoadStarted();

		new AsyncTask<Object, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Object... params) {

				UserInfo ui = controller.getCobrain().getUserInfo();
				Friendship friend = adapter.getItem(position);
				if (ui.removeFriend(friend.getId())) {
					return true;
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				//loaderUtils.dismiss();
				//listener.onLoadCompleted(result);
				if (result) friends.dismiss(position);
			}
			
		}.execute();
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		final boolean debug = false;
		
		if (((ListView)parent) == friends) {
			Friendship friend = adapter.getItem(position);
	
			if (adapter.inEditMode()) {
				adapter.deleteFriend(position);
				return;
			}
			
			if (!debug && friend.isAccepted()) {
				showWishList(friend.getUser(), null, false);
				controller.setMenuItemSelected((ListView)parent, position, true);
			}
			else {
				showFriendAccept(friend);
			}
		}
		else {
			controller.setMenuItemSelected((ListView)parent, position, true);

			Feed f = feedsAdapter.getItem(position);

			if (f.isType("friend_likes_own")) {
				showWishList(f.getUser(), f.getSkuIds(), false);
			}
			else if (f.isType("friend_raves_mine")) {
				showWishList(controller.getCobrain().getUserInfo(), f.getSkuIds(), false);
			}
			
		}
	}
	
	private void showFriendAccept(final Friendship friend) {
		FriendAcceptDialog dialog = new FriendAcceptDialog(friend.getUser(), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				boolean accept = (which == Dialog.BUTTON_POSITIVE);
				
				new AsyncTask<Boolean, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(Boolean... params) {
						boolean accept = params[0];
						UserInfo ui = controller.getCobrain().getUserInfo();
						String friendId = friend.getId();
						
						if (accept)
							return ui.acceptFriendship(friendId);
						else
							return ui.removeFriend(friendId);
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if (result) update();
					}
					
				}.execute(accept);
			}
		});
		
		dialog.show(getChildFragmentManager(), FriendAcceptDialog.TAG);
	}

	void showWishList(User owner, List<Integer> skuIds, boolean showMyPrivateItems) {
		if (!controller.getCobrain().isMe(owner)) {
			controller.showFriendsSharedRack(owner, skuIds);
		}
		else controller.showWishList(owner, showMyPrivateItems, skuIds, false);
		controller.closeMenu(true);
	}

	@Override
	public void onLoadStarted() {
		//if (!silentMode) progress.setVisibility(View.VISIBLE);
		if (silentMode) {
			friends.setVerticalScrollBarEnabled(false);
			state.save(friends, "friendlist");
		}
	}

	@Override
	public void onLoadCompleted(Friendships r) {
		if (silentMode) {
			friends.post(new Runnable() {
				public void run() {
					state.restore(friends, "friendlist");
					friends.setVerticalScrollBarEnabled(true);
				}
			});
		}
		progress.setVisibility(View.GONE);
	}

	
	@Override
	public void onSlidingMenuOpened() {
		startUpdateTimer(true);
	}

	@Override
	public void onSlidingMenuClosed() {
		startUpdateTimer(false);
	}
	
	void startUpdateTimer(boolean start) {
		if (start) {
			int secs = getResources().getInteger(R.integer.default_friends_list_refresh_seconds);
			timer.start(updater, secs*1000);
		}
		else {
			timer.stop(updater);
		}
	}
	
	Runnable updater = new Runnable() {
		public void run() {
			//setSilentMode( timer.getRunCount(this) > 1 );
			update();
		}
	};

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		feedLoader.pauseLoad(arg1 != SCROLL_STATE_IDLE);
		loader.pauseLoad(arg1 != SCROLL_STATE_IDLE);
	}
	
	
}
