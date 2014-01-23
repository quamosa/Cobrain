package com.cobrain.android.model;

public class Invitation {
	private String _id;
	private Member inviter;
	private String token;
	private String updated_at;
	private String created_at;
	private String expires_at;
	private Redemptions redemptions;
	private String link;
	private boolean active;

	public String getLink() {
		return link;
	}
}
