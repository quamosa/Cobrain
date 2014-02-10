package com.cobrain.android.model;

import java.util.List;

public class Rave {
	String status;
	List<String> reasons;
	String updated_at;

	String _id;
	User user;
	
	public String getId() {
		return _id;
	}
	public User getUser() {
		return user;
	}
}
