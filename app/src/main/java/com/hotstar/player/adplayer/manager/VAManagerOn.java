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
import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.Version;
import com.adobe.mediacore.videoanalytics.VideoAnalyticsPSDKPlayerPlugin;
import com.adobe.mobile.Config;
import com.adobe.primetime.core.plugin.IPlugin;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.config.IVAConfig;
import com.adobe.primetime.va.ConfigData;
import com.adobe.primetime.va.VideoHeartbeat;
import com.adobe.primetime.va.plugins.aa.AdobeAnalyticsPlugin;

import java.util.ArrayList;
import java.util.List;

public class VAManagerOn extends VAManager {
    private IVAConfig vaConfig;
    private MediaPlayer mediaPlayer;
    private VideoHeartbeat heartbeat;

    private final String LOG_TAG = com.hotstar.player.adplayer.AdVideoApplication.LOG_APP_NAME
            + VAManagerOn.class.getSimpleName();

    public VAManagerOn(IVAConfig vaConfig, MediaPlayer mediaPlayer) {
        this.vaConfig = vaConfig;
        this.mediaPlayer = mediaPlayer;
    }

    /**
     *
     * Create Video Analytics provider to track video statistics and send to the analytics server by attaching it to
     * the media player
     *
     * @param context the application context where the player is created from
     */
    public void createVAProvider(Context context)
    {
        if (heartbeat != null) {
            destroyVAProvider();
        }

        // set application context in Adobe Mobile Library
        Config.setContext(context);

        List<IPlugin> plugins = new ArrayList<IPlugin>();
        plugins.add(new AdobeAnalyticsPlugin());
        plugins.add(new VideoAnalyticsPSDKPlayerPlugin(context, mediaPlayer, null));

        heartbeat = new VideoHeartbeat(null, plugins);

        ConfigData config = new ConfigData(vaConfig.getVATrackingServer(), vaConfig.getVAJobId(), vaConfig.getVAPublisher());
        config.channel = vaConfig.getVAChannel();
        config.debugLogging = vaConfig.isVADebugLoggingEnabled();
        config.quietMode = vaConfig.isVAQuietModeEnabled();
        config.__primetime = true;
        config.__psdkVersion = Version.getVersion();

        heartbeat.configure(config);

        AdVideoApplication.logger.i(LOG_TAG, "Creating Video Heartbeat:"
                + " trackingServer=" + config.getTrackingServer()
                + " jobId=" + config.getJobId()
                + " publisher=" + config.getPublisher()
                + " channel=" + config.channel
                + " debugLogging=" + (config.debugLogging ? "true" : "false")
                + " quiteMode=" + (config.quietMode ? "true" : "false")
        );
    }

    /**
     *
     * Destroy VA provider by detaching it from the media player
     */
    public void destroyVAProvider()
    {
        if (heartbeat != null) {
            heartbeat.destroy();
            heartbeat = null;
        }
    }
}
