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

package com.hotstar.player.adplayer.drm;

import android.content.res.Resources;

/**
 * Class to manage the translation of DRM error codes to error message strings.
 * The strings are loaded from Android resources.
 */
public class DRMStringsRes implements DRMStrings {

	private String mPackageName;
	private Resources mResources;

	public DRMStringsRes(String packageName, Resources resources) {

		mPackageName = packageName;
		mResources = resources;
	}

	@Override
	public String buildMessage(long majorCode, long minorCode) {
		StringBuilder message = new StringBuilder();
		message.append(majorCode);
		message.append(" ");
		if (minorCode != 0) {
			message.append(minorCode);
			message.append(" ");
		}
		message.append(getMessageFromResource(majorCode));
		return message.toString();
	}

	private String getDefaultMessage() {
		int id = mResources.getIdentifier("drm_message_default", "string",
				mPackageName);
		if (id == 0) {
			return "";
		}
		return mResources.getString(id);
	}

	private String getMessageFromResource(long majorCode) {
		int id = mResources.getIdentifier("drm_message_" + majorCode, "string",
				mPackageName);

		if (id == 0) {
			return getDefaultMessage();
		}

		String result = mResources.getString(id);
		if (result.length() == 0) {
			return getDefaultMessage();
		}
		return result;
	}

}
