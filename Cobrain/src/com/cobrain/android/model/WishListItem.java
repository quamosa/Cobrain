package com.cobrain.android.model;

import java.util.ArrayList;

public class WishListItem {
	int position;
	boolean publish;
	String _id;
	String updated_at;
	ArrayList<Rave> raves;
	Product product;
	Boolean is_new;
	
	public int getPosition() {
		return position;
	}
	public boolean isPublic() {
		return publish;
	}
	public String getId() {
		return _id;
	}
	public String getUpdatedAt() {
		return updated_at;
	}
	public ArrayList<Rave> getRaves() {
		return raves;
	}
	public Product getProduct() {
		return product;
	}
	public boolean isNew() {
		if (is_new == null) return false;
		return is_new;
	}
}
