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
 *  PlayerControlBar class contains the UI Components such as buttons for play,pause,rewind,closed captions and audio setting.
 *  It also contains a seek bar instance of the MarkableSeekbar class to define the timeline of the media item.
 *  This class contains the code for the listeners for each button click and also to define the user's interaction with the player. 
 */

package com.hotstar.player.adplayer.player;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.hotstar.player.R;
import com.adobe.mediacore.timeline.Timeline;
import com.adobe.mediacore.timeline.TimelineMarker;
import com.adobe.mediacore.timeline.advertising.AdBreakPlacement;
import com.adobe.mediacore.utils.TimeRange;
import com.hotstar.player.adplayer.AdVideoApplication;

import org.w3c.dom.Text;

import java.util.*;

public class PlayerAdControlBar {
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "PlayerAdControlBar";

	Context context;
	private View view;
	private Button btnLearnMore = null;

	private boolean isVisible = false;

	private Timer fadeOutTimer;
	private final Handler handler = new Handler();

	public PlayerAdControlBar(Context context, ViewGroup view) {
		this.view = view;
		this.context = context;

		view.setVisibility(View.INVISIBLE);

		btnLearnMore = (Button) view.findViewById(R.id.btnAdLearnMore);
		btnLearnMore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleLearnMore();
			}
		});
	}

	public View getView() {
		return view;
	}

	/**
	 * Hide the top control bar
	 */
	public void hide() {
		Activity activity = (Activity) context;
		if (isVisible && activity != null && isAutohideEnabled()) {
			AdVideoApplication.logger.i(LOG_TAG + "#hide", "Hiding the top control bar.");

			view.clearAnimation();;
			Animation fadeOutAnimation = AnimationUtils.loadAnimation(
					activity.getApplicationContext(), R.anim.fade_out);
			view.startAnimation(fadeOutAnimation);
			view.setVisibility(View.INVISIBLE);

			isVisible = false;
		}
	}

	/**
	 * Show the top control bar
	 */
	public void show() {
		Activity activity = (Activity) context;
		if (!isVisible) {
			AdVideoApplication.logger.i(LOG_TAG + "#show", "Showing the top control bar.");
			view.clearAnimation();;

			Animation fadeInAnimation = AnimationUtils.loadAnimation(
					activity.getApplicationContext(), R.anim.fade_in);
			view.startAnimation(fadeInAnimation);
			view.setVisibility(View.VISIBLE);

			isVisible = true;
		}

		resetFadeOutTimer();
	}

	private boolean isAutohideEnabled() {
		return true;
	}

	/**
	 * Start the fade out timer. The fade out timer is the time it takes before
	 * hiding the top control bar
	 */
	private void startFadeOutTimer() {
		fadeOutTimer = new Timer();
		long STAY_VISIBLE_DURATION = 5000;
		fadeOutTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {

					@Override
					public void run() {
						AdVideoApplication.logger.i(LOG_TAG + "#startFadeOutTimer", "Hiding the top bar from timer thread");
						PlayerAdControlBar.this.hide();
					}
				});
			}
		}, STAY_VISIBLE_DURATION);
	}

	/**
	 * Stop the fade out timer
	 */
	private void stopFadeOutTimer() {
		AdVideoApplication.logger.i(LOG_TAG + "#stopFadeOutTimer", "Stopping the fade-out timer.");
		if (fadeOutTimer != null) {
			fadeOutTimer.cancel();
		}
		view.clearAnimation();
		view.setVisibility(View.VISIBLE);
	}

	/**
	 * Reset the fade out timer. The timer resets when we need an time extension
	 * to hide the control bar due to playback/user event
	 */
	private void resetFadeOutTimer() {
		AdVideoApplication.logger.i(LOG_TAG + "#resetFadeOutTimer", "Restarting the fade-out timer.");
		stopFadeOutTimer();
		startFadeOutTimer();
	}

	/**
	 * Toggle Learn More button
	 */
	protected void toggleLearnMore() {

	}
}
