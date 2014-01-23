package com.cobrain.android.loaders;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

public class ImageLoader {
    private static LruCache<String, Bitmap>     mMemoryCache    = null;
    private static int                          CACHE_SIZE       = 1024 * 1024 * 10;

    public interface OnImageLoadListener {
    	public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b);
    	public void onLoad(String url, ImageView view, Bitmap b, boolean fromCache);
    }

    private static class AsyncLoader extends AsyncTask<String, Void, Bitmap> {
        private ImageView mTarget;
        private int mWidth = -1;
        private int mHeight = -1;
        private OnImageLoadListener mOnLoadListener;
        private String mUrl;
        
        public AsyncLoader(ImageView target) {
            mTarget = target;
        }
        
        public AsyncLoader(ImageView target, int width, int height) {
            mTarget = target;
            mWidth = width;
            mHeight = height;
		}

        public AsyncLoader(ImageView target, int width, int height,
				OnImageLoadListener listener) {
        	this(target, width, height);
        	mOnLoadListener = listener;
		}

		@Override
        protected void onPreExecute() {
            mTarget.setTag(this);
        }

        @Override
        protected Bitmap doInBackground(String...urls) {
            String url = urls[0];
            Bitmap result = null;

            mUrl = url;
            if (url != null) {
            	
            	//lets check one last time before we download the image from the internet
            	//maybe the image has just finished loading by a previous request
            	result = mMemoryCache.get(url);

            	if (result == null) {
	                result = load(url, mWidth, mHeight);
                	if (mOnLoadListener != null) result = mOnLoadListener.onBeforeLoad(mUrl, mTarget, result);
	
	                if (result != null) {
	                    mMemoryCache.put(url, result);
	                }
            	}
            }

            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (mTarget.getTag() == this) {
                mTarget.setTag(null);
                if (result != null) {
                	mTarget.setImageBitmap(result);
                	if (mOnLoadListener != null) mOnLoadListener.onLoad(mUrl, mTarget, result, false);
                }
            } else if (mTarget.getTag() != null) {
            	ImageLoader.cancel(mTarget);
            }
            mTarget = null;
            mOnLoadListener = null;
        }
    }

    public static Bitmap load(String urlString, int width, int height) {
        if (urlString == null || urlString.length() == 0) return null;

        Bitmap bitmap = null;
        URL url = null;

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url == null) return null;
        
        try {
            InputStream is = url.openStream();
            BitmapFactory.Options opts = new BitmapFactory.Options();

            if (width != -1 && height != -1) {
            	opts.inJustDecodeBounds = true;
            	BitmapFactory.decodeStream(is, null, opts);
            	is.close();

	            //download image with sampled size in case image is too large for memory
	            //and make it purgeable
	            int scale = Math.round(opts.outWidth / width);
	            opts.inSampleSize = scale;
	            opts.inPurgeable = true;
	            opts.inJustDecodeBounds = false;
	            opts.inScaled = false;

	            is = url.openStream();
	            Bitmap temp = BitmapFactory.decodeStream(is, null, opts);
	            bitmap = Bitmap.createScaledBitmap(temp, width, height, true);
	            if (temp != bitmap) temp.recycle();
            }
            else {
            	bitmap = BitmapFactory.decodeStream(is);
            }
        	
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
        	e.printStackTrace();
        }

        return bitmap;
    }

    public static void load(String url, ImageView view, OnImageLoadListener listener) {
    	load(url, view, -1, -1, listener);
    }

	public static void load(String url, ImageView view, int width, int height, OnImageLoadListener listener) {
        if (url == null || url.length() == 0) return;
        if (mMemoryCache == null) {
            mMemoryCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return (bitmap.getRowBytes() * bitmap.getHeight());
                }
            };
        }

        Bitmap bitmap = mMemoryCache.get(url);
        if (bitmap == null) {
            final AsyncLoader task = (AsyncLoader) new AsyncLoader(view, width, height, listener);
            if (view.getTag() != null) {
            	cancel(view);
            }
            view.setTag(task);
            task.execute(url);
        } else {
            view.setImageBitmap(bitmap);
            listener.onLoad(url, view, bitmap, true);
        }
	}

	public static void cancel(ImageView view) {
    	AsyncLoader loader = (AsyncLoader) view.getTag();
    	if (loader != null) {
            view.setTag(null);
            loader.mOnLoadListener = null;
    		loader.cancel(true);
    	}
	}
}