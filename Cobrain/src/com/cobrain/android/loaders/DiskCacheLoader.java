package com.cobrain.android.loaders;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Environment;

import com.cobrain.android.utils.DiskLruCache;
import com.cobrain.android.utils.DiskLruCache.Editor;
import com.cobrain.android.utils.DiskLruCache.Snapshot;

public class DiskCacheLoader {

	private DiskLruCache mDiskLruCache;
	private final Object mDiskCacheLock = new Object();
	private boolean mDiskCacheStarting = true;
	private int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
	private String DISK_CACHE_SUBDIR = "thumbnails";
	
	public DiskCacheLoader(Context c, String name, int size) {
	    // Initialize disk cache on background thread
		DISK_CACHE_SIZE = size;
		DISK_CACHE_SUBDIR = name;
	    File cacheDir = getDiskCacheDir(c, DISK_CACHE_SUBDIR);
	    //new InitDiskCacheTask().execute(cacheDir);
	    new InitDiskCacheTask().doInBackground(cacheDir);
	}
	
	public DiskLruCache getCache() {
		return mDiskLruCache;
	}

	private String toInternalKey(String key) {
		return md5(key);
	}

	public Bitmap getBitmapFromDiskCache(String url, int width, int height) {
		Bitmap bitmap = null;
    	if (mDiskLruCache != null) {
    		try {
    			String key = toInternalKey(url + ":" + width + ":" + height);
				Snapshot s = mDiskLruCache.get(key);
				if (s != null) {
					BufferedInputStream is = new BufferedInputStream( s.getInputStream(0) );
					bitmap = BitmapFactory.decodeStream( is );
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return bitmap;
    }

    public void putBitmapIntoDiskCache(String url, int width, int height, Bitmap bmp) {
    	if (mDiskLruCache != null) {
    		try {
    			String key = toInternalKey(url + ":" + width + ":" + height);
				Editor editor = mDiskLruCache.edit(key);
				OutputStream os = editor.newOutputStream(0);
				bmp.compress(CompressFormat.PNG, 100, os);
				os.close();
				editor.commit();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
	private String md5(String s) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(s.getBytes("UTF-8"));
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			return bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError();
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError();
		}
	}
	
	class InitDiskCacheTask extends AsyncTask<File, Void, Void> {
	    @Override
	    protected Void doInBackground(File... params) {
	        synchronized (mDiskCacheLock) {
	            File cacheDir = params[0];
	            try {
					mDiskLruCache = DiskLruCache.open(cacheDir, 1, 1, DISK_CACHE_SIZE);
		            mDiskCacheStarting = false; // Finished initialization
		            mDiskCacheLock.notifyAll(); // Wake any waiting threads
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        return null;
	    }
	}
	
/*	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
	    ...
	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(Integer... params) {
	        final String imageKey = String.valueOf(params[0]);
	
	        // Check disk cache in background thread
	        Bitmap bitmap = getBitmapFromDiskCache(imageKey);
	
	        if (bitmap == null) { // Not found in disk cache
	            // Process as normal
	            final Bitmap bitmap = decodeSampledBitmapFromResource(
	                    getResources(), params[0], 100, 100));
	        }
	
	        // Add final bitmap to caches
	        addBitmapToCache(imageKey, bitmap);
	
	        return bitmap;
	    }
	    ...
	}
	
	public void addBitmapToCache(String key, Bitmap bitmap) {
	    // Add to memory cache as before
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	
	    // Also add to disk cache
	    synchronized (mDiskCacheLock) {
	        if (mDiskLruCache != null && mDiskLruCache.get(key) == null) {
	            mDiskLruCache.put(key, bitmap);
	        }
	    }
	}
	
	public Bitmap getBitmapFromDiskCache(String key) {
	    synchronized (mDiskCacheLock) {
	        // Wait while disk cache is started from background thread
	        while (mDiskCacheStarting) {
	            try {
	                mDiskCacheLock.wait();
	            } catch (InterruptedException e) {}
	        }
	        if (mDiskLruCache != null) {
	            return mDiskLruCache.get(key);
	        }
	    }
	    return null;
	}
*/	
	
	// Creates a unique subdirectory of the designated app cache directory. Tries to use external
	// but if not mounted, falls back on internal storage.
	public static File getDiskCacheDir(Context context, String uniqueName) {
	    // Check if media is mounted or storage is built-in, if so, try and use external cache dir
	    // otherwise use internal cache dir
	    final String cachePath =
	            Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
	                    !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
	                            context.getCacheDir().getPath();
	
	    return new File(cachePath + File.separator + uniqueName);
	}

}
