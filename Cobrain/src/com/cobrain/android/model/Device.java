package com.cobrain.android.model;

/*
 *  "devices" : [
    {
      "_id": "5678987654567890",
      "platform": "ios",
      "notification_token": "xxx",
      "os_version": "7.1",
      "created_at": "...",
    },
 */

public class Device {
	String _id;
	String platform;
	String notification_token;
	String os_version;
	String created_at;

	public String getId() {
		return _id;
	}
	public String getPlatform() {
		return platform;
	}
	public String getNotificationToken() {
		return notification_token;
	}
	public String getOsVersion() {
		return os_version;
	}
	public String getCreatedAt() {
		return created_at;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public void setNotificationToken(String notification_token) {
		this.notification_token = notification_token;
	}
	public void setOSVersion(String os_version) {
		this.os_version = os_version;
	}
}
