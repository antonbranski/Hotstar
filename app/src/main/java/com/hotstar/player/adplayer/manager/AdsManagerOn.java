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

import com.adobe.mediacore.AdvertisingFactory;
import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.MediaPlayer.PlayerState;
import com.adobe.mediacore.MediaPlayerItem;
import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.PSDKConfig;
import com.adobe.mediacore.PlacementOpportunityDetector;
import com.adobe.mediacore.metadata.AdSignalingMode;
import com.adobe.mediacore.metadata.AdvertisingMetadata;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.adobe.mediacore.qos.LoadInfo;
import com.adobe.mediacore.timeline.Timeline;
import com.adobe.mediacore.timeline.TimelineOperation;
import com.adobe.mediacore.timeline.advertising.Ad;
import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.adobe.mediacore.timeline.advertising.AdPolicySelector;
import com.adobe.mediacore.timeline.advertising.AdClick;
import com.adobe.mediacore.timeline.advertising.AdProvider;
import com.adobe.mediacore.timeline.advertising.ContentResolver;
import com.adobe.mediacore.timeline.advertising.MetadataResolver;
import com.adobe.mediacore.timeline.advertising.auditude.AuditudeResolver;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.advertising.CustomAdBreakPolicySelector;
import com.hotstar.player.adplayer.advertising.CustomAdProviderContentResolver;
import com.hotstar.player.adplayer.advertising.CustomAdProviderMetadata;
import com.hotstar.player.adplayer.advertising.CustomDirectAdBreakResolver;
import com.hotstar.player.adplayer.advertising.CustomPlacementOpportunityDetector;
import com.hotstar.player.adplayer.config.IAdConfig;
import com.adobe.mediacore.utils.TimeRangeCollection;
import com.adobe.mediacore.timeline.advertising.customadmarkers.DeleteContentResolver;
import com.adobe.mediacore.timeline.advertising.customadmarkers.CustomAdMarkersContentResolver;

/**
 * 
 * The AdsManager handles the Ad configuration/insertion functionality of the
 * 
 */
public class AdsManagerOn extends AdsManager {

	private IAdConfig adConfig;
	private MediaPlayer mediaPlayer;
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + AdsManagerOn.class.getSimpleName();
	private final ArrayList<AdsManagerEventListener> eventListeners = new ArrayList<AdsManagerEventListener>();
	private ContentResolver mContentResolver;

	/**
	 * 
	 * Class constructor. This method begins the AdsManagerOn's initialisation
	 * process.
	 * 
	 * @param mediaPlayer
	 *            the PSDK's implementation of MediaPlayer which the manager
	 *            wraps
	 * @param adConfig
	 *            which is used to configure advertisement workflow settings for
	 *            the manager
	 * 
	 */
	public AdsManagerOn(IAdConfig adConfig, MediaPlayer mediaPlayer)
	{
		this.adConfig = adConfig;
		this.mediaPlayer = mediaPlayer;

		setCustomTags();
		registerAdClientFactory();
		this.mediaPlayer.addEventListener(MediaPlayer.Event.AD_PLAYBACK, adPlaybackEventListener);
		this.mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
		this.mediaPlayer.addEventListener(MediaPlayer.Event.QOS, qosEventListener);
	}

	/**
	 * Destroy manager by removing PSDK listeners
	 */
	@Override
	public void destroy() {
		mediaPlayer.removeEventListener(MediaPlayer.Event.AD_PLAYBACK, adPlaybackEventListener);
		mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
		mediaPlayer.removeEventListener(MediaPlayer.Event.QOS, qosEventListener);
	}

	@Override
	public void addEventListener(AdsManagerEventListener eventListener) {
		eventListeners.add(eventListener);
	}

	@Override
	public void removeEventListener(AdsManagerEventListener eventListener) {
		eventListeners.remove(eventListener);
	}

	/**
	 * Get Auditude metadata including server URL, zone ID and media ID from the
	 * config
	 * 
	 * @return Auditude metadata from the config
	 */
	@Override
	public Metadata getAdvertisingMetadata() {
		return adConfig.getMetadata();
	}

	/**
	 * Set ad signaling mode to the media resource base on configuration
	 * 
	 * @param mediaResource
	 *            the media resource to be played in the media player
	 * 
	 */
	@Override
	public void adjustAdSignalingMode(MediaResource mediaResource) {
		AdSignalingMode adSignalingMode = adConfig.getDefaultAdSignalingMode();
		if (adSignalingMode != AdSignalingMode.DEFAULT
				&& mediaResource.getMetadata() != null
				&& mediaResource.getMetadata() instanceof MetadataNode
				&& ((MetadataNode) mediaResource.getMetadata()).containsNode(DefaultMetadataKeys.ADVERTISING_METADATA.getValue()))
		{
			AdvertisingMetadata advertisingMetadata = (AdvertisingMetadata) ((MetadataNode) mediaResource
					.getMetadata())
					.getNode(DefaultMetadataKeys.ADVERTISING_METADATA
							.getValue());
			if (advertisingMetadata != null) {
				advertisingMetadata.setSignalingMode(adSignalingMode);
			}
		}
	}

	/**
	 * Determine if clickable ad feature is enabled
	 * 
	 * @return true if clickable ad is enabled, false otherwise
	 * 
	 */
	@Override
	public boolean isClickableAdsEnabled() {
		return adConfig.isClickableAdsEnabled();
	}

	/**
	 * Notify the media player an clickable ad has been clicked
	 */
	@Override
	public void adClick() {
		mediaPlayer.getView().notifyClick();
	}

	/**
	 * Register custom ad tags to PSDK config for ad break detection.
	 */
	private void setCustomTags() {

		try {
			PSDKConfig.setAdTags(adConfig.getAdTags());
			/*
			 * if (!StringUtils.isEmpty(subscribedTags)) {
			 * PSDKConfig.setSubscribedTags(subscribedTags.split(",")); } else {
			 * // No subscribed tags. PSDKConfig.setSubscribedTags(null); }
			 */
		} catch (Exception e) {
			String message = "Exception when setting the custom tags: " + e.getMessage() + ".";
			AdVideoApplication.logger.e(LOG_TAG + "#setCustomTags", message);
		}
	}

	/**
	 * Creates an AdvertisingFactory which actually disables the internal PSDK
	 * advertising workflow by not providing an opportunity detector or a
	 * content resolver.
	 * 
	 * @return a valid instance on AdvertisingFactory.
	 */
	private AdvertisingFactory createDisabledAdvertisingFactory() {
		return new AdvertisingFactory() {

			@Override
			public PlacementOpportunityDetector createOpportunityDetector(MediaPlayerItem item) {
				return null;
			}

			@Override
			public ContentResolver createContentResolver(MediaPlayerItem item) {
				return null;
			}

			@Override
			public AdPolicySelector createAdPolicySelector(MediaPlayerItem arg0) {
				return null;
			}

			@Override
			public List<ContentResolver> createContentResolvers(MediaPlayerItem arg0) {
				return null;
			}
		};
	}

	/**
	 * Register custom ad provider and opportunity detector
	 */
	private void registerAdClientFactory()
	{
		AdvertisingFactory advertisingFactory = null;
		if (!adConfig.isAdvertisingWorkflowEnabled()) {
			advertisingFactory = createDisabledAdvertisingFactory();
		} else if (adConfig.shouldUseCustomAdFactory()) {
			advertisingFactory = createCustomAdvertisingFactory();
		}
		if (advertisingFactory != null) {
			mediaPlayer.registerAdClientFactory(advertisingFactory);
		}
	}

	/**
	 * @private
	 * 
	 *          Creates a custom AdvertisingFactory which uses Auditude or a
	 *          custom content resolver and a custom opportunity detector for in
	 *          stream cues.
	 * 
	 * @return a valid instance on AdvertisingFactory.
	 */
	private AdvertisingFactory createCustomAdvertisingFactory() {
		return new AdvertisingFactory() {

			@Override
			public PlacementOpportunityDetector createOpportunityDetector(MediaPlayerItem item) {
				return new CustomPlacementOpportunityDetector();
			}

			@Override
			public AdPolicySelector createAdPolicySelector(MediaPlayerItem mediaPlayerItem) {
				return new CustomAdBreakPolicySelector(mediaPlayerItem);
			}

			@Override
			public ContentResolver createContentResolver(MediaPlayerItem item) {
				Metadata metadata = mediaPlayer.getCurrentItem().getResource().getMetadata();

				if (metadata != null) {
					if (metadata.containsKey(DefaultMetadataKeys.AUDITUDE_METADATA_KEY.getValue())) {
						return new AuditudeResolver();
					}
					else if (metadata.containsKey(DefaultMetadataKeys.JSON_METADATA_KEY.getValue())) {
						mContentResolver = new CustomDirectAdBreakResolver();
						return mContentResolver;
					}
					else if (metadata.containsKey(CustomAdProviderMetadata.CUSTOM_AD_PROVIDER_METADATA_KEY)) {
						return new CustomAdProviderContentResolver();
					}
				}

				return null;
			}

			 @Override
			 public List<ContentResolver> createContentResolvers(MediaPlayerItem item)
			 {
	                List<ContentResolver> contentResolvers = new ArrayList<ContentResolver>();
	                Metadata metadata = item.getResource().getMetadata();

	                if (metadata != null) {
	                    if (metadata.containsKey(DefaultMetadataKeys.TIME_RANGES_METADATA_KEY.getValue())) {
	                        String timeRangeType = metadata.getValue(DefaultMetadataKeys.TIME_RANGES_METADATA_KEY.getValue());
	                        if (timeRangeType.equals(TimeRangeCollection.TIME_RANGE_TYPE_DELETE)) {
	                            contentResolvers.add(new DeleteContentResolver());
	                        }
							else if (timeRangeType.equals(TimeRangeCollection.TIME_RANGE_TYPE_REPLACE)) {
	                            contentResolvers.add(new DeleteContentResolver());
	                        }
							else if (timeRangeType.equals(TimeRangeCollection.TIME_RANGE_TYPE_MARK)) {
	                            contentResolvers.add(new CustomAdMarkersContentResolver());
	                        }
	                    }
	                    if (metadata.containsKey(DefaultMetadataKeys.AUDITUDE_METADATA_KEY.getValue())) {
	                        contentResolvers.add(new AuditudeResolver());
	                    }
						else if (metadata.containsKey(DefaultMetadataKeys.JSON_METADATA_KEY.getValue())) {
							mContentResolver = new CustomDirectAdBreakResolver();
	                        contentResolvers.add(mContentResolver);
	                    }
	                }
	                return contentResolvers;
	            }
		};
	}

	/**
	 * PlaybackEventListener that intercepts PSDK playback events
	 */
	private final MediaPlayer.PlaybackEventListener playbackEventListener = new MediaPlayer.PlaybackEventListener() {

		@Override
		public void onTimedMetadata(TimedMetadata timedMetadata) {
		}

		@Override
		public void onPlayComplete() {
			AdVideoApplication.logger.i(LOG_TAG + "#onPlayComplete() is called.", "");

			if (mContentResolver == null)
				return;

			if (mContentResolver.getClass().equals(CustomDirectAdBreakResolver.class)) {
				CustomDirectAdBreakResolver adResolver = (CustomDirectAdBreakResolver) mContentResolver;
				adResolver.stopTimeTracking();
			}
			else {
				// other kind AD resolver
			}
		}

		@Override
		public void onPlayStart() {
		}

		@Override
		public void onPrepared() {
			AdVideoApplication.logger.i(LOG_TAG + "#onPrepared() is called.", "");

			if (mContentResolver == null)
				return;

			if (mContentResolver.getClass().equals(CustomDirectAdBreakResolver.class)) {
				CustomDirectAdBreakResolver adResolver = (CustomDirectAdBreakResolver) mContentResolver;
				adResolver.prepareOverlayAdList();
				adResolver.startTimeTracking();
			} else {
				// other kind AD resolver
			}
		}

		@Override
		public void onSizeAvailable(long width, long height) {
		}

		@Override
		public void onStateChanged(PlayerState state, MediaPlayerNotification notification) {
		}

		/**
		 * Dispatched when timeline markers are updated by the PSDK
		 */
		@Override
		public void onTimelineUpdated() {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlayerStateEventListener#onTimelineUpdated()", "Media player timeline has updated.");
			for (AdsManagerEventListener listener : eventListeners) {
				listener.onTimelineUpdated(mediaPlayer.getTimeline());
			}
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
	 * AdPlaybackEventLister that intercepts PSDK Events
	 */
	private MediaPlayer.AdPlaybackEventListener adPlaybackEventListener = new MediaPlayer.AdPlaybackEventListener() {

		/**
		 * On ad break starts, notify listeners to handle ad break start event
		 */
		@Override
		public void onAdBreakStart(AdBreak adBreak) {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.AdPlaybackEveFntListener#onAdBreakStart()", "Ad break start.");
			for (AdsManager.AdsManagerEventListener listener : eventListeners) {
				listener.onAdBreakStarted(adBreak);
			}
		}

		/**
		 * On ad starts, notify listeners to handle ad start event
		 */
		@Override
		public void onAdStart(AdBreak adBreak, Ad ad) {
			if (ad == null) {
				AdVideoApplication.logger.e(LOG_TAG + "::MediaPlayer.AdPlaybackEventListener#onAdStart()", "No ad to start.");
				return;
			}

			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.AdPlaybackEventListener#onAdStart()",
					"Ad playback start: id = " + ad.getId() + " url = "
							+ ad.getPrimaryAsset().getMediaResource().getUrl()
							+ " duration = " + ad.getDuration());

			for (AdsManager.AdsManagerEventListener listener : eventListeners) {
				listener.onAdStarted(adBreak, ad);
			}
		}

		/**
		 * On ad progress, notify listeners to handle ad progress event
		 */
		@Override
		public void onAdProgress(AdBreak adBreak, Ad ad, int percentage) {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.AdPlaybackEventListener#onAdProgress()",
							"Ad playback progress: " + percentage + "% for ad: " + ad.getId());
			for (AdsManager.AdsManagerEventListener listener : eventListeners) {
				listener.onAdProgress(adBreak, ad, percentage);
			}
		}

		/**
		 * On ad complete, notify listeners to handle ad stop event
		 */
		@Override
		public void onAdComplete(AdBreak adBreak, Ad ad) {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.AdPlaybackEventListener#onAdComplete()",
					"Ad playback complete: " + ad.getId());
			for (AdsManager.AdsManagerEventListener listener : eventListeners) {
				listener.onAdCompleted(adBreak, ad);
			}
			if (ad.isClickable()) {
				// _playerClickableAdFragment.hide();
			}
		}

		/**
		 * On ad break complete, notify listeners to handle ad break stop event
		 */
		@Override
		public void onAdBreakComplete(AdBreak adBreak) {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.AdPlaybackEventListener#onAdBreakComplete()", "Ad break complete.");
			for (AdsManager.AdsManagerEventListener listener : eventListeners) {
				listener.onAdBreakCompleted(adBreak);
			}
		}

		/**
		 * On ad click event, notify listeners to handle clickable ad event
		 */
		@Override
		public void onAdClick(AdBreak adBreak, Ad ad, AdClick adClick) {
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.AdPlaybackEventListener#onAdClick()",
							"AdClick " + adClick.toString() + " for Ad id = " + ad.getId());

			String url = adClick.getUrl();
			if (url == null || url.trim().equals("")) {
				AdVideoApplication.logger.w(LOG_TAG + "::MediaPlayer.AdPlaybackEventListener#onAdClick()",
						"URL for clickable ads is invalid");
			}
			else {
				for (AdsManagerEventListener listener : eventListeners) {
					listener.onAdClick(url);
				}
			}
		}

		@Override
		public void onAdBreakSkipped(AdBreak adBreak) {

			// Ad behavior
		}
	};

	/**
	 * QosEventListener that intercepts PSDK events
	 */
	public final MediaPlayer.QOSEventListener qosEventListener = new MediaPlayer.QOSEventListener() {

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
		 * When and Ad-related PSDK operation fails, notify the listeners to
		 * handle the event
		 */
		@Override
		public void onOperationFailed(MediaPlayerNotification.Warning warning) {
			if (warning.getCode().equals(
					MediaPlayerNotification.ErrorCode.AD_INSERTION_FAIL)
					|| warning.getCode().equals(MediaPlayerNotification.ErrorCode.AD_RESOLVER_METADATA_INVALID)
					|| warning.getCode().equals(MediaPlayerNotification.ErrorCode.AD_RESOLVER_RESOLVE_FAIL)) {
				AdVideoApplication.logger.w(LOG_TAG + "::MediaPlayer.QOSEventListener#onOperationFailed",
								"Ad Warning: " + warning + ", code: " + warning.getCode());
			}
		}
	};

	@Override
	public void updatePlayerCurrentTime(long playerCurrentTime) {
		if (mContentResolver == null)
			return;

		if (mContentResolver.getClass().equals(CustomDirectAdBreakResolver.class)) {
			CustomDirectAdBreakResolver adResolver = (CustomDirectAdBreakResolver) mContentResolver;
			adResolver.updatePlayerCurrentTime(playerCurrentTime);
		}
		else {
			// other kind AD resolver
		}
	}

	@Override
	public void registerOverlayAdListener(CustomDirectAdBreakResolver.OverlayAdListener listener) {
		if (mContentResolver == null)
			return;

		if (mContentResolver.getClass().equals(CustomDirectAdBreakResolver.class)) {
			CustomDirectAdBreakResolver adResolver = (CustomDirectAdBreakResolver) mContentResolver;
			adResolver.registerOverlayAdListener(listener);
		}
		else {
			// other kind AD resolver
		}

	}

	@Override
	public void registerVPaidAdListener(CustomDirectAdBreakResolver.VPaidAdListener listener) {
		if (mContentResolver == null)
			return;

		if (mContentResolver.getClass().equals(CustomDirectAdBreakResolver.class)) {
			CustomDirectAdBreakResolver adResolver = (CustomDirectAdBreakResolver) mContentResolver;
			adResolver.registerVPaidAdListener(listener);
		}
		else {
			// other kind AD resolver
		}
	}

	@Override
	public ArrayList getOverlayTimeline() {
		if (mContentResolver == null)
			return null;

		if (mContentResolver.getClass().equals(CustomDirectAdBreakResolver.class)) {
			CustomDirectAdBreakResolver adResolver = (CustomDirectAdBreakResolver) mContentResolver;
			return adResolver.getOverlayAdList();
		}
		else {
			// other kind Ad Resolver
		}

		return null;
	}

	@Override
	public ArrayList getVPaidTimeline() {
		if (mContentResolver == null)
			return null;

		if (mContentResolver.getClass().equals(CustomDirectAdBreakResolver.class)) {
			CustomDirectAdBreakResolver adResolver = (CustomDirectAdBreakResolver) mContentResolver;
			return adResolver.getVPaidAdList();
		}
		else {
			// other kind Ad Resolver
		}

		return null;
	}
}
