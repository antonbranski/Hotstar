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

package com.hotstar.player.adplayer.advertising;

import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.timeline.PlacementOpportunity;
import com.adobe.mediacore.timeline.advertising.ContentResolver;
import com.adobe.mediacore.timeline.advertising.ContentTracker;

public class CustomAdProviderContentResolver extends ContentResolver {

	@Override
	protected ContentTracker doProvideAdTracker() {
		return null;
	}

	@Override
	protected void doResolveAds(Metadata arg0, PlacementOpportunity arg1) {
	}

	@Override
	protected boolean doCanResolve(PlacementOpportunity arg0) {
		return false;
	}
}
