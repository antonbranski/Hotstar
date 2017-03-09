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

package com.hotstar.player.adplayer.config;

import com.adobe.mediacore.ABRControlParameters;

public interface IPlaybackConfig {

	/**
	 * Determine if ABR control is enabled
	 * 
	 * @return true if ABR control is enabled, false otherwise
	 */
	public boolean isABRControlEnabled();

	/**
	 * Get the initial bit rate of ABR control
	 * 
	 * @return int value of the initial bit rate
	 */
	public int getABRInitialBitRate();

	/**
	 * Get the minimum bit rate of ABR control
	 * 
	 * @return int value of the minimum bit rate
	 */
	public int getABRMinBitRate();

	/**
	 * Get the maximum bit rate of ABR control
	 * 
	 * @return int value of the maximum bit rate
	 */
	public int getABRMaxBitRate();

	/**
	 * Get the ABR control policy
	 * 
	 * @return the ABRPolicy object represents the ABR control policy for bit
	 *         rate switching aggressiveness
	 */
	public ABRControlParameters.ABRPolicy getABRPolicy();

	/**
	 * Get the initial buffer time. When the player buffer reaches this value,
	 * playback begins.
	 * 
	 * @return the initial buffer time in milliseconds
	 */
	public long getInitBufferTime();

	/**
	 * Get the desire buffer time. The player buffers fragments until it reaches
	 * this value.
	 * 
	 * @return the desire buffer time in milliseconds
	 */
	public long getBufferTime();

	/**
	 * Determines if choosing a custom position in the DVR window when entering
	 * a stream is enabled
	 * 
	 * @return true if so, false otherwise
	 */
	public boolean isCustomPositionPrefEnabled();

	/**
	 * Gets the custom start time for entering a DVR stream
	 * 
	 * @return the custom start time in milliseconds
	 */
	public long retrieveStartTimePref();

	/**
	 * Determines if seeking after time jump (where the paused position is
	 * outside of seekable range) is enabled
	 * 
	 * @return true if so, false otherwise
	 */
	public boolean isSeekingAfterTimeJumpPrefEnabled();

	/**
	 * Assuming seeking after time jump is enabled, determines if should seek to
	 * new client live point
	 * 
	 * @return true if so, false otherwise
	 */
	public boolean isSeekingAtClientLivePointPrefEnabled();

	/**
	 * Assuming seeking after time jump is enabled, gets the offset to the
	 * client live point
	 * 
	 * @return offset to the client live point for time jump
	 */
	public long retrieveSeekingOffsetPref();
	
	/**
	 * Determine if the player should force to use OpenMAX AL
	 */
	public boolean forceOpenMAXAL();	

}
