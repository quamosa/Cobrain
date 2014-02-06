package com.cobrain.android.model.v1;

import java.util.ArrayList;

public class WishList {
	String _id;
	Member owner;
	ArrayList<WishListItem> items;
	int updates;
	boolean accepted;
	Subscription subscription;
	
	public String getId() {
		return _id;
	}
	public Member getOwner() {
		return owner;
	}

	public ArrayList<WishListItem> getItems() {
		return items;
	}

	public int getUpdates() {
		return updates;
	}
	
	public boolean wasAccepted() {
		return accepted;
	}
	
	public Subscription getSubscription() {
		return subscription;
	}	
}
