package com.cobrain.android.model;

import java.util.List;

public class Feed {
	int _id;
	String type;
	int timestamp;
	User user;
	List<Sku> skus;
	
	public int getId() {
		return _id;
	}
	public String getType() {
		return type;
	}
	public int getTimestamp() {
		return timestamp;
	}
	public User getUser() {
		return user;
	}
	public List<Sku> getSkus() {
		return skus;
	}
}
