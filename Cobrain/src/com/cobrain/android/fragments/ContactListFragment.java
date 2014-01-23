package com.cobrain.android.fragments;

import com.cobrain.android.loaders.ContactLoader.ContactInfo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;

public class ContactListFragment extends BaseCobrainFragment {
	public static final String TAG = "ContactListFragment";
	static final int PICK_CONTACT = 1;
	ContactSelectedListener listener = null;
	private boolean mReturningWithResult;

	public interface ContactSelectedListener {
		public void onContactSelected(ContactInfo contact);
	};
	
	public ContactListFragment() {
	}
	
	public void setContactSelectedListener(ContactSelectedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onDestroyView() {
		listener = null;
		super.onDestroyView();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		go();
		super.onActivityCreated(savedInstanceState);
	}

	public void go() {
		Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, PICK_CONTACT);
	}

	
	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK) {
			switch (reqCode) {
			case (PICK_CONTACT) :

				Uri contactData = data.getData();

				//FIXME: if (listener != null) listener.onContactSelected(getContactInfo(contactData));
				
				break;
			}
		}

		mReturningWithResult = true;

	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mReturningWithResult) {
			//getChildFragmentManager().beginTransaction().remove(this).commit();
			getFragmentManager().popBackStackImmediate();
			mReturningWithResult = false;
		}
	}
	
}
