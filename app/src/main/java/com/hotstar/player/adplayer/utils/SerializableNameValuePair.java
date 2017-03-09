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

package com.hotstar.player.adplayer.utils;

import java.io.Serializable;

/**
 * Utility class for a name value pair object
 */
public class SerializableNameValuePair implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String value;

	public SerializableNameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Get the name of the name value pair
	 * 
	 * @return the name of the name value pair
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the name value pair
	 * 
	 * @param name
	 *            the name to be set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the value of the name value pair
	 * 
	 * @return the value of the name value pair
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the value of the name value pair
	 * 
	 * @param value
	 *            the value to be set
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
