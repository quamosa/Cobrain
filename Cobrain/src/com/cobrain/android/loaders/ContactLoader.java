package com.cobrain.android.loaders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;

public class ContactLoader {
	private static final int PICK_CONTACT = 1;
	Context context;
	
	public ContactLoader(Context c) {
		context = c;
		// TODO Auto-generated constructor stub
	}

	public ContactLoader() {
		// TODO Auto-generated constructor stub
	}

	public class ContactInfo {
		public String id;
		public String name;
		public String number;
		public String email;
	}
	
	public void showContactPicker(Fragment parent) {
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		parent.startActivityForResult(intent, PICK_CONTACT);
	}
	
	public ContactInfo getContactInfo(Uri contactData) {
		ContactInfo ci = new ContactInfo();

		Cursor c =  context.getContentResolver().query(contactData, null, null, null, null);
		String id = null;

		if (c.moveToFirst()) {
			id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

			String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
			ci.name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
			
			if (hasPhone.equals("1")) {
				Cursor phones = context.getContentResolver().query( 
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, 
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
						null, null);
				
				if (phones.moveToFirst()) {
					while (!phones.isAfterLast()) {
	
						String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));
	
						if (ci.number == null) {
							ci.number = phone;
						}
	
						int phoneType = phones.getInt(phones.getColumnIndex(Phone.TYPE));
	
						if (phoneType == Phone.TYPE_MOBILE) {
							ci.number = phone;
							break;
						}
	
						phones.moveToNext();
					}
				}
	
				if (ci.number != null)
					ci.number = ci.number.replaceAll("\\s", "");
	
				//ci.number = phones.getString(phones.getColumnIndex("data1"));
				phones.close();
			}
			else {

				Cursor emails = context.getContentResolver().query( 
						ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, 
						ContactsContract.CommonDataKinds.Email.CONTACT_ID +" = "+ id, 
						null, null);
				
				if (emails.moveToFirst()) {
					while (!emails.isAfterLast()) {
	
						String email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
	
						if (ci.email == null) {
							ci.email = email;
						}
	
						int emailType = emails.getInt(emails.getColumnIndex(Email.TYPE));
	
						if (emailType == Email.TYPE_HOME) {
							ci.email = email;
							break;
						}
	
						emails.moveToNext();
					}
				}
	
				if (ci.email != null)
					ci.email = ci.email.replaceAll("\\s", "");
	
				emails.close();
			}
		}

		c.close();

		return ci;
	}

	public ContactInfo processActivityResult(int reqCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			switch (reqCode) {
			case (ContactLoader.PICK_CONTACT) :

				Uri contactData = data.getData();

				return getContactInfo(contactData);
			}
		}
		return null;
	}

	public void dispose() {
		context = null;
	}

	public void setContext(Context c) {
		context = c;
	}

}
