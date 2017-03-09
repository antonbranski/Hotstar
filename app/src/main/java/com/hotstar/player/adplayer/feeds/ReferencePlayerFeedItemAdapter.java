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

package com.hotstar.player.adplayer.feeds;

import com.adobe.mediacore.metadata.AdSignalingMode;
import com.adobe.mediacore.metadata.AdvertisingMetadata;
import com.adobe.mediacore.metadata.AuditudeSettings;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.utils.NumberUtils;
import com.adobe.mediacore.utils.ReplacementTimeRange;
import com.adobe.mediacore.utils.StringUtils;
import com.adobe.mediacore.utils.TimeRange;
import com.adobe.mediacore.utils.TimeRangeCollection;
import com.adobe.mediacore.videoanalytics.VideoAnalyticsChapterData;
import com.adobe.mediacore.videoanalytics.VideoAnalyticsMetadata;
import com.hotstar.player.HotStarApplication;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.advertising.CustomAdProviderMetadata;
import com.hotstar.player.adplayer.entitlement.EntitlementMetadata;
import com.hotstar.player.adplayer.feeds.ContentRenditionInfo.ContentFormat;
import com.hotstar.player.adplayer.feeds.ContentRenditionInfo.ContentType;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.core.OverlayAdItem;
import com.hotstar.player.adplayer.utils.SerializableNameValuePair;
import com.hotstar.player.custom.SamsungPhoneInfo;
import com.hotstar.player.model.HotStarUserInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class ReferencePlayerFeedItemAdapter implements IFeedItemAdapter {
	private final String LOG_TAG = "[Player]::ReferencePlayerFeedItemAdapter";

	// Entry attributes
	protected static final String NAME_ID = "id";
	protected static final String NAME_TITLE = "title";
	protected static final String NAME_DESCRIPTION = "description";
	protected static final String NAME_CATEGORIES = "categories";
	protected static final String NAME_KEYWORDS = "keywords";
	protected static final String NAME_ISLIVE = "isLive";
	protected static final String NAME_CONTENT = "content";
	protected static final String NAME_CONTENT_FORMAT = "format";
	protected static final String NAME_CONTENT_URL = "url";
	protected static final String NAME_CONTENT_LANGUAGE = "language";
	protected static final String NAME_THUMBNAILS = "thumbnails";
	protected static final String NAME_THUMBNAILS_WIDTH = "width";
	protected static final String NAME_THUMBNAILS_URL = "url";
	protected static final String NAME_METADATA = "metadata";
	protected static final String NAME_METADATA_AD = "ad";
    protected static final String NAME_METADATA_ENTITLEMENT = "entitlement";
	protected static final String NODE_NAME_SIGNALING_MODE = "signaling-mode";
	protected static final String NODE_NAME_DISABLE_CONTENT_CACHE = "disable-cache";

	// Entry attribute values
	protected static final String VALUE_CONTENT_FORMAT_M3U8 = "M3U8";
	protected static final String VALUE_CONTENT_FORMAT_F4M = "F4M";
	protected static final String VALUE_TRUE = "true";
	protected static final String VALUE_FALSE = "false";

    // Entitlement Metadata attributes
    protected static final String METADATA_ENTITLEMENT_ID = "id";

	// Time-range Metadata attributes
	protected final String NODE_NAME_METADATA_TIME_RANGES = "time-ranges";
    protected final String METADATA_TIME_RANGE_TYPE = "type";
    protected final String METADATA_TIME_RANGE_LIST = "time-range-list";
    protected final String METADATA_TIME_RANGE_BEGIN = "begin";
    protected final String METADATA_TIME_RANGE_END = "end";
    protected final String METADATA_TIME_RANGE_REPLACEMENT_DURATION = "replacement-duration";
    protected final String VALUE_TIME_RANGE_TYPE_DELETE = "delete";
    protected final String VALUE_TIME_RANGE_TYPE_MARK = "mark";
    protected final String VALUE_TIME_RANGE_TYPE_REPLACE = "replace";
	
	// Ad Metadata attributes
	protected static final String METADATA_AD_TYPE = "type";
	protected static final String METADATA_AD_DETAILS = "details";

	// Ad Metadata - Primetime Ads
	protected static final String VALUE_AD_TYPE_PRIMETIME_ADS = "Primetime Ads";
	protected static final String NODE_NAME_AD_ZONEID = "zoneid";
	protected static final String NODE_NAME_AD_MEDIAID = "mediaid";
	protected static final String NODE_NAME_AD_DOMAIN = "domain";
	protected static final String NODE_NAME_AD_TARGETING = "targeting";
	protected static final String NODE_NAME_KEY = "key";
	protected static final String NODE_NAME_VALUE = "value";

	// Ad Metadata - Direct Ad Breaks
	protected static final String VALUE_AD_TYPE_DIRECT_AD_BREAKS = "Direct Ad Breaks";
	protected static final String NODE_NAME_ADBREAKS = "ad-breaks";

	// Ad Metadata - Custom Ad Markers
	protected static final String VALUE_AD_TYPE_CUSTOM_AD_MARKERS = "Custom Ad Markers";
	protected static final String NODE_NAME_CUSTOM_AD_MARKERS_ADJUST_SEEK_POSITION = "adjust-seek-position";
	protected static final String NODE_NAME_CUSTOM_AD_MARKERS_TIME_RANGES = "time-ranges";
	protected static final String NODE_NAME_CUSTOM_AD_MARKER_BEGIN = "begin";
	protected static final String NODE_NAME_CUSTOM_AD_MARKER_END = "end";

	// Ad Metadata - Custom Ad Provider
	protected static final String VALUE_AD_TYPE_CUSTOM_AD_PROVIDER = "Custom Ad Provider";
	protected static final String NODE_NAME_CUSTOM_AD_PROVIDER_DOMAIN = "domain";
	protected static final String NODE_NAME_CUSTOM_AD_PROVIDER_AD_PATTERN = "ad-pattern";
	protected static final String NODE_NAME_CUSTOM_AD_PROVIDER_TIME = "time";
	protected static final String NODE_NAME_CUSTOM_AD_PROVIDER_COUNT = "count";
	protected static final String NODE_NAME_CUSTOM_AD_PROVIDER_DURATION = "duration";
	protected static final String NODE_NAME_CUSTOM_AD_PROVIDER_CONTENT_METADATA = "content-metadata";
	protected static final String NODE_NAME_CREATIVE_REPACKAGING_ENABLED = "creative-repackaging";

    // Video Analytics Metadata
    protected static final String NODE_NAME_METADATA_VIDEO_ANALYTICS = "video-analytics";
    protected static final String NODE_NAME_VA_VIDEO_ID = "videoid";
    protected static final String NODE_NAME_VA_VIDEO_NAME = "videoname";
    protected static final String NODE_NAME_VA_VIDEO_CHAPTERS = "chapters";
    protected static final String NODE_NAME_VA_VIDEO_CHAPTER_NAME = "name";
    protected static final String NODE_NAME_VA_VIDEO_CHAPTER_START = "start";
    protected static final String NODE_NAME_VA_VIDEO_CHAPTER_END = "end";
    protected static final String NODE_NAME_VA_PLAY_NAME = "playername";
    protected static final String NODE_NAME_VA_ENABLE_CHAPTER_TRACKING = "chaptertracking";

	protected final JSONObject entryObject;
	protected String smallThumbnailURL = null;
	protected String largeThumbnailURL = null;
	protected List<SerializableNameValuePair> properties = null;
	protected List<ContentRenditionInfo> contentRenditionInfoList = null;
	protected List<ContentRenditionInfo> HLSContentRenditionInfoList = null;
	protected List<ContentRenditionInfo> HDSContentRenditionInfoList = null;
	protected MetadataNode metadataNode = null;
	protected ArrayList<OverlayAdItem> overlayAdItems = null;

	/**
	 * Class constructor. The ReferencePlayerFeedItemAdapter is used for
	 * retrieving specific data for a given content item
	 * 
	 * @param entryObject
	 *            - one content item in JSON format
	 */
	protected ReferencePlayerFeedItemAdapter(JSONObject entryObject) {
		this.entryObject = entryObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getId() {
		return getString(entryObject, NAME_ID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTitle() {
		return getString(entryObject, NAME_TITLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContentType getContentType()
	{
        String isLive = getString(entryObject, NAME_ISLIVE);
		if (isLive != null && isLive.equalsIgnoreCase(VALUE_TRUE)) {
			return ContentType.live;
		} else {
			return ContentType.vod;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetadataNode getStreamMetadata()
	{
		if (this.metadataNode == null) {
			parseMetadata();
		}

		return this.metadataNode;
	}

	@Override
	public ArrayList<OverlayAdItem> getOverlayAdItems() {
		return overlayAdItems;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStreamThumbnailLarge()
	{
		if (this.largeThumbnailURL == null) {
			parseThumbnailUrls();
		}

		return this.largeThumbnailURL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStreamThumbnailSmall()
	{
		if (this.smallThumbnailURL == null) {
			parseThumbnailUrls();
		}

		return this.smallThumbnailURL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SerializableNameValuePair> getProperties()
	{
		if (this.properties == null) {
			parseProperties();
		}

		return this.properties;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ContentRenditionInfo> getHDSContentRenditions()
	{
		if (this.HDSContentRenditionInfoList == null) {
			parseContentFormat();
		}

		return this.HDSContentRenditionInfoList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ContentRenditionInfo> getHLSContentRenditions()
	{
		if (this.HLSContentRenditionInfoList == null) {
			parseContentFormat();
		}

		return this.HLSContentRenditionInfoList;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ContentRenditionInfo> getContentRenditions()
	{
		if (this.contentRenditionInfoList == null) {
			parseContent();
		}

		return this.contentRenditionInfoList;
	}

	/**
	 * Gets field value from JSON object given field name
	 * 
	 * @param jsonObject
	 *            - JSON object to search in
	 * @param fieldName
	 *            - field name to be searched for
	 * @return String - field value or null if field name does not exist or
	 *         field value is empty
	 */
	protected String getString(JSONObject jsonObject, String fieldName)
	{
		/*
		try {
			if (jsonObject.has(fieldName) && !jsonObject.isNull(fieldName)) {
				return jsonObject.getString(fieldName);
			}
		} catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::getString",
					"Error getting field from JSON: " + e.getMessage());
		}
		*/

		try {
			Iterator<String> iter = jsonObject.keys();
			while (iter.hasNext()) {
				String key1 = iter.next();
				if (key1.equalsIgnoreCase(fieldName)) {
					if (!jsonObject.isNull(key1))
						return jsonObject.getString(key1);
					else
						return null;
				}
			}
		}
		catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::getString",
					"Error getting field from JSON: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Sets the small and large thumb URLs
	 */
	protected void parseThumbnailUrls()
	{
		if (getString(entryObject, NAME_THUMBNAILS) == null) {
			return;
		}

		JSONObject smallThumbnailObject = null;
		JSONObject largeThumbnailObject = null;

		try {
			JSONArray content = entryObject.getJSONArray(NAME_THUMBNAILS);
			for (int i = 0; i < content.length(); i++)
			{
				JSONObject contentObject = content.getJSONObject(i);
				String url = getString(contentObject, NAME_THUMBNAILS_URL);
				if (url != null && !url.isEmpty()) {
					if (smallThumbnailObject == null) {
						smallThumbnailObject = largeThumbnailObject = contentObject;
					}
					else {
						if (contentObject.getInt(NAME_THUMBNAILS_WIDTH) < smallThumbnailObject.getInt(NAME_THUMBNAILS_WIDTH)) {
							smallThumbnailObject = contentObject;
						}
						else if (contentObject.getInt(NAME_THUMBNAILS_WIDTH) > largeThumbnailObject.getInt(NAME_THUMBNAILS_WIDTH)) {
							largeThumbnailObject = contentObject;
						}
					}
				}
			}

			if (smallThumbnailObject != null) {
				this.smallThumbnailURL = smallThumbnailObject.getString(NAME_THUMBNAILS_URL);
			}

			if (largeThumbnailObject != null) {
				this.largeThumbnailURL = largeThumbnailObject.getString(NAME_THUMBNAILS_URL);
			}
		}
		catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::parseThumbnailUrls", "Error parsing thumbnail list: " + e.getMessage());
		}
	}

	/**
	 * Sets the category, which is the list of categories tagged for the
	 * content, can be used by application to enhance user experience Sets the
	 * key words, a list of comma separated keywords also used to enhance user
	 * experience
	 */
	protected void parseProperties()
	{
		this.properties = new Vector<SerializableNameValuePair>();
		String categoriesStr = getString(entryObject, NAME_CATEGORIES);

		if (categoriesStr != null) {
			properties.add(new SerializableNameValuePair(NAME_CATEGORIES, categoriesStr));
		}

		String keywords = getString(entryObject, NAME_KEYWORDS);
		if (keywords != null && !keywords.isEmpty()) {
			properties.add(new SerializableNameValuePair(NAME_KEYWORDS, keywords));
		}
	}

	/**
	 * Populates the list of ContentRenditionInfo objects for this content,
	 * content renditions currently can either be in the HDS or HLS format
	 */
	protected void parseContent()
	{
		if (getString(entryObject, NAME_CONTENT) == null) {
			return;
		}

		try
		{
			this.contentRenditionInfoList = new ArrayList<ContentRenditionInfo>();
			JSONArray content = entryObject.getJSONArray(NAME_CONTENT);

			for (int i = 0; i < content.length(); i++)
			{
				JSONObject contentObject = content.getJSONObject(i);

				ContentFormat contentFormat = null;
				String formatStr = getString(contentObject, NAME_CONTENT_FORMAT);
				if (formatStr.equals(VALUE_CONTENT_FORMAT_M3U8)) {
					contentFormat = ContentFormat.hls;
				}
				else if (formatStr.equals(VALUE_CONTENT_FORMAT_F4M)) {
					contentFormat = ContentFormat.hds;
				}

				String url = getString(contentObject, NAME_CONTENT_URL);
				String language = getString(contentObject, NAME_CONTENT_LANGUAGE);
				List<SerializableNameValuePair> renditionProperties = new ArrayList<SerializableNameValuePair>();
				renditionProperties.add(new SerializableNameValuePair(NAME_CONTENT_LANGUAGE, language));
				ContentRenditionInfo info = new ContentRenditionInfo(contentFormat, url, renditionProperties);
				this.contentRenditionInfoList.add(info);
			}

		}
		catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::parseContent", "Error parsing content list: " + e.getMessage());
		}
	}

	/**
	 * Parses the metadata of the content and populates the MetadatNode object,
	 * which contains the key/value pair pointing to a specific MetadataNode
	 * type like AuditudeMetadata, direct ad breaks in a JSON metadata string,
	 * custom ad markers metadata, or any custom MetadataNode subclass with a
	 * custom AdProivder implementation. Currently, this metadata is limited to
	 * ad related metadata
	 */
	protected void parseMetadata()
	{
		if (getString(entryObject, NAME_METADATA) == null) {
			return;
		}

		try
		{
			this.metadataNode = new MetadataNode();

			AdvertisingMetadata advertisingMetadata = null;
			JSONObject metadataObject = entryObject.getJSONObject(NAME_METADATA);

            try
            {
                if (metadataObject.has(NAME_METADATA_AD))
                {
                    JSONObject metadataAdObject = metadataObject.getJSONObject(NAME_METADATA_AD);

                    String adType = metadataAdObject.getString(METADATA_AD_TYPE);
                    JSONObject adDetailsObject = metadataAdObject.getJSONObject(METADATA_AD_DETAILS);

                    if (adType.equals(VALUE_AD_TYPE_PRIMETIME_ADS))
                    {
                        advertisingMetadata = createAuditudeMetadata(adDetailsObject);
                        metadataNode.setValue(DefaultMetadataKeys.AUDITUDE_METADATA_KEY.getValue(), "");
                    }
                    else if (adType.equals(VALUE_AD_TYPE_DIRECT_AD_BREAKS))
                    {
                        advertisingMetadata = createBasicMetadataFrom(adDetailsObject);
                        metadataNode.setValue(DefaultMetadataKeys.JSON_METADATA_KEY.getValue(), "");

						overlayAdItems = createOverlayAdItems(adDetailsObject);
                    }
                    else if (adType.equals(VALUE_AD_TYPE_CUSTOM_AD_MARKERS))
                    {
                        advertisingMetadata = createCustomAdMarkers(adDetailsObject);
                        metadataNode.setValue(DefaultMetadataKeys.CUSTOM_AD_MARKERS_METADATA_KEY.getValue(), "");
                    }
                    else if (adType.equals(VALUE_AD_TYPE_CUSTOM_AD_PROVIDER))
                    {
                        advertisingMetadata = createCustomAdProvider(adDetailsObject);
                        metadataNode.setValue(CustomAdProviderMetadata.CUSTOM_AD_PROVIDER_METADATA_KEY, "");
                    }

                    if (adDetailsObject.has(NODE_NAME_DISABLE_CONTENT_CACHE))
					{
                        metadataNode.setValue(DefaultMetadataKeys.DISABLE_CONTENT_CACHING.getValue(),
                                adDetailsObject.getString(NODE_NAME_DISABLE_CONTENT_CACHE));
                    }

                }

                if (metadataObject.has(NODE_NAME_METADATA_TIME_RANGES)) {
                    advertisingMetadata = parseTimeRangeMetadata(metadataNode,
                            metadataObject.getJSONObject(NODE_NAME_METADATA_TIME_RANGES),
                            advertisingMetadata);
                }

                if (metadataObject.has(NODE_NAME_SIGNALING_MODE)) {
                    advertisingMetadata.setSignalingMode(AdSignalingMode
                            .createFrom(metadataObject.getString(NODE_NAME_SIGNALING_MODE)));
                }

                if (advertisingMetadata != null) {
                    metadataNode.setNode(DefaultMetadataKeys.ADVERTISING_METADATA.getValue(), advertisingMetadata);
                }

            }
            catch (JSONException e)
            {
                AdVideoApplication.logger.w(LOG_TAG + "::parseMetadata",
                        "Error parsing ad metadata list: " + e.getMessage());
            }

            try
            {
                // add entitlement metadata if present
                JSONObject entitlementObject = metadataObject.getJSONObject(NAME_METADATA_ENTITLEMENT);
                if (entitlementObject != null)
                {
                    String resourceId = entitlementObject.getString(METADATA_ENTITLEMENT_ID);
                    if (resourceId != null)
                    {
                        EntitlementMetadata entitlementMetadata = new EntitlementMetadata();
                        entitlementMetadata.setValue(EntitlementMetadata.RESOURCE_ID_KEY, resourceId);

                        metadataNode.setNode(EntitlementMetadata.ENTITLEMENT_METADATA, entitlementMetadata);
                    }
                }
            }
            catch (JSONException e)
            {
                AdVideoApplication.logger.w(LOG_TAG + "::parseMetadata",
                        "Error parsing entitlement metadata list: " + e.getMessage());
            }

            try
            {
                if (metadataObject.has(NODE_NAME_METADATA_VIDEO_ANALYTICS))
                {
                    VideoAnalyticsMetadata videoAnalyticsMetadata = parseVideoAnalyticsMetadata(metadataObject.getJSONObject(NODE_NAME_METADATA_VIDEO_ANALYTICS));
                    metadataNode.setNode(DefaultMetadataKeys.VIDEO_ANALYTICS_METADATA_KEY.getValue(), videoAnalyticsMetadata);
                }
            }
            catch (JSONException e)
            {
                AdVideoApplication.logger.w(LOG_TAG + "::parseMetadata",
                        "Error parsing video analytics metadata list: " + e.getMessage());
            }


		}
        catch (JSONException e)
        {
			AdVideoApplication.logger.e(LOG_TAG + "::parseMetadata",
					"Error parsing metadata list: " + e.getMessage());
		}
	}

    protected VideoAnalyticsMetadata parseVideoAnalyticsMetadata(JSONObject jsonObject) throws JSONException
    {
        VideoAnalyticsMetadata videoAnalyticsMetadata = new VideoAnalyticsMetadata();

        if (jsonObject != null)
		{
            if (jsonObject.has(NODE_NAME_VA_VIDEO_ID)) {
                videoAnalyticsMetadata.setVideoId(jsonObject.getString(NODE_NAME_VA_VIDEO_ID));
            }

            if (jsonObject.has(NODE_NAME_VA_VIDEO_NAME)) {
                videoAnalyticsMetadata.setVideoName(jsonObject.getString(NODE_NAME_VA_VIDEO_NAME));
            }

            if (jsonObject.has(NODE_NAME_VA_VIDEO_CHAPTERS)) {
                JSONArray chapters = jsonObject.getJSONArray(NODE_NAME_VA_VIDEO_CHAPTERS);

                if (chapters.length() > 0)
                {
                    List<VideoAnalyticsChapterData> va_chapters = new ArrayList<VideoAnalyticsChapterData>();

                    for (int i=0; i < chapters.length(); i++)
                    {
                        JSONObject chapter = chapters.getJSONObject(i);

                        if (chapter.has(NODE_NAME_VA_VIDEO_CHAPTER_START) && chapter.has(NODE_NAME_VA_VIDEO_CHAPTER_END))
						{
                            try
                            {
                                Long startTime = Long.parseLong(chapter.getString(NODE_NAME_VA_VIDEO_CHAPTER_START));
                                Long endTime = Long.parseLong(chapter.getString(NODE_NAME_VA_VIDEO_CHAPTER_END));
                                VideoAnalyticsChapterData va_chapter = new VideoAnalyticsChapterData(startTime, endTime);

                                if (chapter.has(NODE_NAME_VA_VIDEO_CHAPTER_NAME)) {
                                    String name = chapter.getString(NODE_NAME_VA_VIDEO_CHAPTER_NAME);
                                    va_chapter.setName(name);
                                }

                                va_chapters.add(va_chapter);
                            }
                            catch (NumberFormatException e)
                            {
                                AdVideoApplication.logger.e(LOG_TAG + "::parseVideoAnalyticsMetadata", e.getMessage());
                            }
                        }
                    }

                    if (!va_chapters.isEmpty()) {
                        videoAnalyticsMetadata.setChapters(va_chapters);
                    }
                }
            }

            if (jsonObject.has(NODE_NAME_VA_PLAY_NAME)) {
                videoAnalyticsMetadata.setPlayName(jsonObject.getString(NODE_NAME_VA_PLAY_NAME));
            }

            if (jsonObject.has(NODE_NAME_VA_ENABLE_CHAPTER_TRACKING)) {
                videoAnalyticsMetadata.setEnableChapterTracking(NumberUtils.parseBoolean(jsonObject.getString(NODE_NAME_VA_ENABLE_CHAPTER_TRACKING)));
            }
        }

        return videoAnalyticsMetadata;
    }

    /**
	 * Creates Custom Ad Provider Metadata from JSON object This is only an
	 * example of how a customer can have custom ad data in their feeds and
	 * implement their own AdProvider to resolve the ad data to ad content urls
	 * and Placement info
	 * 
	 * @param jsonObject
	 *            - to be parsed
	 * @return CustomAdProviderMetadata -
	 */
	protected CustomAdProviderMetadata createCustomAdProvider(JSONObject jsonObject)
	{
		CustomAdProviderMetadata result = new CustomAdProviderMetadata();

		try {
			String domain = jsonObject.getString(NODE_NAME_CUSTOM_AD_PROVIDER_DOMAIN);
			result.setDomain(domain);

			JSONArray jsonArray = jsonObject.getJSONArray(NODE_NAME_CUSTOM_AD_PROVIDER_AD_PATTERN);

			ArrayList<CustomAdProviderMetadata.AdPattern> adPatternList = new ArrayList<CustomAdProviderMetadata.AdPattern>();
			for (int i = 0; i < jsonArray.length(); i++)
			{
				JSONObject aJsonObject = jsonArray.getJSONObject(i);
				Long time = Long.parseLong(aJsonObject.getString(NODE_NAME_CUSTOM_AD_PROVIDER_TIME));
				Long count = Long.parseLong(aJsonObject.getString(NODE_NAME_CUSTOM_AD_PROVIDER_COUNT));
				Long duration = Long.parseLong(aJsonObject.getString(NODE_NAME_CUSTOM_AD_PROVIDER_DURATION));

				CustomAdProviderMetadata.AdPattern adPattern = new CustomAdProviderMetadata.AdPattern(time, count, duration);
				adPatternList.add(adPattern);
			}
			result.setAdPatternList(adPatternList);

			Metadata contentMetadata = getMetadataFromJson(jsonObject, NODE_NAME_CUSTOM_AD_PROVIDER_CONTENT_METADATA);
			result.setContentMetadata(contentMetadata);
		}
		catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::createCustomAdProvider",
					"Error parsing custom ad provider: " + e.getMessage());
		}

		return result;
	}

	/**
	 * Creates and returns an AuditudeSettings object
	 * 
	 * @param jsonObject
	 *            - to be parsed
	 * @return AuditudeSettings - set with the Auditude domain, media id, zone
	 *         id, targeting parameters, and creative packaging option.
	 */
	protected AdvertisingMetadata createAuditudeMetadata(JSONObject jsonObject)
	{
		AuditudeSettings result = new AuditudeSettings();
		String zoneId;

		try {
			String domain = jsonObject.getString(NODE_NAME_AD_DOMAIN);
			result.setDomain(domain);
			String mediaId = jsonObject.getString(NODE_NAME_AD_MEDIAID);
			result.setMediaId(mediaId);
			zoneId = jsonObject.getString(NODE_NAME_AD_ZONEID);
			result.setZoneId(zoneId);

			Metadata targetingParameters = getMetadataFromJson(jsonObject, NODE_NAME_AD_TARGETING);
			if (HotStarApplication.getInstance().getLoginStatus() == HotStarApplication.UserStatusType.STATUS_USER_LOGIN_REGISTERED) {
				HotStarUserInfo userInfo = HotStarApplication.getInstance().getUserInfo();
				targetingParameters.setValue("age", String.valueOf(userInfo.age));
				targetingParameters.setValue("usergender", userInfo.gender);
				targetingParameters.setValue("location", HotStarApplication.getInstance().getLocation());
				targetingParameters.setValue("device", SamsungPhoneInfo.getInstance().modelString());
			}

			result.setTargetingParameters(targetingParameters);

			boolean creativePackagingEnabled = false;
			if (jsonObject.has(NODE_NAME_CREATIVE_REPACKAGING_ENABLED)) {
				creativePackagingEnabled = NumberUtils.parseBoolean(jsonObject
						.getString(NODE_NAME_CREATIVE_REPACKAGING_ENABLED));
			}
			result.setCreativeRepackagingEnabled(creativePackagingEnabled);
		}
		catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::createAuditudeMetadata",
					"Error parsing auditude metadata: " + e.getMessage());
		}
		catch (NumberFormatException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::createAuditudeMetadata",
					"Error parsing auditude metadata: " + e.getMessage());
		}

		return result;
	}
	
	/**
     * Returns a TimeRangeCollection of TimeRanges representing marker ranges, delete ranges,
     * or replace ranges. Also sets the TIME_RANGES_METADATA_KEY in the metadata node
     * @param metadataTimeRangeObject to be parsed
     * @return TimeRangeCollection of type DELETE, MARK, or REPLACE
     */
    protected AdvertisingMetadata parseTimeRangeMetadata(MetadataNode metadataNode,
														 JSONObject metadataTimeRangeObject, AdvertisingMetadata advertisingMetadata)
	{
        TimeRangeCollection timeRanges = null;

        try {
            String timeRangeType = metadataTimeRangeObject.getString(METADATA_TIME_RANGE_TYPE);
            JSONArray timeRangesArray = metadataTimeRangeObject.getJSONArray(METADATA_TIME_RANGE_LIST);
            Metadata options = null;

            if (timeRangeType.equals(VALUE_TIME_RANGE_TYPE_DELETE)) {
                metadataNode.setValue(DefaultMetadataKeys.TIME_RANGES_METADATA_KEY.getValue(), VALUE_TIME_RANGE_TYPE_DELETE);
                timeRanges = new TimeRangeCollection(TimeRangeCollection.Type.DELETE_RANGES);
                for (int i = 0; i < timeRangesArray.length(); i++) {
                    JSONObject timeRangeObject = timeRangesArray.getJSONObject(i);
                    Long begin = Long.parseLong(timeRangeObject.getString(METADATA_TIME_RANGE_BEGIN));
                    Long end = Long.parseLong(timeRangeObject.getString(METADATA_TIME_RANGE_END));
                    TimeRange timeRange = TimeRange.createRange(begin, end - begin);
                    timeRanges.addTimeRange(timeRange);
                }
            }
			else if (timeRangeType.equals(VALUE_TIME_RANGE_TYPE_MARK)) {
                metadataNode.setValue(DefaultMetadataKeys.TIME_RANGES_METADATA_KEY.getValue(), VALUE_TIME_RANGE_TYPE_MARK);
                timeRanges = new TimeRangeCollection(TimeRangeCollection.Type.MARK_RANGES);
                for (int i = 0; i < timeRangesArray.length(); i++) {
                    JSONObject timeRangeObject = timeRangesArray.getJSONObject(i);
                    Long begin = Long.parseLong(timeRangeObject.getString(METADATA_TIME_RANGE_BEGIN));
                    Long end = Long.parseLong(timeRangeObject.getString(METADATA_TIME_RANGE_END));
                    TimeRange timeRange = TimeRange.createRange(begin, end - begin);
                    timeRanges.addTimeRange(timeRange);
                }
                // set other time-range related key/values
                options = new MetadataNode();
                boolean isAdjustSeekPositionEnabled = metadataTimeRangeObject
                        .getBoolean(NODE_NAME_CUSTOM_AD_MARKERS_ADJUST_SEEK_POSITION);
                if (isAdjustSeekPositionEnabled) {
                    options.setValue(DefaultMetadataKeys.METADATA_KEY_ADJUST_SEEK_ENABLED.getValue(), VALUE_TRUE);
                } else {
                    options.setValue(DefaultMetadataKeys.METADATA_KEY_ADJUST_SEEK_ENABLED.getValue(), VALUE_FALSE);
                }
            }
			else if (timeRangeType.equals(VALUE_TIME_RANGE_TYPE_REPLACE)) {
                metadataNode.setValue(DefaultMetadataKeys.TIME_RANGES_METADATA_KEY.getValue(), VALUE_TIME_RANGE_TYPE_REPLACE);
                timeRanges = new TimeRangeCollection(TimeRangeCollection.Type.REPLACE_RANGES);
                for (int i = 0; i < timeRangesArray.length(); i++) {
                    JSONObject timeRangeObject = timeRangesArray.getJSONObject(i);
                    Long begin = Long.parseLong(timeRangeObject.getString(METADATA_TIME_RANGE_BEGIN));
                    Long end = Long.parseLong(timeRangeObject.getString(METADATA_TIME_RANGE_END));

                    Long replacementDuration;
                    if (timeRangeObject.has(METADATA_TIME_RANGE_REPLACEMENT_DURATION)) {
                        replacementDuration = Long.parseLong(timeRangeObject.getString(METADATA_TIME_RANGE_REPLACEMENT_DURATION));
                    } else {
                        replacementDuration = -1L;
                    }

                    ReplacementTimeRange timeRange = ReplacementTimeRange.createRange(begin, end - begin, replacementDuration);
                    timeRanges.addTimeRange(timeRange);
                }
            }

            if (advertisingMetadata == null) {
                advertisingMetadata = new AdvertisingMetadata();
            }
            advertisingMetadata.setTimeRanges(timeRanges, options);

        } catch (JSONException e) {
            //PMPDemoApp.logger.e(LOG_TAG + "#parseTimeRangeMetadata", e.getMessage());
        }
        return advertisingMetadata;
    }

	/**
	 * Creates and returns the direct ad break metadata from JSON object. The
	 * JSON Metadata String for direct ad breaks expects data in a certain
	 * format, see the Reference JSON structure for the format
	 * 
	 * @param jsonObject
	 *            - to be parsed
	 * @return AdvertisingMetadata - set with a string containing direct ad
	 *         break data
	 * @throws JSONException
	 *             if there is an error parsing the JSON object
	 */
	protected AdvertisingMetadata createBasicMetadataFrom(JSONObject jsonObject)
			throws JSONException
	{
		AdvertisingMetadata result = new AdvertisingMetadata();

		String serializedJSONAdBreaks = jsonObject.getString(NODE_NAME_ADBREAKS);
		result.setValue(DefaultMetadataKeys.JSON_METADATA_KEY.getValue(), serializedJSONAdBreaks);
		return result;
	}

	/**
	 * Creates Metadata from targeting parameters in the Auditude metadata,
	 * helper function of createAuditudeMetadata
	 * 
	 * @param jsonObject
	 *            - to be parsed
	 * @param nodeName
	 *            - to be searched for
	 * @return Metadata - metadata containing the key/value pairs
	 * @throws JSONException
	 *             if there is an error parsing the JSON object
	 */
	protected Metadata getMetadataFromJson(JSONObject jsonObject, String nodeName)
			throws JSONException
	{
		Metadata metadata = new MetadataNode();
		if (jsonObject.has(nodeName))
		{
			JSONArray jsonArray = new JSONArray(jsonObject.getString(nodeName));
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonChild = jsonArray.getJSONObject(i);

				String key = null;
				if (jsonChild.has(NODE_NAME_KEY)) {
					key = jsonChild.getString(NODE_NAME_KEY).trim();
				}

				String value = null;
				if (jsonChild.has(NODE_NAME_VALUE)) {
					value = jsonChild.getString(NODE_NAME_VALUE).trim();
				}

				if (!StringUtils.isEmpty(key) && !StringUtils.isEmpty(value)) {
					metadata.setValue(key, value);
				}
			}
		}
		return metadata;
	}

	/**
	 * Creates and returns a metadata object for custom ad markers
	 * 
	 * @param jsonObject
	 *            - to be parsed
	 * @return AdvertisingMetadata - containing a TimeRangeCollection of
	 *         TimeRanges for each custom ad marker, also sets the
	 *         adjust-seek-position option
	 * @throws JSONException
	 *             if there is an error parsing the JSON object
	 */
	protected AdvertisingMetadata createCustomAdMarkers(JSONObject jsonObject)
			throws JSONException
	{
		AdvertisingMetadata result = new AdvertisingMetadata();

		TimeRangeCollection timeRangeCollection = new TimeRangeCollection(TimeRangeCollection.Type.MARK_RANGES);
		boolean isAdjustSeekPositionEnabled = jsonObject.getBoolean(NODE_NAME_CUSTOM_AD_MARKERS_ADJUST_SEEK_POSITION);
		JSONArray jsonArray = jsonObject.getJSONArray(NODE_NAME_CUSTOM_AD_MARKERS_TIME_RANGES);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject aJsonObject = jsonArray.getJSONObject(i);
			Long begin = Long.parseLong(aJsonObject.getString(NODE_NAME_CUSTOM_AD_MARKER_BEGIN));
			Long end = Long.parseLong(aJsonObject.getString(NODE_NAME_CUSTOM_AD_MARKER_END));

			TimeRange timeRange = TimeRange.createRange(begin, end - begin);
			timeRangeCollection.addTimeRange(timeRange);
		}

		Metadata options = new MetadataNode();
		if (isAdjustSeekPositionEnabled) {
			options.setValue(DefaultMetadataKeys.METADATA_KEY_ADJUST_SEEK_ENABLED.getValue(), VALUE_TRUE);
		} else {
			options.setValue(DefaultMetadataKeys.METADATA_KEY_ADJUST_SEEK_ENABLED.getValue(), VALUE_FALSE);
		}

		result.setNode(
				DefaultMetadataKeys.CUSTOM_AD_MARKERS_METADATA_KEY.getValue(),
				(MetadataNode) timeRangeCollection.toMetadata(options));
		return result;
	}

	/**
	 * Populates the list of ContentRenditionInfo objects by content format (HDS
	 * and HLS)
	 */
	protected void parseContentFormat()
	{
		if (this.contentRenditionInfoList == null) {
			parseContent();
		}

		this.HDSContentRenditionInfoList = new ArrayList<ContentRenditionInfo>();
		this.HLSContentRenditionInfoList = new ArrayList<ContentRenditionInfo>();
		for (ContentRenditionInfo rendition : this.contentRenditionInfoList) {
			if (rendition.getContentFormat() == ContentFormat.hds) {
				this.HDSContentRenditionInfoList.add(rendition);
			} else if (rendition.getContentFormat() == ContentFormat.hls) {
				this.HLSContentRenditionInfoList.add(rendition);
			}
		}

		if (this.HDSContentRenditionInfoList.isEmpty())
			this.HDSContentRenditionInfoList = null;
		if (this.HLSContentRenditionInfoList.isEmpty())
			this.HLSContentRenditionInfoList = null;
	}


	/**
	 * Creates and returns the overlay ad items from JSON object.
	 *
	 * @param jsonObject
	 * @return
	 */
	protected ArrayList<OverlayAdItem> createOverlayAdItems(JSONObject jsonObject) {

		if (!jsonObject.has(NODE_NAME_ADBREAKS))
			return null;

		ArrayList<OverlayAdItem> list = new ArrayList<OverlayAdItem>();
		try {
			JSONArray arrayObject = jsonObject.getJSONArray(NODE_NAME_ADBREAKS);
			for(int i = 0 ; i < arrayObject.length(); i++) {
				JSONObject adbreakObject = arrayObject.getJSONObject(i);
				ArrayList<OverlayAdItem> adList = createOverlayAdListItems(adbreakObject);
				if (adList != null) {
					list.addAll(adList);
				}
			}

			if (list.size() == 0) return null;
			return list;
		}
		catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::createOverlayAdItems",
					"Error parsing custom overlay ad items: " + e.getMessage());
		}
		catch (NumberFormatException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::createOverlayAdItems",
					"Error parsing custom overlay ad items: " + e.getMessage());
		}

		return null;
	}


	/**
	 * Create and returns overlay ad-list items
	 *
	 * @param jsonObject
	 * @return
	 */
	protected ArrayList<OverlayAdItem> createOverlayAdListItems(JSONObject jsonObject) {

		ArrayList<OverlayAdItem> list = new ArrayList<OverlayAdItem>();
		try {
			int adStartTime = jsonObject.getInt("time");
			String adTag = jsonObject.getString("tag");
			int adReplace = jsonObject.getInt("replace");

			if (jsonObject.has("ad-list")) {
				JSONArray adListObjects = jsonObject.getJSONArray("ad-list");

				// iterate all ad-list items
				for (int i=0; i<adListObjects.length(); i++) {
					JSONObject adListObject = adListObjects.getJSONObject(i);
					int adDuration = adListObject.getInt("duration");
					String adListTag = adListObject.getString("tag");
					String adUrl = adListObject.getString("url");

					if (adUrl.contains("png") || adUrl.contains("jpg")) {
						OverlayAdItem adItem = new OverlayAdItem();
						adItem.setAdStartTime(adStartTime);
						adItem.setAdReplace(adReplace);
						adItem.setAdTag(adTag);
						adItem.setAdDuration(adDuration);
						adItem.setAdListTag(adListTag);
						adItem.setAdUrl(adUrl);

						list.add(adItem);
					}

					adStartTime += adDuration;
				}

				if (list.size() == 0) return null;
				return list;
			}
		}
		catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::createOverlayAdListItems",
					"Error parsing custom overlay ad-list items: " + e.getMessage());
		}
		catch (NumberFormatException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::createOverlayAdListItems",
					"Error parsing custom overlay ad-list items: " + e.getMessage());
		}

		return null;
	}
}
