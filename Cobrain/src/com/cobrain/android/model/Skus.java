package com.cobrain.android.model;

import java.util.List;

public class Skus {
	List<Sku> skus;
	
	//FIXME: these are mine for now, maybe we can have these added to the api
	User owner;	
	String _id;
	String signal;
	
	public List<Sku> get() {
		return skus;
	}

	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
		if (owner != null) 
			this._id = owner.getId();
		else this._id = null;
	}
	public String getId() {
		return _id;
	}

	public void setSignal(String signal) {
		this.signal = signal;
	}
	public String getSignal() {
		return signal;
	}
}
