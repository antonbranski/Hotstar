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
public class MarkableSeekBar extends SeekBar {
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + MarkableSeekBar.class.getSimpleName();

	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Bitmap thumbImage, thumbPressedImage, thumbDisabledImage;
	private float thumbHalfWidth, thumbHalfHeight, padding;
	private MarkerHolder adMarkerHolder = new ListMarkerHolder();
	private MarkerHolder cueMarkerHolder = new ListMarkerHolder();
	private MarkerHolder overlayAdMarkerHolder = new ListMarkerHolder();
	private MarkerHolder vpaidAdMarkerHolder = new ListMarkerHolder();
	private Drawable adMarkerBackground, cueMarkerBackground;
	private Drawable clientLivePointBackground;
	private Range seekableRange = new Range();
	private Drawable background, seekableBackground, stripeBg;
	public final static int AD_CUE_SIZE = 7;

	static class Range {
		float start, end;
	}

	public enum RangeType {
		BUFFER, SEEK;
	}

	public MarkableSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MarkableSeekBar(Context context) {
		super(context);
		init();
	}

	public MarkableSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MarkerHolder getAdMarkerHolder() {
		return adMarkerHolder;
	}

	public MarkerHolder getCueMarkerHolder() {
		return cueMarkerHolder;
	}

	public MarkerHolder getOverlayAdMarkerHolder() {
		return overlayAdMarkerHolder;
	}

	public MarkerHolder getVPaidAdMarkerHolder() {
		return vpaidAdMarkerHolder;
	}

	private void init() {
		// Load the background drawables
		background = getResources().getDrawable(R.drawable.seekbar_background);
		stripeBg = getResources().getDrawable(R.drawable.stripebg);
		adMarkerBackground = getResources().getDrawable(R.drawable.msb_mark_bg);
		cueMarkerBackground = getResources().getDrawable(R.drawable.msb_cue_bg);
		clientLivePointBackground = getResources().getDrawable(
				R.drawable.msb_clp_bg);
		seekableBackground = getResources().getDrawable(
				R.drawable.seekbar_background);

		// Load the thumb bitmaps
		thumbImage = BitmapFactory.decodeResource(getResources(),
				R.drawable.seeker_circle);
		thumbPressedImage = BitmapFactory.decodeResource(getResources(),
				R.drawable.seeker_circle);
		thumbDisabledImage = BitmapFactory.decodeResource(getResources(),
				R.drawable.seeker_circle);

		thumbHalfWidth = 0.5f * thumbImage.getWidth();
		thumbHalfHeight = 0.5f * thumbImage.getHeight();
		padding = thumbHalfWidth;
	}

	/**
	 * class for an ad marker
	 */
	public static class Marker {
		private int relativeStart, relativeEnd;
		private long time;
		private long duration;

		/**
		 * Create a marker object with relative start and end. Start and end
		 * points are relative to the playback range
		 *
		 * @param relativeStart
		 *            the relative start point of the marker
		 * @param relativeEnd
		 *            the relative end point of the marker
		 */
		private Marker(int relativeStart, int relativeEnd) {
			this.relativeStart = relativeStart;
			this.relativeEnd = relativeEnd;
		}

		/**
		 * Creates a new MarkableSeekBar.Marker that should be added to the
		 * SeekBar. It will calculate its relative position by scaling a
		 * relative start point and relative end point. The scaling will be done
		 *
		 *
		 *
		 * @param time
		 *            time of the marker to be placed
		 * @param duration
		 *            duration of the marker to be placed
		 * @param initialPosition
		 *            initial position of the timeline of the asset displayed by
		 *            the SeekBar
		 * @param playBackView
		 *            the play back view of the asset that is displayed by the
		 *            SeekBar
		 * @param leftEndPoint
		 *            the left endpoint that represents the length of the
		 *            SeekBar. The right endpoint is considered to be zero
		 * @return a new MarkableSeekBar.Marker
		 */
		public static MarkableSeekBar.Marker createMarker(long time, long duration, long initialPosition, long playBackView, int leftEndPoint) {
			long adjustedStartTime = time - initialPosition;
			AdVideoApplication.logger.i("#createNewMarker", "Creating marker starting at " + adjustedStartTime + " position = " + initialPosition);

			int start = (int) ((adjustedStartTime / (float) playBackView) * leftEndPoint);
			int end = start + AD_CUE_SIZE;
			AdVideoApplication.logger.i("#createNewMarker", "Creating marker with start = " + start + " end =" + end);

			if (start == end) {
				end = start + 1;
			}

			if (end > leftEndPoint) {
				end = leftEndPoint;
			}

			if (start < 0 && end < leftEndPoint) {
				start = 0;
			}

			MarkableSeekBar.Marker newMarker = new MarkableSeekBar.Marker(
					start, end);
			newMarker.setDuration(duration);
			newMarker.setTime(time);

			return newMarker;
		}

		public int getRelativeStart() {
			return relativeStart;
		}

		public int getRelativeEnd() {
			return relativeEnd;
		}

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public long getDuration() {
			return duration;
		}

		public void setDuration(long duration) {
			this.duration = duration;
		}

		public void updateMarker(int newRelativeStart, int newRelativeEnd) {
			relativeEnd = newRelativeEnd;
			relativeStart = newRelativeStart;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Marker other = (Marker) obj;
			return this.relativeStart == other.relativeStart
					&& this.relativeEnd == other.relativeEnd;
		}

		@Override
		public String toString() {
			return "Marker = { relativeStart = " + relativeStart
					+ ", relativeEnd = " + relativeEnd + ", duration = "
					+ duration + ", time = " + time + "}";
		}
	}

	/**
	 * Converts a normalized value into screen space.
	 */
	private float normalizedToScreen(double normalizedCoord) {
		return (float) (padding + normalizedCoord * (getWidth() - 2 * padding));
	}

	/**
	 * Draws the thumb image on specified X coordinate.
	 */
	private void drawThumb(float screenCoord, Canvas canvas) {
		Bitmap thumb = thumbImage;
		if (!isEnabled()) {
			thumb = thumbDisabledImage;
		} else if (isPressed()) {
			thumb = thumbPressedImage;
		}
		canvas.drawBitmap(thumb, screenCoord - thumbHalfWidth,
				(float) ((0.5f * getHeight()) - thumbHalfHeight), paint);
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
				invalidate = seekableRange.start != start
						|| seekableRange.end != end;
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

		int left = (int) padding;
		int top = getTop(background);
		int right = (int) (getWidth() - padding);
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

		// drawRange(canvas, progressBackground, 0, getProgress());

		// Draw cueMarkerHolder
		if (cueMarkerBackground != null) {
			List<Marker> cueMarkers = cueMarkerHolder.getMarkers();
			for (Marker cue : cueMarkers) {
				drawRange(canvas, cueMarkerBackground, cue.getRelativeStart(),
						cue.getRelativeEnd());
			}
		}

		// Draw adMarkerHolder
		if (adMarkerBackground != null) {
			List<Marker> adMarkers = adMarkerHolder.getMarkers();
			for (Marker adMarker : adMarkers) {
				drawRange(canvas, adMarkerBackground,
						adMarker.getRelativeStart(), adMarker.getRelativeEnd());
			}
		}

		// Draw overlayAdMarkerHolder
		if (adMarkerBackground != null) {
			List<Marker> overlayAdMarkers = overlayAdMarkerHolder.getMarkers();
			for (Marker overlayAdMarker : overlayAdMarkers) {
				drawRange(canvas, adMarkerBackground,
						overlayAdMarker.getRelativeStart(), overlayAdMarker.getRelativeEnd());
			}
		}

		// Draw vpaidAdMarkerHolder
		if (adMarkerBackground != null) {
			List<Marker> vpaidAdMarkers = vpaidAdMarkerHolder.getMarkers();
			for	(Marker vpaidAdMarker : vpaidAdMarkers) {
				drawRange(canvas, adMarkerBackground,
						vpaidAdMarker.getRelativeStart(), vpaidAdMarker.getRelativeEnd());
			}
		}

 		drawRange(canvas, clientLivePointBackground, seekableRange.end - 10,
				seekableRange.end);

		// draw elapsed time on control bar
		drawRange(canvas, stripeBg, seekableRange.start, getProgress());

		// Draw thumb
		drawThumb(normalizedToScreen(ratio), canvas);
	}

	/**
	 * interface for a holder of markers
	 */
	public interface MarkerHolder {

		/**
		 * Clear all markers in the holder
		 */
		public void clear();

		/**
		 * Add a marker to the holder
		 *
		 * @param marker
		 *            the marker to be addeds
		 */
		public void addMarker(Marker marker);

		/**
		 * Remove a marker from the holder
		 *
		 * @param marker
		 *            the marker to be removed
		 */
		public void removeMarker(Marker marker);

		/**
		 * Get all markers in list format
		 *
		 * @return a list of all markers from the holder
		 */
		public List<Marker> getMarkers();
	}

	/**
	 * implementation of MarkerHolder with List
	 */
	public class ListMarkerHolder implements MarkerHolder {
		private List<Marker> markers = new ArrayList<Marker>();

		@Override
		public void clear() {
			this.markers.clear();
		}

		@Override
		public void addMarker(Marker marker) {
			if (marker == null
					|| marker.getRelativeStart() > marker.getRelativeEnd()
					|| marker.getRelativeStart() > getMax()
					|| marker.getRelativeEnd() > getMax()) {
				throw new IllegalArgumentException();
			}
			this.markers.add(marker);
		}

		@Override
		public void removeMarker(Marker marker) {
			this.markers.remove(marker);
		}

		@Override
		public List<Marker> getMarkers() {
			return this.markers;
		}
	}
}
