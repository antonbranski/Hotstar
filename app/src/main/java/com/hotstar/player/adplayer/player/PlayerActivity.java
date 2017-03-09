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

/*
 *  PlayerActivity class contains the UI Component the menu screen for the PSDK player which also displays the Home button,Logs button and the Settings button.
 *  This class handles the user interaction with menu.
 */

package com.hotstar.player.adplayer.player;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.adobe.mediacore.Version;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.R;
import com.hotstar.player.adplayer.core.VideoItem;

public class PlayerActivity extends ActionBarActivity implements PlayerClickableAdFragment.OnAdUserInteraction {
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "PlayerActivity";
	private VideoItem _contentInfo;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		AdVideoApplication.logger.i(LOG_TAG + "#onCreate", "Player activity created.");
		super.onCreate(savedInstanceState);

		/*
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		}
		*/

		setContentView(R.layout.activity_player);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		logVersion();

		initialize(getIntent());
	}

	private void logVersion() {
		AdVideoApplication.logger.i(LOG_TAG + "#logVersion", "AVE version: " + Version.getAVEVersion() + ". PSDK version: " + Version.getVersion() + ".");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		AdVideoApplication.logger.i(LOG_TAG + "::onNewIntent", "New media item.");
		// Initialize with the new item.
		setIntent(intent);
		initialize(intent);
	}

	private void initialize(Intent intent) {
		_contentInfo = (VideoItem) intent.getExtras().getSerializable("CONTENT_INFO");
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		PlayerFragment fragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.playerFragment);
		fragment.onFragmentRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		PlayerFragment fragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.playerFragment);
		fragment.onFragmentSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onAdClick() {
		PlayerFragment playerFragment = (PlayerFragment) getSupportFragmentManager().findFragmentById(R.id.playerFragment);

		if (playerFragment != null) {
			playerFragment.notifyAdClick();
		}
	}
}
