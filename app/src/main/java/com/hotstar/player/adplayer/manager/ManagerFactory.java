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

import com.adobe.mediacore.MediaPlayer;
import com.hotstar.player.adplayer.config.ConfigProvider;

/**
 * 
 * ManagerFactory is a static class to create PSDK feature managers from a
 * configuration object and PSDK media player. Each creation process returns a
 * manager with feature implementation. If the feature is not enabled through
 * the configuration, a default manager is used with default feature
 * functionalities. The playback manager is an exception because it is required
 * for media resource playback
 * 
 */
public class ManagerFactory {

	/**
	 * Create a Playback manager instance
	 * 
	 * @param configProvider
	 *            the configuration object
	 * @param mediaPlayer
	 *            the PSDK media player instance that handles the video playback
	 * @return a PlaybackManager instance. It is always enabled because playback
	 *         functionality is always required.
	 */
	public static PlaybackManager getPlaybackManager(ConfigProvider configProvider, MediaPlayer mediaPlayer) {
		return new PlaybackManager(configProvider, mediaPlayer);
	}

	/**
	 * Create an Ads manager instance
	 * 
	 * @param enable
	 *            whether the feature is enabled
	 * @param configProvider
	 *            the configuration object
	 * @param mediaPlayer
	 *            the PSDK media player instance that handles the video playback
	 * @return an AdsManagerOn if feature is enabled, otherwise an AdsManager
     */
	public static AdsManager getAdsManager(boolean enable, ConfigProvider configProvider, MediaPlayer mediaPlayer) {
		if (enable)
			return new AdsManagerOn(configProvider, mediaPlayer);
		else
			return new AdsManager();
	}

	/**
	 * Create a CC manager instance
	 * 
	 * @param enable
	 *            whether the feature is enabled
	 * @param configProvider
	 *            the configuration object
	 * @param mediaPlayer
	 *            the PSDK media player instance that handles the video playback
	 * @return a CCManagerOn if feature is enabled, otherwise a CCManger
	 */
	public static CCManager getCCManager(boolean enable, ConfigProvider configProvider, MediaPlayer mediaPlayer) {
		if (enable)
			return new CCManagerOn(configProvider, mediaPlayer);
		else
			return new CCManager();
	}

	/**
	 * Create a QOS manager instance
	 * 
	 * @param enable
	 *            whether the feature is enabled
	 * @param configProvider
	 *            the configuration object
	 * @param mediaPlayer
	 *            the PSDK media player instance that handles the video playback
	 * @return a QOSManagerOn if feature is enabled, otherwise a QOSManager
	 */
	public static QosManager getQosManager(boolean enable, ConfigProvider configProvider, MediaPlayer mediaPlayer) {
		if (enable)
			return new QosManagerOn(configProvider, mediaPlayer);
		else
			return new QosManager();
	}

	/**
	 * Create an AA manager instance
	 * 
	 * @param enable
	 *            whether the feature is enabled
	 * @param configProvider
	 *            the configuration object
	 * @param mediaPlayer
	 *            the PSDK media player instance that handles the video playback
	 * @return an AAManagerOn if feature is enabled, otherwise an AAManager
	 */
	public static AAManager getAAManager(boolean enable, ConfigProvider configProvider, MediaPlayer mediaPlayer) {
		if (enable)
			return new AAManagerOn(configProvider, mediaPlayer);
		else
			return new AAManager();
	}

	/**
	 * Create a DRM manager instance
	 * 
	 * @param configProvider
	 *            the configuration object
	 * @param mediaPlayer
	 *            the PSDK media player instance that handles the video playback
	 * 
	 * @return a DrmManager
	 */
	public static DrmManager getDrmManager(ConfigProvider configProvider, MediaPlayer mediaPlayer) {
		return new DrmManager(mediaPlayer);
	}

    /**
     * Create a VA manager instance
     *
     * @param enable whether the feature is enabled
     * @param configProvider the configuration object
     * @param mediaPlayer the PSDK media player instance that handles the video playback
     * @return a VAManagerOn if feature is enabled, otherwise a VAManager
     */
    public static VAManager getVAManager(boolean enable, ConfigProvider configProvider, MediaPlayer mediaPlayer)
    {
        if (enable)
            return new VAManagerOn(configProvider, mediaPlayer);
        else
            return new VAManager();
    }

    /**
     * Create an Entitlement manager instance. The Entitlement Manager manages the player's entitlement workflows which
     * interact with the Primetime PayTV Pass system. Subsequent calls will return the same manager instance.
     *
     * <p>
     * To enable the Entitlement workflows,
     * {@link com.hotstar.player.adplayer.manager.EntitlementManager#initializeAccessEnabler(android.app.Application)}
     * must be called first to initialize the AccessEnabler library. If the AccessEnabler library is not initialized,
     * this method will return a manager instance with the entitlement workflows disabled.
     * </p>
     *
     * @return if the AccessEnabler library is initialized, then a manager is returned
     * with the entitlement features enabled. Otherwise, a manager is returned with the entitlement features disabled.
     *
     * @see com.hotstar.player.adplayer.manager.EntitlementManager
     * @see com.hotstar.player.adplayer.manager.EntitlementManagerOn
     */
    public static EntitlementManager getEntitlementManager()
    {
        if (entitlementManager == null)
        {
            if (EntitlementManager.getAccessEnabler() != null)
            {
                // enable entitlement features
                entitlementManager = new EntitlementManagerOn();
            }
            else
            {
                // disable entitlement features
                entitlementManager = new EntitlementManager();
            }
        }

        return entitlementManager;
    }

    private static EntitlementManager entitlementManager;
}
