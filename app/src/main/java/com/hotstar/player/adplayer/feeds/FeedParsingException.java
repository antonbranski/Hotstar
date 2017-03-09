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

public class FeedParsingException extends Exception {
	private static final long serialVersionUID = 1L;

	public FeedParsingException() {
		super();
	}

	public FeedParsingException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public FeedParsingException(String detailMessage) {
		super(detailMessage);
	}

	public FeedParsingException(Throwable throwable) {
		super(throwable);
	}

}
