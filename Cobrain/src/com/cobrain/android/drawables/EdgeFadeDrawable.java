package com.cobrain.android.drawables;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class EdgeFadeDrawable extends Drawable {

	private GradientDrawable mDrawableLeft;
	private GradientDrawable mDrawableRight;
	private int FADE_SIZE = 10;
	private int color;
	Paint p = new Paint();
	Rect r = new Rect();

	public EdgeFadeDrawable(int color) {
		setColor(color);
		p.setStyle(Style.FILL_AND_STROKE);
	}
	
	public void setColor(int color) {
		if (this.color != color) {
			this.color = color;
			p.setColor(color);
			init();
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		copyBounds(r);
		
        //mDrawableRight.setBounds(r.left, r.top, r.left + FADE_SIZE , r.bottom);
        canvas.drawRect(r.centerX() - FADE_SIZE/2, r.top, r.centerX() + FADE_SIZE/2, r.bottom, p);
        //canvas.drawRect(r.left + FADE_SIZE, r.top, r.right - FADE_SIZE, r.bottom, p);
        //mDrawableLeft.setBounds(r.right - FADE_SIZE, r.top, r.right, r.bottom);
        //mDrawableLeft.draw(canvas);
        //mDrawableRight.draw(canvas);
	}

   private void init() {
	   
        mDrawableLeft = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT, new int[] {
                        color,
                        Color.parseColor("#00000000") });
        mDrawableRight = new GradientDrawable(
                GradientDrawable.Orientation.RIGHT_LEFT, new int[] {
                        color,
                        Color.parseColor("#00000000") });
                        
    }
	   
	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return PixelFormat.TRANSLUCENT;
	}

}
