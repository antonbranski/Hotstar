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
import com.adobe.mediacore.MediaPlayerItem;
import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.MediaPlayer.PlayerState;
import com.adobe.mediacore.info.AudioTrack;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.adobe.mediacore.qos.LoadInfo;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.config.IAAConfig;

/**
 * 
 * The AAManager handles the Alternate Audio functionality
 * 
 */
public class AAManagerOn extends AAManager {

	@SuppressWarnings("unused")
	private IAAConfig aaConfig;
	private MediaPlayer mediaPlayer;

	private List<Integer> failedAlternateAudioTracks = new ArrayList<Integer>();
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + AAManagerOn.class.getSimpleName();

	/**
	 * 
	 * Class constructor. This method begins the AAManagerOn's initialization
	 * process.
	 * 
	 * @param player
	 *            the PSDK's implementation of MediaPlayer which the manager
	 *            wraps
	 * @param aaConfig
	 *            which is used to configure alternate audio settings for the
	 *            manager
	 * 
	 */
	public AAManagerOn(IAAConfig aaConfig, MediaPlayer mediaPlayer) {
		this.aaConfig = aaConfig;
		this.mediaPlayer = mediaPlayer;

		this.mediaPlayer.addEventListener(MediaPlayer.Event.QOS, qosEventListener);
		this.mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
	}

	/**
	 * Destroy manager by removing PSDK listeners
	 */
	@Override
	public void destroy() {
		mediaPlayer.removeEventListener(MediaPlayer.Event.QOS, qosEventListener);
		mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
	}

	/**
	 * Determines if alternate audio is available
	 * 
	 * @return true if alterate audio is available
	 */
	@Override
	public boolean hasAlternateAudio() {
		return mediaPlayer.getCurrentItem().hasAlternateAudio();
	}

	/**
	 * Select the audio track from the given index
	 * 
	 * @param index
	 *            the index of the available audio track to be chosen
	 * @return true if audio track is successfully selected, false otherwise or
	 *         the audio track was failed to switch to before
	 */
	@Override
	public boolean selectAlternateAudioTrack(int index)
	{
		if (failedAlternateAudioTracks.contains(index)) {
			AdVideoApplication.logger.i(LOG_TAG + "#selectAlternateAudio",
							"Selection to the current audio track has previously failed. Select a different track.");
			return false;
		}

		AudioTrack selectedAudioTrack = getSelectedAudioTrack();
		if (index >= 0) {
			selectedAudioTrack = mediaPlayer.getCurrentItem().getAudioTracks().get(index);
			AdVideoApplication.logger.i(LOG_TAG + "#selectAlternateAudio", "Audio track " + selectedAudioTrack + " selected.");

		}

		boolean result = mediaPlayer.getCurrentItem().selectAudioTrack(selectedAudioTrack);
		return result;
	}

	/**
	 * Get the currently selected audio track object
	 * 
	 * @return AudioTrack the audio track object that's currently selected
	 */
	@Override
	public AudioTrack getSelectedAudioTrack()
	{
		return mediaPlayer.getCurrentItem().getSelectedAudioTrack();
	}

	/**
	 * Get the index of the currently selected audio track in the list of
	 * available tracks
	 * 
	 * @return index of the selected audio track in the list. If none of the
	 *         track matches, return -1
	 */
	@Override
	public int getSelectedAudioTrackIndex()
	{
		MediaPlayerItem item = mediaPlayer.getCurrentItem();
		if (item == null) {
			return INVALID_AUDIO_TRACK;
		}
		List<AudioTrack> audioTracks = item.getAudioTracks();
		AudioTrack selectedAudioTrack = item.getSelectedAudioTrack();

		for (int i = 0; i < audioTracks.size(); i++) {
			if (audioTracks.get(i).equals(selectedAudioTrack))
				return i;
		}

		return INVALID_AUDIO_TRACK;
	}

	/**
	 * Converts the alternate audio tracks to a string array.
	 * 
	 * @return array of AA track names
	 */
	@Override
	public String[] getAudioTracks()
	{
		List<String> audioTracksAsStrings = new ArrayList<String>();

		MediaPlayerItem currentItem = mediaPlayer.getCurrentItem();
		if (currentItem != null) {
			int index = 0;
			List<AudioTrack> audioTracks = currentItem.getAudioTracks();

			Iterator<AudioTrack> iterator = audioTracks.iterator();
			while (iterator.hasNext()) {
				AudioTrack audioTrack = iterator.next();
				String name = audioTrack.getName() + "[ " + audioTrack.getLanguage() + "]";
				if (failedAlternateAudioTracks.contains(index)) {
					name += " *broken track*";
				}
				audioTracksAsStrings.add(name);
				index++;
			}
		}

		return audioTracksAsStrings.toArray(new String[audioTracksAsStrings.size()]);
	}

	/**
	 * Register audio track index when audio switching operation failure.
	 * 
	 * @param innerNotification
	 *            PSDK warning notification created from operation failed event
	 */
	private void registerFailedAudioTrackIndex(MediaPlayerNotification innerNotification) {
		if (innerNotification != null
				&& innerNotification.getCode().equals(MediaPlayerNotification.ErrorCode.AUDIO_TRACK_ERROR))
		{
			Metadata metadata = innerNotification.getMetadata();
			if (metadata != null)
			{
				String name = metadata.getValue("AUDIO_TRACK_NAME");
				String language = metadata.getValue("AUDIO_TRACK_LANGUAGE");
				MediaPlayerItem currentItem = mediaPlayer.getCurrentItem();
				if (currentItem != null)
				{
					List<AudioTrack> audioTracks = currentItem.getAudioTracks();

					for (AudioTrack audioTrack : audioTracks)
					{
						if (audioTrack.getLanguage().equals(language)
								&& audioTrack.getName().equals(name))
						{
							// Keep the track ID in a list. Next time the user
							// selects this track, an warning will be displayed.
							failedAlternateAudioTracks.add(audioTracks.indexOf(audioTrack));
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * PlaybackEventListener that intercepts PSDK playback events
	 */
	private final MediaPlayer.PlaybackEventListener playbackEventListener = new MediaPlayer.PlaybackEventListener() {

		/**
		 * Select first audio track when playback is ready
		 */
		@Override
		public void onPrepared() {
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

		@Override
		public void onStateChanged(PlayerState state, MediaPlayerNotification notification) {

		}

		@Override
		public void onTimedMetadata(TimedMetadata metadata) {

		}

		@Override
		public void onTimelineUpdated() {

		}

		@Override
		public void onUpdated() {

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

		@Override
		public void onBufferStart() {

		}

		@Override
		public void onBufferComplete() {

		}

		@Override
		public void onSeekStart() {
		}

		@Override
		public void onSeekComplete(long adjustedTime) {

		}

		@Override
		public void onLoadInfo(LoadInfo loadInfo) {

		}

		/**
		 * Register failed audio track index when playback operation failed
		 * 
		 */
		@Override
		public void onOperationFailed(MediaPlayerNotification.Warning warning) {
			MediaPlayerNotification innerNotification = warning.getInnerNotification();
			registerFailedAudioTrackIndex(innerNotification);
		}
	};

}
