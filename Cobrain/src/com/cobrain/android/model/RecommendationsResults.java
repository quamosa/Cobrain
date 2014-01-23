package com.cobrain.android.model;

import java.util.List;

public class RecommendationsResults {
	List<Product> products;
    int page;
    int perPage;
    int count;
    int total;	
    
    public List<Product> getProducts() {
    	return products;
    }
    
    public int getPage() {
    	return page;
    }
    
    public int getPerPage() {
    	return perPage;
    }
    
    public int getCount() {
    	return count;
    }
    
    public int getTotal() {
    	return total;
    }
}
