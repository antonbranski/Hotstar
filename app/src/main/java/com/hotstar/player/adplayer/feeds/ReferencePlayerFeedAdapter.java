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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hotstar.player.adplayer.AdVideoApplication;

/**
 * The ReferencePlayerFeedAdapter is the corresponding feed adapter to the
 * JSON-based input format that Primetime Reference Implementation currently uses by default. It
 * is the class for iterating the Primetime reference implementation feed response content items.
 * 
 * @author dshei
 * 
 */
public final class ReferencePlayerFeedAdapter implements IFeedAdapter {
	private static final String LOG_TAG = "[Player]::ReferencePlayerFeedAdapter";

	private final static String NAME_ENTRIES = "entries";

	protected final JSONArray entries;
	protected int entryIndex = -1;

	/**
	 * Class constructor for the ReferencePlayerFeedAdapter. Used by the
	 * getInstance method once the JSON feed is parsed
	 * 
	 * @param entries
	 *            - a JSON array of video content items
	 */
	protected ReferencePlayerFeedAdapter(JSONArray entries) {
		this.entries = entries;
	}

	/**
	 * Returns an instance of the ReferencePlayerFeedAdapter after parsing the
	 * JSON feed
	 * 
	 * @param jsonFeed
	 *            - feed response in JSON format to be parsed
	 * @return ReferencePlayerFeedAdapter - an instance of the
	 *         ReferencePlayerFeedAdapter that will iterate through the JSON
	 *         content list response
	 * @throws FeedParsingException
	 *             - if there is an error parsing the JSON feed
	 */
	public static ReferencePlayerFeedAdapter getInstance(String jsonFeed)
			throws FeedParsingException {
		if (jsonFeed == null) {
			return null;
		}

		try {
			JSONObject object = new JSONObject(jsonFeed);
			JSONArray entries = object.getJSONArray(NAME_ENTRIES);
			return new ReferencePlayerFeedAdapter(entries);
		} catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::getInstance",
					"Error parsing content list: " + e.getMessage());
			throw new FeedParsingException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IFeedItemAdapter getFirstItem() {
		this.entryIndex = 0;
		return getEntry();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IFeedItemAdapter getNextItem() {
		this.entryIndex++;
		return getEntry();
	}

	/**
	 * Returns the ReferencePlayerFeedItemAdapter that represents a single
	 * content item from the Primetime reference implementation JSON input feed
	 * 
	 * @return ReferencePlayerFeedItemAdapter - the adapter that will be used
	 *         for retrieving specific data for a given content item
	 */
	protected ReferencePlayerFeedItemAdapter getEntry() {
		try {
			if (entryIndex < entries.length()) {
				return new ReferencePlayerFeedItemAdapter(
						entries.getJSONObject(entryIndex));
			}
		} catch (JSONException e) {
			AdVideoApplication.logger.e(LOG_TAG + "::getEntry",
					"Error parsing content list: " + e.getMessage());
		}

		return null;
	}

}
