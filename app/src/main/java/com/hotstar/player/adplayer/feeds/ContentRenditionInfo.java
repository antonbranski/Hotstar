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

import java.util.List;

import com.hotstar.player.adplayer.utils.SerializableNameValuePair;

/**
 * The ContentRenditionInfo class represents a rendition of a video content,
 * which can be a backup, alternate camera angels, etc ...
 * 
 * @author dshei
 * 
 */
public class ContentRenditionInfo {

	private ContentFormat format;
	private String url;
	private List<SerializableNameValuePair> renditionProperties;

	/**
	 * Class constructor. The ContentRenditionInfo represents a rendition of a
	 * video content
	 * 
	 * @param format
	 *            - See ContentFormat enum definition
	 * @param url
	 *            - Content url for this rendition
	 * @param renditionProperties
	 *            - Name Value pairs for different properties like language,
	 *            camera angle etc describing the rendition
	 */
	public ContentRenditionInfo(ContentFormat format, String url,
			List<SerializableNameValuePair> renditionProperties) {
		this.format = format;
		this.url = url;
		this.renditionProperties = renditionProperties;
	}

	/**
	 * Returns the content format
	 * 
	 * @return ContentFormat - see ContentFormat enum definition
	 */
	public ContentFormat getContentFormat() {
		return format;
	}

	/**
	 * Returns the rendition url
	 * 
	 * @return String - rendition url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the rendition properties
	 * 
	 * @return List<SerializableNameValuePair> - Name Value pairs for different
	 *         properties like language, camera angle etc describing the
	 *         rendition
	 */
	public List<SerializableNameValuePair> getRenditionProperties() {
		return renditionProperties;
	}

	public enum ContentFormat {
		hds, hls
	}

	public enum ContentType {
		live, vod
	}
}
