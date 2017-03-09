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

package com.hotstar.player.adplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import com.adobe.mediacore.Version;
import com.adobe.mediacore.logging.Log;
import com.adobe.mediacore.logging.LogFactory;
import com.adobe.mediacore.logging.Logger;
import com.adobe.mediacore.qos.DeviceInformation;
import com.adobe.mediacore.qos.QOSProvider;
import com.adobe.mediacore.utils.StringUtils;
import com.hotstar.player.adplayer.logging.InMemoryLogger;
import com.hotstar.player.adplayer.manager.DrmManager;
import com.hotstar.player.adplayer.manager.EntitlementManager;
import com.hotstar.player.R;

public class AdVideoApplication extends Application {
	private static final String LOG_TAG = "[Base]";

	public final static boolean DEFAULT_RETURN_HOME_ON_PLAY_COMPLETE = false,
			DEFAULT_AUTOHIDE_CONTROL_BAR = true,
			DEFAULT_REMOTE_LOGGING_ACTIVE = false,
			DEFAULT_TOAST_WARNINGS = false;
	public static final int DEFAULT_MAX_ENTRY_COUNT = 1000;
	// public static final boolean DEFAULT_DVR_START_TIME_ENABLED = false;
	public static final boolean DEFAULT_DVR_SEEKING_OUTSIDE_ENABLED = false;
	// public static final int CUSTOM_DVR_START_TIME = 30000;
	public static final int DEFAULT_DVR_SEEKING_OFFSET = 0;

	public static final String SETTINGS_CONTENT_URL_KEY = "settings_content_url";
	public static final String SETTINGS_LOG_MAX_ENTRY_COUNT_KEY = "settings_log_items_count";
	public static final String SETTINGS_REMOTE_LOGGING_ACTIVE = "settings_remote_logging_active";
	public static final String SETTINGS_REMOTE_LOGGING_HOST = "settings_remote_logging_host";
	public static final String SETTINGS_REMOTE_LOGGING_PORT = "settings_remote_logging_port";
	public static final String SETTINGS_REMOTE_LOGGING_SESSION_TAG = "settings_remote_logging_session_tag";
	public static final String SETTINGS_LOG_VERBOSITY = "settings_log_verbosity";
	public static final String SETTINGS_CONTROLBAR_AUTOHIDE = "settings_controlbar_autohide";
	public static final String SETTINGS_RETURN_HOME_ON_PLAY_COMPLETE = "settings_return_home_on_play_complete";
	public static final String SETTINGS_TOAST_WARNINGS = "settings_toast_warnings";
	public static final String SETTINGS_PUB_OVERLAY_POSITION = "settings_pub_overlay_position";
	public static final String SETTINGS_DVR_SEEKING_OUTSIDE_ENABLED = "settings_dvr_seek_outside_enabled";
	public static final String SETTINGS_DVR_SEEKING_OFFSET = "settings_dvr_seek_offset";
	public static final String SETTINGS_DVR_SEEKING_CLP_ENABLED = "settings_dvr_seek_clp_enabled";
	public static final String SETTINGS_DRM_TESTING_ENABLE_TEST_TOOLS = "preference_drm_testing_enable_test_tools";
	public static final String SETTINGS_DRM_TESTING_SUPPRESS_AUTOMATIC_ACQUISITIONS = "preference_drm_testing_suppress_automatic_acquisitions";
	public static final String SETTINGS_DRM_TESTING_SUPPRESS_AUTOMATIC_PLAY = "preference_drm_testing_suppress_automatic_play";
	public static final String SETTINGS_VCMS_TYPE = "REFERENCE";

	public static final String LOG_APP_NAME = "[AdVideoApplication]::";

	public static Logger logger = new InMemoryLogger();

/*
	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		int logCapacity;
		try {
			logCapacity = Integer.parseInt(sharedPreferences.getString(
					AdVideoApplication.SETTINGS_LOG_MAX_ENTRY_COUNT_KEY, "1000"));
		} catch (NumberFormatException e) {
			logger.e(LOG_TAG + "::onCreate()",
					"LOG_MAX_ENTRY_COUNT in settings is not Integer.");
			logCapacity = 1000;
		}
		logger.setCapacity(logCapacity);
		logger.setVerbosityLevel(LogActivity.getVerbosity(this));

		// change the log factory to use the application logger
		// as logger for all classes.
		Log.setLogFactory(new LogFactory() {
			@Override
			public Logger getLogger(String logTag) {
				return logger;
			}
		});

		DrmManager.loadDRMServices(getApplicationContext());

        // initialize the AccessEnabler library, required for Primetime PayTV Pass entitlement workflows
        EntitlementManager.initializeAccessEnabler(this);  // comment out this line to disable entitlement workflows
	}

	public static void showAbout(Activity parent) {
		AdVideoApplication.logger.i(LOG_TAG + "#showAbout", "Displaying about information.");

		final AlertDialog.Builder ab = new AlertDialog.Builder(parent);
		ab.setTitle(R.string.aboutTitle);
		View view = parent.getLayoutInflater().inflate(R.layout.about, null);

		// PSDK version
		TextView tv = (TextView) view.findViewById(R.id.aboutPsdkVersion);
		tv.setText(parent.getString(R.string.aboutPsdkVersion) + " " + Version.getVersion());

		// Android app version
		tv = (TextView) view.findViewById(R.id.aboutAppVersion);
		tv.setText(parent.getString(R.string.aboutApp_version_label) + " " + parent.getString(R.string.aboutApp_version));

		// PSDK description
		tv = (TextView) view.findViewById(R.id.aboutPsdkDescription);
		tv.setText(parent.getString(R.string.aboutPsdkDescription) + " "
				+ Version.getDescription());

		// AVE version
		tv = (TextView) view.findViewById(R.id.aboutAveVersion);
		tv.setText(parent.getString(R.string.aboutAveVersion) + " " + Version.getAVEVersion());

        // AccessEnabler version
        tv = (TextView) view.findViewById(R.id.aboutAccessEnablerVersion);
        if (tv != null)
        {
            String entitlementVersion = EntitlementManager.getVersion();
            if (!StringUtils.isEmpty(entitlementVersion))
            {
                tv.setText(parent.getString(R.string.aboutAccessEnablerVersion) + " " + entitlementVersion);
            }
            else
            {
                // if AccessEnabler library is not loaded, do not show version number
                tv.setVisibility(View.GONE);
            }
        }

		// Show device information
		DeviceInformation deviceInfo = new QOSProvider(
				parent.getApplicationContext()).getDeviceInformation();
		tv = (TextView) view.findViewById(R.id.aboutDeviceModel);
		tv.setText(parent.getString(R.string.aboutDeviceModel) + " "
				+ deviceInfo.getManufacturer() + " - " + deviceInfo.getModel());
		
		tv = (TextView) view.findViewById(R.id.aboutDeviceBrand);
		tv.setText(parent.getString(R.string.aboutDeviceBrand) + " " + Build.BRAND);	
		
		tv = (TextView) view.findViewById(R.id.aboutDeviceSoftware);
		tv.setText(parent.getString(R.string.aboutDeviceSoftware) + " "
				+ deviceInfo.getOS() + ", SDK: " + deviceInfo.getSDK() + ", Build: " + Build.ID);
		
		tv = (TextView) view.findViewById(R.id.aboutDeviceProduct);
		tv.setText(parent.getString(R.string.aboutDeviceProduct) + " " + Build.PRODUCT);
		
		tv = (TextView) view.findViewById(R.id.aboutDeviceFingerprint);
		tv.setText(parent.getString(R.string.aboutDeviceFingerprint) + " " + Build.FINGERPRINT);		

		tv = (TextView) view.findViewById(R.id.aboutDeviceProcessor);
		tv.setText(parent.getString(R.string.aboutDeviceProcessor) + " " + System.getProperty("os.arch", "Unknown"));		
		
		tv = (TextView) view.findViewById(R.id.aboutDeviceResolutin);
		String orientation = parent.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape"
				: "portrait";
		tv.setText(parent.getString(R.string.aboutDeviceResolution) + " "
				+ deviceInfo.getWidthPixels() + "x"
				+ deviceInfo.getHeightPixels() + " (" + orientation + ")");

		ab.setView(view);
		ab.setNegativeButton(R.string.close,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Just close the dialog.
					}
				});
		ab.show();
	}
	*/
}
