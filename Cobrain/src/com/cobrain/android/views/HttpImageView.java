package com.cobrain.android.views;

import com.cobrain.android.loaders.ImageLoader;
import com.cobrain.android.loaders.ImageLoader.OnImageLoadListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class HttpImageView extends ImageView implements OnImageLoadListener {
	String url;
	private boolean hasLayout;
	private OnImageLoadListener listener;
	private boolean layoutRequested;
	
	public HttpImageView(Context context) {
		super(context);
	}

	public HttpImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HttpImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setImageUrl(String url, OnImageLoadListener listener) {
		this.listener = listener;
		if (this.url != url || !hasLayout) {
			this.url = url;
			if (!layoutRequested) {
				if (hasLayout) loadImage();
				else {
					layoutRequested = true;
					requestLayout();
				}
			}
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		hasLayout = true;
		layoutRequested = false;
		loadImage();
	}

	void loadImage() {
		if (url != null) {
			ImageLoader.get.load(url, this, getWidth(), getHeight(), this);
		}
	}

	@Override
	public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b) {
		if (listener != null) return listener.onBeforeLoad(url, view, b);
		return b;
	}

	@Override
	public void onLoad(String url, ImageView view, Bitmap b, int fromCache) {
		if (listener != null) listener.onLoad(url, view, b, fromCache);
	}
}
