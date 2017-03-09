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

package com.hotstar.player.adplayer.manager;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap.Config;

import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.MediaPlayer.PlayerState;
import com.adobe.mediacore.ABRControlParameters;
import com.adobe.mediacore.BufferControlParameters;
import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.info.Profile;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.adobe.mediacore.qos.LoadInfo;
import com.adobe.mediacore.timeline.Timeline;
import com.adobe.mediacore.utils.TimeRange;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.config.IPlaybackConfig;

/**
 * 
 * The PlaybackManager handles the main playback functionality of the
 * CoreHLSPlayer by wrapping the PSDK's DefaultMediaPlayer object
 * 
 */
public class PlaybackManager implements IManager {
	private IPlaybackConfig playbackConfig;
	private MediaPlayer mediaPlayer;
	private int currentVolume;
	private boolean isPrepared;
	private boolean isBuffering;
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME
			+ PlaybackManager.class.getSimpleName();
	private final ArrayList<PlaybackManagerEventListener> eventListeners = new ArrayList<PlaybackManagerEventListener>();

	/**
	 * 
	 * Class constructor. This method begins the PlaybackManager's
	 * initialisation process.
	 * 
	 * @param configProvider
	 *            which is used to configure playback settings for the manager
	 * @param player
	 *            the PSDK's implementation of MediaPlayer which the manager
	 *            wraps
	 * 
	 */
	public PlaybackManager(IPlaybackConfig playbackConfig, MediaPlayer mediaPlayer) {
		this.playbackConfig = playbackConfig;
		this.mediaPlayer = mediaPlayer;

		this.mediaPlayer.addEventListener(MediaPlayer.Event.QOS, qosEventListener);
		this.mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
	}

	/**
	 * This method initiates the process of PlaybackManager clean-up. It
	 * de-registers any event handlers on the DefaultMediaPlayer instance.
	 * 
	 */
	@Override
	public void destroy() {
		mediaPlayer.removeEventListener(MediaPlayer.Event.QOS, qosEventListener);
		mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
	}

	/**
	 * PlaybackManagerEventListener: listeners that can respond appropriately to
	 * Video Events
	 */
	public interface PlaybackManagerEventListener {

		/**
		 * Handle buffer start event
		 */
		public void onBufferStart();

		/**
		 * Handle buffer complete event
		 */
		public void onBufferComplete();

		/**
		 * Handle seeking event when seeking started
		 * 
		 */
		public void onSeeking();

		/**
		 * Handle seek complete event
		 * 
		 * @param localAdjustedTime
		 *            the timestamp in the current playback timeline where the
		 *            seek operation took place
		 */
		public void onSeekCompleted(long localAdjustedTime);

		/**
		 * Handle the event when the media player has successfully prepared the
		 * media
		 */
		public void onPrepared();

		/**
		 * Handle the event when the playback of the media resource has started
		 */
		public void onPlaying();

		/**
		 * Handle the event when the end of the media resource has been reached
		 */
		public void onComplete();

		/**
		 * Handle the event when the media resource size is available
		 * 
		 * @param height
		 *            the height of the media resource size
		 * @param width
		 *            the width of the media resourcd size
		 */
		public void onDimensionsChange(long height, long width);

		/**
		 * Handle the event when the media player changes its state
		 * 
		 * @param state
		 *            the new state the playback changed to
		 * @param notification
		 *            the notification from PSDK playback event
		 */
		public void onStateChange(PlayerState state,
				MediaPlayerNotification notification);

		/**
		 * Handle the event when a playback error occurs
		 * 
		 * @param error
		 *            the media notification object that includes error code and
		 *            description of the playback error
		 * 
		 */
		public void onError(MediaPlayerNotification error);

		/**
		 * Handle the event when the media player has successfully updated the
		 * media from live assests refresh
		 * 
		 * @param localSeekablekRange
		 *            the new local seekable range from the media resource
		 * @param timeline
		 *            the new timeline from the media resource
		 */
		public void onUpdate(TimeRange localSeekableRange, Timeline<?> timeline);
	}

	public void addEventListener(PlaybackManagerEventListener eventListener) {
		eventListeners.add(eventListener);
	}

	public void removeEventListener(PlaybackManagerEventListener eventListener) {
		eventListeners.remove(eventListener);
	}

	/**
	 * 
	 * This convenience method provides an easy way to configure the
	 * PlaybackManager for playback. This method creates a media resource using
	 * AdsManager and the resource URL.
	 * 
	 * @param url
	 *            the URL of the video stream
	 * @param adsManager
	 *            the feature manager to handle ad metadata setup in the player
	 */
	public void setupVideo(String url, AdsManager adsManager) {
		setBufferControlParams();
		MediaResource playerResource = MediaResource.createFromUrl(url, adsManager.getAdvertisingMetadata());
		adsManager.adjustAdSignalingMode(playerResource);
		mediaPlayer.replaceCurrentItem(playerResource);
		if(playbackConfig.forceOpenMAXAL()) {
			mediaPlayer.setCustomConfiguration("forceOMXAL");
		}
	}

	/**
	 * Play the media resource prepared in the player If the player is in
	 * PREPARED, or PAUSED state, play the video from its current time If the
	 * player is in COMPLETE state, play the video from the beginning If the
	 * player is in PLAYING state, do nothing
	 * 
	 * All other states will be ignored
	 */
	public void play()
	{
		switch (mediaPlayer.getStatus())
		{
			case COMPLETE:
				mediaPlayer.seek(0);
				break;
			case PREPARED:
			case PAUSED:
				AdVideoApplication.logger.i(LOG_TAG + "::PlayerControlBar.ControlBarEventListener#handleEvent()", "Starting playback.");
				mediaPlayer.play();
				break;
			case SUSPENDED:
				AdVideoApplication.logger.i(LOG_TAG + "::PlayerControlBar.ControlBarEventListener#handleEvent()", "Starting playback.");
				mediaPlayer.play();
				break;
			default:
				AdVideoApplication.logger.d(LOG_TAG + "::PlayerControlBar.ControlBarEventListener#handleEvent()",
								"Ignoring play event due to current player state: " + mediaPlayer.getStatus());
		}
	}

	/**
	 * Pause the media resource playing in the player
	 * 
	 * The player has to be in PLAYING state. All other states will be ignored
	 */
	public void pause()
	{
		AdVideoApplication.logger.i(LOG_TAG + "::PlayerControlBar.ControlBarEventListener#handleEvent()", "Pausing the player instance.");
		if (mediaPlayer.getStatus() == PlayerState.PLAYING) {
			mediaPlayer.pause();
		}
	}

	/**
	 * 
	 * This convenience method provides an easy way to toggle between play and
	 * pause. If the current playback status is PLAYING this method pauses the
	 * content and returns false. If the current playback status is PAUSED this
	 * method plays the content and returns true. If the PlaybackManager detects
	 * that the current playhead is outwith the seekable range the method shifts
	 * the playhead to the live playhead. If the current playback status is
	 * COMPLETED this method resets the player to beginning of the content and
	 * returns false.
	 * 
	 * @return true if the new state is play and false if new state is pause.
	 */
	public boolean playPauseVideo()
	{
		if (mediaPlayer.getStatus() == PlayerState.PLAYING) {
			mediaPlayer.pause();
			return false;
		}
		else if (mediaPlayer.getStatus() == PlayerState.COMPLETE
				|| mediaPlayer.getStatus() == PlayerState.PREPARED
				|| mediaPlayer.getStatus() == PlayerState.PAUSED) {
			mediaPlayer.play();
			return true;
		}

		return false;
	}

	/**
	 * Performs a seeking operation on the relative timeline (timeline resolved 
	 * with ads) for the playback. When seek completes, the play head does not 
	 * always move to the given seek position. PlaybackManager returns the adjusted 
	 * time that play head actually moves to on the onSeekComplete event.
	 * 
	 * @param position
	 *            the seek position the play head should move to
	 */
	public void seek(long relativePosition) {
		mediaPlayer.seek(relativePosition);
	}
	
	/**
	 * Performs a seeking operation within primary content of the media item. It will 
	 * only seek to places in the timeline with primary content, it will not seek to 
	 * places with ad content. 
	 * @param localPosition
	 * 			  the local seek position the play head should move
	 */
	public void seekToLocal(long localPosition) {
		mediaPlayer.seekToLocalTime(localPosition);
	}

	/**
	 * 
	 * This convenience method provides an easy way to mute the
	 * PlaybackManager's volume
	 * 
	 */
	public void mute() {
		setVolume(0);
	}

	/**
	 * 
	 * This convenience method provides an easy way to unmute the
	 * PlaybackManager
	 * 
	 */
	public void unmute() {
		setVolume(currentVolume);
	}

	/**
	 * 
	 * This setter method configures the volume of the PlaybackManager.
	 * 
	 * @param volume
	 *            the desired volume within the range 0-100
	 * 
	 */
	public void setVolume(int volume) {
		AdVideoApplication.logger.i(LOG_TAG + "#setVolume", "Changing volume to [" + String.valueOf(volume) + "].");
		mediaPlayer.setVolume(volume);
		if (volume > 0) {
			currentVolume = volume;
		}
	}

	/**
	 * 
	 * This getter method returns the PlaybackManager's current volume level.
	 * 
	 * @return volume the current volume
	 * 
	 */
	public int getVolume() {
		return currentVolume;
	}

	/**
	 * Determine if the media resource is a live event
	 * 
	 * @return true if the media resource is a live stream, false if it is a VOD
	 *         asset
	 */
	public boolean isLive() {
		return mediaPlayer.getCurrentItem() != null && mediaPlayer.getCurrentItem().isLive();
	}

	/**
	 * Determine if playback has completed
	 * 
	 * @return true if playback is completed, false otherwise
	 */
	public boolean isCompleted() {
		return mediaPlayer.getStatus() == MediaPlayer.PlayerState.COMPLETE;
	}

	/**
	 * Determine if playback is paused
	 * 
	 * @return true if playback is paused, false otherwise
	 */
	public boolean isPaused() {
		return mediaPlayer.getStatus() == MediaPlayer.PlayerState.PAUSED;
	}

	/**
	 * Determine if playback is currently playing
	 * 
	 * @return true if playback is currently playing, false otherwise
	 */
	public boolean isPlaying() {
		return mediaPlayer.getStatus() == MediaPlayer.PlayerState.PLAYING;
	}

	/**
	 * Returns whether the current resource is protected.
	 * 
	 * @return true if the resource is protected, false otherwise.
	 */
	public boolean isProtected() {
		return mediaPlayer.getCurrentItem().isProtected();
	}

	/**
	 * Determine if the media player is prepared to play the item. If the player
	 * is already playing the item, it is considered as prepared
	 * 
	 * @return true if the media player is prepared, false otherwise
	 */
	public boolean isPrepared() {
		return isPrepared;
	}

	/**
	 * Determine if the playback is currently buffering
	 * 
	 * @return true if the playback is buffering, false otherwise
	 */
	public boolean isBuffering() {
		return isBuffering;
	}

	/**
	 * Returns the player's buffer range
	 * 
	 * @return TimeRange the player's buffer range
	 */
	public TimeRange getBuffedRange() {
		return mediaPlayer.getBufferedRange();
	}

	/**
	 * Get the status of the player in String format
	 * 
	 * @return PlayerState the player status
	 */
	public PlayerState getStatus() {
		return mediaPlayer.getStatus();
	}

	/**
	 * This getter method returns the current playhead time of the currently
	 * loaded content.
	 * 
	 * @return the current playback time in long value
	 */
	public long getCurrentTime() {
		return mediaPlayer.getCurrentTime();
	}

	/**
	 * Returns the current full playbackRange object.
	 * 
	 * @return the current playback range
	 */
	public TimeRange getPlaybackRange() {
		return mediaPlayer.getPlaybackRange();
	}

	/**
	 * Get the start time time of the current playback range
	 * 
	 * @return long the start timestamp in the current playback range
	 */
	public long getPlaybackRangeStart() {
		return mediaPlayer.getPlaybackRange().getBegin();
	}

	/**
	 * Get the end time of the current playback range
	 * 
	 * @return long the end timestamp in the current playback range
	 */
	public long getPlaybackRangeEnd() {
		return mediaPlayer.getPlaybackRange().getEnd();
	}

	/**
	 * 
	 * This getter method returns the local time of the primary content.
	 * 
	 * @return the local time in long value
	 * 
	 */
	public long getLocalTime() {
		return mediaPlayer.getLocalTime();
	}

	public TimeRange getLocalSeekRange() {
		TimeRange localSeekRange = TimeRange.createRange(
				mediaPlayer.convertToLocalTime(mediaPlayer.getSeekableRange().getBegin()),
				mediaPlayer.convertToLocalTime(mediaPlayer.getSeekableRange().getEnd())
						- mediaPlayer.convertToLocalTime(mediaPlayer.getSeekableRange().getBegin()));
		return localSeekRange;

	}

	public long getLocalSeekRangeStart() {
		return mediaPlayer.convertToLocalTime(mediaPlayer.getSeekableRange().getBegin());
	}

	public long getLocalSeekRangeEnd() {
		return mediaPlayer.convertToLocalTime(mediaPlayer.getSeekableRange().getEnd());
	}

	/**
	 * Returns the player's timeline.
	 * 
	 * @return the player's timeline with timeline markers.
	 */
	@SuppressWarnings("rawtypes")
	public Timeline getTimeline() {
		return mediaPlayer.getTimeline();
	}

	/**
	 * Returns the TimedMetadata of the currently loaded content.
	 * 
	 * @return a list of timed metadata
	 * 
	 */
	public List<TimedMetadata> getTimedMetadata() {
		return mediaPlayer.getCurrentItem().getTimedMetadata();
	}

	/**
	 * Determine if ABR control is enabled
	 * 
	 * @return true if ABR control is enabled, false otherwise
	 */
	private boolean isAbrControlEnabled() {
		return playbackConfig.isABRControlEnabled();
	}

	/**
	 * This method configures the ABR control parameters from the
	 * IPlaybackConfig object.
	 * 
	 */
	public void setAbrControlParams()
	{
		if (isAbrControlEnabled()) {
			int initialBitRate = playbackConfig.getABRInitialBitRate();
			int minBitRate = playbackConfig.getABRMinBitRate();
			int maxBitRate = playbackConfig.getABRMaxBitRate();
			ABRControlParameters.ABRPolicy abrPolicy = playbackConfig.getABRPolicy();
			abrPolicy = abrPolicy != null ? abrPolicy : ABRControlParameters.ABRPolicy.ABR_MODERATE;
			mediaPlayer.setABRControlParameters(new ABRControlParameters(initialBitRate, minBitRate, maxBitRate, abrPolicy));
		}
	}

	/**
	 * 
	 * This method configures the Buffer control parameters from the
	 * IPlaybackConfig object.
	 * 
	 */
	public void setBufferControlParams()
	{
		try
		{
			mediaPlayer.setBufferControlParameters(getBufferParamsFromSettings());
		}
		catch (IllegalArgumentException e) {
			AdVideoApplication.logger.w(LOG_TAG + "#setBufferControlParams", "Unable to apply buffering params: " + e.getMessage() + ".");
		}
	}

	/**
	 * 
	 * This getter method returns the currently configured PlaybackManager
	 * buffering profile.
	 * 
	 * 
	 * public BufferControlParameters getBufferControlParams() { return
	 * mediaPlayer.getBufferControlParameters(); }
	 * 
	 * /** Create a buffer control parameter from bufferring config
	 * 
	 * @return BufferControlParameters with the initial buffer time and play
	 *         buffer time value
	 */
	private BufferControlParameters getBufferParamsFromSettings()
	{
		long initBufferTime = playbackConfig.getInitBufferTime();
		long playBufferTime = playbackConfig.getBufferTime();
		return BufferControlParameters.createDual(initBufferTime, playBufferTime);
	}

	/**
	 * Log the bitrate profiles of the media resource
	 */
	private void showProfiles()
	{
		if (mediaPlayer == null || mediaPlayer.getCurrentItem() == null || mediaPlayer.getCurrentItem().getProfiles() == null) {
			return;
		}

		List<Profile> profiles = mediaPlayer.getCurrentItem().getProfiles();
		for (Profile profile : profiles) {
			AdVideoApplication.logger.i(LOG_TAG + "#showProfiles()", "Profile bitrate: " + profile.getBitrate());
		}
	}

	/**
	 * PlaybackEventListener that intercepts PSDK playback events
	 */
	private final MediaPlayer.PlaybackEventListener playbackEventListener = new MediaPlayer.PlaybackEventListener() {

		/**
		 * When the media player prepared the media, notify the listeners to
		 * handle the event.
		 */
		@Override
		public void onPrepared() {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onPrepared()", "Media prepared.");

			if (mediaPlayer.getCurrentItem() == null) {
				// This could happen if we were to call reset prematurely.
				return;
			}

			isPrepared = true;
			showProfiles();
			setAbrControlParams();
			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onPrepared();
			}
		}

		/**
		 * When the media player updated the media from live asset manifest
		 * refreshes, notify the listeners to handle the event.
		 */
		@Override
		public void onUpdated() {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onUpdated()", "Media refreshed.");
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onUpdated()",
					"New playback range is " + mediaPlayer.getPlaybackRange());
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onUpdated()",
					"New seekable range is " + mediaPlayer.getSeekableRange());
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onUpdated()",
					"New buffered range is " + mediaPlayer.getBufferedRange());

			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onUpdate(getLocalSeekRange(),
						mediaPlayer.getTimeline());
			}
		}

		/**
		 * When the playback has started, notify the listeners to handle the
		 * event
		 */
		@Override
		public void onPlayStart() {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onPlayStart()", "Playback started.");
			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onPlaying();
			}
		}

		/**
		 * When the playback has reached the end, notify the listeners to handle
		 * the event.
		 */
		@Override
		public void onPlayComplete() {
			long currentTime = mediaPlayer.getCurrentTime();
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onPlayComplete()",
					"Time on playback complete [" + String.valueOf(currentTime) + "].");
			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onComplete();
			}
		}

		/**
		 * When the size of the media is available, notify the listeners to
		 * handle the event
		 */
		@Override
		public void onSizeAvailable(long height, long width) {
			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onDimensionsChange(height, width);
			}
		}

		/**
		 * When the media player has a new state, notify the listeners to handle
		 * the event
		 */
		@Override
		public void onStateChanged(MediaPlayer.PlayerState state,
				MediaPlayerNotification notification) {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlayerStateEventListener#onStateChanged()",
							"Player state changed to [" + state + "].");

			if (state == PlayerState.INITIALIZED) {
				// for live streams with DVR, checks to see if custom start
				// position is enabled
				if (isLive() && playbackConfig.isCustomPositionPrefEnabled()) {
					mediaPlayer.prepareToPlay(playbackConfig
							.retrieveStartTimePref());
				} else {
					mediaPlayer.prepareToPlay();
				}
			}
			else if (state == PlayerState.ERROR) {
				AdVideoApplication.logger.e(LOG_TAG + "::MediaPlayer.PlayerStateEventListener#onStateChanged()",
								"Error: " + notification + ".");
				long major = Long.valueOf(notification.getMetadata().getValue("NATIVE_ERROR_CODE"));
				if (!DrmManager.isDrmError(major)) {
					for (PlaybackManagerEventListener listener : eventListeners) {
						listener.onError(notification);
					}
				}
			}
			else {
				for (PlaybackManagerEventListener listener : eventListeners) {
					listener.onStateChange(state, notification);
				}
			}
		}

		@Override
		public void onTimelineUpdated() {

		}

		@Override
		public void onTimedMetadata(TimedMetadata metadata) {

		}

		@Override
		public void onRatePlaying(float rate) {

		}

		@Override
		public void onRateSelected(float rate) {

		}

		@Override
		public void onReplaceMediaPlayerItem() {

		}

		@Override
		public void onProfileChanged(long profile, long time) {
			
		}

	};

	/**
	 * QosEventListener that intercepts PSDK events
	 */
	private final MediaPlayer.QOSEventListener qosEventListener = new MediaPlayer.QOSEventListener() {

		/**
		 * When the buffering starts, notify the listeners to handle the event
		 */
		@Override
		public void onBufferStart() {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.QOSEventListener#onBufferStart()",
					"Buffering started.");
			isBuffering = true;
			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onBufferStart();
			}
		}

		/**
		 * When the buffering completes, notify the listeners to handle the
		 * event
		 */
		@Override
		public void onBufferComplete() {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.QOSEventListener#onBufferComplete()",
					"Buffering complete.");
			isBuffering = false;
			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onBufferComplete();
			}
		}

		/**
		 * When the seek operation starts, log the event
		 */
		@Override
		public void onSeekStart() {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.QOSEventListener#onSeekStart()",
					"Seek starting.");
			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onSeeking();
			}
		}

		/**
		 * When the seek operation completes, notify the listeners to handle the
		 * event
		 */
		@Override
		public void onSeekComplete(long adjustedTime)
		{
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.QOSEventListener#onSeekComplete()",
					"Seek complete at position: " + adjustedTime + ".");
			for (PlaybackManagerEventListener listener : eventListeners) {
				listener.onSeekCompleted(mediaPlayer.convertToLocalTime(adjustedTime));
			}
		}

		/**
		 * When a fragment successfully downloaded, log the event
		 */
		@Override
		public void onLoadInfo(LoadInfo loadInfo) {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.QOSEventListener#onLoadInfo()",
					"Url: " + loadInfo.getUrl() + ", size: "
							+ loadInfo.getSize() + " bytes, download duration: "
							+ loadInfo.getDownloadDuration() + "ms");
		}

		/**
		 * When PSDK operation failed, notify the listeners to handle the event
		 */
		@Override
		public void onOperationFailed(MediaPlayerNotification.Warning warning)
		{
			StringBuffer sb = new StringBuffer("Player operation failed: ");
			sb.append(warning.getCode()).append(" - ").append(warning.getDescription());
			if (warning.getMetadata() != null) {
				sb.append("Warning metadata: ").append(
						warning.getMetadata().toString());
			}

			MediaPlayerNotification innerNotification = warning
					.getInnerNotification();
			while (innerNotification != null) {
				sb.append("Inner notification: ");
				sb.append(innerNotification.getCode()).append(" - ")
						.append(innerNotification.getDescription());
				Metadata metadata = innerNotification.getMetadata();
				if (metadata != null) {
					for (String key : metadata.keySet()) {
						sb.append(" - ").append(key).append(": ")
								.append(metadata.getValue(key));
					}
				}
				innerNotification = innerNotification.getInnerNotification();
			}

			String errMsg = sb.toString();

			AdVideoApplication.logger.w(LOG_TAG
					+ "::MediaPlayer.QOSEventListener#onOperationFailed()",
					errMsg);
		}
	};

	public static final String SETTINGS_ABR_CTRL_ENABLED = "settings_abr_enabled";
	public static final String SETTINGS_ABR_INITIAL_BITRATE = "settings_abr_initial_bit_rate";
	public static final String SETTINGS_ABR_MIN_BITRATE = "settings_abr_min_bit_rate";
	public static final String SETTINGS_ABR_MAX_BITRATE = "settings_abr_max_bit_rate";
	public static final String SETTINGS_ABR_POLICY = "settings_abr__policy";

	public static final int DEFAULT_INIT_BIT_RATE = 0;
	public static final int DEFAULT_MIN_BIT_RATE = 0;
	public static final int DEFAULT_MAX_BIT_RATE = 2500000;
	public static final boolean DEFAULT_ABR_CTRL_ENABLED = false;

	public static final String SETTINGS_BUFFER_INIT = "settings_init_buffer";
	public static final String SETTINGS_BUFFER_TIME = "settings_buffer_time";

	public static final int DEFAULT_INIT_BUFFER = 2000;
	public static final int DEFAULT_BUFFER_TIME = 30000;
	
	public static final String SETTINGS_FORCE_OMXAL = "settings_force_omxal";	
	public static final boolean DEFAULT_FORCE_OMXAL = false;

	public static final String SETTINGS_DVR_START_TIME_ENABLED = "settings_dvr_start_time_enabled";
	public static final String SETTINGS_DVR_START_TIME = "settings_dvr_start_time";
	public static final String SETTINGS_DVR_SEEKING_OUTSIDE_ENABLED = "settings_dvr_seeking_outside_enabled";
	public static final String SETTINGS_DVR_SEEKING_CLP_ENABLED = "settings_dvr_seeking_clp_enabled";
	public static final String SETTINGS_DVR_SEEKING_OFFSET = "settings_dvr_seeking_offset";
	

	public static final boolean DEFAULT_DVR_START_TIME_ENABLED = false;
	public static final long CUSTOM_DVR_START_TIME = 30000;
	public static final boolean DEFAULT_DVR_SEEKING_OUTSIDE_ENABLED = false;
	public static final boolean DEFAULT_DVR_SEEKING_CLP_ENABLED = false;
	public static final long CUSTOM_DVR_SEEKING_OFFSET = 0;
}
