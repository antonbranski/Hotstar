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
import java.util.Iterator;
import java.util.List;
import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.MediaPlayer.PlayerState;
import com.adobe.mediacore.MediaPlayer.Visibility;
import com.adobe.mediacore.MediaPlayerItem;
import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.TextFormat;
import com.adobe.mediacore.TextFormatBuilder;
import com.adobe.mediacore.info.ClosedCaptionsTrack;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.config.ICCConfig;

/**
 * 
 * The CCManager handles the Closed Caption functionality
 * 
 */
public class CCManagerOn extends CCManager {

	private ICCConfig ccConfig;
	private MediaPlayer mediaPlayer;
	private int selectedClosedCaptionsIndex = -1;
	private boolean isPrepared;
	private PlayerState lastKnownStatus;

	private final ArrayList<CCManagerEventListener> eventListeners = new ArrayList<CCManagerEventListener>();

	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + CCManagerOn.class.getSimpleName();

	/**
	 * 
	 * Class constructor. This method begins the CCManagerOn's initialisation
	 * process and configures the initial CC style.
	 * 
	 * @param player
	 *            the PSDK's implementation of MediaPlayer which the manager
	 *            wraps
	 * @param ccConfig
	 *            which is used to configure closed captioning settings for the
	 *            manager
	 * 
	 */
	public CCManagerOn(ICCConfig ccConfig, MediaPlayer mediaPlayer) {
		this.ccConfig = ccConfig;
		this.mediaPlayer = mediaPlayer;
		this.mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
	}

	/**
	 * Destroy manager by removing PSDK listeners
	 */
	@Override
	public void destroy() {
		mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
	}

	@Override
	public void addEventListener(CCManagerEventListener eventListener) {
		eventListeners.add(eventListener);
	}

	@Override
	public void removeEventListener(CCManagerEventListener eventListener) {
		eventListeners.remove(eventListener);
	}

	/**
	 * PlaybackEventListener that intercepts PSDK playback events
	 */
	private final MediaPlayer.PlaybackEventListener playbackEventListener = new MediaPlayer.PlaybackEventListener() {

		/**
		 * Initialize closed captioning when the playback is ready
		 */
		@Override
		public void onPrepared() {
			isPrepared = true;
			initialize();
		}

		@Override
		public void onPlayComplete() {

		}

		@Override
		public void onPlayStart() {

		}

		@Override
		public void onSizeAvailable(long width, long height) {

		}

		/**
		 * Keep track of the last known state of the player
		 */
		@Override
		public void onStateChanged(PlayerState state, MediaPlayerNotification notification) {
			lastKnownStatus = state;
		}

		@Override
		public void onTimedMetadata(TimedMetadata metadata) {

		}

		@Override
		public void onTimelineUpdated() {

		}

		/**
		 * Log closed captioning tracks on playback update
		 */
		@Override
		public void onUpdated()
		{
			showClosedCaptions();
			for (CCManagerEventListener listener : eventListeners) {
				listener.onCaptionUpdated();
			}
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

	// Public methods

	/**
	 * Determine if closed captioning is available
	 * 
	 * @return true if closed captioning is available, false otherwise
	 */
	public boolean hasClosedCaptions() {
		return mediaPlayer.getCurrentItem().hasClosedCaptions();
	}

	/**
	 * Set the index of the currently selected closed captioning track
	 * 
	 * @param index
	 *            in the closed captioning track list
	 */
	private void setSelectedClosedCaptionsIndex(int index) {
		this.selectedClosedCaptionsIndex = index;
	}

	/**
	 * Get the index of the currently selected closed captioning track
	 * 
	 * @return int the selected index of the closed captioning track list
	 */
	@Override
	public int getSelectedClosedCaptionsIndex() {
		return this.selectedClosedCaptionsIndex;
	}

	/**
	 * Select closed captioning track from the list with the given index
	 `*
	 * @param index
	 *            in the closed captioning track list
	 */
	@Override
	public void selectClosedCaptionTrack(int index) {

		MediaPlayerItem currentItem = mediaPlayer.getCurrentItem();
		if (index >= 0) {
			ClosedCaptionsTrack desiredClosedCaptionsTrack = currentItem.getClosedCaptionsTracks().get(index);
			boolean result = currentItem.selectClosedCaptionsTrack(desiredClosedCaptionsTrack);
			if (result) {
				AdVideoApplication.logger.i(LOG_TAG + "#", "CC track:" + desiredClosedCaptionsTrack.getName() + "selected.");
				setCCVisibility(true);
				setSelectedClosedCaptionsIndex(index);
			}
		}
		else {
			AdVideoApplication.logger.i(LOG_TAG + "#", "None Option Selected.");
			setSelectedClosedCaptionsIndex(-1);
			setCCVisibility(false);
		}
	}

	/**
	 * Turn closed captioning visibility on and off
	 * 
	 * @param visible
	 *            the visibility flag
	 */
	@Override
	public void setCCVisibility(boolean visible)
	{
		if (mediaPlayer != null && lastKnownStatus != MediaPlayer.PlayerState.RELEASED) {
			if (visible) {
				mediaPlayer.setCCVisibility(Visibility.VISIBLE);
			}
			else {
				mediaPlayer.setCCVisibility(Visibility.INVISIBLE);
			}
		}
	}

	/**
	 * Get the CC visibility
	 * 
	 * @return true if CC is visible, false otherwise
	 */
	@Override
	public boolean getCCVisibility() {
		return mediaPlayer.getCCVisibility() == Visibility.VISIBLE ? true : false;
	}

	/**
	 * Set closed captioning style based on config.
	 */
	@Override
	public void setCCStyle()
	{
		if (mediaPlayer == null || !isPrepared)
			return;

		TextFormat tf = new TextFormatBuilder(ccConfig.getCCFont(),
				ccConfig.getCCSize(), ccConfig.getCCFontEdge(),
				ccConfig.getCCFontColor(), ccConfig.getCCBackgroundColor(),
				TextFormat.Color.DEFAULT, ccConfig.getCCEdgeColor(),
				ccConfig.getCCFontOpacity(), ccConfig.getCCBackgroundOpacity(),
				TextFormat.DEFAULT_OPACITY, "default").toTextFormat();
		mediaPlayer.setCCStyle(tf);
	}

	/**
	 * Get the array of closed captioning in string format. Each of them
	 * concatenates with a given label.
	 * 
	 * @param label
	 *            - the status of the activity of the track.
	 * @return an array of String each represents the closed captioning track
	 */
	@Override
	public List<String> getClosedCaptionTracks(String label)
	{
		List<String> closedCaptionsTracksAsStrings = new ArrayList<String>();
		MediaPlayerItem currentItem = mediaPlayer.getCurrentItem();
		List<ClosedCaptionsTrack> closedCaptionsTracks = currentItem.getClosedCaptionsTracks();
		Iterator<ClosedCaptionsTrack> iterator = closedCaptionsTracks.iterator();

		if (currentItem != null) {
			while (iterator.hasNext()) {
				ClosedCaptionsTrack closedCaptionsTrack = iterator.next();
				String isActive = closedCaptionsTrack.isActive() ? " (" + label + ")" : "";
				closedCaptionsTracksAsStrings.add(closedCaptionsTrack.getName() + " : " + closedCaptionsTrack.getLanguage() + isActive);
			}
		}

		return closedCaptionsTracksAsStrings;
	}

	/**
	 * Show the closed captioning tracks in log
	 */
	private void showClosedCaptions() {
		List<ClosedCaptionsTrack> closedCaptionsTracks = mediaPlayer.getCurrentItem().getClosedCaptionsTracks();

		Iterator<ClosedCaptionsTrack> iterator = closedCaptionsTracks.iterator();
		while (iterator.hasNext()) {
			ClosedCaptionsTrack track = iterator.next();
			AdVideoApplication.logger.i(LOG_TAG + "#", "CC track: " + track.getName() + ". Has activity: " + track.isActive() + ".");
		}
	}

	/**
	 * Select the initial CC track and set its visibility and style
	 */
	private void initialize() {

		setCCStyle();
	}

}
