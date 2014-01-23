package com.cobrain.android.adapters;

import android.graphics.drawable.Drawable;

public class NavigationMenuItem {
	public int id;
	public Drawable icon;
	public CharSequence captionTop;
	public CharSequence caption;
	public CharSequence label;
	public CharSequence labelCopy;
	@Override
	public String toString() {
		return caption.toString();
	}
}
