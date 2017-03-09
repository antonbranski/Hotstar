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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import com.adobe.ave.drm.DRMAcquireLicenseSettings;
import com.adobe.ave.drm.DRMLicense;
import com.adobe.ave.drm.DRMLicenseAcquiredCallback;
import com.adobe.ave.drm.DRMManager;
import com.adobe.ave.drm.DRMMetadata;
import com.adobe.ave.drm.DRMOperationCompleteCallback;
import com.adobe.ave.drm.DRMOperationErrorCallback;
import com.adobe.mediacore.DRMMetadataInfo;
import com.adobe.mediacore.DRMService;
import com.adobe.mediacore.DefaultDRMService;
import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.MediaPlayerItemLoader;
import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.MediaPlayer.PlayerState;
import com.adobe.mediacore.MediaPlayerItemLoader.LoaderListener;
import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.drm.DRMStringsRes;

/**
 * Listens for events from the media player: Event.DRM and Event.PLAYBACK, and
 * filters for events that indicate a DRM error (native error codes 3300 to
 * 3399). These are processed in three ways depending on the error: re-acquire
 * the license from the license server, clear the local license cache, notify
 * the user that an error has occurred. The caller is notified by
 * DRMErrorHandlerListener callback if the error has been resolved and play can
 * be retried, or if an error has occurred which prevents play.
 * 
 */
public class DrmManager implements IManager {

	public final static boolean DEFAULT_DRM_PRECACHE = false;
	public final static String SETTINGS_DRM_PRE_CACHE = "settings_drm_precache";

	private final static String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "DRMManager";

	private MediaPlayer mediaPlayer;
	private DRMManager mDrmManager;
	private DRMMetadata mDrmMetadata;
	private boolean hasRetried = false;

	private List<DrmManagerEventListener> eventListeners = new ArrayList<DrmManagerEventListener>();

	// internal tracking of the last drm message we got from DRMManager
	private String retryError = "";

	public DrmManager(MediaPlayer mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
		this.mDrmManager = mediaPlayer.getDRMManager();
		this.mediaPlayer.addEventListener(MediaPlayer.Event.DRM, drmEventListener);
		this.mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
	}

	/**
	 * Destroy manager by removing PSDK listeners
	 */
	@Override
	public void destroy() {
		mediaPlayer.removeEventListener(MediaPlayer.Event.DRM, drmEventListener);
		mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, playbackEventListener);
	}

	public void addEventListener(DrmManagerEventListener eventListener) {
		eventListeners.add(eventListener);
	}

	public void removeEventListener(DrmManagerEventListener eventListener) {
		eventListeners.remove(eventListener);
	}

	public interface DrmManagerEventListener {
		/**
		 * Notify the user that a DRM error has occurred.
		 * 
		 * @param majorCode
		 *            the major error code of the DRM error
		 * @param minorCode
		 *            the minor error code of the DRM error
		 * @param error
		 *            a custom error message configured on the DRM service for
		 *            this particualar error
		 * 
		 */
		public void onError(long majorCode, long minorCode, String error);

		/**
		 * The player should try to re-load the media following automatic
		 * resolution of the DRM error.
		 */
		public void onRetry();

	}

	/**
	 * Perform operations necessary to start up the DRM service. This
	 * precomputes anything that can be precomputed so that future playback will
	 * have the shortest delay possible.
	 * 
	 * @param context
	 *            the calling application context.
	 */
	public static void loadDRMServices(Context context)
	{
		DRMService drmService = new DefaultDRMService(context);
		drmService.setDRMEventListener(new DRMService.DRMEventListener() {
			@Override
			public void onInitialized() {
				AdVideoApplication.logger.w(LOG_TAG + "#onDRMinitialized", "DRM services initialized.");
			}

			@Override
			public void onError(long majorCode, long minorCode, Exception exception) {
				AdVideoApplication.logger.w(LOG_TAG + "#onDRMError",
						"DRM services failed to initialized." + "[majorCode="
								+ String.valueOf(majorCode) + ", minorCode="
								+ String.valueOf(minorCode) + ", exception="
								+ String.valueOf(exception) + "].");
			}
		});
		AdVideoApplication.logger.i(LOG_TAG + "#onCreate", "DRM layer initializing.");
		drmService.initialize();
	}

	/**
	 * Pre-load the DRM licenses. If the licenses are not already cached
	 * locally, they will be retrieved from the license server identified by the
	 * DRMMetadata associated with the resource. This function is provided to
	 * enable early completion of the license workflow, which can result in
	 * faster playback start. Only metadata is loaded, not the actual media.
	 * 
	 * @param url
	 *            the manifest URL
	 * @param listener
	 *            An instance of MediaPlayerItemLoader.LoaderListener to receive
	 *            the completion notifications.
	 * @param context
	 *            the application context where the preload occurs from
	 */
	public static void preLoadDrmLicenses(String url, final LoaderListener listener, final Context context) {

		final MediaResource resource = MediaResource.createFromUrl(url, null);

		new Handler().post(new Runnable() {
			@Override
			public void run() {
				MediaPlayerItemLoader itemLoader = new MediaPlayerItemLoader(
						context, listener);
				itemLoader.load(resource);
			}
		});
	}

	/**
	 * @param majorCode
	 *            is the NATIVE_ERROR_CODE from the MediaPlayerNotification
	 * @return true if the majorCode is a DRM error.
	 */
	public static boolean isDrmError(long majorCode) {
		return majorCode >= 3300 && majorCode <= 3400;
	}

	private final MediaPlayer.DRMEventListener drmEventListener = new MediaPlayer.DRMEventListener() {

		@Override
		public void onDRMMetadata(final DRMMetadataInfo drmMetadataInfo) {
			AdVideoApplication.logger.i(LOG_TAG + "::onDRMMetadata",
					"DRM metadata available: " + drmMetadataInfo + ".");

			mDrmMetadata = drmMetadataInfo.getDRMMetadata();
		}
	};

	private final MediaPlayer.PlaybackEventListener playbackEventListener = new MediaPlayer.PlaybackEventListener() {

		@Override
		public void onStateChanged(PlayerState state,
				MediaPlayerNotification notification) {

			if (state == PlayerState.ERROR) {
				long major = Long.valueOf(notification.getMetadata().getValue(
						"NATIVE_ERROR_CODE"));
				long minor = 0;

				if (isDrmError(major)) {

					handleDRMError(major, minor);
				}
			}
		}

		@Override
		public void onPlayComplete() {
		}

		@Override
		public void onPlayStart() {
		}

		@Override
		public void onPrepared() {
			if (mediaPlayer.getCurrentItem().isProtected()) {
				AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onPrepared()",
						"Stream is protected.");
			} else {
				AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlaybackEventListener#onPrepared()",
						"Stream is NOT protected.");
			}
			showDrmMetadataInfos();
		}

		@Override
		public void onSizeAvailable(long arg0, long arg1) {
		}

		@Override
		public void onTimedMetadata(TimedMetadata arg0) {
		}

		@Override
		public void onTimelineUpdated() {
		}

		@Override
		public void onUpdated() {
			showDrmMetadataInfos();
		}

		@Override
		public void onRatePlaying(float rate) {

		}

		@Override
		public void onRateSelected(float rate) {

		}

		@Override
		public void onReplaceMediaPlayerItem() {
	
		}

		@Override
		public void onProfileChanged(long profile, long time) {
			
		}

	};

	/**
	 * Handle DRM error base on the major code
	 * 
	 * @param majorCode
	 *            the major code of the DRM error
	 * @param minorCode
	 *            the minor code of the DRM error
	 */
	private void handleDRMError(long majorCode, long minorCode) {

		AdVideoApplication.logger.d(LOG_TAG, "DRM error, majorCode " + majorCode + " minorCode " + minorCode + " retried " + hasRetried);

		if (!hasRetried) {

			hasRetried = true;

			if (requiresRetry(majorCode, minorCode)) {
				forceAcquireLicense();
				return;
			}
			else if (requiresResetVouchers(majorCode, minorCode)) {
				resetLocalDRMCache();
				return;
			}
		}

		sendDrmError(majorCode, minorCode, retryError);
	}

	/**
	 * Show DRM metadata information in the log
	 */
	private void showDrmMetadataInfos() {
		if (mediaPlayer == null || mediaPlayer.getCurrentItem() == null
				|| mediaPlayer.getCurrentItem().getDRMMetadataInfos() == null) {
			return;
		}
		List<DRMMetadataInfo> drmMetadataInfos = mediaPlayer.getCurrentItem()
				.getDRMMetadataInfos();
		for (DRMMetadataInfo drmMetadataInfo : drmMetadataInfos) {
			AdVideoApplication.logger.i(LOG_TAG + "#showDrmMetadata",
					drmMetadataInfo.toString());
		}
	}

	/**
	 * Reset local DRM cache and notify the listeners to retry playback
	 */
	private void resetLocalDRMCache() {
		AdVideoApplication.logger.d(LOG_TAG, "Clearing all locally cached DRM licenses.");

		mDrmManager.resetDRM(new DRMOperationErrorCallback() {
			public void OperationError(long majorCode, long minorCode,
					Exception e) {
				AdVideoApplication.logger.d(LOG_TAG, "License cache reset failed, error: "
						+ "" + majorCode + " 0x" + Long.toHexString(minorCode)
						+ " " + e);
				sendDrmError(majorCode, minorCode, "");
			}
		}, new DRMOperationCompleteCallback() {
			public void OperationComplete() {
				AdVideoApplication.logger.d(LOG_TAG,
						"License cache reset complete, restarting player.");
				sendDrmRetry();
			}
		});
	}

	/**
	 * Force to acquire the DRM license and notify the listeners to retry
	 * playback
	 */
	private void forceAcquireLicense() {
		AdVideoApplication.logger.d(LOG_TAG,
				"Attempting to re-aquire DRM license from server.");

		if (mDrmMetadata == null) {
			AdVideoApplication.logger.d(LOG_TAG, "No metadata.");
			return;
		}
		mDrmManager.acquireLicense(mDrmMetadata,
				DRMAcquireLicenseSettings.FORCE_REFRESH,
				new DRMOperationErrorCallback() {

					@Override
					public void OperationError(long majorCode, long minorCode,
							Exception ex) {
						String error = "";
						if (ex != null)
							error = ex.toString();
						AdVideoApplication.logger.d(LOG_TAG, "License aquisition failed: "
								+ majorCode + " " + minorCode + " " + error);

						sendDrmError(majorCode, minorCode, "");

					}
				}, new DRMLicenseAcquiredCallback() {
					@Override
					public void LicenseAcquired(DRMLicense arg0) {

						sendDrmRetry();
					}
				});

	}

	// DRM major code that requires retry
	private static long[] sRequiresRetry = { 3300, // Invalid Voucher
			3303, // Content expired
			3305, // Server connection failed
			3308, // Wrong license key
			3312, // LicenseIntegrity
			3321, // Individualization Failed
			3322, // DeviceBindingFailed
			3325, // CorruptServerStateStore
			3326, // StoreTamperingDetected
			3327, // ClockTamperingDetected
			3328, // ServerErrorTryAgai
			3332 }; // CachedLicenseExpired

	// DRM major code that requires reset local cache
	private static long[] sRequiresResetVouchers = { 3322, // DeviceBindingFailed
			3323, // CorruptGlobalStateStore
			3326, // StoreTamperingDetected
			3346 }; // Migration failed

	/**
	 * Helpler function to check if the given major code belongs to a DRM code
	 * category
	 * 
	 * @param majorCode
	 *            the major code to be categorized
	 * @param category
	 *            the category to check whether the major code belongs to
	 * @return true if majorCode belongs to category, false otherwise
	 */
	private boolean requiredFor(long majorCode, long[] category) {
		for (long l : category)
			if (l == majorCode)
				return true;
		return false;
	}

	private boolean requiresRetry(long majorCode, long minorCode) {
		return requiredFor(majorCode, sRequiresRetry);
	}

	private boolean requiresResetVouchers(long majorCode, long minorCode) {
		return requiredFor(majorCode, sRequiresResetVouchers);
	}

	/**
	 * Notify the listeners to handle DRM error
	 * 
	 * @param majorCode
	 *            the major code of the DRM error
	 * @param minorCode
	 *            the minor code of the DRM error
	 * @param error
	 *            the custom error message defined by customer (not supported by
	 *            PSDK yet)
	 */
	private void sendDrmError(long majorCode, long minorCode, String error) {
		for (DrmManagerEventListener listener : eventListeners) {
			listener.onError(majorCode, minorCode, error);
		}
	}

	/**
	 * Notify the listeners to retry DRM playback
	 */
	private void sendDrmRetry() {
		for (DrmManagerEventListener listener : eventListeners) {
			listener.onRetry();
		}
	}

	/**
	 * Build a application default message for DRM errors, defined in the String
	 * resource
	 * 
	 * @param majorCode
	 *            the major code of the DRM error
	 * @param minorCode
	 *            the minor code of the DRM error
	 * @param packageName
	 *            the package name of the player application
	 * @param resources
	 *            the resource
	 * 
	 * @return an DRM error message defined from the application String resource
	 *         (res/values/strings)
	 */
	public static String buildMessage(long majorCode, long minorCode,
			String packageName, Resources resources) {
		return new DRMStringsRes(packageName, resources).buildMessage(
				majorCode, minorCode);
	}

}
