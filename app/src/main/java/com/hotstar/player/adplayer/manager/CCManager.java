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

package com.hotstar.player.adplayer.manager;

import java.util.List;

/**
 * 
 * The CCManager handles the Closed Caption functionality
 * 
 */
public class CCManager implements IManager {

	/**
	 * CCManagerEventListener: listeners that can respond appropriately to
	 * closed captioning event
	 */
	public interface CCManagerEventListener {

		/**
		 * Handle the event when the closed caption tracks activity changes
		 */
		public void onCaptionUpdated();

	}

	public void addEventListener(CCManagerEventListener eventListener) {

	}

	public void removeEventListener(CCManagerEventListener eventListener) {

	}

	/**
	 * Converts CC tracks to an array
	 * 
	 * @param label
	 * @return empty list
	 */
	public List<String> getClosedCaptionTracks(String label) {
		List<String> ccArray = null;
		return ccArray;
	}

	/**
	 * Selects the CC track
	 * 
	 * @param index
	 */
	public void selectClosedCaptionTrack(int index) {

	}

	/**
	 * Returns the index of the current CC track
	 * 
	 * @return default value of -1
	 */
	public int getSelectedClosedCaptionsIndex() {
		return -1;
	}

	/**
	 * Checks if CC option is on or off
	 * 
	 * @return false
	 */
	public boolean hasClosedCaptions() {
		return false;
	}

	/**
	 * Empty call to update CC visibility
	 */
	public void setCCVisibility(boolean visible) {

	}

	/**
	 * Empty call to get CC visibility
	 */
	public boolean getCCVisibility() {
		return false;
	}

	/**
	 * Empty call to set CC style
	 */
	public void setCCStyle() {

	}

	@Override
	public void destroy() {
	}

	public static final String SETTINGS_CC_VISIBILITY = "settings_cc_visibility";
	public static final String SETTINGS_CC_STYLE_FONT = "settings_cc_style_font";
	public static final String SETTINGS_CC_STYLE_FONT_COLOR = "settings_cc_style_font_color";
	public static final String SETTINGS_CC_STYLE_BACKGROUND_COLOR = "settings_cc_style_bg_color";
	public static final String SETTINGS_CC_STYLE_EDGE_COLOR = "settings_cc_style_edge_color";
	public static final String SETTINGS_CC_STYLE_SIZE = "settings_cc_style_size";
	public static final String SETTINGS_CC_STYLE_FONT_EDGE = "settings_cc_style_font_edge";
	public static final String SETTINGS_CC_STYLE_FONT_OPACITY = "settings_cc_style_font_opacity";
	public static final String SETTINGS_CC_STYLE_BACKGROUND_OPACITY = "settings_cc_style_bg_opacity";

	public static final boolean DEFAULT_CC_VISIBILITY = true;

}
