package com.cobrain.android.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

public class HelperUtils {

	public static int getStatusBarHeight(Activity a){
	    Rect rectangle = new Rect();
	    Window window = a.getWindow();
	    window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
	    return rectangle.top;
	}

	public static class SMS {
		public static void sendSMS(String toPhoneNumber, String message) {
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(toPhoneNumber, null, message, null, null);
		}		
	}
	
	public static class Graphics {
		//using this for pre-HoneyComb devices (Gingerbread)
		public static void setAlpha(View v, float alpha) {
			if (v instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) v;
				
				for (int i = 0; i < vg.getChildCount(); i++) {
					View cv = vg.getChildAt(i);
					setAlpha(cv, alpha);
				}
			}
			
			if (v instanceof TextView) {
				//increase alpha for text to make it clearer to read
				alpha += (1f - alpha) * (.5f);
				int color = ((TextView) v).getTextColors().getDefaultColor();
				int xalpha = (int) (Color.alpha(color) * alpha);
				int red = Color.red(color);
				int green = Color.green(color);
				int blue = Color.blue(color);
				color = Color.argb(xalpha, red, green, blue);
				((TextView) v).setTextColor(color);
			}
			else if (v instanceof ViewGroup) {
				Drawable d = v.getBackground();
				if (d instanceof ColorDrawable) {
					int color = ((ColorDrawable) d).getColor();
					int xalpha = (int) (Color.alpha(color) * alpha);
					int red = Color.red(color);
					int green = Color.green(color);
					int blue = Color.blue(color);
					color = Color.argb(xalpha, red, green, blue);
					d.mutate().setAlpha(xalpha);
				}
			}
		}
	}

	static Handler handler = new Handler();

	public static class Strings {
		public static String wordCase(String word) {
			return word.substring (0,1).toUpperCase() + 
					word.substring(1).toLowerCase();
		}
	}

	public static class Bitmaps {
	    // Scale and keep aspect ratio 
	    static public Bitmap scaleToFitWidth(Bitmap b, int width) {
	        float factor = width / (float) b.getWidth();
	        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), false);  
	    }

	    // Scale and keep aspect ratio     
	    static public Bitmap scaleToFitHeight(Bitmap b, int height) {
	        float factor = height / (float) b.getHeight();
	        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, false);  
	    }

	    // Scale and keep aspect ratio 
	    static public Bitmap scaleToFit(Bitmap b, int width, int height, boolean filter) {
	        float factorH2W = b.getHeight() / (float) b.getWidth();
	        float w, h;
	        
	        if (factorH2W > 1) {
		        h = height / factorH2W;
		        w = width;
	        }
	        else {
		        w = width * factorH2W;
	        	h = height;
	        }
	        
	        return Bitmap.createScaledBitmap(b, (int) w, (int) h, filter);  
	    }

	    static public Bitmap scaleToFill(Bitmap b, int width, int height, boolean filter) {
	        float factorH = height / (float) b.getWidth();
	        float factorW = width / (float) b.getWidth();
	        float factorToUse = (factorH > factorW) ? factorW : factorH;
	        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorToUse), (int) (b.getHeight() * factorToUse), filter);  
	    }
	    
	    // Scale and dont keep aspect ratio 
	    static public Bitmap strechToFill(Bitmap b, int width, int height) {
	        float factorH = height / (float) b.getHeight();
	        float factorW = width / (float) b.getWidth();
	        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factorW), (int) (b.getHeight() * factorH), false);  
	    }

	    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
	        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
	            bitmap.getHeight(), Config.ARGB_8888);
	        Canvas canvas = new Canvas(output);
	     
	        final int color = 0xff424242;
	        final Paint paint = new Paint();
	        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	        final RectF rectF = new RectF(rect);
	        //final float roundPx = 12;
	     
	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	     
	        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	        canvas.drawBitmap(bitmap, rect, rect, paint);
	     
	        return output;
	    }	
	}		
	
	public static void runOnUiThread(Runnable run) {
		if (Looper.myLooper() == Looper.getMainLooper()) run.run();
		else handler.post(run);
	}
	
	public static class Timing {

		public static class Timer {
			HashMap<Runnable, MyRunnable> runs = new HashMap<Runnable, MyRunnable>();
			Runnable r;
			
			public class MyRunnable implements Runnable {
				private boolean cancel;
				private int delay;
				private Runnable run;
				private boolean running;
				private int runCount;
				
				public MyRunnable(Runnable r, int delay) {
					this.run = r;
					this.delay = delay;
				}
				public void run() {
					if (!cancel) {
						running = true;
						if (run != null) {
							runCount++;
							run.run();
						}
						handler.postDelayed(this, delay);
					}
				}
				public void start() {
					if (!running) {
						runCount = 0;
						cancel = false;
						run();
					}
				}
				public void stop() {
					running = false;
					cancel = true;
					handler.removeCallbacks(this);
				}
				public void dispose() {
					stop();
					run = null;
				}
			}
			
			public int getRunCount(Runnable r) {
				return runs.get(r).runCount;
			}
			
			public void stop(Runnable r) {
				MyRunnable run = runs.get(r);
				if (run != null)
					run.stop();
			}
			
			public void start(Runnable r, int delay) {
				MyRunnable run = runs.get(r);
				if (run == null) {
					run = new MyRunnable(r, delay);
					runs.put(r, run);
				}
				run.start();
			}
			
			public void dispose() {
				for (Runnable r : runs.keySet()) {
					MyRunnable mr = runs.get(r); 
					mr.dispose();
				}
				runs.clear();
			}

		}
		
	}
	
	public static class Storage {
		public static class TempStore {
			static HashMap<String, Object> store = new HashMap<String, Object>();
			public static void push(String key, Object object) {
				store.put(key, object);
			}
			public static Object pull(String key) {
				return store.remove(key);
			}
		}
	}

	public static class Assets {
		public static String read(Context c, String fileName) {
			try {
				InputStream is = c.getAssets().open(fileName);
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				StringBuilder buf = new StringBuilder();
				String str;

			    while ((str = in.readLine()) != null) {
			    	buf.append(str);
			    }

			    in.close();
			    return buf.toString();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
