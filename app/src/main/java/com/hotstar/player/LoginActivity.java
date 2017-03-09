package com.hotstar.player;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.events.BusProvider;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends ActionBarActivity {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + LoginActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState != null)
            return;

        setContentView(R.layout.activity_login);

        Button btnRegisterNow = (Button) findViewById(R.id.btnRegisterNow);
        btnRegisterNow.setOnClickListener(btnRegisterNowClickListener);

        Button btnGotoSignIn = (Button) findViewById(R.id.btnGotoSignIn);
        btnGotoSignIn.setOnClickListener(btnGotoSignInClickListener);

        Button btnGotoLibrary = (Button) findViewById(R.id.btnGotoLibrary);
        btnGotoLibrary.setOnClickListener(btnGotoLibraryClickListener);

        BusProvider.get().register(this);
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void onResume ()
    {
        super.onResume();
    }

    protected void onDestroy() {
        super.onDestroy();

        BusProvider.get().unregister(this);
    }

    protected void onPause() {
        super.onPause();
    }

    private final View.OnClickListener btnRegisterNowClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // here
        }
    };

    private final View.OnClickListener btnGotoSignInClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, SignInActivity.class);
            startActivity(intent);
        }
    };

    private final View.OnClickListener btnGotoLibraryClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // Set userstatus with LOGIN with ANONYMOUS user
            HotStarApplication.getInstance().anonymousLogin();

            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
    };
}