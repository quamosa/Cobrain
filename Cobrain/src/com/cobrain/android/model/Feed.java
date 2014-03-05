package com.cobrain.android.model;

import java.util.List;

public class Feed {
	String _id;
	String type;
	String timestamp;
	User user;
	List<Integer> skus;
	
	public String getId() {
		return _id;
	}
	public String getType() {
		return type;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public User getUser() {
		return user;
	}
	public List<Integer> getSkuIds() {
		return skus;
	}
	public boolean isType(String type) {
		if (this.type != null) {
			return this.type.equals(type);
		}
		return false;
	}
}
