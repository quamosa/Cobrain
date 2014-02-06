package com.cobrain.android.model;

import java.util.List;

public class Scenario {
	int id;
	String url;
	int order;
	String whyText;
	List<Sku> skus;
	
	public int getId() {
		return id;
	}
	public String getUrl() {
		return url;
	}
	public int getOrder() {
		return order;
	}
	public String getWhyText() {
		return whyText;
	}
	public List<Sku> getSkus() {
		return skus;
	}
	
	
}
