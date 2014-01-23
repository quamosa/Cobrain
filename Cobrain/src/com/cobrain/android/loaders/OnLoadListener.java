package com.cobrain.android.loaders;

public interface OnLoadListener<T> {
	public void onLoadStarted();
	public void onLoadCompleted(T r);
}

