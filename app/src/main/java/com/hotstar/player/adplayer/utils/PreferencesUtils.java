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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hotstar.player.adplayer.AdVideoApplication;

/**
 * Utility class for extracting data from shared preferences
 */
public class PreferencesUtils {

	private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME
			+ PreferencesUtils.class.getSimpleName();

	/**
	 * Determine if the player should show warning in toast from shared
	 * preferences
	 * 
	 * @param context
	 *            the context this application is associated with
	 * @return true if show warning in toast, false otherwise
	 */
	public static boolean getShowToastWarningsPref(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean(AdVideoApplication.SETTINGS_TOAST_WARNINGS,
				AdVideoApplication.DEFAULT_TOAST_WARNINGS);
	}

	/**
	 * Determine if the application should return to the catalog screen after
	 * playback completes
	 * 
	 * @param context
	 *            the context this application is asociated with
	 * @return true if the app should return to the catalog screen, false
	 *         otherwise
	 */
	public static boolean shouldReturnToCatalogOnPlaybackComplete(
			Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean(
				AdVideoApplication.SETTINGS_RETURN_HOME_ON_PLAY_COMPLETE,
				AdVideoApplication.DEFAULT_RETURN_HOME_ON_PLAY_COMPLETE);
	}

	/**
	 * Get shared preference value in int type
	 * 
	 * @param pref
	 *            the shared preference object associated with the current
	 *            context
	 * @param key
	 *            the name the preference key
	 * @param defaultValue
	 *            the default value if no value has been assigned to the
	 *            preference key
	 * @return preference value assigned by the key in int type, if it has not
	 *         been set, return default value
	 */
	public static int getIntPreference(SharedPreferences pref, String key,
			int defaultValue) {
		String value = pref.getString(key, String.valueOf(defaultValue));
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			AdVideoApplication.logger.w(LOG_TAG + "#getIntPreference", "Unable to parse ["
					+ value + "] to int value. Returning default value: "
					+ defaultValue + ".");
			return defaultValue;
		}
	}

	/**
	 * Get shared preference value in loong type
	 * 
	 * @param pref
	 *            the shared preference object associated with the current
	 *            context
	 * @param key
	 *            the name the preference key
	 * @param defaultValue
	 *            the default value if no value has been assigned to the
	 *            preference key
	 * @return preference value assigned by the key in long type, if it has not
	 *         been set, return default value
	 */
	public static long getLongPreference(SharedPreferences pref, String key,
			long defaultValue) {
		String value = pref.getString(key, String.valueOf(defaultValue));
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			AdVideoApplication.logger.w(LOG_TAG + "#getLongPreference", "Unable to parse ["
					+ value + "] to long value. Returning default value: "
					+ defaultValue + ".");
			return defaultValue;
		}
	}
}
