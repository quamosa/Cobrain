package com.cobrain.android.model;

import java.util.List;

public class Friendships {
	List<Friendship> friendships;

	public List<Friendship> get() {
		return getFriendships();
	}
	public List<Friendship> getFriendships() {
		return friendships;
	}
}
