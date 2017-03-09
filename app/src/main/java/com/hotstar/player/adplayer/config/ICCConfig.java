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

package com.hotstar.player.adplayer.config;

import com.adobe.mediacore.TextFormat;

public interface ICCConfig {

	/**
	 * Get the closed captioning visibility config
	 * 
	 * @return true if visibility is set to true, false otherwise
	 */
	public boolean getCCVisibility();

	/**
	 * Get the closed captioning font style
	 * 
	 * @return TextFormat.Font object represents font style
	 */
	public TextFormat.Font getCCFont();

	/**
	 * Get the closed captioning font edge
	 * 
	 * @return TextFormat.FontEdge represents of font edge
	 */
	public TextFormat.FontEdge getCCFontEdge();

	/**
	 * Get the closed captioning font color
	 * 
	 * @return TextFormat.Color object represents the font color
	 */
	public TextFormat.Color getCCFontColor();

	/**
	 * Get the closed captioning font opacity
	 * 
	 * @return int value of the font opacity
	 */
	public int getCCFontOpacity();

	/**
	 * Get the closed captioning background color
	 * 
	 * @return TextFormat.Color object represents the background color
	 */
	public TextFormat.Color getCCBackgroundColor();

	/**
	 * Get the closed captioning edge color
	 * 
	 * @return TextFormat.Color object represents the edge color
	 */
	public TextFormat.Color getCCEdgeColor();

	/**
	 * Get the closed captioning background opacity
	 * 
	 * @return int value of the background opacity
	 */
	public int getCCBackgroundOpacity();

	/**
	 * Get the closed captioning font size
	 * 
	 * @return TextFormat.Size object represents the size
	 */
	public TextFormat.Size getCCSize();

}
