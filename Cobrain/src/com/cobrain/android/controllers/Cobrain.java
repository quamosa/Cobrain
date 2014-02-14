package com.cobrain.android.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.cobrain.android.R;
import com.cobrain.android.fragments.BaseCobrainFragment;
import com.cobrain.android.fragments.ContactListFragment.ContactSelectedListener;
import com.cobrain.android.model.Sku;
import com.cobrain.android.model.Skus;
import com.cobrain.android.model.User;
import com.cobrain.android.model.UserInfo;
import com.cobrain.android.service.web.ResponseListener;
import com.cobrain.android.service.web.WebRequest;
import com.google.gson.Gson;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class Cobrain {
	final String SHARED_PREFS_COBRAIN = "cobrain";

	private String email;
	private String username;
	private String password;
	private String apiKey;
	private String token;
	private UserInfo userInfo;
	private OnLoggedInListener loggedInListener;
	private Context context;
	
	public Cobrain(Context c) {
		context = c;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public interface OnLoggedInListener {
		void onLoggedIn(UserInfo ui);
		void onLoggedOut(UserInfo ui);
		void onFailure(String message);
		void onAccountCreated(UserInfo userInfo);
	}

	public interface CobrainController {
		public static final int VIEW_HOME = 0;
		public static final int VIEW_TEACH = 1;
		public static final int VIEW_FRIENDS_MENU = 2;
		
		public Cobrain getCobrain();
		public void showDialog(String message);
		public void showProgressDialog(String title, String message);
		public void dismissDialog();
		public void showHome();
		public void showMain(int defaultView);
		public void showSavedAndShare();
		public void showTeachMyCobrain(boolean addToBackStack);
		public void showNerveCenter();
		public ActionBar getSupportActionBar();
		public void closeMenu(boolean animate);
		public SlidingMenu getSlidingMenu();
		public void disableSlidingMenu();
		public void showNavigationMenu();
		public void showFriendsMenu();
		public void showOptionsMenu(boolean show);
		public void showSignup(String signupUrl);
		public void showForgotPassword(String email);
		public void showLogin(String loginUrl);
		void hideSoftKeyBoard();
		public void showWishList(User owner, boolean showMyPrivateItems, List<Integer> skuIds, boolean addToBackStack);
		public void showWishList(Skus list, boolean showMyPrivateItems, boolean addToBackStack);
		public void showErrorDialog(String message);
		public void showBrowser(String url, int containerId, String merchant, boolean addToBackStack);
		public void showContactList(ContactSelectedListener listener);
		public boolean processIntents();
		public CobrainView getShown();
		public void showRavesUserList(String itemId);
		public void setMenuItemSelected(View v, int position, boolean selected);
		public void setSubTitle(CharSequence title);
		public void setTitle(CharSequence title);
		public TextView getSubTitleView();
		public TextView getTitleView();
		public void showDefaultActionBar();
		public void showCraves(CraveStrip<?> strip, Sku sku, int containerId, boolean addToBackStack);
		public void dispatchOnFragmentAttached(BaseCobrainFragment f);
		public void dispatchOnFragmentDetached(BaseCobrainFragment f);
		public void setCurrentCobrainView(CobrainView cv);
		public void showFriendsSharedRack(User owner, List<Integer> skuIds);
	}
	
	public interface CobrainView {
		public void onError(String message);
		//public void onLoggedIn(UserInfo ui);
		//public void onLoggedOut(UserInfo ui);
		//public void on

		public void onSlidingMenuOpened();
		public void onSlidingMenuClosed();
		public void setSilentMode(boolean silent);

		public abstract void setSubTitle(CharSequence title);
		public abstract void setTitle(CharSequence title);

		public abstract CobrainController getCobrainController();

		//public void showFilterMenu(View menuItem);
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}
	
	public void setOnLoggedInListener(OnLoggedInListener listener) {
		loggedInListener = listener;
	}
	
	private String getAuthToken(String response) {
		String token = null;
		final String INPUT = "input name=\"authenticity_token\" type=\"hidden\" value=\"";

		int a = response.indexOf(INPUT);
		if (a >= 0) {
			a += INPUT.length();
			int b = response.indexOf("\"", a);
			if (b >= 0) {
				token = response.substring(a, b);
			}
		}
		
		return token;
	}

	public boolean quickLogout() {
		String url = context.getString(R.string.url_logout_get, context.getString(R.string.url_cobrain_app));
    	return new WebRequest().get(url).go() == 200;
	}
	
	public boolean logout() {
		if (!isLoggedIn()) return false;
		
    	WebRequest wr = new WebRequest() {
			public void onResponse(int responseCode, String response, HashMap<String, String> headers) {
				
				switch (responseCode) {
				case 200:
		    		apiKey = null;
		    		username = null;
		    		onLogout(true);
					break;
				default:
		    		apiKey = null;
		    		username = null;
		    		onLogout(true);
					//FIXME: dont wait for success log out ---> onLogout(false);
				}
				saveApiKey();
			}
    	};

    	String url = context.getString(R.string.url_logout_get, context.getString(R.string.url_cobrain_app));
		wr.get(url).execute();
		return true;
	}

	private boolean checkToken(String response) {
		String token = getAuthToken(response);
		if (token != null) {
			if (this.token != token) {
				this.token = token;
				return true;
			}
		}
		return this.token != null;
	}

	private boolean passwordChanged = false;
	
	public boolean changePassword(String username, final String currentPassword, final String newPassword, final String confirmPassword) {
		quickLogin(null, username, currentPassword, new OnLoggedInListener() {
			
			@Override
			public void onLoggedOut(UserInfo ui) {
			}
			
			@Override
			public void onLoggedIn(UserInfo ui) {
				if (apiKey != null) {
			    	String url = context.getString(R.string.url_password_change_get, context.getString(R.string.url_cobrain_app));
			    	WebRequest wr = new WebRequest().setHeaders(userInfo.getApiKeyHeader()).get(url);
		
			    	if (wr.go() == 200) {
						if (checkToken(wr.getResponse())) {
					    	HashMap<String, String> fields = new HashMap<String, String>();
							fields.put("authenticity_token", token);
							fields.put("_method", "put");
							fields.put("user[current_password]", currentPassword);
							fields.put("user[password]", newPassword);
							fields.put("user[password_confirmation]", confirmPassword);
					    	url = context.getString(R.string.url_password_change_post, context.getString(R.string.url_cobrain_app));
					    	if (wr.post(url).setFormFields(fields).go() == 200) {
					    		passwordChanged = true;
					    	}
						}
					}
				}
			}

	    	/*
	    	 * <div class="notice">You updated your account successfully.</div>
<div class="content">
<h1>Password Changed</h1>
<p class="info-screen">Your password has been changed.</p>
	    	 */

			@Override
			public void onFailure(String message) {
			}
			
			@Override
			public void onAccountCreated(UserInfo userInfo) {
			}
		});
		
		if (passwordChanged) {
			passwordChanged = false;
			return true;
		}
		return false;
	}
	
	public boolean resetPassword(String email) {
    	this.email = email;
    	String url = context.getString(R.string.url_password_reset_get, context.getString(R.string.url_cobrain_app));
    	WebRequest wr = new WebRequest().get(url);

    	if (wr.go() == 200) {
			if (checkToken(wr.getResponse())) {
		    	HashMap<String, String> fields = new HashMap<String, String>();
				fields.put("authenticity_token", token);
				fields.put("user[email]", Cobrain.this.email);
		    	url = context.getString(R.string.url_password_reset_post, context.getString(R.string.url_cobrain_app));
		    	if (wr.post(url).setFormFields(fields).go() == 200) {
		    		return true;
		    	}
			}
		}

    	if (loggedInListener != null) {
    		loggedInListener.onFailure("We had a problem trying to reset your password. Please try again later.");
    	}
    	
		return false;
			
/*		- (BOOL)forgotPassword:(NSString *)email
		{
		    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:[NSURL URLWithString:[NSString stringWithFormat:@"https://cobrain.com/account/password/new"]]];
		    NSHTTPURLResponse *response = nil;
		    NSError *error = nil;
		    NSData *data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
		    NSString *htmlString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
		    
		    NSString *tokenSearchString = @"input name=\"authenticity_token\" type=\"hidden\" value=\"";
		    
		    NSString *tokenFull = [[htmlString componentsSeparatedByString:tokenSearchString] lastObject];
		    NSString *token = [tokenFull substringWithRange:NSMakeRange(0, [tokenFull rangeOfString:@"\"" options:0].location)];
		    
		    
		    [request setHTTPMethod:@"POST"];
		    NSString *postString = [NSString stringWithFormat:@"utf8=%%E2%%9C%%93&authenticity_token=%@&user%%5Bemail%%5D=%@&button=",  encodeToPercentEscapeString(token), encodeToPercentEscapeString(email)];
		    data = [postString dataUsingEncoding:NSUTF8StringEncoding];
		    [request setHTTPBody:data];
		    [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"content-type"];
		    [request setValue:[NSString stringWithFormat:@"%u", [data length]] forHTTPHeaderField:@"Content-Length"];
		    [request setURL:[NSURL URLWithString:[NSString stringWithFormat:@"https://cobrain.com/account/password"]]];
		    response = nil;
		    error = nil;
		    data = [NSURLConnection sendSynchronousRequest:request returningResponse:&response error:&error];
		    if ([[response.URL lastPathComponent] isEqualToString:@"login"]) {
		        return YES;
		    } else {
		        return NO;
		    }
		    
		}
*/		
	}
	
	public boolean isLoggedIn() {
		return apiKey != null && userInfo != null;
	}
	
	public boolean quickLogin(String url, String username, String password, OnLoggedInListener listener) {
    	WebRequest wr;

    	if (url == null) {
    		url = context.getString(R.string.url_login_post, context.getString(R.string.url_cobrain_app));
    		wr = new WebRequest().post(url);
    	}
    	else wr = new WebRequest().get(url);
		if (wr.go() == 200) {
			String token = getAuthToken(wr.getResponse());
			if (token != null) {
				OnLoggedInListener l = loggedInListener;
				loggedInListener = listener;
				this.username = username;
				this.password = password;
				onAuthTokenReceived(token);
				loggedInListener = l;
				return true;
			}
		}
		return false;
	}
	
	public void login(String url, String username, String password) {
		if (url == null && username.equals(this.username) && isLoggedIn()) {
			//we are already logged in don't do anything
			return;
		}

		this.username = username;
		this.password = password;

    	WebRequest wr;

    	if (url == null) {
    		url = context.getString(R.string.url_login_post, context.getString(R.string.url_cobrain_app));
    		wr = new WebRequest().post(url);
    	}
    	else wr = new WebRequest().get(url);
    	
    	wr.setOnResponseListener(
    		new ResponseListener() {
			
			@Override
			public void onResponseInBackground(int responseCode, String response,
				HashMap<String, String> headers) {
				
				switch (responseCode) {
				case 200:
					String token = getAuthToken(response);
					if (token != null) {
						onAuthTokenReceived(token);
					}
					else {
						//login page has changed! uh oh!
					}
					break;
				default:
					onLogin(false);
				}
			}
			
		}
    	).execute();
	}

	public void createAccount(String signupUrl, String email, String password) {
		username = email;
		this.password = password;
		
		final String url = (signupUrl != null) ? signupUrl :
			context.getString(R.string.url_account_post, context.getString(R.string.url_cobrain_app));
		
		WebRequest wr = new WebRequest();
		if (signupUrl != null) {
			wr.get(url);
		}
		else wr.post(url);
		
    	wr.setOnResponseListener(
    		new ResponseListener() {
			
			@Override
			public void onResponseInBackground(int responseCode, String response,
				HashMap<String, String> headers) {
				
				switch (responseCode) {
				case 200:
					String token = getAuthToken(response);
					if (token != null) {
						HashMap<String, String> fields = new HashMap<String, String>();

						try {
							fields.put("authenticity_token", token);
				    		fields.put("user[password]", Cobrain.this.password);
				    		fields.put("button", "");
							fields.put("user[email]", username);
							//fields.put("utf8", "&#x2713;");
						} catch (Exception e) {
							e.printStackTrace();
						}

						String url = context.getString(R.string.url_account_post, context.getString(R.string.url_cobrain_app));
						WebRequest wr = new WebRequest().post(url).setFormFields(fields);
						switch(wr.go()) {
							case 200:
								boolean success = wr.getHeaders().containsKey("api-key");
								if (success) apiKey = wr.getHeaders().get("api-key"); else apiKey = null;
								onAccountCreated(success);
								break;
							default:
								onAccountCreated(false);
						}

					}
					else {
						//login page has changed! uh oh!
					}
					break;
				default:
					onLogin(false);
				}
			}
			
		}
    	).execute();
	}


	///just some important events in the lifetime of the cobrain user
	//these events block, need to be run outside of ui thread
	
	void onAuthTokenReceived(String token) {
		//now that we have our auth token we can log in the user
		
		HashMap<String, String> fields = new HashMap<String, String>();

		try {
			fields.put("authenticity_token", token);
    		fields.put("user[password]", password);
    		fields.put("button", "");
			fields.put("user[email]", username);
			//fields.put("utf8", "&#x2713;");
		} catch (Exception e) {
			e.printStackTrace();
		}

    	String url = context.getString(R.string.url_login_post, context.getString(R.string.url_cobrain_app));
		WebRequest wr = new WebRequest().post(url).setFormFields(fields);
		switch(wr.go()) {
			case 200:
				boolean success = wr.getHeaders().containsKey("api-key");
				if (success) apiKey = wr.getHeaders().get("api-key"); else apiKey = null;
	
				onLogin(success);
	
				break;
			default:
				onLogin(false);
		}
		
		//lets logout after the login so we can clear out our session
		//for changing passwords and any other stuff we need to do repeatedly against the app
		quickLogout();
    	
	}

	void onLogout(boolean success) {
		if (success) {
			if (loggedInListener != null)
				loggedInListener.onLoggedOut(userInfo);
		}
		else {
			if (loggedInListener != null) 
				loggedInListener.onFailure("We had a problem trying to log you out. Please try again.");
		}
	}
	
	void onLogin(boolean success) {
		if (success) {
			signIn(apiKey);
			if (userInfo.apiKey != null) {
				if (userInfo.getName() == null || userInfo.getGenderPreference() == null || userInfo.getZipcode() == null) {
					loggedInListener.onAccountCreated(userInfo);
					return;
				}
			}
			else {
				success = false;
			}
		}
		
		if (loggedInListener != null) {
			if (success) {
				loggedInListener.onLoggedIn(userInfo);
			}
			else {
				loggedInListener.onFailure("We had a problem trying to log you in. Please try again.");
			}
		}
	}
	
	void onAccountCreated(boolean success) {
		if (success) {
			signIn(apiKey);
		}

		if (loggedInListener != null) {
			if (success) {
				loggedInListener.onAccountCreated(userInfo);
			}
			else {
				loggedInListener.onFailure("We had a problem trying to create your account. Please try again.");
			}
		}
	}

	public class BitlyResponse {
		BitlyData data;
	}
	public class BitlyData {
		BitlyExpand expand;
	}
	public class BitlyExpand {
		String long_url;
	}
	
	public void handleOpenURL(String url) {
		url = url.replace("cobrain://", "http://");
		WebRequest wr = new WebRequest().get(url);
		
		if (wr.go() == 200) {
			Uri uri = Uri.parse(url);
			if (uri.getHost().equals("bit.ly")) {
				try {
					String encodedLink = URLEncoder.encode(url, "UTF-8");
					String bitly = String.format(
							"https://api-ssl.bit.ly/v3/expand?access_token=%s&shortUrl=%s",
							context.getString(R.string.bit_ly_access_token),
							encodedLink);

					if (wr.get(bitly).go() == 200) {
						BitlyResponse b = new Gson().fromJson(wr.getResponse(), BitlyResponse.class);
						String longUrl = b.data.expand.long_url;
						String[] parsed = longUrl.split("phone=");
						String queryString = parsed[parsed.length - 1];
						parsed = queryString.split("&inviter=");
						String hashedPhone = parsed[0];
						userInfo.sendHashedPhone(hashedPhone);
					}
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/*
	 * 
	 * 
 
 	
 	(BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url
	 +{
	 +    NSString *urlString = [url absoluteString];
	 +    urlString = [urlString stringByReplacingOccurrencesOfString:@"cobrain://" withString:@"http://"];
	 +    NSHTTPURLResponse *response = nil;
	 +    NSError *error = nil;
	 +    NSString *longURL = nil;
	 +
	 +   
	 +    [NSURLConnection sendSynchronousRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:urlString]] returningResponse:&response error:&error];
	 +    if (!error) {
	 +//        NSString *resultString = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
	 +        if ([[url host] isEqualToString:@"bit.ly"]) {
	 +            NSString *encodedLinkString =  (NSString *)CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes(NULL, (CFStringRef) urlString, NULL, (CFStringRef) @"!*'();:@&=+$,/?%#[]", kCFStringEncodingUTF8));
	 +            NSString *bitly = [NSString stringWithFormat:@"https://api-ssl.bit.ly/v3/expand?access_token=%@&shortUrl=%@", kCBBitlyAccessToken, encodedLinkString];
	 +            NSData *data = [NSURLConnection sendSynchronousRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:bitly]] returningResponse:&response error:&error];
	 +            if (!error) {
	 +                id jsonResponse = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableLeaves error:&error];
	 +                if (!error) {
	 +                    if ([jsonResponse isKindOfClass:[NSDictionary class]]) {
	 +                        NSDictionary *expand = [jsonResponse objectForKey:@"expand"];
	 +                        if ([expand isKindOfClass:[NSDictionary class]]) {
	 +                            longURL = [expand objectForKey:@"long_url"];
	 +                            NSArray *parsedURL = [longURL componentsSeparatedByString:@"phone="];
	 +                            if (parsedURL.count == 2) {
	 +                                NSString *queryString = parsedURL.lastObject;
	 +                                parsedURL = [queryString componentsSeparatedByString:@"&inviter="];
	 +                                if (parsedURL.count == 2) {
	 +                                    NSString *hashedPhone = parsedURL.firstObject;
	 +                                    [[CBAPIAccess sharedCBAPIAccess] sendHashedPhoneNumber:hashedPhone];
	 +                                }
	 +                            }
	 +                            
	 +                        }
	 +                    }
	 +                }
	 +            }
	 +        }
	 +        [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithBool:YES] forKey:kCBUDGoDirectlyToMySharedCraves];
	 +        [self didSignIn:nil];	
	 */
	
	private void signIn(String apiKey) {
		saveApiKey();
		userInfo = new UserInfo(context) {
			public void reportError(String message) {
				//TODO: this needs to be controller.showError
				loggedInListener.onFailure(message);
				
			}
		};
		userInfo.signIn(apiKey);
	}

	public void checkLogin() {
		if (!isLoggedIn()) onLogout(true);
	}

	public void dispose() {
		if (userInfo != null) {
			userInfo.dispose();
			userInfo = null;
		}
		loggedInListener = null;
	}

	public void onResume() {
		
	}
	
	public void onStop() {
	}

	void saveApiKey() {
		String prefix = context.getString(R.string.url_cobrain_api);
		Editor edit = getEditableSharedPrefs();
		edit.putString(prefix + ":apiKey", apiKey);
		edit.commit();
	}
	
	public SharedPreferences getSharedPrefs() {
		SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_COBRAIN, Context.MODE_PRIVATE);
		return prefs;
	}
	
	public Editor getEditableSharedPrefs() {
		return getSharedPrefs().edit();
	}
	
	public boolean restoreLogin(final Runnable runWhenLoggedIn) {
		String prefix = context.getString(R.string.url_cobrain_api);
		apiKey = getSharedPrefs().getString(prefix + ":apiKey", null);
		if (apiKey != null) {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					//onLogin(true);
					signIn(apiKey);
					if (userInfo != null) apiKey = userInfo.apiKey;
					else apiKey = null;
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					runWhenLoggedIn.run();
				}
				
			}.execute();
			return true;
		}
		return false;
	}

	public Context getContext() {
		return context;
	}

	public boolean isMe(User owner) {
		return owner != null && userInfo != null && owner.getId().equals( userInfo.getId() );
	}
}
