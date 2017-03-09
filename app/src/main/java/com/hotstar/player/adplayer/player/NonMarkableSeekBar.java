/*
 * ************************************************************************
 *
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the 
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a 
 * source other than Adobe, then your use, modification, or distribution of it requires the prior 
 * written permission of Adobe.
 *
 **************************************************************************
 */

/*
 *  MarkableSeekBar class handles the UI components such as the slider, the thumb of the slider,
 *  the ad marker holder, the cue markers,buffer range and the seek range backgrounds. It initializes the markers based on the contents information and plots those on the slider.
 */

package com.hotstar.player.adplayer.player;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.hotstar.player.R;
import com.hotstar.player.adplayer.AdVideoApplication;

/**
 * This is a seek bar with ad markers, seekable and buffer range.
 */
public class NonMarkableSeekBar extends SeekBar {
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + NonMarkableSeekBar.class.getSimpleName();

	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Range seekableRange = new Range();
	private Drawable background, seekableBackground, stripeBg;

	static class Range {
		float start, end;
	}

	public enum RangeType {
		BUFFER, SEEK;
	}

	public NonMarkableSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NonMarkableSeekBar(Context context) {
		super(context);
		init();
	}

	public NonMarkableSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		// Load the background drawables
		background = getResources().getDrawable(R.drawable.seekbar_background);
		stripeBg = getResources().getDrawable(R.drawable.stripebg);
		seekableBackground = getResources().getDrawable(R.drawable.seekbar_background);
	}

	/**
	 * Converts a normalized value into screen space.
	 */
	private float normalizedToScreen(double normalizedCoord) {
		return (float) (normalizedCoord * getWidth());
	}

	/**
	 * Returns the drawable intrinsec height. If it has no intrinsic height
	 * (such as with a solid color), returns a third of the view height.
	 *
	 * @param drawable
	 * @return
	 */
	private int getDrawableHeight(Drawable drawable) {
		int height = drawable.getIntrinsicHeight();
		if (height == -1) {
			// Has no intrinsic height, make it a third the view height.
			return getHeight() / 3;
		}
		return height;
	}

	/**
	 * Gets top coordinate based on the drawable height and the view height.
	 *
	 * @param drawable
	 * @return
	 */
	private int getTop(Drawable drawable) {
		int height = getDrawableHeight(drawable);
		if (height >= getHeight()) {
			return 0;
		}
		// Center in view.
		return (getHeight() - height) / 2;
	}

	/**
	 * Gets bottom coordinate based on the drawable height and the view height.
	 *
	 * @param drawable
	 * @return
	 */
	private int getBottom(Drawable drawable) {
		int height = getDrawableHeight(drawable);
		if (height >= getHeight()) {
			return getHeight();
		}
		// Center in view.
		return height + (getHeight() - height) / 2;
	}

	/**
	 * Set the time range of the given range type
	 *
	 * @param start
	 *            the start point of the new range
	 * @param end
	 *            the end point of the new range
	 * @param type
	 *            the range type to be updated
	 */
	public void setRange(int start, int end, RangeType type) {
		if (start > getMax() || start < 0 || end > getMax() || end < 0) {
			AdVideoApplication.logger.w(LOG_TAG + "#setRange",
					"Cannot position received range [" + start + "," + end
							+ "] for " + type.toString());
			start = 0;
			end = 0;
		}

		boolean invalidate = false;
		switch (type) {
			case SEEK:
				invalidate = seekableRange.start != start || seekableRange.end != end;
				this.seekableRange.start = start;
				this.seekableRange.end = end;
				break;
			case BUFFER:
				break;
			default:
				break;
		}

		if (invalidate) {
			invalidate();
		}
	}

	/**
	 * Draw the time range on the seek bar
	 *
	 * @param canvas
	 *            the canvas to be drawn to
	 * @param drawable
	 *            the resource to be drawn
	 * @param start
	 *            the starting point where the range starts
	 * @param end
	 *            the end point where the range ends
	 */
	private void drawRange(Canvas canvas, Drawable drawable, float start,
						   float end) {
		if (end > 0 && drawable != null) {
			float ratioStart = start / getMax();
			float ratioEnd = end / getMax();
			int left = (int) normalizedToScreen(ratioStart);
			int top = getTop(drawable);
			int right = (int) normalizedToScreen(ratioEnd);
			int bottom = getBottom(drawable);

			if (drawable.getIntrinsicWidth() <= right - left) {
				// Set its bound where it's needed
				drawable.setBounds(left, top, right, bottom);
				// Draw on the canvas
				drawable.draw(canvas);
			}
		}
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (isIndeterminate()) {
			super.onDraw(canvas);
			return;
		}

		float ratio = (float) getProgress() / getMax();

		int left = (int) 0;
		int top = getTop(background);
		int right = (int) getWidth();
		int bottom = getBottom(background);

		// Draw main background
		if (background != null) {
			// Set its bound where it's needed
			background.setBounds(left, top, right, bottom);
			// Draw on the canvas
			background.draw(canvas);
		}

		drawRange(canvas, seekableBackground, seekableRange.start,
				seekableRange.end);

		// draw elapsed time on control bar
		drawRange(canvas, stripeBg, seekableRange.start, getProgress());

	}
}
