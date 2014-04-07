package com.cobrain.android.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.cobrain.android.utils.HelperUtils;
import com.cobrain.android.utils.LoaderUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageHandlerLoader {
    private static final boolean DISK_CACHE_ENABLED = false;
	private LruCache<String, Bitmap> mMemoryCache = null;
    private int CACHE_SIZE = 1024 * 1024 * 10;
    private String CACHE_NAME = "cache";
    private DiskCacheLoader diskCacheLoader;
	private static final boolean DEBUG = true;
	public static final int CACHE_NONE = 0;
	public static final int CACHE_MEMORY = 1;
	public static final int CACHE_DISK = 2;

    public static ImageHandlerLoader get = new ImageHandlerLoader();

    public HandlerThread handlerThread = new HandlerThread("ImageLoader");
    public Handler handler = new Handler(handlerThread.getLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

	public ImageHandlerLoader newInstance() {
    	return new ImageHandlerLoader();
    }
    public ImageHandlerLoader newInstance(int cacheSize) {
    	return new ImageHandlerLoader(CACHE_NAME, cacheSize);
    }
    public ImageHandlerLoader() {
	}
    public ImageHandlerLoader(String cacheName, int cacheSize) {
    	CACHE_SIZE = cacheSize;
    	CACHE_NAME = cacheName;
	}
    
    public interface OnImageLoadListener {
    	public Bitmap onBeforeLoad(String url, ImageView view, Bitmap b);
    	public void onLoad(String url, ImageView view, Bitmap b, int fromCache);
    }

    private class AsyncLoader extends AsyncTask<String, Void, Bitmap> {
        private ImageView mTarget;
        private int mWidth = -1;
        private int mHeight = -1;
        private OnImageLoadListener mOnLoadListener;
        private String mUrl;
		public InputStream stream;
		public int fromCache = CACHE_NONE;
        
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
            	
            	//start diskcache
            	//then check if the image is there
            	initDiskCache(mTarget.getContext());

            	//lets check one last time before we download the image from the internet
            	//maybe the image has just finished loading by a previous request
            	result = mMemoryCache.get(url);

            	if (result == null || !isCorrectSize(result, mWidth, mHeight)) {
	                result = load(this, url, mWidth, mHeight);
                	if (mOnLoadListener != null) result = mOnLoadListener.onBeforeLoad(mUrl, mTarget, result);
	
	                if (result != null) {
	                    mMemoryCache.put(url, result);
	                }
            	}
            	else fromCache = CACHE_MEMORY;
            }

            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (mTarget.getTag() == this) {
                mTarget.setTag(null);
                if (result != null) {
                	mTarget.setImageBitmap(result);
                }
            	if (mOnLoadListener != null) mOnLoadListener.onLoad(mUrl, mTarget, result, fromCache);
            } else if (mTarget.getTag() != null && mTarget.getTag() instanceof ImageHandlerLoader) {
            	ImageHandlerLoader loader = (ImageHandlerLoader) mTarget.getTag();
            	loader.cancel(mTarget);
            }
            mTarget = null;
            mOnLoadListener = null;
        }
    }

    public void initDiskCache(Context c) {
    	if (DISK_CACHE_ENABLED)
	    	if (diskCacheLoader == null)
	    		diskCacheLoader = new DiskCacheLoader(c, "images", CACHE_SIZE);
    }

    private Bitmap overlayColor(Bitmap bitmap, int color) {
		Bitmap b = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint p = new Paint();
		p.setColor(color);
		c.drawBitmap(bitmap, 0, 0, p);
		c.drawRect(0, 0, b.getWidth(), b.getHeight(), p);
		bitmap.recycle();
		return b;
    }
    
    public Bitmap load(AsyncLoader asyncLoader, String urlString, int width, int height) {
        if (urlString == null || urlString.length() == 0) return null;

        Bitmap bitmap = null;
        
        if (diskCacheLoader != null)
        	bitmap = diskCacheLoader.getBitmapFromDiskCache(urlString, width, height);
        
        if (bitmap != null) {
        	asyncLoader.fromCache = CACHE_DISK;
        	return bitmap;
        }

        try {
            URL url = new URL(urlString);
            //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
            InputStream is = url.openStream(); //conn.getInputStream(); 
            
            asyncLoader.stream = is;
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
	            if (temp != null) {
		            //bitmap = Bitmap.createScaledBitmap(temp, width, height, true);
		            bitmap = HelperUtils.Bitmaps.scaleToFill(temp, width, height, true);
		            if (temp != bitmap) temp.recycle();
	            }
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

        asyncLoader.stream = null;

        if (bitmap != null) {
        	if (diskCacheLoader != null)
        		diskCacheLoader.putBitmapIntoDiskCache(urlString, width, height, bitmap);
        }
        
        return bitmap;
    }

    public void load(String url, ImageView view, OnImageLoadListener listener) {
    	load(url, view, -1, -1, listener);
    }

	public void load(String url, ImageView view, int width, int height, OnImageLoadListener listener) {
        if (url == null || url.length() == 0) return;
        if (mMemoryCache == null) {
            mMemoryCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return (bitmap.getRowBytes() * bitmap.getHeight());
                }
            };
        }

        if (view.getTag() != null) {
        	cancel(view);
        }
        
        /*
        if (width == -2 || height == -2) {
        	boolean dow = false, doh = false;
        	if (width == -2) {
        		dow = true;
        		width = view.getMeasuredWidth();
        	}
        	if (height == -2) {
        		doh = true;
        		height = view.getMeasuredWidth();
        	}
        	if (dow && width == 0 || doh && height == 0) {
        		view.measure(0, 0);
        		if (dow) width = view.getMeasuredWidth();
        		if (doh) height = view.getMeasuredHeight();
        	}
        }*/
        
        Bitmap bitmap = mMemoryCache.get(url);
        
        if (bitmap == null || !isCorrectSize(bitmap, width, height)) {
            final AsyncLoader task = (AsyncLoader) new AsyncLoader(view, width, height, listener);
            view.setTag(task);
            task.execute(url);
            handler.ru
        } else {
            view.setImageBitmap(bitmap);
            if (listener != null) listener.onLoad(url, view, bitmap, CACHE_MEMORY);
        }
	}

    static boolean isCorrectSize(Bitmap b, int width, int height) {
    	if ((width != -1 && b.getWidth() != width) && (height != -1 && b.getHeight() != height)) return false;
		return true;
    }
    
	public void cancel(ImageView view) {
		if (!(view.getTag() instanceof AsyncLoader)) return;
		
    	AsyncLoader loader = (AsyncLoader) view.getTag();
    	if (loader != null) {
            view.setTag(null);
            loader.mOnLoadListener = null;
            LoaderUtils.cancelAnimation(view);
            /*
             * if (loader.stream != null) {
            	try {
					loader.stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
            */
    		loader.cancel(true);
    	}
	}
}
