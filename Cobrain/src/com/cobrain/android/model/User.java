package com.cobrain.android.model;

import com.cobrain.android.model.v1.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class User extends Member {
	public static final String NOTIFICATION_TASTEMAKER = "tastemaker";
	public static final String NOTIFICATION_TASTEMAKER_WIN = "congrats-tastemaker";
	public static final String NOTIFICATION_TRENDSETTER_WIN = "congrats-trendsetter";
	
	String avatar_url;
	List<Badge> badges;
	List<String> notifications;
	Checklist checklist;
    List<String> hashed_phone_numbers;

    public List<Badge> getBadges() {
		return badges;
	}

	public String getAvatarUrl() {
		return avatar_url;
	}

	public class BadgeComparator implements Comparator<Badge> {
	    @Override
	    public int compare(Badge a, Badge b) {
	        return Integer.compare(a.priority, b.priority);
	    }
	}

	public boolean hasNotification(String name) {
		if (notifications == null) return false;
		return notifications.contains(name);
	}

	public boolean hasBadge(String code) {
		Badge b = getBadge();
		if (b != null) {
			return b.getCode().equals(code);
		}
		return false;
	}
	
	public Badge getBadge() {
		if (badges != null && badges.size() > 0) {
			ArrayList<Badge> bs = new ArrayList<Badge>(badges);
			Collections.sort(bs, new BadgeComparator());
			return bs.get(0);
		}
		return null;
	}
	
	public Checklist getChecklist() {
		return checklist;
	}
    public List<String> getHashedPhoneNumbers() { return hashed_phone_numbers; }

    public boolean addHashedPhoneNumber(String hashedPhone) {
        if (!hasHashedPhoneNumber(hashedPhone)) {
            hashed_phone_numbers.add(hashedPhone);
            return true;
        }
        return false;
    }

    public boolean hasHashedPhoneNumber(String hashedPhone) {
        if (hashed_phone_numbers != null) {
            return hashed_phone_numbers.contains(hashedPhone);
        }
        return false;
    }
}
