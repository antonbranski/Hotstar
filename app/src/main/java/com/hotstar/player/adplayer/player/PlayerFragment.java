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
 *  PlayerFragment class contains all the UI components such as the playerFrame,
 *  ControlBar,playerClickableAdFragment,adOverlay and audioProfile.
 *  It handles the initialization of all these components as well as creating the player,setting up the views, creating feature managers for the media player,
 *  handling media events such as resume,play,pause and handling the event listeners for QoSManager,DRMManager,CCManager,AAManager,
 *  AdsManager and PlaybackManager.
 */

package com.hotstar.player.adplayer.player;

import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.adobe.mediacore.DefaultMediaPlayer;
import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.MediaPlayer.PlayerState;
import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.adobe.mediacore.timeline.Timeline;
import com.adobe.mediacore.timeline.advertising.Ad;
import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.adobe.mediacore.utils.TimeRange;
import com.felipecsl.gifimageview.library.GifImageView;
import com.github.pedrovgs.DraggableView;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.R;
import com.hotstar.player.adplayer.advertising.CustomDirectAdBreakResolver;
import com.hotstar.player.adplayer.config.ConfigProvider;
import com.hotstar.player.adplayer.drm.DRMErrorDialog;
import com.hotstar.player.adplayer.manager.AAManager;
import com.hotstar.player.adplayer.manager.AAManagerOn;
import com.hotstar.player.adplayer.manager.AdsManager;
import com.hotstar.player.adplayer.manager.AdsManagerOn;
import com.hotstar.player.adplayer.manager.CCManager;
import com.hotstar.player.adplayer.manager.DrmManager;
import com.hotstar.player.adplayer.manager.DrmManager.DrmManagerEventListener;
import com.hotstar.player.adplayer.manager.ManagerFactory;
import com.hotstar.player.adplayer.manager.PlaybackManager;
import com.hotstar.player.adplayer.manager.PlaybackManager.PlaybackManagerEventListener;
import com.hotstar.player.adplayer.manager.QosManager;
import com.hotstar.player.adplayer.manager.VAManager;
import com.hotstar.player.adplayer.core.OverlayAdItem;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.player.CustomFrameLayout.OnSizeChangeListener;
import com.hotstar.player.adplayer.utils.Clock;
import com.hotstar.player.adplayer.utils.PreferencesUtils;
import com.hotstar.player.custom.BytesManager;
import com.hotstar.player.custom.GifDataDownloader;
import com.hotstar.player.adplayer.manager.OverlayAdResourceManager;
import com.hotstar.player.custom.ScreenshotManager;
import com.hotstar.player.events.BackToBaseFragmentEvent;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.ConnectorErrEvent;
import com.hotstar.player.events.PlayerFailureEvent;
import com.hotstar.player.fragments.BaseFragment;
import com.hotstar.player.model.TransitionBaseFragmentModel;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.ACTION_VIEW;

public class PlayerFragment extends Fragment {
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME  + PlayerFragment.class.getSimpleName();

	private Activity mActivity = null;

	private final String CURRENT_PLAYER_POSITION = "current_player_position";
	private final String CURRENT_PLAYER_STATE = "current_player_state";
	private final String PLAYBACK_CLOCK = "PlaybackClock";
	private final static float CONTROL_BAR_RATIO = 0.6f;
	private final static float ACTION_BAR_RATIO = 0.2f;

	private MediaPlayer mediaPlayer;
	private MediaPlayer.PlayerState savedPlayerState;
	private VideoItem videoItem;

	private Clock playbackClock;
	private Clock.ClockEventListener playbackClockEventListener;

	// feature managers
	private PlaybackManager playbackManager;
	private AdsManager adsManager;
	private CCManager ccManager;
	private AAManager aaManager;
	private QosManager qosManager;
	private DrmManager drmManager;
	private VAManager vaManager;

	// lifecycle tracking variables
	private long lastKnownTime;
	private boolean isRestored;
	private MediaPlayer.PlayerState lastKnownStatus = MediaPlayer.PlayerState.IDLE;
	private PlayerControlBar.ControlBarEvent lastIgnoredEvent;

	// UI components
	private FrameLayout playerFrame;
	private PlayerControlBar controlBar;
	private PlayerControlTopBar controlTopBar;
	private PlayerAdControlBar controlAdBar;

	private PlayerClickableAdFragment playerClickableAdFragment;
	private ProgressBar spinner;
	// private TextView audioProfile;
	private ViewGroup playerFragmentView;
	// private ImageButton _ccButton,_rewindButton,btnTouchPause;
	private ImageView overlayAdImageView;
	private GifImageView overlayAdGifView;

	private long savedMovieWidth;
	private long savedMovieHeight;
	private boolean isAdPlaying = false;
	private Handler handler = new Handler();

	// VPAID start local time
	private long vpaidStartLocalTime = 0;
	private String originalURL;
	private String currentVPaidURL;
	private Metadata originalMetadata;
	private GifImageView vpaidAdIconGifView;

	// Screenshot draggable view
	private ImageView thumbnailImageView;
	private ImageView thumbnailPlayImageView;
	private HashMap<String, Boolean> vPaidUserExperienceMap = new HashMap<>();

	// white & black color long value
	private final int WHITE_COLOR = -1;
	private final int BLACK_COLOR = -16777216;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mActivity = activity;
		BusProvider.get().register(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mActivity = null;
		BusProvider.get().unregister(this);
	}

	@Override
	public ViewGroup onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState)
	{
		playerFragmentView = (ViewGroup) inflater.inflate(R.layout.fragment_player, container, false);
		return playerFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		initialize(getActivity().getIntent());
		super.onActivityCreated(savedInstanceState);
	}

	/**
	 * Initialize the player fragment by setting up views, media player and
	 * feature managers
	 * 
	 * @param intent
	 *            the intent that contains the video item
	 */
	private void initialize(Intent intent)
	{
		videoItem = (VideoItem) intent.getExtras().getSerializable("CONTENT_INFO");
		AdVideoApplication.logger.i(LOG_TAG + "#initialize", "Initializing the media player with item [" + videoItem.getTitle() + "].");

		playerClickableAdFragment = (PlayerClickableAdFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.playerClickInfo);
		playerFrame = (FrameLayout) playerFragmentView.findViewById(R.id.playerFrame);

		ViewGroup controlBarView = (ViewGroup) playerFragmentView.findViewById(R.id.ControlBarItem);
		controlBar = new PlayerControlBar(getActivity(), controlBarView);

		// ViewGroup controlTopBarView = (ViewGroup) playerFragmentView.findViewById(R.id.ControlTopBarItem);
		// controlTopBar = new PlayerControlTopBar(getActivity(), controlTopBarView);

		ViewGroup controlAdBarView = (ViewGroup) playerFragmentView.findViewById(R.id.AdControlBarItem);
		controlAdBar = new PlayerAdControlBar(getActivity(), controlAdBarView);

		spinner = (ProgressBar) playerFragmentView.findViewById(R.id.pbBufferingSpinner);

		overlayAdImageView = (ImageView) playerFragmentView.findViewById(R.id.overlayAdImageView);
		overlayAdGifView = (GifImageView) playerFragmentView.findViewById(R.id.overlayAdGifView);
		overlayAdGifView.setOnFrameAvailable(new GifImageView.OnFrameAvailable() {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
			@Override
			public Bitmap onFrameAvailable(Bitmap bitmap) {
				return bitmap;
			}
		});

		java.io.InputStream is;
		is = mActivity.getResources().openRawResource(R.drawable.vpaid_ad_icon);

		vpaidAdIconGifView = (GifImageView) playerFragmentView.findViewById(R.id.vpaidAdIconGifView);
		vpaidAdIconGifView.setBytes(BytesManager.getBytes(is));
		vpaidAdIconGifView.setOnFrameAvailable(new GifImageView.OnFrameAvailable() {
			@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
			@Override
			public Bitmap onFrameAvailable(Bitmap src) {
				if (src == null)
					return src;

				int width = src.getWidth();
				int height = src.getHeight();
				Bitmap b = src.copy(Bitmap.Config.ARGB_8888, true);
				b.setHasAlpha(true);

				int[] pixels = new int[width * height];
				src.getPixels(pixels, 0, width, 0, 0, width, height);

				for (int i = 0; i < width * height; i++) {
					if ((pixels[i] == WHITE_COLOR) || (pixels[i] == BLACK_COLOR)) {
						pixels[i] = 0;
					}
				}

				b.setPixels(pixels, 0, width, 0, 0, width, height);
				return b;
			}
		});
		vpaidAdIconGifView.setVisibility(View.INVISIBLE);
		vpaidAdIconGifView.setOnClickListener(vPaidADIconClickListener);

		thumbnailPlayImageView = (ImageView) playerFragmentView.findViewById(R.id.thumbnailPlayImageView);
		thumbnailPlayImageView.setVisibility(View.INVISIBLE);
		thumbnailImageView = (ImageView) playerFragmentView.findViewById(R.id.thumbnailImageView);
		thumbnailImageView.setVisibility(View.INVISIBLE);
		thumbnailImageView.setOnClickListener(thumbnailViewClickListener);
		OverlayAdResourceManager.getInstance().preloadImage(videoItem.getThumbnail().getLargeThumbnailUrl());

//		audioProfile = (TextView) playerFragmentView.findViewById(R.id.audioTextView);
//		audioProfile.setText(R.string.qosAudioProfile);
//		audioProfile.setVisibility(View.INVISIBLE);
//
//		_ccButton = (ImageButton) playerFragmentView.findViewById(R.id.sbPlayerControlCC);
//		_rewindButton = (ImageButton) playerFragmentView.findViewById(R.id.playerRewind);
				((CustomFrameLayout) playerFrame).addOnSizeChangeListener(new OnSizeChangeListener() {

					@Override
					public void onSizeChanged() {
						// Scale player, since container size has changed.
						handler.post(new Runnable() {

							@Override
							public void run() {
								setPlayerViewSize(savedMovieWidth, savedMovieHeight);
							}
						});
					}
				});

		createPlayer();
		setupViews();
		createManagers();
		prepareMedia();
	}

	/**
	 * Notify AdsManager to handle a clickable ad on click event
	 */
	public void notifyAdClick()
	{
		adsManager.adClick();
	}

	/**
	 * Create the PSDK media player and set up the views and listeners
	 * associated with it
	 */
	private void createPlayer()
	{
		AdVideoApplication.logger.i(LOG_TAG + "#createPlayer", "Creating player.");

		// create Media Player
		mediaPlayer = createMediaPlayer();
		playerFrame.addView(mediaPlayer.getView());
		playerFrame.setOnTouchListener(userActionEventListener);
		playerFrame.setOnClickListener(userActionEventListener);
		mediaPlayer.getView().setOnTouchListener(userActionEventListener);

		installListeners();
	}

	/**
	 * Install clock event listener and user action event listener to monitor
	 * time progression and user actions.
	 */
	private void installListeners()
	{
		playbackClock = new Clock(PLAYBACK_CLOCK, 1000);
		playbackClockEventListener = new Clock.ClockEventListener() {
			@Override
			public void onTick(String name) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (controlBar != null && mediaPlayer != null) {
							if (playbackManager.isPlaying()) {
								controlBar.setSeekabledRange(playbackManager.getLocalSeekRange());
								controlBar.setPosition(playbackManager.getLocalTime());
							}

							if (!playbackManager.isCompleted()) {
								if (playbackManager.isLive()) {
									controlBar.update();
								}
							}

							// post current time to AdsManager
							adsManager.updatePlayerCurrentTime(playbackManager.getLocalTime());
						}

						qosManager.updateQosInformation();
					}
				});
			}
		};

		playbackClock.addClockEventListener(playbackClockEventListener);

		if (controlBar != null) {
			controlBar.addEventListener(controlBarEventListener);
		}
	}

	/**
	 * Create sub views for this fragment
	 */
	private void setupViews()
	{
//		ViewGroup adsView = (ViewGroup) playerFragmentView.findViewById(R.id.adsOverlay);
//		adOverlay = new AdOverlay(adsView);
//
//		ImageButton ccView = (ImageButton) playerFragmentView.findViewById(R.id.sbPlayerControlCC);
//
//		ccView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				displayClosedCaptioningDialog();
//			}
//		});
//
//		ImageButton aaView = (ImageButton) playerFragmentView.findViewById(R.id.sbPlayerControlAA);
//		aaView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				displayAlternateAudioDialog();
//			}
//		});
//
//		// Setup pause on touch button
//		btnTouchPause = (ImageButton) playerFragmentView.findViewById(R.id.btnTouchPause);
//		btnTouchPause.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				controlBar.togglePlayPause();
//			}
//		});

	}

	/**
	 * Create feature managers for the media player
	 */
	private void createManagers()
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		ConfigProvider config = new ConfigProvider(sharedPreferences, videoItem);
		playbackManager = ManagerFactory.getPlaybackManager(config, mediaPlayer);
		playbackManager.addEventListener(playbackManagerEventListener);

		qosManager = ManagerFactory.getQosManager(true, config, mediaPlayer);
		qosManager.addEventListener(qosManagerEventListener);
		qosManager.createQOSProvider(getActivity());

        vaManager = ManagerFactory.getVAManager(config.isVAEnabled(), config, mediaPlayer);
        vaManager.createVAProvider(getActivity().getApplicationContext());

		adsManager = ManagerFactory.getAdsManager(true, config, mediaPlayer);
		adsManager.addEventListener(adsManagerEventListener);

		ccManager = ManagerFactory.getCCManager(true, config, mediaPlayer);
		aaManager = ManagerFactory.getAAManager(true, config, mediaPlayer);

		drmManager = ManagerFactory.getDrmManager(config, mediaPlayer);
		drmManager.addEventListener(drmEventListener);
	}

	/**
	 * Prepare media resource in the player through playback manager
	 */
	private void prepareMedia()
	{
		AdVideoApplication.logger.i(LOG_TAG + "#prepareMedia", "Local time before media resource is set: " + playbackManager.getLocalTime() + ".");
		try {
			playbackManager.setupVideo(videoItem.getUrl(), adsManager);
		}
		catch (IllegalArgumentException e) {
			finishActivity(e.getMessage());
		}
		catch (IllegalStateException e) {
			finishActivity(e.getMessage());
		}
	}

	/*
	 * Request audio focus from the Android device.
	 */
	private void requestAudioFocus()
	{
		AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

		// Request audio focus for playback
		int result = am.requestAudioFocus(null,
				// Use the music stream.
				AudioManager.STREAM_MUSIC,
				// Request permanent focus.
				AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);

		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
			AdVideoApplication.logger.i(LOG_TAG + "#requestAudioFocus()", "Gained audio focus.");
		}
	}

	/*
	 * Abandon audio focus from the Android device.
	 */
	private void abandonAudioFocus()
	{
		AudioManager am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		am.abandonAudioFocus(null);

		AdVideoApplication.logger.i(LOG_TAG + "#abandonAudioFocus()", "Abandoned audio focus.");
	}

	/**
	 * Create a PSDK media player instance from the current context
	 * 
	 * @return a PSDK media player instance
	 */
	private MediaPlayer createMediaPlayer()
	{
		MediaPlayer mediaPlayer = DefaultMediaPlayer.create(getActivity().getApplicationContext());
		return mediaPlayer;
	}

	// Save/Restore state for the current fragment

	/**
	 * Called from main activity.
	 */
	protected void onFragmentRestoreInstanceState(Bundle savedInstanceState)
	{
		AdVideoApplication.logger.i(LOG_TAG + "#onRestoreInstanceState", "Restoring activity state.");

		lastKnownTime = savedInstanceState.getLong(CURRENT_PLAYER_POSITION, -1);
		AdVideoApplication.logger.i(LOG_TAG + "#onRestoreInstanceState", "Restored saved current player time: " + lastKnownTime);

		savedPlayerState = (MediaPlayer.PlayerState) savedInstanceState.getSerializable(CURRENT_PLAYER_STATE);
		AdVideoApplication.logger.d(LOG_TAG + "#onRestoreInstanceState", "Retrieved saved player state: " + savedPlayerState);

		isRestored = true;
	}

	/**
	 * Called from main activity.
	 */
	protected void onFragmentSaveInstanceState(Bundle outState)
	{
		AdVideoApplication.logger.i(LOG_TAG + "#onSaveInstanceState", "Saving activity state.");

		outState.putLong(CURRENT_PLAYER_POSITION, lastKnownTime);
		AdVideoApplication.logger.i(LOG_TAG + "#onSaveInstanceState", "Current player time [" + lastKnownTime + "] saved.");

		outState.putSerializable(CURRENT_PLAYER_STATE, savedPlayerState);
		AdVideoApplication.logger.i(LOG_TAG + "#onSaveInstanceState", "Current player state [" + savedPlayerState + "] saved.");
	}

	// Fragment life cycle functions implementation

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		// Ignore orientation/keyboard change.
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Resume player fragment by requesting audio focus and (re)starting
	 * playback clocking
	 */
	@Override
	public void onResume()
	{
		super.onResume();
		AdVideoApplication.logger.i(LOG_TAG + "#onResume", "Player activity resumed. Last known status: " + lastKnownStatus.name());
		requestAudioFocus();

		playbackClock.start();
	}

	/**
	 * Pause player fragment by pausing the playback, stopping the player clock,
	 * memorizing the current playback status and abandon audio focus
	 */
	@Override
	public void onPause()
	{
		AdVideoApplication.logger.i(LOG_TAG + "#onPause", "Player activity paused.");

		controlBar.hide();
		controlAdBar.hide();
		// controlTopBar.hide();
		playbackClock.stop();

		if (mediaPlayer != null)
		{
			savedPlayerState = playbackManager.getStatus();
			lastKnownTime = playbackManager.getCurrentTime();

			if (playbackManager.isPlaying()) {
				controlBar.pressPause();
				lastKnownStatus = savedPlayerState = playbackManager.getStatus();
			}
		}
		abandonAudioFocus();
		super.onPause();
	}

	/**
	 * Destroy player fragment and the media player
	 */
	@Override
	public void onDestroy()
	{
		AdVideoApplication.logger.i(LOG_TAG + "#onDestroy()", "Releasing resources.");
		super.onDestroy();

		destroyPlayer();
	}

	// Private functions for cleaning up resources
	/**
	 * Close player activity and display the error message
	 * 
	 * @param message
	 *            the error message
	 */
	private void finishActivity(String message)
	{
		Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
		mActivity.finish();
	}

	/**
	 * Destroy the media player object. This includes removing all listeners,
	 * destroying QOS provider if used, and removing all views the media player
	 * object is attached to
	 */
	private void destroyPlayer()
	{
		uninstallListeners();

		qosManager.destroyQOSProvider();
        vaManager.destroyVAProvider();
		playerFrame.removeAllViews();

		if (mediaPlayer != null) {
			destroyMediaPlayer(mediaPlayer);
			mediaPlayer = null;
		}

		lastKnownStatus = MediaPlayer.PlayerState.RELEASED;
	}

	/**
	 * Uninstall all listeners the player fragment listens to
	 * 
	 */
	private void uninstallListeners()
	{
		if (controlBar != null) {
			controlBar.removeEventListener(controlBarEventListener);
			controlBar = null;
		}

		if (playbackClock != null) {
			playbackClock.removeClockEventListener(playbackClockEventListener);
			playbackClock = null;
			playbackClockEventListener = null;
		}
	}

	/**
	 * Release the media player
	 * 
	 * @param mediaPlayer
	 *            the media player object to be destroyed
	 */
	private void destroyMediaPlayer(MediaPlayer mediaPlayer)
	{
		destroyManagers();
		mediaPlayer.release();
	}

	/**
	 * Destroy all feature managers
	 */
	private void destroyManagers()
	{
		playbackManager.destroy();
		adsManager.destroy();
		ccManager.destroy();
		aaManager.destroy();
		drmManager.destroy();
		qosManager.destroy();
	}

	// UI manipulation

	/**
	 * Specifies whether the user can perform a seek on the seek bar anymore.
	 * 
	 * @param enabled
	 */
	private void setControlBarEnabled(boolean enabled)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

		boolean canSeekInAd = sharedPreferences.getBoolean(AdsManagerOn.SETTINGS_SEEK_IN_AD, AdsManagerOn.DEFAULT_SEEK_IN_AD);
		if (enabled) {
			// Enable the control bar thumb.
			controlBar.enable();
//			_rewindButton.setEnabled(true);

		}
		else {
			// Disable the control bar thumb, only if the user is restricted to
			// perform any further seeks.
			if (canSeekInAd == false) {
				controlBar.disable();
//				_rewindButton.setEnabled(false);
			}
		}
	}

	/**
	 * Specifies whether the user can click on CC while ad is in progress.
	 * 
	 * @param enabled
	 */
	private void setCCEnabled(boolean enabled)
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		boolean canSeekInAd = sharedPreferences.getBoolean(AdsManagerOn.SETTINGS_SEEK_IN_AD, AdsManagerOn.DEFAULT_SEEK_IN_AD);
		if (enabled) {
			// Enable the control bar thumb.
//			_ccButton.setEnabled(true);
		}
		else {
			// Disable the control bar thumb, only if the user is restricted to
			// perform any further seeks.
			if (canSeekInAd == false) {
//				_ccButton.setEnabled(false);
			}
		}
	}

	/**
	 * Show a given message in toast with short duration
	 * 
	 * @param message
	 *            the message to be shown in toast
	 */
	private void showToast(String message)
	{
		if (!PreferencesUtils.getShowToastWarningsPref(getActivity())) {
			return;
		}

		Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show buffering spinner to indicate the buffering process
	 */
	private void showBufferingSpinner()
	{
		handler.post(new Runnable() {
			@Override
			public void run() {
				spinner.setVisibility(View.VISIBLE);
			}
		});
	}

	/**
	 * Hide buffering spinner to indicate buffering is over
	 */
	private void hideBufferingSpinner()
	{
		handler.post(new Runnable() {
			@Override
			public void run() {
				spinner.setVisibility(View.INVISIBLE);
			}
		});
	}

	/**
	 * Show play button again by simulating a user pressing the stop button. We do
	 * this because the stop event can be driven by the playback reaches its
	 * end.
	 */
	private void showPlayButton()
	{
		handler.post(new Runnable() {

			@Override
			public void run() {
				controlBar.pressStop();
			}
		});
	}

	/**
	 * Display playback and seekable range. Move the play head to the current
	 * time.
	 */
	private void displayRanges()
	{
		controlBar.setSeekabledRange(playbackManager.getLocalSeekRange());
		controlBar.setPosition(playbackManager.getLocalTime());
		controlBar.setStreamType(playbackManager.isLive());
	}

	/**
	 * Set the player view size with givent width and height
	 * 
	 * @param movieWidth
	 *            the width of the player view to be set to
	 * @param movieHeight
	 *            the height of the player view to be set to
	 */
	private void setPlayerViewSize(long movieWidth, long movieHeight)
	{
		if (mediaPlayer == null || mediaPlayer.getView() == null) {
			AdVideoApplication.logger.w(LOG_TAG + "#setPlayerViewSize", "Unable to find player view.");
			return;
		}

		AdVideoApplication.logger.i(LOG_TAG + "#setPlayerViewSize", "Original movie size: " + movieWidth + "x" + movieHeight);

		FrameLayout layout = (FrameLayout) playerFragmentView.findViewById(R.id.playerFrame);
		int layoutWidth = layout.getWidth();
		int layoutHeight = layout.getHeight();
		float screenAspectRatio = (float) layoutWidth / layoutHeight;

		if (movieWidth == 0 || movieHeight == 0) {
			// If movie size is not available, fill the screen.
			movieWidth = layoutWidth;
			movieHeight = layoutHeight;
		}

		float movieAspectRatio = (float) movieWidth / movieHeight;
		int width, height;

		if (movieAspectRatio <= screenAspectRatio) {
			// Resize to fill height.
			width = (int) (layoutHeight * movieAspectRatio);
			height = layoutHeight;
		}
		else {
			// Resize to fill width.
			width = layoutWidth;
			height = (int) (layoutWidth * (1 / movieAspectRatio));
		}

		AdVideoApplication.logger.i(LOG_TAG + "#setPlayerViewSize", "Movie width x height: " + movieWidth + "x" + movieHeight);
		AdVideoApplication.logger.i(LOG_TAG + "#setPlayerViewSize", "Setting player view size to: " + width + "x" + height);
		mediaPlayer.getView().setLayoutParams(
				new FrameLayout.LayoutParams(width, height, Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL));
		RelativeLayout overlayAdLayout = (RelativeLayout) playerFragmentView.findViewById(R.id.OverlayAdLayout);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 1);
		layoutParams.addRule(RelativeLayout.ABOVE, controlBar.getView().getId());
		overlayAdLayout.setLayoutParams(layoutParams);
	}

	/**
	 * Run the last ignored command if there is any. This is used when users
	 * attempt to press the play or pause button too early before the player is
	 * ready.
	 */
	private synchronized void runLastIgnoredCommand()
	{
		if (lastIgnoredEvent == null) {
			return;
		}

		// Perform last ignored event.
		AdVideoApplication.logger.i(LOG_TAG + "#runLastIgnoredCommand()", "Performing last ignored event: " + lastIgnoredEvent.getEventType().getName());

		switch (lastIgnoredEvent.getEventType())
		{
			case PAUSE:
				if (playbackManager.isPlaying()) {
					controlBar.pressPause();
				}
				break;
			case PLAY:
				controlBar.pressPlay();
				break;
			case SEEK:
				long position = (Long) lastIgnoredEvent.getEventInfo();
				controlBar.seek(position);
				break;
			case SEEK_CLP:
				break;
			case STOP:
				break;
		}

		lastIgnoredEvent = null;
	}

	/**
	 * Displays a chooser dialog, allowing the user to select the desired closed
	 * captions.
	 */
	private void displayClosedCaptioningDialog()
	{
		AdVideoApplication.logger.i(LOG_TAG + "#selectClosedCaptions", "Displaying closed captions chooser dialog.");
		final List<String> items = ccManager.getClosedCaptionTracks(getActivity()
						.getString(R.string.active));

		// Adding the option of having "none" for user convenience

		items.add(0, "None");

		final String[] charItems = items.toArray(new String[items.size()]);

		final AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
		ab.setTitle(R.string.PlayerControlCCDialogTitle);
		ab.setSingleChoiceItems(charItems,
				ccManager.getSelectedClosedCaptionsIndex() + 1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int index) {

//						ImageButton ccButtonView = (ImageButton) playerFragmentView.findViewById(R.id.sbPlayerControlCC);
//
//						// Select the new closed captioning track.
//						ccManager.selectClosedCaptionTrack(index - 1);
//						// Dismiss dialog.
//
//						if (index != 0) {
//							ccButtonView.setImageResource(R.drawable.closecaptionon);
//						}
//
//						else {
//							ccButtonView.setImageResource(R.drawable.closecaptionoff);
//						}
						dialog.cancel();
					}
				})
				.setNegativeButton(R.string.PlayerControlCCDialogCancel, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Just cancel the dialog.
					}
				});
		ab.show();
	}

	//Set visibility of pause button
	
	private void setPlayPauseButtonVisibility(boolean visible)
	{
//	    if (btnTouchPause != null) {
//	    	btnTouchPause.setVisibility(visible ? View.VISIBLE : View.GONE);
//	    }
	}
	
	/**
	 * Displays a chooser dialog, allowing the user to select the desired
	 * alternate audio track.
	 */
	private void displayAlternateAudioDialog()
	{
		AdVideoApplication.logger.i(LOG_TAG + "#selectAlternateAudio", "Displaying alternate audio chooser dialog.");

		final int selectedAlternateAudio = aaManager.getSelectedAudioTrackIndex();
		if (selectedAlternateAudio != AAManagerOn.INVALID_AUDIO_TRACK)
		{
			final String items[] = aaManager.getAudioTracks();
			new AlertDialog.Builder(getActivity())
					.setTitle(R.string.PlayerControlAADialogTitle)
					.setSingleChoiceItems(items, selectedAlternateAudio,
							new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton) {
									boolean result = aaManager.selectAlternateAudioTrack(whichButton);
									if (result) {
										AdVideoApplication.logger.i(LOG_TAG + "#selectAlternateAudio", "Audio track selection successful");
									}
									else {
										AdVideoApplication.logger.i(LOG_TAG + "#selectAlternateAudio", "Audio track selection failed");
									}

									// Dismiss dialog2.
									dialog.cancel();
								}
							})
					.setNegativeButton(R.string.PlayerControlCCDialogCancel,
							new DialogInterface.OnClickListener()
							{
								public void onClick(DialogInterface dialog, int whichButton) {
									// Just cancel the dialog.
								}
							}).show();

		}
		else {
			AdVideoApplication.logger.i(LOG_TAG + "#selectAlternateAudioFailed", "Unable to detect the currently selected audio track.");
			Toast.makeText(getActivity(), "Unable to detect the currently selected audio track", Toast.LENGTH_SHORT).show();
		}
	}

	// ------------------------------------------------------------------------------
	//
	// Setup listeners
	//
	// ------------------------------------------------------------------------------
	private final PlayerControlBar.ControlBarEventListener controlBarEventListener = new PlayerControlBar.ControlBarEventListener() {
		@Override
		public void handleEvent(PlayerControlBar.ControlBarEvent controlBarEvent) {
			AdVideoApplication.logger.i(LOG_TAG + "::PlayerControlBar.ControlBarEventListener#handleEvent()", "Player control bar event: " + controlBarEvent.getEventType().getName());

			if (!playbackManager.isPrepared()) {
				// The player is not prepared. We will not run commands on it.
				AdVideoApplication.logger.i(LOG_TAG + "::PlayerControlBar.ControlBarEventListener#handleEvent()",
						"Player is not prepapred. Ignoring and saving last command: " + controlBarEvent.getEventType().getName());
				lastIgnoredEvent = controlBarEvent;
				return;
			}

			switch (controlBarEvent.getEventType())
			{
				case PLAY:
					playbackManager.play();
					setPlayPauseButtonVisibility(false);
					break;

				case PAUSE:
					playbackManager.pause();
					setPlayPauseButtonVisibility(true);
					break;

				case SEEK:
					long localSeekPosition = (Long) controlBarEvent.getEventInfo();
					playbackManager.seekToLocal(localSeekPosition);
					if (playbackManager.isCompleted()) {
						controlBar.setStatusToPlay();
					}
					break;

				case SEEK_CLP:
					playbackManager.seek(MediaPlayer.LIVE_POINT);
					setPlayPauseButtonVisibility(false);
					break;

				case STOP:
					setPlayPauseButtonVisibility(false);
					break;

				default:
					AdVideoApplication.logger.w(LOG_TAG + "#handleEvent()", "Unknown event: " + controlBarEvent.getEventType().getName());
			}
		}
	};

	private interface UserActionEventListener extends View.OnTouchListener, View.OnClickListener
	{
		// Empty UserActionEvent Listener interface class
	}

	private final UserActionEventListener userActionEventListener = new UserActionEventListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					break;

				case MotionEvent.ACTION_UP:
					float ratio = event.getY() / v.getHeight();
					AdVideoApplication.logger.i(LOG_TAG + "::UserActionEventListener#onTouch", "Handling onTouch user action: ratio=" + ratio + ".");

					// Check if user clicked above the control bar
					if (ratio > ACTION_BAR_RATIO && ratio < CONTROL_BAR_RATIO) {
						controlBar.togglePlayPause();
					}

					// Show the action bar and the control bar
					if (isAdPlaying == false)
						controlBar.show();
					else
						controlAdBar.show();
					// controlTopBar.show();
					break;
			}

			return false;
		}

		@Override
		public void onClick(View v) {
			// empty event handler
		}
	};
	/**
	 * Set up feature manager listeners
	 */

	private final QosManager.QosManagerEventListener qosManagerEventListener = new QosManager.QosManagerEventListener() {
		@Override
		public void onQosUpdate(ArrayList<QosManager.QosItem> qosItems)
		{
//			ViewGroup qosView = (ViewGroup) playerFragmentView.findViewById(R.id.qosTopLayout);

			for (QosManager.QosItem item : qosItems)
			{
//				// Check if item view is already inflated.
//				View tv = qosView.findViewById(item.getId());
//				if (tv == null) {
//					// Inflate the text view.
//					tv = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_player_qos, null).findViewById(R.id.qosTextView);
//					tv.setId(item.getId());
//				}
//
//				// Set the new text value.
//				((TextView) tv).setText(item.getName() + ": " + item.getValue());
//
//				if (tv != null && tv.getParent() != null) {
//					((ViewGroup) tv.getParent()).removeView(tv);
//				}
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//					qosView.addView(tv, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//				}
//				else {
//					qosView.addView(tv, new ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//				}
			}
			// not used : qosView.setVisibility(View.VISIBLE);
		}
	};

	private final PlaybackManager.PlaybackManagerEventListener playbackManagerEventListener = new PlaybackManagerEventListener() {

		/**
		 * Show buffering spinner when buffering starts
		 */
		@Override
		public void onBufferStart() {
			showBufferingSpinner();
		}

		/**
		 * Hide buffering spinner when buffering completes
		 */
		@Override
		public void onBufferComplete() {
			hideBufferingSpinner();
		}

		/**
		 * Move the playhead to the post seek position after seeking completes
		 * If the playback is over, show a replay button. If users paused the
		 * video before seeking, pause the video again.
		 */
		@Override
		public void onSeekCompleted(long localAdjustedTime) {
            // if player was playing prior to user action seek, resume play
            if (controlBar.wasPlaying()) {
                controlBar.pressPlay();
            }

			controlBar.setPosition(localAdjustedTime);
			if (localAdjustedTime == playbackManager.getLocalSeekRangeEnd()
					&& playbackManager.isCompleted()) {
				showPlayButton();
			}
		}

		/**
		 * Run last ignored command if the users tried to perform
		 * play/pause/seek operation before player was prepared. Start the
		 * playback if autoplay is set to true. If the player was restored from
		 * a crash, seek to the position where the player was crashed. Hide the
		 * CC or AA button if closed captioning or late binding audio are not
		 * available.
		 */
		@Override
		public void onPrepared() {

			hideBufferingSpinner();
			if(isAdPlaying == false)
				controlBar.show();
			else
				controlTopBar.show();
			// controlTopBar.show();
			displayRanges();

			runLastIgnoredCommand();

			boolean playbackShouldStart = !isRestored;
			if (isRestored) {
				AdVideoApplication.logger.i(LOG_TAG + "MediaPlayer.PlayerStateEventListener#onStateChanged", "Initial seek to location: " + lastKnownTime);
				if (lastKnownTime >= 0) {
					playbackManager.seek(lastKnownTime);
				}

				playbackShouldStart = (savedPlayerState == PlayerState.PLAYING);
			}

			if (playbackShouldStart) {
				controlBar.pressPlay();
			}

			// register overlay ad listener
			adsManager.registerOverlayAdListener(overlayAdListener);
			controlBar.setOverlayAdTimeline(adsManager.getOverlayTimeline());

			// register vpaid ad listener
			adsManager.registerVPaidAdListener(vpaidAdListener);
			controlBar.setVPaidAdTimeline(adsManager.getVPaidTimeline());
		}

		/**
		 * Update control bar when the playback timeline updates
		 */
		@Override
		public void onUpdate(TimeRange localSeekRange, Timeline<?> timeline) {
			controlBar.setSeekabledRange(playbackManager.getLocalSeekRange());
			controlBar.setTimeline(timeline);
		}

		@Override
		public void onPlaying() {
		}

		/**
		 * When the playback completes, goes back to the catalog screen if users
		 * chooses to do this from the setting.
		 */
		@Override
		public void onComplete()
		{
			if (PreferencesUtils.shouldReturnToCatalogOnPlaybackComplete(getActivity())) {
				getActivity().finish();
			}
			else {
				// Show the replay button.
				showPlayButton();
				// Cancel clock thread.
				playbackClock.stop();
				// Update the position time.
				controlBar.setPosition(playbackManager.getLocalSeekRangeEnd());
				// Update QoS manually, since we've stopped the timer.
				qosManager.updateQosInformation();
			}
		}

		/**
		 * Update player view when the player receives a new width and height
		 */
		@Override
		public void onDimensionsChange(long height, long width)
		{
			if (width != savedMovieWidth || height != savedMovieHeight) {
				// Should resize the player.
				savedMovieWidth = width;
				savedMovieHeight = height;
				setPlayerViewSize(savedMovieWidth, savedMovieHeight);
				// _playerQos.setQosItem(R.string.qosResolution, width + "x" + height);
			}
		}

		/**
		 * Update the UI based on playback state changes
		 */
		@Override
		public void onStateChange(PlayerState state, MediaPlayerNotification notification)
		{
			AdVideoApplication.logger.i(LOG_TAG + "::MediaPlayer.PlayerStateEventListener#onStateChanged()", "Player state changed to [" + state + "].");

			if (lastKnownStatus == MediaPlayer.PlayerState.COMPLETE) {
				// Start the clock thread.
				playbackClock.start();
			}

			lastKnownStatus = state;

			switch (lastKnownStatus)
			{
				case PREPARING:
					showBufferingSpinner();
					controlBar.hide();
					controlAdBar.hide();
					// controlTopBar.hide();
					break;

				default:
					// do nothing
			}
		}

		/**
		 * Destroy the player activity when the player encounters an error. This
		 * is an inrecoverable error from a playback session.
		 */
		@Override
		public void onError(MediaPlayerNotification error) {
			finishActivity(error.getInnerNotification().getDescription());
		}

		@Override
		public void onSeeking() {
		}
	};

	private final AdsManager.AdsManagerEventListener adsManagerEventListener = new AdsManager.AdsManagerEventListener() {

		/**
		 * Display the ad overlay and disable control bar when the ad break
		 * starts
		 */
		@Override
		public void onAdBreakStarted(AdBreak adBreak) {
			setControlBarEnabled(false);
			setCCEnabled(false);
			
		}

		/**
		 * Hide the ad overlay and re-enable the control bar when the ad berak
		 * is over
		 */
		@Override
		public void onAdBreakCompleted(AdBreak adBreak) {
			setControlBarEnabled(true);
			setCCEnabled(true);
		}

		/**
		 * Show ad break progress when each ad creative starts If this is a
		 * clickable ad, show the clickable ad fragment
		 */
		@Override
		public void onAdStarted(AdBreak adBreak, Ad ad) {
			if (adsManager.isClickableAdsEnabled() && ad.isClickable()) {
				if (playerClickableAdFragment != null)
					playerClickableAdFragment.show();
			}

			isAdPlaying = true;
		}

		/**
		 * Hide ad break progress when each ad ends Hide the clickable ad
		 * fragment
		 */
		@Override
		public void onAdCompleted(AdBreak adBreak, Ad ad) {
			if (playerClickableAdFragment != null)
				playerClickableAdFragment.hide();

			isAdPlaying = false;
		}

		@Override
		public void onAdProgress(AdBreak adBreak, Ad ad, int percentage) {
		}

		/**
		 * Open an external browser to redirect to the clickable ad URL
		 */
		@Override
		public void onAdClick(String url) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(ACTION_VIEW, uri);
			try {
				startActivity(intent);
			}
			catch (Exception e) {
				AdVideoApplication.logger.w(LOG_TAG + "::MediaPlayer.AdsManagerEventListener#handleAdClick()", "An error occured while opening the narive browser");
			}
		}

		/**
		 * Update control bar with new timeline markers
		 */
		@Override
		public void onTimelineUpdated(Timeline<?> timeline) {
			controlBar.setTimeline(timeline);
		}
	};

	private final DrmManagerEventListener drmEventListener = new DrmManagerEventListener() {

		/**
		 * Display a dialog to describe the DRM error
		 */
		@Override
		public void onError(long majorCode, long minorCode, String error)
		{
			try {
				DRMErrorDialog dialog = new DRMErrorDialog(getActivity());
				Bundle args = new Bundle();

				if (error.isEmpty()) {
					// if custom message is not given
					String message = DrmManager.buildMessage(majorCode, minorCode, getActivity().getPackageName(), getActivity().getResources());
					args.putString("message", message);
				}
				else {
					// if custom message is given
					args.putString("message", error);
				}

				dialog.setArguments(args);
				dialog.show();

			}
			catch (Exception e) {
				AdVideoApplication.logger.d(LOG_TAG, e.getMessage());
			}
		}

		/**
		 * Reset the media player to retry DRM resource playback
		 */
		@Override
		public void onRetry() {
			AdVideoApplication.logger.e(LOG_TAG, "Reset player.");
			mediaPlayer.reset();
			prepareMedia();
		}

	};

	private final CustomDirectAdBreakResolver.VPaidAdListener vpaidAdListener = new CustomDirectAdBreakResolver.VPaidAdListener() {
		private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME  + CustomDirectAdBreakResolver.VPaidAdListener.class.getSimpleName();

		@Override
		public void onBefore5SecsVPaidAd(String vpaidURL) {
			AdVideoApplication.logger.e(LOG_TAG + "#onBefore5SecsVPaidAd()", vpaidURL);
			if (vPaidUserExperienceMap.containsKey(vpaidURL) == false) {
				vPaidUserExperienceMap.put(vpaidURL, false);
			}
		}

		@Override
		public void onStartVPaidAd(String vpaidURL, long startTime, long duration) {
			AdVideoApplication.logger.e(LOG_TAG + "#onStartVPaidAd()", vpaidURL);

			if((vPaidUserExperienceMap.containsKey(vpaidURL) == true) && (vPaidUserExperienceMap.get(vpaidURL).booleanValue() == true)) {
				// if user experiece in vpaid ad, ....
				return;
			}

			currentVPaidURL = vpaidURL;
			vpaidAdIconGifView.setVisibility(View.VISIBLE);
			vpaidAdIconGifView.startAnimation();
		}

		@Override
		public void onCompleteVPaidAd(String vpaidURL) {
			AdVideoApplication.logger.e(LOG_TAG + "#onCompleteVPaidAD()", vpaidURL);

			if((vPaidUserExperienceMap.containsKey(vpaidURL) == true) && (vPaidUserExperienceMap.get(vpaidURL).booleanValue() == true)) {
				// if user experiece in vpaid ad, ....
				return;
			}

			currentVPaidURL = "";
			vpaidAdIconGifView.setVisibility(View.INVISIBLE);
			vpaidAdIconGifView.stopAnimation();
		}
	};

	private final CustomDirectAdBreakResolver.OverlayAdListener overlayAdListener = new CustomDirectAdBreakResolver.OverlayAdListener() {
		private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME  + CustomDirectAdBreakResolver.OverlayAdListener.class.getSimpleName();

		@Override
		public void onBefore5SecsOverlayAd(String imageURL) {
			AdVideoApplication.logger.e(LOG_TAG + "#onBefore5SecsOverlayAd", imageURL);
			controlBar.show();
		}

		@Override
		public void onStartOverlayAd(String imageURL, long startTime, long duration) {
			AdVideoApplication.logger.e(LOG_TAG + "#onStartOverlayAD():" , imageURL);
			String ext = imageURL.substring(imageURL.lastIndexOf("."));
			if (ext.equalsIgnoreCase(".gif")) {
				if (overlayAdGifView != null) {
					OverlayAdResourceManager.getInstance().display(imageURL, overlayAdGifView);
				}
			}
			else  {
				if (overlayAdImageView != null) {
					OverlayAdResourceManager.getInstance().display(imageURL, overlayAdImageView);
				}
			}
		}

		@Override
		public void onCompleteOverlayAd(String imageURL) {
			AdVideoApplication.logger.e(LOG_TAG + "#onCompleteOverlayAD(): " + imageURL, "");
			String ext = imageURL.substring(imageURL.lastIndexOf("."));
			if (ext.equalsIgnoreCase(".gif")) {
				if (overlayAdGifView != null) {
					OverlayAdResourceManager.getInstance().hide(overlayAdGifView);
				}
			}
			else {
				if (overlayAdImageView != null) {
					OverlayAdResourceManager.getInstance().hide(overlayAdImageView);
				}
			}
		}
	};

	/**
	 * PlaybackEventListener for VPAID Ad event listener
	 */
	private final MediaPlayer.PlaybackEventListener vpaidAdEventListener = new MediaPlayer.PlaybackEventListener() {

		/**
		 * Select first audio track when playback is ready
		 */
		@Override
		public void onPrepared() {
			mediaPlayer.play();
			vpaidAdIconGifView.setVisibility(View.INVISIBLE);
			OverlayAdResourceManager.getInstance().display(videoItem.getThumbnail().getLargeThumbnailUrl(), thumbnailImageView);
			OverlayAdResourceManager.getInstance().show(thumbnailPlayImageView);
		}

		@Override
		public void onPlayComplete() {
			mediaPlayer.replaceCurrentItem(MediaResource.createFromUrl(originalURL, originalMetadata));
			mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, vpaidAdEventListener);
			mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, originalAdEventListener);
			OverlayAdResourceManager.getInstance().hide(thumbnailImageView);
			OverlayAdResourceManager.getInstance().hide(thumbnailPlayImageView);
		}

		@Override
		public void onPlayStart() {

		}

		@Override
		public void onSizeAvailable(long width, long height) {

		}

		@Override
		public void onStateChanged(PlayerState state, MediaPlayerNotification notification) {

		}

		@Override
		public void onTimedMetadata(TimedMetadata metadata) {

		}

		@Override
		public void onTimelineUpdated() {

		}

		@Override
		public void onUpdated() {

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
	 * PlaybackEventListener for original ad event listener
	 */
	private final MediaPlayer.PlaybackEventListener originalAdEventListener = new MediaPlayer.PlaybackEventListener() {

		/**
		 * Select first audio track when playback is ready
		 */
		@Override
		public void onPrepared() {
			mediaPlayer.seekToLocalTime(vpaidStartLocalTime);
			mediaPlayer.play();
			mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, originalAdEventListener);
		}

		@Override
		public void onPlayComplete() {
		}

		@Override
		public void onPlayStart() {

		}

		@Override
		public void onSizeAvailable(long width, long height) {

		}

		@Override
		public void onStateChanged(PlayerState state, MediaPlayerNotification notification) {

		}

		@Override
		public void onTimedMetadata(TimedMetadata metadata) {

		}

		@Override
		public void onTimelineUpdated() {

		}

		@Override
		public void onUpdated() {

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

	private final View.OnClickListener vPaidADIconClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			vpaidStartLocalTime = mediaPlayer.getLocalTime();
			originalURL = mediaPlayer.getCurrentItem().getResource().getUrl();
			originalMetadata = mediaPlayer.getCurrentItem().getResource().getMetadata();

			mediaPlayer.replaceCurrentItem(MediaResource.createFromUrl(currentVPaidURL, null));
			mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, vpaidAdEventListener);

			// user see vpaid
			vPaidUserExperienceMap.put(currentVPaidURL, true);
		}
	};

	private final View.OnClickListener thumbnailViewClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			mediaPlayer.replaceCurrentItem(MediaResource.createFromUrl(originalURL, originalMetadata));
			mediaPlayer.removeEventListener(MediaPlayer.Event.PLAYBACK, vpaidAdEventListener);
			mediaPlayer.addEventListener(MediaPlayer.Event.PLAYBACK, originalAdEventListener);
			OverlayAdResourceManager.getInstance().hide(thumbnailImageView);
			OverlayAdResourceManager.getInstance().hide(thumbnailPlayImageView);

			if ((vPaidUserExperienceMap.containsKey(currentVPaidURL) == true) && (vPaidUserExperienceMap.get(currentVPaidURL).booleanValue() == true)) {
				// user experience in vpaid
				return;
			}
			vpaidAdIconGifView.setVisibility(View.VISIBLE);
		}
	};
}