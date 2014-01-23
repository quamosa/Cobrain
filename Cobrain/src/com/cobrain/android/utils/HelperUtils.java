package com.cobrain.android.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
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
	
	public static void runOnUiThread(Runnable run) {
		if (Looper.myLooper() == Looper.getMainLooper()) run.run();
		else handler.post(run);
	}
}
