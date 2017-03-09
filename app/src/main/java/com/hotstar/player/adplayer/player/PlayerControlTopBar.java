package com.hotstar.player.adplayer.player;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.hotstar.player.R;
import com.hotstar.player.adplayer.AdVideoApplication;

import java.util.Timer;
import java.util.TimerTask;
import android.os.Handler;

public class PlayerControlTopBar {
    Context context;
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "PlayerControlTopBar";

    private ImageButton btnPlayerClose = null;
    private ImageButton btnPlayerShare = null;
    private ImageButton btnPlayerMoreFill = null;
    private View view;

    private boolean isVisible = false;

    private Timer fadeOutTimer;
    private final Handler handler = new Handler();

    public PlayerControlTopBar(Context context, ViewGroup view) {
        this.view = view;
        this.context = context;

        view.setVisibility(View.INVISIBLE);

        btnPlayerClose = (ImageButton) view.findViewById(R.id.btnPlayerControlClose);
        btnPlayerClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayerClose();
            }
        });
        btnPlayerShare = (ImageButton) view.findViewById(R.id.btnPlayerControlShare);
        btnPlayerShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayerShare();
            }
        });
        btnPlayerMoreFill = (ImageButton) view.findViewById(R.id.btnPlayerControlMoreFill);
        btnPlayerMoreFill.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                togglePlayerMoreFill();
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
                        AdVideoApplication.logger.i(LOG_TAG + "#startFadeOutTimer",
                                "Hiding the top bar from timer thread");
                        PlayerControlTopBar.this.hide();
                    }
                });
            }
        }, STAY_VISIBLE_DURATION);
    }

    /**
     * Stop the fade out timer
     */
    private void stopFadeOutTimer() {
        AdVideoApplication.logger.i(LOG_TAG + "#stopFadeOutTimer",
                "Stopping the fade-out timer.");
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
        AdVideoApplication.logger.i(LOG_TAG + "#resetFadeOutTimer",
                "Restarting the fade-out timer.");
        stopFadeOutTimer();
        startFadeOutTimer();
    }

    /**
     * Toggle close button
     */
    protected void togglePlayerClose() {
        Activity activity = (Activity) context;
        if (activity != null)
            activity.finish();
    }

    /**
     * Toggle Share button
     */
    protected void togglePlayerShare() {

    }

    /**
     * Toggle MoreFill button
     */
    protected void togglePlayerMoreFill() {

    }

}