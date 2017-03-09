package com.hotstar.player;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.UiThread;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.HotStarUserInfoFailureEvent;
import com.hotstar.player.events.HotStarUserInfoGotEvent;
import com.hotstar.player.model.HotStarUserInfo;
import com.hotstar.player.webservice.HotStarWebService;
import com.squareup.otto.Subscribe;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignInActivity extends ActionBarActivity {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + SignInActivity.class.getSimpleName();

    private boolean bInMetro = true;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (savedInstanceState != null)
            return;

        setContentView(R.layout.activity_signin);
        Button btnSignIn = (Button) findViewById(R.id.btnSingIn);
        btnSignIn.setOnClickListener(btnSignInClickListener);

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

    private final View.OnClickListener btnSignInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AdVideoApplication.logger.i(LOG_TAG + "#onSignInClickListener", "Sign In button is clicked");
            EditText etUsername = (EditText) findViewById(R.id.etUserName);
            EditText etPassword = (EditText) findViewById(R.id.etPassword);
            CheckBox chkLocation = (CheckBox) findViewById(R.id.chkLocation);

            final String username = etUsername.getText().toString();
            final String password = etPassword.getText().toString();

            if (username.equalsIgnoreCase("") == true) {
                Toast.makeText(SignInActivity.this, "Please input the username", Toast.LENGTH_SHORT).show();
            }
            if (password.equalsIgnoreCase("") == true) {
                Toast.makeText(SignInActivity.this, "Please input the password", Toast.LENGTH_SHORT).show();
            }

            bInMetro = chkLocation.isChecked();
            HotStarWebService.getInstance().getUserInfo(username, password);
        }
    };

    @Subscribe
    public void onHotStarUserInfoFailureEvent(HotStarUserInfoFailureEvent event) {
        final String message = event.getMessage();
        this.runOnUiThread(new Runnable() {
            public void run() {
                hideSoftKeyboard();
                Toast.makeText(SignInActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Subscribe
    public void onHotStarUserInfoGotEvent(HotStarUserInfoGotEvent event) {
        AdVideoApplication.logger.e(LOG_TAG + "#SignedUser",
                "Username=" + event.getUserInfo().userName + " InMetro=" + (bInMetro==true? "YES" : "No"));

        // Set userstatus with LOGIN with REGISTERED user
        HotStarApplication.getInstance().registeredLogin(event.getUserInfo());
        HotStarApplication.getInstance().setInMetroLocation(bInMetro);

        // goto home screen
        Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }
}