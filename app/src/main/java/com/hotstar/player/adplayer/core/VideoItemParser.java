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

package com.hotstar.player.adplayer.core;

import java.util.ArrayList;

import com.hotstar.player.adplayer.feeds.FeedAdapterFactory;
import com.hotstar.player.adplayer.feeds.IFeedAdapter;
import com.hotstar.player.adplayer.feeds.IFeedItemAdapter;

/**
 * The VideoItemParser uses the FeedAdapterFactory to get an instance of the
 * IFeedAdapter for parsing the feed response. The IFeedAdapter represents the
 * entire content response and the IFeedItemAdapter represents a single content
 * item from the feed. The VideoItemParser uses the IFeedAdapter to iterate
 * through the content items and build the list of VideoItem objects
 */
public class VideoItemParser {

	private final ArrayList<VideoItem> liveContentList = new ArrayList<VideoItem>();
	private final ArrayList<VideoItem> vodContentList = new ArrayList<VideoItem>();

	/**
	 * Class constructor. This method encompasses the entire parsing process of
	 * the content response and builds a list of VideoItem objects that is the
	 * Primetime reference implementation's representation of each video content.
	 * 
	 * @param response
	 *            Content response from the VCMS in any format
	 * @param vcmsType
	 *            Type of VCMS that the Primetime reference implementation is interacting with
	 * @throws Exception
	 */
	public VideoItemParser(String response, String vcmsType) throws Exception {
		IFeedAdapter feedAdapter = FeedAdapterFactory.getFeedAdapter(response,
				vcmsType);

		if (feedAdapter != null) {
			IFeedItemAdapter feedItem = null;

			while ((feedItem = feedAdapter.getNextItem()) != null) {
				VideoItem videoItem = new VideoItem(feedItem);

				switch (videoItem.getType()) {
				case live:
					liveContentList.add(videoItem);
					break;

				case vod:
					vodContentList.add(videoItem);
					break;

				default:
					throw new RuntimeException("Unsupported content type: "
							+ videoItem.getType());
				}
			}
		}
	}

	/**
	 * Returns the list of live content video items
	 * 
	 * @return
	 */
	public ArrayList<VideoItem> getLiveContentList() {
		return liveContentList;
	}

	/**
	 * Returns the list of VOD content video items
	 * 
	 * @return
	 */
	public ArrayList<VideoItem> getVodContentList() {
		return vodContentList;
	}
}
