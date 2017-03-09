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

import com.adobe.mediacore.metadata.AdSignalingMode;
import com.adobe.mediacore.metadata.Metadata;

public interface IAdConfig {

	/**
	 * Determine if advertising workflow is enabled
	 * 
	 * @return true if ad workflow is enabled, false otherwise
	 */
	public boolean isAdvertisingWorkflowEnabled();

	/**
	 * Determine if custom ad factory should be used
	 * 
	 * @return true if custom ad factory should be used, false otherwise
	 */
	public boolean shouldUseCustomAdFactory();

	/**
	 * Determine if the ads should be clickable
	 * 
	 * @return true if clickable ad feature is enabled, false otherwise
	 */
	public boolean isClickableAdsEnabled();

	/**
	 * Get Auditidue meta data
	 * 
	 * @return Metadata object contains Auditude configuration
	 */
	public Metadata getMetadata();

	/**
	 * Get the list of custom ad tags. These are the custom tag names for ad
	 * replacement.
	 * 
	 * @return an array of ad cue tags
	 */
	public String[] getAdTags();

	/**
	 * Get the default ad signaling mode from config
	 * 
	 * @return AdSignalingMode the default signaling mode
	 */
	public AdSignalingMode getDefaultAdSignalingMode();

}
