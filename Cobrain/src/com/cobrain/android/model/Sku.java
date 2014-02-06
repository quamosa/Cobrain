package com.cobrain.android.model;

import java.util.List;

import com.cobrain.android.model.v1.Product;
import com.cobrain.android.model.Rave;

public class Sku extends Product {
	String description;
	String gender;
	String age;
	Category category;
	boolean active;
	Opinion opinion;
	Rave rave;
	List<Rave> raves;
	
	public String getDescription() {
		return description;
	}
	public String getGender() {
		return gender;
	}
	public String getAge() {
		return age;
	}
	public Category getCategory() {
		return category;
	}
	public boolean isActive() {
		return active;
	}
	public Opinion getOpinion() {
		return opinion;
	}
	public Rave getRave() {
		return rave;
	}
	public List<Rave> getRaves() {
		return raves;
	}
	
}
