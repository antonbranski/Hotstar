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

public class FeedAdapterFactory {
	public static final String VCMS_TYPE_REFERENCE = "REFERENCE";

	/**
	 * Returns the corresponding IFeedAdapter based on the given VCMS type
	 * 
	 * @param feedResponse
	 *            - response that can be one of various media input feed formats
	 * @param VCMSType
	 *            - VCMS type
	 * @return IFeedAdapter - an instance of a feed adapter class that
	 *         corresponds with the VCMSType input
	 * @throws FeedParsingException
	 *             - if there is an error parsing the feed response
	 */
	public static IFeedAdapter getFeedAdapter(String feedResponse,
			String VCMSType) throws FeedParsingException {
		if (VCMSType == null) {
			return null;
		} else if (VCMSType.equalsIgnoreCase(VCMS_TYPE_REFERENCE)) {
			return getReferencePlayerFeedAdapter(feedResponse);
		}

		return null;
	}

	/**
	 * Returns the reference implementation feed adapter, which is the corresponding
	 * feed adapter to the JSON-based input format that the Reference Implementation
	 * currently uses by default
	 * 
	 * @param feedResponse
	 *            - content list response in JSON format
	 * @return IFeedAdapter - an instance of the ReferencePlayerFeedAdapter that
	 *         will iterate through the JSON content list response
	 * @throws FeedParsingException
	 *             - if there is an error parsing the JSON content list
	 */
	public static IFeedAdapter getReferencePlayerFeedAdapter(String feedResponse)
			throws FeedParsingException {
		return ReferencePlayerFeedAdapter.getInstance(feedResponse);
	}
}