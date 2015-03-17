package com.sunilsahoo.drivesafe.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

import com.sunilsahoo.drivesafe.R;


public class IndicatorGallery extends Gallery {

	private static int galleryWidth = 0;
	final static String TAG = IndicatorGallery.class.getName();

	public IndicatorGallery(Context context) {
		super(context);
	}

	public IndicatorGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IndicatorGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isInEditMode() {
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		galleryWidth = MeasureSpec.getSize(widthMeasureSpec);

		setSpacingOfgalleryElement(UserTestScreen.getCharCount());
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return true;
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

	/**
	 * calculates free space between two adjacent images
	 * 
	 * @param imageId
	 * @param numItem
	 * @return
	 */
	private int setMarginOfImage(int imageId, int numItem) {
		int freespacePerImage = 0;
		try {
			Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
					imageId);
			int widthOfImage = bitmap.getWidth();
			int totalWidthOfImage = numItem * widthOfImage;
			if ((numItem % 2) == 0) {
				freespacePerImage = (IndicatorGallery.galleryWidth - totalWidthOfImage)
						/ (numItem + 2);
			} else {
				freespacePerImage = (IndicatorGallery.galleryWidth - totalWidthOfImage)
						/ (numItem + 1);
			}
			if(freespacePerImage > 15){
				freespacePerImage = 15;
			}
			
		} catch (Exception ex) {
			Log.e("UsertestScreen",
					"Error in calculating freespace :" + ex.getMessage());
		}
		return freespacePerImage;
	}

	/**
	 * set spacing between each element of a gallery
	 * 
	 * @param numItem
	 */
	private void setSpacingOfgalleryElement(int numItem) {
		setSpacing(setMarginOfImage(R.drawable.indicator_white,
				numItem));
		setGravity(Gravity.CENTER);

		setSelection(numItem / 2);
	}
}
