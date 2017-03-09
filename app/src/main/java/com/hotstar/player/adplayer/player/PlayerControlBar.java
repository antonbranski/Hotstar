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
 *  PlayerControlBar class contains the UI Components such as buttons for play,pause,rewind,closed captions and audio setting.
 *  It also contains a seek bar instance of the MarkableSeekbar class to define the timeline of the media item.
 *  This class contains the code for the listeners for each button click and also to define the user's interaction with the player. 
 */

package com.hotstar.player.adplayer.player;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.hotstar.player.R;
import com.adobe.mediacore.timeline.Timeline;
import com.adobe.mediacore.timeline.TimelineMarker;
import com.adobe.mediacore.timeline.advertising.AdBreakPlacement;
import com.adobe.mediacore.utils.TimeRange;
import com.hotstar.player.adplayer.AdVideoApplication;

import org.w3c.dom.Text;

import java.util.*;

public class PlayerControlBar {
	Context context;
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "PlayerControlBar";

	private final int SEEK_BAR_MAX_LEN = 1000;

	private static final int MS_DELTA = 500;
	private final static int MS_IN_SECOND = 1000;
	private final static int MS_IN_MINUTE = 60 * MS_IN_SECOND;
	private final static int MS_IN_HOUR = 60 * MS_IN_MINUTE;

	private float seekBarStep;

	private TimeRange localSeekableRange;
	private long position;

	private MarkableSeekBar seekBar;
	private ImageButton btnPlayPause;
	private View view;
	private TextView txtCurrentTime = null;
	private TextView txtTotalTime = null;

	private boolean isVisible = false;
	private boolean isPlaying = false;
	private boolean isLive = false;
    private boolean wasPlaying = false;

	private Timer fadeoutTimer;
	private final Handler handler = new Handler();
	private final ArrayList<ControlBarEventListener> eventListeners = new ArrayList<ControlBarEventListener>();

	class ControlBarEvent {
		private final Event eventType;
		private final Object eventInfo;

		ControlBarEvent(Event eventType, Object eventInfo) {
			this.eventType = eventType;
			this.eventInfo = eventInfo;
		}

		Event getEventType() {
			return eventType;
		}

		Object getEventInfo() {
			return eventInfo;
		}
	}

	/**
	 * An interface for handling control bar events
	 */
	public interface ControlBarEventListener {
		public void handleEvent(ControlBarEvent controlBarEvent);
	}

	/**
	 * Enum for list of player control bar events PLAY: playback starts PAUSE:
	 * playback pauses STOP: playback stops SEEK: jump to a particular position
	 * in the playback range SEEK_CLP: jump to the current live point of a live
	 * event
	 * 
	 */
	public enum Event {
		PLAY("play"), PAUSE("pause"), STOP("stop"), SEEK("seek"), SEEK_CLP(
				"seek_client_live_point");

		private final String name;

		Event(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	/**
	 * Create a player control bar object for the control bar view from the
	 * current context
	 * 
	 * @param context
	 *            the application context where this control bar is created in
	 * @param view
	 *            the UI component for the control bar
	 */
	public PlayerControlBar(Context context, ViewGroup view) {
		this.view = view;
		this.context = context;
		localSeekableRange = TimeRange.createRange(0, 0);

		view.setVisibility(View.INVISIBLE);

		// Update start and end time text based on the 0 playback range.
		updateTimeDisplay();

		// Setup play/pause/replay button
		btnPlayPause = (ImageButton) view.findViewById(R.id.btnPlayerControlPlayPause);
		btnPlayPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				togglePlayPause();
			}
		});

		// current time
		txtCurrentTime = (TextView) view.findViewById(R.id.txtCurrentTime);
		txtTotalTime = (TextView) view.findViewById(R.id.txtTotalTime);

		// Setup seekbar
		seekBar = (MarkableSeekBar) view
				.findViewById(R.id.sbPlayerControlSeekBar);
		seekBar.setMax(SEEK_BAR_MAX_LEN);
		setPosition(0);

		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean isFromUser) {

				if (isFromUser) {
					AdVideoApplication.logger
							.i(LOG_TAG
									+ "::SeekBar.OnSeekBarChangeListener#onProgressChanged",
									"New seek bar position: " + progress);
					setPosition((long) (progress * seekBarStep)
							+ localSeekableRange.getBegin());
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				AdVideoApplication.logger
						.i(LOG_TAG
								+ "::SeekBar.OnSeekBarChangeListener#onStartTrackingTouch",
								"Seek bar activated.");
				stopFadeOutTimer();

				// Remember if the player was playing, in order to restore this
				// state when seeking is done.
				wasPlaying = isPlaying;

				if (isPlaying) {
					// Pause the playback while seeking is performed.
					pressPause();
				}
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				AdVideoApplication.logger
						.i(LOG_TAG
								+ "::SeekBar.OnSeekBarChangeListener#onStopTrackingTouch",
								"Seek bar de-activated.");

				// Calculate seek position, based on the seek bar progress.
				// We round this position, but we make sure we don't exceed the
				// total duration.
				long seekPosition = Math.min(
						Math.round(seekBar.getProgress() * seekBarStep),
						localSeekableRange.getDuration());
				seek(localSeekableRange.getBegin() + seekPosition);
				AdVideoApplication.logger
						.i(LOG_TAG
								+ "::SeekBar.OnSeekBarChangeListener#onStopTrackingTouch",
								"Seek position is "
										+ (seekPosition + localSeekableRange
												.getBegin()));

				startFadeOutTimer();
			}
		});

	}

    /**
     * Determines if player was playing prior to user seek
     * @return true if player was playing prior to user action seek, false otherwise
     */
    public boolean wasPlaying() {
        return wasPlaying;
    }

	/**
	 * Get the UI view associated with this control bar object
	 * 
	 * @return the UI view of this control bar object
	 */
	public View getView() {
		return view;
	}

	public void addEventListener(ControlBarEventListener eventListener) {
		eventListeners.add(eventListener);
	}

	public void removeEventListener(ControlBarEventListener eventListener) {
		eventListeners.remove(eventListener);
	}

	/**
	 * Toggle play/pause image for play/pause/replay button. The replay image is
	 * not toggled on and off through this function.
	 */
	protected void togglePlayPause() {
		if (!isPlaying) {
			pressPlay();
		} else {
			pressPause();
		}

		resetFadeOutTimer();
	}

	/**
	 * Notify the listeners a seek event has occurred
	 * 
	 * @param position
	 *            where to seek to
	 */
	public void seek(long position) {
		dispatchEvent(new ControlBarEvent(Event.SEEK, position));
	}

	/**
	 * Notify the listeners a seek at live point event has occurred
	 */
	public void seekAtClientLivePoint() {
		dispatchEvent(new ControlBarEvent(Event.SEEK_CLP, null));
	}

	/**
	 * Press the pause button. Notify the listeners a pause event has occurred.
	 */
	public void pressPause() {
		isPlaying = false;
		showPlayButton();
		dispatchEvent(new ControlBarEvent(Event.PAUSE, null));
	}

	/**
	 * Press the play button. Notify the listeners a play event has occurred
	 */
	public void pressPlay() {
		isPlaying = true;
		showPauseButton();
		dispatchEvent(new ControlBarEvent(Event.PLAY, null));
	}
	
	/**
	 * Sets the control bar status to 'playing' and sets the play/pause button to 'pause'. 
	 * This function is only called when you make a seek after the video playback is 
	 * complete and control bar needs to be set to a playing state, this is different 
	 * from the pressPlay() call because it is not simulating a press play action nor is
	 * it notifying the listeners a play event has occurred
	 */
	public void setStatusToPlay() {
		isPlaying = true;
		showPauseButton();
	}

	/**
	 * Press the stop button Notify the listeners a stop event has occurred.
	 */
	public void pressStop() {
		isPlaying = false;
		showPlayButton();
		dispatchEvent(new ControlBarEvent(Event.STOP, null));
	}

	/**
	 * Set the stream type of the current stream
	 * 
	 * @param streamIsLive
	 *            whether this stream is a live event or not
	 */
	public void setStreamType(boolean streamIsLive) {
		isLive = streamIsLive;
	}

	/**
	 * Move the playhead in the seek bar to the given position. Update the
	 * timestamp text at the same time
	 * 
	 * @param position
	 *            where the playhead should be moved to
	 */
	synchronized public void setPosition(long position) {
		this.position = position - localSeekableRange.getBegin();
		updateTimeDisplay();

		if (seekBarStep != 0) {
			seekBar.setProgress((int) (this.position / seekBarStep));
		} else {
			seekBar.setProgress(0);
		}
	}

	/**
	 * Sets the seekable range of the control bar to be the local seekable range of the media item. 
	 * The local seekable range is defined to be the seekable range of the media item that is only
	 * primary content, it does not contain ad content.  
	 * 
	 * @param range
	 *            the time range represents the seekable range.
	 */
	synchronized public void setSeekabledRange(TimeRange timeRange) {
		if (timeRange == null || timeRange.getDuration() == 0) {
			seekBar.setRange(0, 0, MarkableSeekBar.RangeType.SEEK);
			return;
		}
		localSeekableRange = timeRange;
		seekBarStep = timeRange.getDuration() / (float) SEEK_BAR_MAX_LEN;
	}

	/**
	 * Hide the control bar.
	 */
	public void hide() {
		Activity activity = ((Activity) context); /* getActivity() */
		if (isVisible && activity != null && isAutohideEnabled()) {
			AdVideoApplication.logger.i(LOG_TAG + "#hide", "Hiding the control bar.");

			view.clearAnimation();
			Animation fadeOutAnimation = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.fade_out);
			view.startAnimation(fadeOutAnimation);
			view.setVisibility(View.GONE);

			hideActionBar(activity);

			isVisible = false;
		}
	}

	/**
	 * Show the control bar.
	 */
	public void show() {
		Activity activity = ((Activity) context);
		if (!isVisible) {
			AdVideoApplication.logger.i(LOG_TAG + "#show", "Showing the control bar.");
			view.clearAnimation();
			Animation fadeInAnimation = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.fade_in);
			view.startAnimation(fadeInAnimation);
			view.setVisibility(View.VISIBLE);

			showActionBar(activity);
			isVisible = true;
		}

		resetFadeOutTimer();
	}

	/**
	 * Enable the seek bar.
	 */
	public void enable() {
		AdVideoApplication.logger.i(LOG_TAG + "#enable", "Enabling play control bar.");
		seekBar.setEnabled(true);
	}

	/**
	 * Disable the seek bar.
	 */
	public void disable() {
		AdVideoApplication.logger.i(LOG_TAG + "#disable", "Disabling play control bar.");
		hide();
		seekBar.setEnabled(false);
	}

	/**
	 * Determine if the control bar should be auto hidden
	 * 
	 * @return true if users set auto hide to true, false otherwise.
	 */
	private boolean isAutohideEnabled() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(((Activity) context) /*
																 * (getActivity()
																 */);
		return sharedPreferences.getBoolean(
				AdVideoApplication.SETTINGS_CONTROLBAR_AUTOHIDE,
				AdVideoApplication.DEFAULT_AUTOHIDE_CONTROL_BAR);
	}

	/**
	 * Display the pause image in the play/pause/replay button.
	 */
	private void showPauseButton() {
		btnPlayPause.setImageResource(R.drawable.pause);
		btnPlayPause.setContentDescription("pause");
	}

	/**
	 * Display the play image in the play/pause/replay button.
	 */
	private void showPlayButton() {
		btnPlayPause.setImageResource(R.drawable.play);
		btnPlayPause.setContentDescription("play");
	}

	/**
	 * Hide the action bar from the given activity.
	 * 
	 * @param activity
	 *            the activity where the action bar belongs to
	 */
	private void hideActionBar(Activity activity) {
		if (activity != null) {
			ActionBar actionBar = ((ActionBarActivity) activity).getSupportActionBar();
			if (actionBar != null) {
				actionBar.hide();
			}
		}
	}

	/**
	 * Show the action bar from the given activity
	 * 
	 * @param activity
	 *            the activity where the action bar belongs to
	 */
	private void showActionBar(Activity activity) {
		if (activity != null) {
			ActionBar actionBar = ((ActionBarActivity) activity).getSupportActionBar();
			if (actionBar != null) {
				actionBar.show();
			}
		}
	}

	/**
	 * Start the fade out timer. The fade out timer is the time it takes before
	 * hiding the control bar.
	 */
	private void startFadeOutTimer() {
		fadeoutTimer = new Timer();
		long STAY_VISIBLE_DURATION = 5000;
		fadeoutTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						AdVideoApplication.logger.i(LOG_TAG + "#startFadeOutTimer", "Hiding the bar from timer thread.");
						PlayerControlBar.this.hide();
					}
				});
			}
		}, STAY_VISIBLE_DURATION);
	}

	/**
	 * Stop the fade out timer.
	 */
	private void stopFadeOutTimer() {
		AdVideoApplication.logger.i(LOG_TAG + "#stopFadeOutTimer", "Stopping the fade-out timer.");
		if (fadeoutTimer != null) {
			fadeoutTimer.cancel();
		}
		view.clearAnimation();
		view.setVisibility(View.VISIBLE);
	}

	/**
	 * Reset the fade out timer. The timer resets when we need an time extension
	 * to hide the control bar due to playback/user event.
	 */
	private void resetFadeOutTimer() {
		AdVideoApplication.logger.i(LOG_TAG + "#resetFadeOutTimer", "Restarting the fade-out timer.");
		stopFadeOutTimer();
		startFadeOutTimer();
	}

	/**
	 * Convert a time stamp to text in HH:MM:SS format.
	 * 
	 * @param timeStamp
	 *            the time in milliseconds
	 * @return a String representation of the time in HH:MM:SS format
	 */
	private String timeStampToText(long timeStamp) {
		if (timeStamp % 1000 >= MS_DELTA) {
			timeStamp += MS_DELTA;
		}

		long hours = timeStamp / (MS_IN_HOUR);
		long minutes = (timeStamp - (hours * MS_IN_HOUR)) / MS_IN_MINUTE;
		long seconds = (timeStamp - (hours * MS_IN_HOUR) - (minutes * MS_IN_MINUTE))
				/ MS_IN_SECOND;

		StringBuilder stringBuilder = new StringBuilder();
		if (hours > 0) {
			stringBuilder.append(leadingZero(hours));
			stringBuilder.append(":");
		}
		stringBuilder.append(leadingZero(minutes));
		stringBuilder.append(":");
		stringBuilder.append(leadingZero(seconds));

		return stringBuilder.toString();
	}

	/**
	 * Add '0' in front of a number if it's less than 10
	 * 
	 * @param number
	 *            the number to add the leading 0 to
	 * @return a String representation of the number with leading zero.
	 */
	private String leadingZero(long number) {
		return (number < 10) ? "0" + number : "" + number;
	}

	/**
	 * if VOD stream, update current time and end time of the control bar if
	 * LIVE stream, set the text as LIVE
	 */
	private void updateTimeDisplay() {
		if (txtCurrentTime != null)
			txtCurrentTime.setText(timeStampToText(position + localSeekableRange.getBegin()));

		if (txtTotalTime != null)
			txtTotalTime.setText(timeStampToText(localSeekableRange.getEnd()));

		AdVideoApplication.logger.e(
				LOG_TAG + "::currentTime()",
				timeStampToText(position
						+ localSeekableRange.getBegin())
						+ "/" + timeStampToText(localSeekableRange.getEnd()));

	}

	/**
	 * Dispatch a control bar event to the control bar listener
	 * 
	 * @param controlBarEvent
	 *            the control bar event to be dispatched
	 */
	private void dispatchEvent(ControlBarEvent controlBarEvent) {
		for (ControlBarEventListener listener : eventListeners) {
			listener.handleEvent(controlBarEvent);
		}
	}

	/**
	 * Set the playback timeline with admarkers in the seek bar
	 * 
	 * @param timeline
	 *            the timeline to be set that contains ad markers
	 */
	public void setTimeline(Timeline<?> timeline) {
		seekBar.getAdMarkerHolder().clear();
		if (timeline == null)
			return;

		// Add the seek bar markers.
		Iterator<? extends TimelineMarker> iterator = timeline.timelineMarkers();
		while (iterator.hasNext()) {
			TimelineMarker timelineMarker = iterator.next();
			AdBreakPlacement ad = (AdBreakPlacement) timelineMarker;
			long localTime = ad.getAdBreak().getLocalTime();
			MarkableSeekBar.Marker marker = MarkableSeekBar.Marker
					.createMarker(localTime, timelineMarker.getDuration(),
							localSeekableRange.getBegin(),
							localSeekableRange.getDuration(), SEEK_BAR_MAX_LEN);
			try {
				seekBar.getAdMarkerHolder().addMarker(marker);
			}
			catch (IllegalArgumentException e) {
				AdVideoApplication.logger.e(LOG_TAG + "::setTimeline()",
						"Failed to add ad marker on the seek bar." + marker.toString());
			}
		}
	}

	/**
	 * Set the playback timeline with overlay Admarkers in the seek bar
	 *
	 * @param timeline
	 */
	public void setOverlayAdTimeline(ArrayList timeline) {
		seekBar.getOverlayAdMarkerHolder().clear();
		if (timeline == null)
			return;

		// Add the seek bar markers.
		for (Object object : timeline)
		{
			if (object.getClass().equals(AdBreakPlacement.class)) {
				AdBreakPlacement ad = (AdBreakPlacement) object;
				long localTime = ad.getAdBreak().getTime();
				MarkableSeekBar.Marker marker = MarkableSeekBar.Marker.createMarker(localTime, ad.getDuration(),
								localSeekableRange.getBegin(),
								localSeekableRange.getDuration(), SEEK_BAR_MAX_LEN);
				try {
					seekBar.getOverlayAdMarkerHolder().addMarker(marker);
				}
				catch (IllegalArgumentException e) {
					AdVideoApplication.logger.e(
							LOG_TAG + "::setTimeline()",
							"Failed to add overlay ad marker on the seek bar." + marker.toString());
				}
			}
		}
	}

	/**
	 * Set the playback timeline with vpaid admarkers in the seek bar
	 *
	 * @param timeline
	 */
	public void setVPaidAdTimeline(ArrayList timeline) {
		seekBar.getVPaidAdMarkerHolder().clear();
		if (timeline == null)
			return;

		// Add the seek bar markers
		for (Object object : timeline)
		{
			if (object.getClass().equals(AdBreakPlacement.class)) {
				AdBreakPlacement ad = (AdBreakPlacement) object;
				long localTime = ad.getAdBreak().getTime();
				MarkableSeekBar.Marker marker = MarkableSeekBar.Marker.createMarker(localTime, ad.getDuration(),
						localSeekableRange.getBegin(),
						localSeekableRange.getDuration(), SEEK_BAR_MAX_LEN);
				try {
					seekBar.getVPaidAdMarkerHolder().addMarker(marker);
				}
				catch (IllegalArgumentException e) {
					AdVideoApplication.logger.e(
							LOG_TAG + "::setTimeline()",
							"Failed to add vpaid ad marker on the seek bar." + marker.toString());
				}
			}
		}
	}

	/**
	 * Add a cue marker to the seek bar. This is different from setTimeline as
	 * this one only adds one marker to the seek bar.
	 * 
	 * @param time
	 *            the start time of the marker
	 * @param duration
	 *            the duration of the marker
	 */
	public void addOpportunity(long time, long duration) {
		MarkableSeekBar.Marker cue = MarkableSeekBar.Marker.createMarker(time,
				duration, localSeekableRange.getBegin(),
				localSeekableRange.getDuration(), SEEK_BAR_MAX_LEN);

		try {
			seekBar.getCueMarkerHolder().addMarker(cue);
		}
		catch (IllegalArgumentException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::addOpportunity()",
					"Failed to add cue on the seek bar." + cue.toString()
							+ localSeekableRange.toString());
		}
	}

	/**
	 * Invalidate the seek bar ad and cue markers if they need to be redraw.
	 * This is only called during the live event as the playback range shifts
	 */
	public void update() {
		boolean redrawMarkers = updateSeekBarMarkers(seekBar.getAdMarkerHolder());
		boolean redrawCues = updateSeekBarMarkers(seekBar.getCueMarkerHolder());

		if (redrawCues || redrawMarkers) {
			seekBar.invalidate();
		}
	}

	/**
	 * Update the seek bar markers by removing or updating them in the new
	 * playback range.
	 * 
	 * @param markerHolder
	 *            the holder of all markers
	 * @return true if markerHolder is not empty (markers must have been
	 *         redraw), false otherwise.
	 * 
	 */
	private boolean updateSeekBarMarkers(MarkableSeekBar.MarkerHolder markerHolder) {
		boolean redrawMarkers = false;
		List<MarkableSeekBar.Marker> markerList = markerHolder.getMarkers();

		if (markerList != null && markerList.size() != 0) {
			for (int index = markerList.size() - 1; index >= 0; index--) {
				MarkableSeekBar.Marker marker = markerList.get(index);
				long startTime = marker.getTime();
				long endTime = marker.getTime() + marker.getDuration();

				if (localSeekableRange.contains(startTime)
						|| localSeekableRange.contains(endTime)) {
					int start = (int) (((startTime - localSeekableRange
							.getBegin()) / (float) localSeekableRange
							.getDuration()) * SEEK_BAR_MAX_LEN);
					int end = start + MarkableSeekBar.AD_CUE_SIZE;
					if (start < 0 && end < SEEK_BAR_MAX_LEN) {
						start = 0;
					} else if (end > SEEK_BAR_MAX_LEN) {
						end = SEEK_BAR_MAX_LEN;
					}

					if (end <= 0) {
						markerHolder.removeMarker(marker);
					} else {
						marker.updateMarker(start, end);
					}
				}
			}
			redrawMarkers = true;
		}

		return redrawMarkers;
	}

}
