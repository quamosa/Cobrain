package com.cobrain.android.model;

public class Badge {
	public final static String TASTEMAKER = "tastemaker";
	public final static String TRENDSETTER = "trendsetter";
	
	String _id;
	String code;
	String name;
	String created_at;
	int priority;
	
	public String getId() {
		return _id;
	}
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	public String getCreatedAt() {
		return created_at;
	}
	public int getPriority() {
		return priority;
	}
	
}
