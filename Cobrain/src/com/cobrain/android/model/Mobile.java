package com.cobrain.android.model;

public class Mobile {
	public final static String APP_UPDATED = "app_updated";
	public final static String CRAVES_ON_SALE = "craves_on_sale";
	public final static String TASTEMAKER_PROGRES = "tastemaker_progress";
	public final static String RAVED = "raved";
	public final static String FRIENDSHIP_ACCEPTED = "friendship_accepted";
	
	boolean app_updated;
	boolean craves_on_sale;
	boolean tastemaker_progress;
	boolean raved;
	boolean friendship_accepted;
	
	public boolean isAppUpdated() {
		return app_updated;
	}
	public boolean isCravesOnSale() {
		return craves_on_sale;
	}
	public boolean isTastemakerProgress() {
		return tastemaker_progress;
	}
	public boolean isRaved() {
		return raved;
	}
	public boolean isFriendshipAccepted() {
		return friendship_accepted;
	}
	public boolean get(String code) {
		if (code != null) {
			if (code.equals(APP_UPDATED)) return app_updated;
			if (code.equals(CRAVES_ON_SALE)) return craves_on_sale;
			if (code.equals(TASTEMAKER_PROGRES)) return tastemaker_progress;
			if (code.equals(RAVED)) return raved;
			if (code.equals(FRIENDSHIP_ACCEPTED)) return friendship_accepted;
		}
		return false;
	}
	public void set(String code, boolean enabled) {
		if (code != null) {
			if (code.equals(APP_UPDATED)) app_updated = enabled;
			if (code.equals(CRAVES_ON_SALE)) craves_on_sale = enabled;
			if (code.equals(TASTEMAKER_PROGRES)) tastemaker_progress = enabled;
			if (code.equals(RAVED)) raved = enabled;
			if (code.equals(FRIENDSHIP_ACCEPTED)) friendship_accepted = enabled;
		}
	}
}
