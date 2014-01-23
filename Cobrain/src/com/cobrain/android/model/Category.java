package com.cobrain.android.model;

public class Category {
	int id;
	String name;
	int depth;
	int totalProducts;
	int totalProductsOnSale;
	int order;

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getDepth() {
		return depth;
	}
	public int getTotalProducts() {
		return totalProducts;
	}
	public int getTotalProductsOnSale() {
		return totalProductsOnSale;
	}
	public int getOrder() {
		return order;
	}
}
