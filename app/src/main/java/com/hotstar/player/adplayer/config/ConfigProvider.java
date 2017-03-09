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

import android.content.SharedPreferences;
import com.adobe.mediacore.ABRControlParameters;
import com.adobe.mediacore.ABRControlParameters.ABRPolicy;
import com.adobe.mediacore.TextFormat;
import com.adobe.mediacore.TextFormat.Color;
import com.adobe.mediacore.TextFormat.FontEdge;
import com.adobe.mediacore.TextFormat.Size;
import com.adobe.mediacore.metadata.AdSignalingMode;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.utils.StringUtils;
import com.hotstar.player.adplayer.manager.AdsManager;
import com.hotstar.player.adplayer.manager.CCManager;
import com.hotstar.player.adplayer.manager.PlaybackManager;
import com.hotstar.player.adplayer.manager.QosManager;
import com.hotstar.player.adplayer.manager.VAManager;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.utils.PreferencesUtils;

import java.util.Locale;

/**
 * This is the class to get configuration for the media player. Customers must
 * implement all the config interface so the feature managers can read
 * configurations.
 */
public class ConfigProvider implements ICCConfig, IAAConfig, IPlaybackConfig, IAdConfig, IQosConfig, IVAConfig {

	private SharedPreferences preferences;
	private VideoItem video;

	public ConfigProvider(SharedPreferences preferences, VideoItem video) {
		this.preferences = preferences;
		this.video = video;
	}

	// CC Config
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getCCVisibility() {
		return preferences.getBoolean(CCManager.SETTINGS_CC_VISIBILITY, CCManager.DEFAULT_CC_VISIBILITY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TextFormat.Font getCCFont() {
		String value = preferences.getString(CCManager.SETTINGS_CC_STYLE_FONT, TextFormat.Font.DEFAULT.getValue());
		TextFormat.Font font = TextFormat.Font.valueOf(value.toUpperCase(Locale.getDefault()));
		return font;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FontEdge getCCFontEdge() {
		String value = preferences.getString(CCManager.SETTINGS_CC_STYLE_FONT_EDGE, TextFormat.FontEdge.DEFAULT.getValue());
		TextFormat.FontEdge fontEdge = TextFormat.FontEdge.valueOf(value.toUpperCase(Locale.getDefault()));
		return fontEdge;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Color getCCFontColor() {
		String value = preferences.getString(CCManager.SETTINGS_CC_STYLE_FONT_COLOR, TextFormat.Color.DEFAULT.getValue());
		TextFormat.Color fontColor = TextFormat.Color.valueOf(value.toUpperCase(Locale.getDefault()));
		return fontColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCCFontOpacity() {
		String value = preferences.getString(CCManager.SETTINGS_CC_STYLE_FONT_OPACITY, String.valueOf(TextFormat.DEFAULT_OPACITY));
		int fontOpacity = Integer.parseInt(value);
		return fontOpacity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Color getCCBackgroundColor() {
		String value = preferences.getString(CCManager.SETTINGS_CC_STYLE_BACKGROUND_COLOR, TextFormat.Color.DEFAULT.getValue());
		TextFormat.Color bgColor = TextFormat.Color.valueOf(value.toUpperCase(Locale.getDefault()));
		return bgColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Color getCCEdgeColor() {
		String value = preferences.getString(CCManager.SETTINGS_CC_STYLE_EDGE_COLOR, TextFormat.Color.DEFAULT.getValue());
		TextFormat.Color edgeColor = TextFormat.Color.valueOf(value.toUpperCase(Locale.getDefault()));
		return edgeColor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCCBackgroundOpacity() {
		String value = preferences.getString(CCManager.SETTINGS_CC_STYLE_BACKGROUND_OPACITY, String.valueOf(TextFormat.DEFAULT_OPACITY));
		int bgOpacity = Integer.parseInt(value);
		return bgOpacity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Size getCCSize() {
		String value = preferences.getString(CCManager.SETTINGS_CC_STYLE_SIZE, TextFormat.Size.DEFAULT.getValue());
		TextFormat.Size size = TextFormat.Size.valueOf(value.toUpperCase(Locale.getDefault()));
		return size;
	}
	
	// Playback Config
	@Override
	public boolean forceOpenMAXAL() {		
		return preferences.getBoolean(PlaybackManager.SETTINGS_FORCE_OMXAL, PlaybackManager.DEFAULT_FORCE_OMXAL);
	}

	// Playback ABR Config

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isABRControlEnabled() {
		return preferences.getBoolean(PlaybackManager.SETTINGS_ABR_CTRL_ENABLED, PlaybackManager.DEFAULT_ABR_CTRL_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getABRInitialBitRate() {
		return PreferencesUtils.getIntPreference(preferences, PlaybackManager.SETTINGS_ABR_INITIAL_BITRATE, PlaybackManager.DEFAULT_INIT_BIT_RATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getABRMinBitRate() {
		return PreferencesUtils.getIntPreference(preferences, PlaybackManager.SETTINGS_ABR_MIN_BITRATE, PlaybackManager.DEFAULT_MIN_BIT_RATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getABRMaxBitRate() {
		return PreferencesUtils.getIntPreference(preferences, PlaybackManager.SETTINGS_ABR_MAX_BITRATE, PlaybackManager.DEFAULT_MAX_BIT_RATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ABRPolicy getABRPolicy() {
		int mbrPolicyAsInt = PreferencesUtils.getIntPreference(preferences, PlaybackManager.SETTINGS_ABR_POLICY, 0);
		ABRControlParameters.ABRPolicy abrPolicy = policyFromInt(mbrPolicyAsInt);
		abrPolicy = abrPolicy != null ? abrPolicy : ABRControlParameters.ABRPolicy.ABR_MODERATE;

		return abrPolicy;
	}

	/**
	 * Convert integer representation for the policy to ABRPolicy object
	 * 
	 * @param policy
	 *            the integer representation of policy
	 * @return ABRPolicy object CONSERVATIVE if policy = 0, MODERATE if policy =
	 *         1, AGGRESSIVE if policy = 2
	 */
	private ABRControlParameters.ABRPolicy policyFromInt(int policy) {
		switch (policy)
		{
			case 0:
				return ABRControlParameters.ABRPolicy.ABR_CONSERVATIVE;
			case 1:
				return ABRControlParameters.ABRPolicy.ABR_MODERATE;
			case 2:
				return ABRControlParameters.ABRPolicy.ABR_AGGRESSIVE;
			default:
				return null;
		}
	}

	// AD Config
	/**
	 * {inheritDoc}
	 */
	@Override
	public boolean isAdvertisingWorkflowEnabled() {
		return preferences.getBoolean(AdsManager.SETTINGS_ADVERTISING_WORKFLOW_ENABLED, AdsManager.DEFAULT_ADVERTISING_WORKFLOW_ENABLED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean shouldUseCustomAdFactory() {
		//
		// Modified by Xiaoming
		//
		return true;
		// return preferences.getBoolean(AdsManager.SETTINGS_CUSTOM_AD_CLIENT_FACTORY, AdsManager.DEFAULT_USE_CUSTOM_AD_CLIENT_FACTORY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isClickableAdsEnabled() {
		return preferences.getBoolean(AdsManager.SETTINGS_CLICKABLE_ADS, AdsManager.DEFAULT_CLICKABLE_ADS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Metadata getMetadata() {
		return video.getAdvertisingMetadata();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getAdTags() {
		String customAdCues = preferences.getString(AdsManager.SETTINGS_CUSTOM_AD_CUES, "");
		if (!StringUtils.isEmpty(customAdCues)) {
			return customAdCues.split(",");
		}
		else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public AdSignalingMode getDefaultAdSignalingMode() {
		return AdSignalingMode.createFrom(preferences.getString(
				AdsManager.SETTINGS_ADVERTISING_SIGNALING_MODE,
				AdsManager.DEFAULT_ADVERTISING_SIGNALING_MODE));
	}

	// QOS Config

	/**
	 * {inheritDoc}
	 */
	@Override
	public boolean isQosVisible() {
		return preferences.getBoolean(QosManager.SETTINGS_QOS_VISIBILITY, QosManager.DEFAULT_QOS_VISIBILITY);
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public long getInitBufferTime() {
		long initBufferTime = PreferencesUtils.getLongPreference(preferences,
				PlaybackManager.SETTINGS_BUFFER_INIT,
				PlaybackManager.DEFAULT_INIT_BUFFER);
		return initBufferTime;
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public long getBufferTime() {
		long playBufferTime = PreferencesUtils.getLongPreference(preferences,
				PlaybackManager.SETTINGS_BUFFER_TIME,
				PlaybackManager.DEFAULT_BUFFER_TIME);
		return playBufferTime;
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public boolean isCustomPositionPrefEnabled() {
		return preferences.getBoolean(
				PlaybackManager.SETTINGS_DVR_START_TIME_ENABLED,
				PlaybackManager.DEFAULT_DVR_START_TIME_ENABLED);
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public long retrieveStartTimePref() {
		long customDVRStartTime = PreferencesUtils.getLongPreference(
				preferences, PlaybackManager.SETTINGS_DVR_START_TIME,
				PlaybackManager.CUSTOM_DVR_START_TIME);
		return customDVRStartTime;
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public boolean isSeekingAfterTimeJumpPrefEnabled() {
		return preferences.getBoolean(
				PlaybackManager.SETTINGS_DVR_SEEKING_OUTSIDE_ENABLED,
				PlaybackManager.DEFAULT_DVR_SEEKING_OUTSIDE_ENABLED);
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public boolean isSeekingAtClientLivePointPrefEnabled() {
		return preferences.getBoolean(
				PlaybackManager.SETTINGS_DVR_SEEKING_CLP_ENABLED,
				PlaybackManager.DEFAULT_DVR_SEEKING_CLP_ENABLED);
	}

	/**
	 * {inheritDoc}
	 */
	@Override
	public long retrieveSeekingOffsetPref() {
		long seekingOffset = PreferencesUtils.getLongPreference(preferences,
				PlaybackManager.SETTINGS_DVR_SEEKING_OFFSET,
				PlaybackManager.CUSTOM_DVR_SEEKING_OFFSET);
		return seekingOffset;
	}

    // VA config

    /**
     * {inheritDoc}
     */
    @Override
    public boolean isVAEnabled() {
        return preferences.getBoolean(VAManager.SETTINGS_VA_ENABLED, VAManager.DEFAULT_VA_ENABLED);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public boolean isVADebugLoggingEnabled() {
        return preferences.getBoolean(VAManager.SETTINGS_VA_DEBUG_LOGGING_ENABLED, VAManager.DEFAULT_VA_DEBUG_LOGGING);
    }

    @Override
    public boolean isVAQuietModeEnabled() {
        return preferences.getBoolean(VAManager.SETTINGS_VA_QUITE_MODE, VAManager.DEFAULT_VA_QUITE_MODE);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String getVATrackingServer() {
        return preferences.getString(VAManager.SETTINGS_VA_TRACKING_SERVER, VAManager.DEFAULT_VA_TRACKING_SERVER);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String getVAJobId() {
        return preferences.getString(VAManager.SETTINGS_VA_JOB_ID, VAManager.DEFAULT_VA_JOB_ID);
    }

    /**
     * {inheritDoc}
     */
    @Override
    public String getVAPublisher() {
        return preferences.getString(VAManager.SETTINGS_VA_PUBLISHER, VAManager.DEFAULT_VA_PUBLISHER);
    }

    @Override
    public String getVAChannel() {
        return preferences.getString(VAManager.SETTINGS_VA_CHANNEL, VAManager.DEFAULT_VA_CHANNEL);
    }


}
