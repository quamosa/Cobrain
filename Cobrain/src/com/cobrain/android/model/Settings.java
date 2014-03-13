package com.cobrain.android.model;

import java.util.Calendar;
import java.util.Locale;

public class Settings {
	boolean tastemaker_campaign;
	String sms_invitation_message;
	Calendar updated = Calendar.getInstance(Locale.US);
	
	public boolean isTastemakerCampaignEnabled() {
		return tastemaker_campaign;
	}
	public String getSmsInvitationMessage() {
		return sms_invitation_message;
	}
}
