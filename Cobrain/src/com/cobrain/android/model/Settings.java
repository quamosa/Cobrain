package com.cobrain.android.model;

public class Settings {
	boolean tastemaker_campaign;
	String sms_invitation_message;
	
	public boolean isTastemakerCampaignEnabled() {
		return tastemaker_campaign;
	}
	public String getSmsInvitationMessage() {
		return sms_invitation_message;
	}
}
