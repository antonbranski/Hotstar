/*******************************************************************************
 * ADOBE CONFIDENTIAL
 *  ___________________
 *
 *  Copyright 2015 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe Systems Incorporated and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe Systems Incorporated and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe Systems Incorporated.
 ******************************************************************************/

package com.hotstar.player.adplayer.manager;

import android.content.Context;

public class VAManager implements IManager {
    /**
     * Empty call to create video analytics provider
     *
     * @param context
     */
    public void createVAProvider(Context context)
    {

    }

    /**
     * Empty call to destroy video analytics provider
     */
    public void destroyVAProvider()
    {

    }

    @Override
    public void destroy() {
    }

    public static final String SETTINGS_VA_ENABLED = "settings_va_enabled";
    public static final String SETTINGS_VA_TRACKING_SERVER = "settings_va_tracking_server";
    public static final String SETTINGS_VA_JOB_ID = "settings_va_job_id";
    public static final String SETTINGS_VA_PUBLISHER = "settings_va_publisher";
    public static final String SETTINGS_VA_CHANNEL = "settings_va_channel";
    public static final String SETTINGS_VA_DEBUG_LOGGING_ENABLED = "settings_va_debug_logging_enabled";
    public static final String SETTINGS_VA_QUITE_MODE = "settings_va_quiet_mode";

    // Default configuration values for the Adobe Video Analytics Heartbeat library.
    // Please consult with your Adobe Primetime enablement team member for the correct values for your account.
    public static final String DEFAULT_VA_TRACKING_SERVER = "http://metrics.adobeprimetime.com";
    public static final String DEFAULT_VA_CHANNEL = "";
    public static final String DEFAULT_VA_JOB_ID = "j2";
    public static final String DEFAULT_VA_PUBLISHER = "DEMOZ";
    public static final boolean DEFAULT_VA_DEBUG_LOGGING = false;
    public static final boolean DEFAULT_VA_QUITE_MODE = true;
    public static final boolean DEFAULT_VA_ENABLED = true;
}
