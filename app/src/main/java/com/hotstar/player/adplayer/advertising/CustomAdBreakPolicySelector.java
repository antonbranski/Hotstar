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

import com.adobe.mediacore.DefaultAdPolicySelector;
import com.adobe.mediacore.MediaPlayerItem;

/**
 * Since this is an empty class with no customized behavior, by default the
 * player will implement the following default behavior of these 8 scenarios
 * 
 * Scenario 1: Running into an ad break during normal play Default Behavior:
 * Play a watched ad break, remove after play
 * 
 * Scenario 2: Seek forward over ad break(s) into content Default Behavior: Play
 * the last break skipped over, then skip to the selected seek position
 * 
 * Scenario 3: Seek backward over ad break(s) into content Default Behavior: Do
 * not play any ad break and skip to the user seek position
 * 
 * Scenario 4: Seek forward into ad break Default Behavior: Play from the the
 * beginning of ad seeked into
 * 
 * Scenario 5: Seek backward into ad break Default Behavior: Play from the
 * beginning of the ad seeked into
 * 
 * Scenario 6: Seek over watched ad break(s) Default Behavior: If last ad break
 * skipped is watched already, skip to user seeked position
 * 
 * Scenario 7: Seek into a watched ad break Default Behavior: Skip ad break and
 * seek to position immediately after ad
 * 
 * Scenario 8: Trick Play Default Behavior: All ads are skipped during trick
 * play, play the last break skipped over after trick play ends, skip to the
 * user selected trick play position upon break(s) playback completion
 * 
 * @author dshei
 * 
 */

public class CustomAdBreakPolicySelector extends DefaultAdPolicySelector {

	public CustomAdBreakPolicySelector(MediaPlayerItem arg0) {
		super(arg0);
	}


}
