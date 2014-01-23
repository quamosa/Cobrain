package com.cobrain.android.loaders;

import java.util.HashMap;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

// Apparently there's memory leaks using custom fonts so lets manage them here with a hashmap
// @Author Walter

public class FontLoader {
	private static final String TAG = "FontLoader";
	static HashMap<String, Typeface> fonts = new HashMap<String, Typeface>();
	private static String FONTS_SUBDIR = "fonts/";
	
	public static Typeface load(Context c, String fontName) {
		Typeface tf = fonts.get(fontName);
		if (tf == null) {
			try {
				tf = Typeface.createFromAsset(c.getAssets(), FONTS_SUBDIR + fontName);
				fonts.put(fontName, tf);
			} catch (Exception e) {
                Log.e(TAG, "Could not get typeface '" + fontName
                        + "' because " + e.getMessage());
                return null;
            }
		}
		return tf;
	}
	
	public void dispose() {
		fonts.clear();
	}
}
