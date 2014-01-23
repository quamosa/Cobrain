package com.cobrain.android.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.cobrain.android.R;
import com.cobrain.android.service.web.WebRequest;
import com.cobrain.android.service.web.WebRequest.OnResponseListener;
import com.cobrain.android.utils.HelperUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

public class UserInfo {
	private static final String TAG = "UserInfo";

	public String apiKey; //FIXME: only public for testing
	
	@SerializedName("_id")
	private String userId;

	private String name;
	private String email;

	@SerializedName("gender")
	private String genderPreference;

	public String getGenderPreference() {
		return genderPreference;
	}

	public String getZipcode() {
		return zipcode;
	}

	@SerializedName("zip")
	private String zipcode;
	
	private String previousUserApiKey;
	private boolean firstUse;
	private transient Gson gson = new Gson();
	
	@SerializedName("sign_in_count")
	private int signInCount;

	private Context context;
	
	HashMap<String, WishList> listCache = new HashMap<String, WishList>();

	Invitation invite;
	ArrayList<Invitation> invites;
	private String wishListId;

	public interface OnSignedInListener {
		public void onSignedIn(boolean success);
	}

	public void dispose() {
		listCache.clear();
	}
	
	public UserInfo() {
	}
	
	public UserInfo(Context c) {
		context = c;
	}

	public boolean testBaseUrl(String url) {
		WebRequest wr = new WebRequest();
		return wr.get(url).go() == 200;
	}

	public String getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}
	
	public String getEmail() {
		return email;
	}

	public boolean isNotFirstUse() {
		return firstUse;
	}

	public void reportError(String message) {
	}

	/**
	 * this blocks and should not run on UI Thread
	 */
	boolean fetchUserInfo() {
		String url = context.getString(R.string.url_profile_get, context.getString(R.string.url_cobrain_api));
		WebRequest wr = new WebRequest().get(url).setHeaders(apiKeyHeader());

		if (wr.go() == 200) {
		
			UserInfo ui = gson.fromJson(wr.getResponse(), UserInfo.class);
			UserInfo me = UserInfo.this;
			me.userId = ui.userId;
			me.name = ui.name;
			me.email = ui.email;
			me.genderPreference = ui.genderPreference;
			me.zipcode = ui.zipcode;
			me.signInCount = me.signInCount;
			//notifications?

			return true;
		}
		else reportError("Could not fetch user info");
		
		return false;
	}

	/**
	 * this blocks and should not run on UI Thread
	 */
	void fetchWishList() {
		refreshList(getWishListId());
	}
	void fetchInvitations() {
		if (apiKey != null) {
			String url = context.getString(R.string.url_profile_invitations_get, context.getString(R.string.url_cobrain_api));
			WebRequest wr = new WebRequest().setHeaders(apiKeyHeader()).get(url);
			if (wr.go() == 200) {
				try {
					Type type = new TypeToken<List<Invitation>>(){}.getType();
					invites = new Gson().fromJson(wr.getResponse(), type);
				}
				catch(IllegalStateException e) {
					invites = new ArrayList<Invitation>();
					Invitation i = new Gson().fromJson(wr.getResponse(), Invitation.class);
					invites.add(i);
				}
				if (invites.size() == 0) generateInvitation();
			}
			else reportError("Could not get your invitations.");
		}
	}

	ArrayList<Invitation> getInvitations() {
		return invites;
	}
	
	HashMap<String, String> apiKeyHeader() {
		HashMap<String, String> fields = new HashMap<String, String>();
		fields.put("api-key", apiKey);	
		return fields;
	}

	/**
	 * this blocks and should not run on UI Thread
	 * @param apiKey
	 */
	public void signIn(String apiKey) {
		//isValidApiKey(apiKey, null);
		this.apiKey = apiKey;
		
		if (fetchUserInfo()) {
			fetchInvitations();
			fetchWishList();
		}
		else {
			this.apiKey = null;
		}
	}
	
	public void logout() {
		WebRequest wr = new WebRequest();

		String url = context.getString(R.string.url_logout_get, context.getString(R.string.url_cobrain_app));
		wr.get(url)
		.setHeaders(apiKeyHeader())
		.setOnResponseListener(new OnResponseListener() {
			
			@Override
			public void onResponseInBackground(int responseCode, String response,
					HashMap<String, String> headers) {
			}
			
			@Override
			public void onResponse(int responseCode, String response,
					HashMap<String, String> headers) {

				if (responseCode == 200) {
					
					UserInfo ui = gson.fromJson(response, UserInfo.class);
					UserInfo me = UserInfo.this;
					me.userId = ui.userId;
					me.name = ui.name;
					me.email = ui.email;
					me.genderPreference = ui.genderPreference;
					me.zipcode = ui.zipcode;
					me.signInCount = me.signInCount;
				}
				else reportError("Could not log out user");
			}
			
		}).go(true);
	}
	
	public boolean saveProfile(String name, String zipcode, String gender) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_profile_put, context.getString(R.string.url_cobrain_api));
			WebRequest wr = new WebRequest().put(url).setHeaders(apiKeyHeader()).setContentType("application/json")
					.setBody("{\"name\": \"" + name + "\", \"zip\": \"" + zipcode + "\", \"gender\": \"" + gender + "\"}");

			if (wr.go() == 200) {
				this.name = name;
				this.zipcode = zipcode;
				this.genderPreference = gender;
				String response = wr.getResponse();
				return true;
			}
			else reportError("Could not save your profile. Please try again later.");
		}
		return false;
	}

	public RecommendationsResults getRecommendations(int categoryId, int maxPerPage, int page, boolean onSale) {
		if (apiKey != null) {
			String u = context.getString(R.string.url_recommendations_get, context.getString(R.string.url_cobrain_api));
			String url = String.format("%s?category=%d&perPage=%d&page=%d",
				u, categoryId, maxPerPage, page);

			if (onSale) url += "&onSale=true";
				
			WebRequest wr = new WebRequest().get(url).setHeaders(apiKeyHeader());
			if (wr.go() == 200) {

				RecommendationsResults r = gson.fromJson(wr.getResponse(), RecommendationsResults.class);
				return r;
				
			}
			else reportError("Could not get recommendations");
		}
		return null;
	}

	public TrainingResult getTrainings() {
		if (apiKey == null) return null;
		
		String url = context.getString(R.string.url_trainings_post, context.getString(R.string.url_cobrain_api));
		WebRequest wr = new WebRequest().post(url).setHeaders(apiKeyHeader());
		
		String query = "cached=false";
		wr.setBody(query);
		
		if (wr.go() == 200) {
			TrainingResult tr = new Gson().fromJson(wr.getResponse(), TrainingResult.class);
			return tr;
		}
		else reportError("Could not load training.");
		
		return null;
	}

	public boolean saveTrainingAnswers(int trainingId, ArrayList<Integer> answerIds) {
		if (apiKey == null) return false;
		
		String query = "";
		String id = "id=%d";
		String url = context.getString(R.string.url_trainings_answers_post, context.getString(R.string.url_cobrain_api), trainingId);
		
		for (Integer i : answerIds) {
			if (query.length() > 0) query += "&";
			query += String.format(id, i);
		}
		
		WebRequest wr = new WebRequest().post(url).setHeaders(apiKeyHeader()).setBody(query);
		
		if (wr.go() == 200) {
			TrainingResult tr = new Gson().fromJson(wr.getResponse(), TrainingResult.class);
			return true;
		}
		else reportError("Could not save training answers.");
		
		return false;
	}

	@Deprecated
	public boolean skipTraining(int trainingId) {
		if (apiKey == null) return false;
		
		String url = context.getString(R.string.url_trainings_skip_post, context.getString(R.string.url_cobrain_api), trainingId);
		WebRequest wr = new WebRequest().post(url).setHeaders(apiKeyHeader());
		
		if (wr.go() == 200) {
			return true;
		}
		else reportError("Could not skip training.");
		
		return false;
	}
	
	
	boolean isLoggedIn() {
		return apiKey != null;
	}

	String generateInvitation() {
		if (apiKey != null) {
			String url = context.getString(R.string.url_invitations_post, context.getString(R.string.url_cobrain_api));
			WebRequest wr = new WebRequest().setHeaders(apiKeyHeader()).post(url);
			if (wr.go() == 200) {
				invite = new Gson().fromJson(wr.getResponse(), Invitation.class);
				fetchInvitations();
				return invite.getLink();
			}
			else reportError("Could not generate an invitation.");
		}
		return null;
	}
	
	public boolean forgetMe() {
		if (apiKey == null) return false;
		
		String url = context.getString(R.string.url_profile_delete, context.getString(R.string.url_cobrain_api));
		WebRequest wr = new WebRequest().setHeaders(apiKeyHeader()).delete(url);
		if (wr.go() == 200) {
			return true;
		}
		else reportError("Could not remove your Cobrain profile.");

		return false;
	}

	public String getInviteUrl() {
		if (apiKey != null) {
			if (invites.size() > 0) return invites.get(0).getLink();
		}
		
		return null;
	}

	public WishList getList(String id) {
		return getList(id, false);
	}
	
	public WishList getList(String id, boolean fromCache) {
		if (apiKey != null) {
			
			if (fromCache && listCache.containsKey(id)) return listCache.get(id);
			
			String url = context.getString(R.string.url_list_get, context.getString(R.string.url_cobrain_api), id);
			WebRequest wr = new WebRequest().get(url).setHeaders(apiKeyHeader());
			
			if (wr.go() == 200) {
				WishList ml = new Gson().fromJson(wr.getResponse(), WishList.class);
				listCache.put(ml.getId(), ml);
				return ml;
			}
			else reportError("Could not get list: " + id);
		}
		
		return null;
	}
	
	public String getWishListId() {
		if (wishListId == null) {
			wishListId = getLists().get(0)._id;
		}
		return wishListId;
	}
	
	public WishList getCachedWishList() {
		String id = getWishListId();
		return getList(id, true);
	}

	public WishList getWishList() {
		String id = getWishListId();
		return getList(id, false);
	}

	public WishList getListForUser(String userId) {
		ArrayList<WishList> lists = getLists();
		for (WishList list : lists) {
			if (list.getOwner().getId().equals(userId)) {
				return list;
			}
		}
		return null;
	}

	public ArrayList<WishList> getLists() {
		if (apiKey != null) {
			String url = context.getString(R.string.url_lists_get, context.getString(R.string.url_cobrain_api));
			WebRequest wr = new WebRequest().get(url).setHeaders(apiKeyHeader());

			if (wr.go() == 200) {
				Type type = new TypeToken<List<WishList>>(){}.getType();
				ArrayList<WishList> lr = new Gson().fromJson(wr.getResponse(), type);
				return lr;
			}
			else reportError("Could not get lists.");
		}
		
		return null;
	}
	
	public boolean addToList(String listId, int itemId, boolean isPublic) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_lists_items_post, context.getString(R.string.url_cobrain_api), listId);
			WebRequest wr = new WebRequest().post(url).setHeaders(apiKeyHeader()).setContentType("application/json")
					.setBody("{\"id\": " + itemId + ", \"publish\": " + isPublic + "}");

			if (wr.go() == 200) {
				String response = wr.getResponse();
				refreshList(listId);
				return true;
			}
			else reportError("Could not add an item to your list.");
		}
		return false;
	}

	public boolean removeFromList(String listId, String itemId) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_lists_item_del, context.getString(R.string.url_cobrain_api), listId, itemId);
			WebRequest wr = new WebRequest().delete(url).setHeaders(apiKeyHeader());

			if (wr.go() == 200) {
				String response = wr.getResponse();
				refreshList(listId);
				return true;
			}
			else reportError("Could not remove an item from your list.");
		}
		return false;
	}

	void refreshList(String listId) {
		//refresh our list
		if (listCache.remove(listId) != null);
			getList(listId); 
	}
	
	public boolean publishListItem(String listId, String itemId, boolean publish) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_lists_item_publish_put, context.getString(R.string.url_cobrain_api), listId, itemId);
			WebRequest wr = new WebRequest().put(url).setHeaders(apiKeyHeader()).setContentType("application/json")
					.setBody("{\"publish\": " + publish + "}");

			if (wr.go() == 200) {
				String response = wr.getResponse();
				refreshList(listId);
				return true;
			}
			else reportError("Could not publish an item in your list.");
		}
		return false;
	}
	
	public CategoryTree getCategories(int id) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_category_get, context.getString(R.string.url_cobrain_api), id);
			WebRequest wr = new WebRequest().get(url).setContentType("application/json").setHeaders(apiKeyHeader());

			if (wr.go() == 200) {
				CategoryTree cr = new Gson().fromJson(wr.getResponse(), CategoryTree.class);
				return cr;
			}
			else reportError("Could not get category information.");
		}
		
		return null;
	}

	public boolean subscribe(String listId, String subscriptionId) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_lists_subscribe_put, context.getString(R.string.url_cobrain_api), listId, subscriptionId);
			WebRequest wr = new WebRequest().put(url).setHeaders(apiKeyHeader());

			if (wr.go() == 200) {
				String response = wr.getResponse();
				Log.i(TAG, response);
				refreshList(getWishListId());
				return true;
			}
			else reportError("Could not unsubscribe to a friend's crave list.");
		}
		return false;
	}

	public boolean unsubscribe(String listId, String subscriptionId) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_lists_unsubscribe_delete, context.getString(R.string.url_cobrain_api), listId, subscriptionId);
			WebRequest wr = new WebRequest().delete(url).setHeaders(apiKeyHeader());

			if (wr.go() == 200) {
				String response = wr.getResponse();
				refreshList(getWishListId());
				return true;
			}
			else reportError("Could not unsubscribe to a friend's crave list.");
		}
		return false;
	}

	public boolean raveListItem(String listId, String itemId) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_lists_rave_post, context.getString(R.string.url_cobrain_api), listId, itemId);
			WebRequest wr = new WebRequest().post(url).setHeaders(apiKeyHeader());

			if (wr.go() == 200) {
				String response = wr.getResponse();
				refreshList(listId);
				return true;
			}
			else reportError("Could not rave item.");
		}
		return false;
	}

	public boolean unraveListItem(String listId, String itemId, String raveId) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_lists_rave_delete, context.getString(R.string.url_cobrain_api), listId, itemId, raveId);
			WebRequest wr = new WebRequest().delete(url).setHeaders(apiKeyHeader());

			if (wr.go() == 200) {
				String response = wr.getResponse();
				refreshList(listId);
				return true;
			}
			else reportError("Could not remove your rave.");
		}
		return false;
	}
	
	public boolean sendHashedPhone(String phone) {
		if (apiKey != null) {
			String url = context.getString(R.string.url_hashed_phone_post, context.getString(R.string.url_cobrain_app));
			String body = String.format("{\"phone\":\"%s\",\"user\":\"%s\"}", 
					phone, getUserId());
			WebRequest wr = new WebRequest().post(url).setContentType("application/json").setHeaders(apiKeyHeader())
					.setBody(body);

			if (wr.go() == 200) {
				String response = wr.getResponse();
				if (response.contains("success")) return true;
				else {
					reportError("Had a problem sending your invitation information.");
				}
			}
			else reportError("Could not send your invitation information.");
		}
		return false;
	}

	public void validateInvitation() {
		HelperUtils.SMS.sendSMS(context.getString(R.string.sms_invite_validation_number), userId);
	}
	
/*
 * 	 * 
sendHashedPhoneNumber:(NSString *)hashedPhone
+{
+    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@%@", self.webAppBaseURL, kCBPathWebAppHashedPhone]]];
+    [request setHTTPMethod:@"POST"];
+    [request setValue:self.apiKey forHTTPHeaderField:kCBMAURLHeaderApiKey];
+    [request setValue:kCBMAURLHeaderContentTypeValue forHTTPHeaderField:kCBMAURLHeaderContentTypeKey];
+    NSString *bodyString = [NSString stringWithFormat:@"{\"phone\":\"%@\",\"user\":\"%@\"}", hashedPhone, [CBUserModel userID]];
+    NSData *data = [bodyString dataUsingEncoding:NSUTF8StringEncoding];
+    [request setHTTPBody:data];
+    NSHTTPURLResponse *response = nil;
+    NSError *error = nil;
+    data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
+    if (!error) {
+        NSString *responseString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
+    }
*/

}