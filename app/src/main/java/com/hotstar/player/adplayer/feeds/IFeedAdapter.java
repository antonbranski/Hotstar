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

/**
 * IFeedAdapter is the interface to be implemented for iterating the feed
 * response content items
 */
public interface IFeedAdapter {
	/**
	 * Returns the first content item in the feed response. Use it for resetting
	 * the item list iteration
	 * 
	 * @return IFeedItemAdapter - if the number of items is greater than 0
	 */
	public IFeedItemAdapter getFirstItem();

	/**
	 * Returns the next content item in the feed response. Use it for iterating
	 * through the feed response items
	 * 
	 * @return IFeedItemAdapter - if the number of items is greater than the
	 *         current iteration count, returns null if no more items exist
	 */
	public IFeedItemAdapter getNextItem();
}
