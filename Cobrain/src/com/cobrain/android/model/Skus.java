package com.cobrain.android.model;

import java.util.List;

public class Skus {
	List<Sku> skus;
	
	//FIXME: these are mine for now, maybe we can have these added to the api
	User owner;	
	String _id;
	
	public List<Sku> get() {
		return skus;
	}

	public User getOwner() {
		return null;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public void setId(String id) {
		this._id = id;
	}

	public String getId() {
		return _id;
	}
}
