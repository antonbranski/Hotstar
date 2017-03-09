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

import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.timeline.Timeline;
import com.adobe.mediacore.timeline.advertising.Ad;
import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.hotstar.player.adplayer.advertising.CustomDirectAdBreakResolver;

import java.util.ArrayList;

/**
 * 
 * The AdsManager handles the Ad configuration/insertion functionality
 * 
 */
public class AdsManager implements IManager {

	/**
	 * AdsManagerEventListener: listeners that can respond appropriately to Ads
	 * event
	 */
	public interface AdsManagerEventListener {
		/**
		 * Handle ad break start event
		 * 
		 * @param adBreak
		 *            the current ad break that is starting
		 */
		public void onAdBreakStarted(AdBreak adBreak);

		/**
		 * Handle ad break stop event
		 * 
		 * @param adBreak
		 *            the current ad break that is completed
		 */
		public void onAdBreakCompleted(AdBreak adBreak);

		/**
		 * Handle ad start event
		 * 
		 * @param adBreak
		 *            the current ad break that is in progress
		 * @param ad
		 *            the current ad that is starting
		 */
		public void onAdStarted(AdBreak adBreak, Ad ad);

		/**
		 * Handle ad stop event
		 * 
		 * @param adBreak
		 *            the current ad break that is in progress
		 * @param ad
		 *            the current ad that is ending
		 */
		public void onAdCompleted(AdBreak adBreak, Ad ad);

		/**
		 * Handle ad progress event
		 * 
		 * @param adBreak
		 *            the current ad break that is in progress
		 * @param ad
		 *            the current ad that is in progress
		 * @param percentage
		 *            the integer presentation of the percentage of the ad that
		 *            has been played so far
		 */
		public void onAdProgress(AdBreak adBreak, Ad ad, int percentage);

		/**
		 * Handle the event when the external URL to be redirected from a
		 * clickable ad is available
		 * 
		 * @param url
		 *            the click through URL of the clickable ad
		 */
		public void onAdClick(String url);

		/**
		 * Handle timeline marker updates, timeline is usually updated at the
		 * beginning of a stream with ads, or whenever existing ads are updated
		 * or new ads added
		 * 
		 * @param timeline
		 */
		public void onTimelineUpdated(Timeline<?> timeline);

	}

	public void addEventListener(
			AdsManager.AdsManagerEventListener eventListener) {

	}

	public void removeEventListener(
			AdsManager.AdsManagerEventListener eventListener) {

	}

	public Metadata getAdvertisingMetadata() {
		return null;
	}

	public void adjustAdSignalingMode(MediaResource mediaResource) {

	}

	public boolean isClickableAdsEnabled() {
		return false;
	}

	public void adClick() {

	}

	@Override
	public void destroy() {

	}


	public void updatePlayerCurrentTime(long playerMilliseconds) {

	}

	public void registerOverlayAdListener(CustomDirectAdBreakResolver.OverlayAdListener listener) {

	}

	public void registerVPaidAdListener(CustomDirectAdBreakResolver.VPaidAdListener listener) {

	}

	public ArrayList getOverlayTimeline() {
		return null;
	}

	public ArrayList getVPaidTimeline() {
		return null;
	}


	public static final boolean DEFAULT_ADVERTISING_WORKFLOW_ENABLED = true;
	public static final boolean DEFAULT_USE_CUSTOM_AD_CLIENT_FACTORY = false;
	public static final String DEFAULT_ADVERTISING_SIGNALING_MODE = "default";
	public static final boolean DEFAULT_SEEK_IN_AD = false;
	public static final boolean DEFAULT_CLICKABLE_ADS = true;

	public static final String SETTINGS_CLICKABLE_ADS = "settings_clickable_ads";
	public static final String SETTINGS_CUSTOM_AD_CUES = "settings_custom_ad_cues";
	public static final String SETTINGS_SUBSCRIBED_CUES = "settings_subscribed_cues";
	public static final String SETTINGS_SEEK_IN_AD = "settings_seek_in_ad";
	public static final String SETTINGS_ADVERTISING_WORKFLOW_ENABLED = "settings_advertising_workflow_enabled";
	public static final String SETTINGS_CUSTOM_AD_CLIENT_FACTORY = "settings_custom_ad_client_factory";
	public static final String SETTINGS_ADVERTISING_SIGNALING_MODE = "settings_advertising_signaling_mode";

}
