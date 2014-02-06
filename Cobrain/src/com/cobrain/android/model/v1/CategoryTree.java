package com.cobrain.android.model.v1;

import java.util.ArrayList;

import com.cobrain.android.model.Category;

public class CategoryTree extends Category {
	ArrayList<Category> ancestors;
	ArrayList<Category> siblings;
	ArrayList<Category> children;

	public ArrayList<Category> getAncestors() {
		return ancestors;
	}
	public ArrayList<Category> getSiblings() {
		return siblings;
	}
	public ArrayList<Category> getChildren() {
		return children;
	}
}
