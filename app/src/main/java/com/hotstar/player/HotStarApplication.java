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

package com.hotstar.player;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.utils.DeviceInfo;
import com.hotstar.player.model.HotStarUserInfo;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class HotStarApplication extends AdVideoApplication {
	public static final boolean DEBUG = true;

	public enum UserStatusType {
		STATUS_USER_LOGOUT,
		STATUS_USER_LOGIN_ANONYMOUS,
		STATUS_USER_LOGIN_REGISTERED
	};

	private static DisplayImageOptions imageOptions = null;
	private static HotStarApplication instance = null;

	private HotStarUserInfo mUserInfo = null;
	private UserStatusType mLoginStatus = UserStatusType.STATUS_USER_LOGOUT;
	private boolean mInMetro = true;

	public static Context getAppContext() {
		return instance.getApplicationContext();
	}

	public static HotStarApplication getInstance() {
		if (instance == null)
			instance = new HotStarApplication();

		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		showUserDeviceInfo();
		initFontLibrary();

		// un-used : ImageLoader instance
		// initImageLoader(getApplicationContext());

		mLoginStatus = UserStatusType.STATUS_USER_LOGOUT;
		mUserInfo = null;

		instance = this;
	}

	/**
	 * User Management Functions
	 *
	 */
	public void anonymousLogin() {
		mLoginStatus = UserStatusType.STATUS_USER_LOGIN_ANONYMOUS;
	}

	public void registeredLogin(HotStarUserInfo userInfo) {
		mUserInfo = userInfo;
		mLoginStatus = UserStatusType.STATUS_USER_LOGIN_REGISTERED;
	}

	public void logout() {
		mLoginStatus = UserStatusType.STATUS_USER_LOGOUT;
		mUserInfo = null;
	}

	public String getUsername() {
		if (mLoginStatus == UserStatusType.STATUS_USER_LOGIN_ANONYMOUS) {
			return "Anonymous";
		}
		else if (mLoginStatus == UserStatusType.STATUS_USER_LOGIN_REGISTERED) {
			if (mUserInfo == null)
				return "ERROR-USER";
			else
				return mUserInfo.userName;
		}
		else if (mLoginStatus == UserStatusType.STATUS_USER_LOGOUT) {
			return "LOGOUT";
		}

		return "";
	}

	public HotStarUserInfo getUserInfo() {
		return mUserInfo;
	}

	public UserStatusType getLoginStatus() {
		return mLoginStatus;
	}

	/**
	 * Location management functions
	 *
	 */
	public void setInMetroLocation(boolean isInMetro) {
		mInMetro = isInMetro;
	}

	public boolean isInMetroLocation() {
		return mInMetro;
	}

	public String getLocation() {
		if (mInMetro == true)
			return "Metro";
		else
			return "NonMetro";
	}

	private void showUserDeviceInfo() {
		AdVideoApplication.logger.e(LOG_APP_NAME + "#Id", Build.ID);
		AdVideoApplication.logger.e(LOG_APP_NAME + "#Manufacturer", Build.MANUFACTURER);
		AdVideoApplication.logger.e(LOG_APP_NAME + "#Board", Build.BOARD);
		AdVideoApplication.logger.e(LOG_APP_NAME + "#Brand", Build.BRAND);
		AdVideoApplication.logger.e(LOG_APP_NAME + "#Device", Build.DEVICE);
		AdVideoApplication.logger.e(LOG_APP_NAME + "#Display", Build.DISPLAY);
		AdVideoApplication.logger.e(LOG_APP_NAME + "#Model", Build.MODEL);
		AdVideoApplication.logger.e(LOG_APP_NAME + "#Product", Build.PRODUCT);
	}


	/**
	 * Library Initialization Functions
	 *
	 */
	public static void initFontLibrary() {
		CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
				.setDefaultFontPath("fonts/GibsonLight-Regular.ttf")
				.setFontAttrId(R.attr.fontPath)
				.build());
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.memoryCacheSize(50 * 1024 * 1024);
		config.defaultDisplayImageOptions(getDisplayImageOptions());
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		config.writeDebugLogs(); // Remove for release app

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config.build());
	}

	public static DisplayImageOptions getDisplayImageOptions() {
		if (imageOptions != null)
			return imageOptions;

		imageOptions = new DisplayImageOptions.Builder()
				//.showImageForEmptyUri(R.drawable.ic_empty)
				//.showImageOnFail(R.drawable.ic_error)
				// .resetViewBeforeLoading(true)
				// .cacheInMemory(true)
				// .cacheOnDisk(false)
				// .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
				// .bitmapConfig(Bitmap.Config.RGB_565)
				// .considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(1000))
				.build();

		return imageOptions;
	}
}
