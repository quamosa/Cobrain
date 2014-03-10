package com.cobrain.android.model;

/*
 {
    "id": 2434567893,
    "user" : {
      "id": 1234567855,
      "name": "Leonardo",
      "avatar_url": "//gravatar.com/user/5293e37295e73817da000005.png",
    }
    "accepted": true
  }
 */

public class Friendship {
	String _id;
	User user;
	boolean accepted;

	public String getId() {
		return _id;
	}
	public User getUser() {
		return user;
	}
	public boolean isAccepted() {
		return accepted;
	}

}
