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

import com.adobe.mediacore.info.AudioTrack;

/**
 * 
 * The AAManager handles the Alternate Audio functionality of the CoreHLSPlayer
 * 
 */
public class AAManager implements IManager {

	/**
	 * Determines if alternate audio is available
	 * 
	 * @return false
	 */
	public boolean hasAlternateAudio() {
		return false;
	}

	/**
	 * Selects the audio track from a given index
	 * 
	 * @param index
	 *            of selected audio track
	 * @return false
	 */
	public boolean selectAlternateAudioTrack(int index) {
		return false;
	}

	/**
	 * Gets the current selected audio track object
	 * 
	 * @return null
	 */
	public AudioTrack getSelectedAudioTrack() {
		return null;
	}

	/**
	 * Gets the audio track as a string array
	 * 
	 * @return empty array
	 */
	public String[] getAudioTracks() {
		String[] aaArray = {};
		return aaArray;
	}

	/**
	 * Gets the index of the selected audio track
	 * 
	 * @return -1
	 */
	public int getSelectedAudioTrackIndex() {
		return -1;
	}

	@Override
	public void destroy() {

	}

	public static final int INVALID_AUDIO_TRACK = -1;

}
