package com.cobrain.android.model;

import java.util.List;

import com.cobrain.android.model.v1.Member;

public class User extends Member {
	String avatar_url;
	List<Badge> badges;

	public List<Badge> getBadges() {
		return badges;
	}

	public String getAvatarUrl() {
		return avatar_url;
	}
}
