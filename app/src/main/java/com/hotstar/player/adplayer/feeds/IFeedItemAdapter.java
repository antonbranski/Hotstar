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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import com.adobe.mediacore.metadata.MetadataNode;
import com.hotstar.player.adplayer.feeds.ContentRenditionInfo.ContentType;
import com.hotstar.player.adplayer.core.OverlayAdItem;
import com.hotstar.player.adplayer.utils.SerializableNameValuePair;

/**
 * The IFeedItemAdapter is the interface to be implemented for retrieving
 * specific data for a given content item.
 */
public interface IFeedItemAdapter {
	/**
	 * Returns the content identifier from the response. Can map to an item guid
	 * that may be included in the response.
	 * 
	 * @return String - item identifier
	 */
	public String getId();

	/**
	 * Returns the content's title
	 * 
	 * @return String - content title
	 */
	public String getTitle();

	/**
	 * Returns the content type as live or vod.
	 * 
	 * @return ContentType - see ContentType enum definition above.
	 */
	public ContentType getContentType();

	/**
	 * Returns the list of ContentRenditionInfo objects for the renditions of
	 * this content that have the HDS format.
	 * 
	 * @return List<ContentRenditionInfo> - if one or more content renditions
	 *         with the HDS format exist. null - if none of the content
	 *         renditions use the HDS format.
	 */
	public List<ContentRenditionInfo> getHDSContentRenditions();

	/**
	 * Returns the list of ContentRenditionInfo objects for the renditions of
	 * this content that have the HLS format.
	 * 
	 * @return List<ContentRenditionInfo> - if one or more content renditions
	 *         with the HLS format exist. null - if none of the content
	 *         renditions use the HLS format.
	 */
	public List<ContentRenditionInfo> getHLSContentRenditions();

	/**
	 * Returns the list of ContentRenditionInfo objects for the renditions of
	 * this content that have the HLS format.
	 * 
	 * @return List<ContentRenditionInfo> - if one or more content renditions
	 *         exist. null - if no content renditions exist.
	 */
	public List<ContentRenditionInfo> getContentRenditions();

	/**
	 * Returns the MetadataNode that contains the key/value pair pointing to a
	 * specific MetadataNode type like AuditudeMetadata, direct ad breaks in a
	 * JSON metadata string or custom ad markers metadata which are supported by
	 * PSDK's corresponding inbuilt AdProvider implementations. The Metadata
	 * node type can also refer to any custom MetadataNode subclass with a
	 * custom AdProvider implementation that may be defined and registered by
	 * the client.
	 * 
	 * Refer to the PSDK for creating
	 * com.adobe.mediacore.metadata.AuditudeMetadata. The MetadataNode returned
	 * should have a node with key set to
	 * com.adobe.mediacore.metadata.DefaultMetadataKeys.AUDITUDE_METADATA_KEY
	 * and value set to an instance of AuditudeMetadata.
	 * 
	 * The JSON Metadata String for direct ad breaks expects data in a certain
	 * format, see the Reference JSON structure for the JSON for ad breaks. The
	 * MetadataNode returned should have an entry with key set to
	 * com.adobe.mediacore.metadata.DefaultMetadataKeys.JSON_METADATA_KEY and
	 * value set to the json string representing the ad breaks.
	 * 
	 * Refer to com.adobe.mediacore.utils.TimeRangeCollection for creating
	 * custom ad markers metadata. The MetadataNode returned should have an
	 * entry with key set to com.adobe.mediacore.metadata.DefaultMetadataKeys.
	 * CUSTOM_AD_MARKERS_METADATA_KEY and value set to the object returned by
	 * TimeRangeCollection.toMetadata().
	 * 
	 * Refer to the Primetime Reference Implementation for an example on how each
	 * of these metadata types as well as a custom Metadata type are created
	 * using the reference JSON structure.
	 * 
	 * @return com.adobe.mediacore.metadata.MetadataNode - if the content
	 *         includes metadata. Currently this is limited to ad-related
	 *         metadata used by the PSDK for ad serving. null - if the content
	 *         response does not include any metadata
	 */
	public MetadataNode getStreamMetadata();

	/**
	 * Return overlay ad item list
	 *
	 * @return
	 */
	public ArrayList<OverlayAdItem> getOverlayAdItems();

	/**
	 * Returns a URL to a thumbnail for this item that will be used as the large
	 * thumbnail in the app.
	 * 
	 * @return String - containing the URL if the response contains a URL to a
	 *         thumbnail that can be classified as "large". null - if the
	 *         response does not include the URL to a thumbnail that the client
	 *         would like to display as the large thumbnail
	 */
	public String getStreamThumbnailLarge();

	/**
	 * Returns a URL to a thumbnail for this item that will be used as the small
	 * thumbnail in the app.
	 * 
	 * @return String - containing the URL if the response contains a URL to a
	 *         thumbnail that can be classified as "small". null - if the
	 *         response does not include the URL to a thumbnail that the client
	 *         would like to display as the small thumbnail
	 */
	public String getStreamThumbnailSmall();

	/**
	 * Returns a list of properties as name value pairs such as content
	 * categories, keywords etc that can be used by the app to enhance the
	 * experience.
	 * 
	 * @return List<SerializableNameValuePair> - the list may be empty if no
	 *         properties need to be set.
	 */
	public List<SerializableNameValuePair> getProperties();

}
