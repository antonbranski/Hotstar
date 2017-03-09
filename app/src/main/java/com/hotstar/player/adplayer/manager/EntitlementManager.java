/*******************************************************************************
 * ADOBE CONFIDENTIAL
 *  ___________________
 *
 *  Copyright 2014 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe Systems Incorporated and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe Systems Incorporated and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe Systems Incorporated.
 ******************************************************************************/

package com.hotstar.player.adplayer.manager;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.CountDownTimer;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.api.AccessEnablerException;
import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.adobe.mobile.Config;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.R;
import com.hotstar.player.adplayer.crypto.SignatureGenerator;
import com.hotstar.player.adplayer.crypto.SigningCredential;
import com.hotstar.player.adplayer.core.VideoItem;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class EntitlementManager implements IManager
{

    /**
     *  Post-sign in authentication 'success' icon
     */
    //public static final int ICON_AUTH = R.drawable.icon_auth_01;

    /**
     *  Pre-sign in authentication 'protected' icon
     */
    //public static final int ICON_KEY = R.drawable.icon_key_01;

    /**
     * Activity request code for {@link com.hotstar.player.adplayer.ui.entitlement.MvpdPickerActivity}
     */
    public static final int MVPD_PICKER_ACTIVITY = 100;

    /**
     * Activity request code for {@link com.hotstar.player.adplayer.ui.entitlement.MvpdLoginActivity}
     */
    public static final int MVPD_LOGIN_ACTIVITY = 101;

    /**
     * URL used by this application to connect with the Adobe Primetime PayTV Pass service.
     * Set to either STAGING_URI or PRODUCTION_URI
     */
    public static String PAYTV_PASS_URI;

    /**
     * ID used to configure application with Programmer
     */
    protected static String REQUESTOR_ID;

    /**
     * URL to a Token Verification Server
     */
    protected static String TVS_URL;

    /**
     * URL to Adobe Primetime PayTV Pass staging server.
     */
    private static String STAGING_URI;

    /**
     * URL to Adobe Primetime PayTV Pass production server
     */
    private static String PRODUCTION_URI;

    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "EntitlementManager";

    private static AccessEnabler accessEnabler;
    private static SignatureGenerator signatureGenerator;

    /**
     * Initializes the AccessEnabler library used to communicate with the Adobe Primetime PayTV Pass service.
     * It is intended that this method be called when the application starts.
     *
     * @param application the application instance
     */
    public static void initializeAccessEnabler(Application application)
    {

        // Programmer requestor ID, change to ID provided by your Adobe Primetime PayTV Pass representative
        REQUESTOR_ID = application.getResources().getString(R.string.adobepass_requestor_id);

        // Adobe Primetime PayTV Pass service provider endpoint for production environment
        PRODUCTION_URI = application.getResources().getString(R.string.adobepass_sp_url_production);

        // Adobe Primetime PayTV Pass service provider endpoint for staging environment
        STAGING_URI = application.getResources().getString(R.string.adobepass_sp_url_staging);

        // set to STAGING_URI when testing against the staging/test environment
        // set to PRODUCTION_URI when deploying the application for production use
        String environmentUri = STAGING_URI;

        // Adobe Primetime PayTV Pass service URI used by this application
        PAYTV_PASS_URI = environmentUri + "/adobe-services";

        // Token Verification Service URL
        TVS_URL = "https://" + environmentUri + "/tvs/v1/validate";

        /* Creation of a signature generator and signing of the requestor id is included here for demonstration
        purposes only. Do not sign your requestor id locally within the application. Instead, call to a trusted
        network server to sign the requestor id.
         */
        String credentialStorePassword = application.getResources().getString(R.string.adobepass_credential_store_password);
        InputStream credentialStore = /*application.getResources().openRawResource(R.raw.adobepass);*/ null;

        // load signing credentials
        SigningCredential signingCredential = new SigningCredential(credentialStore, credentialStorePassword);

        // initialize the signature generator
        signatureGenerator = new SignatureGenerator(signingCredential);

        // initialize the Adobe Mobile Analytics library for use with this application
        Config.setContext(application);

        try
        {
            accessEnabler = AccessEnabler.Factory.getInstance(application);
            AdVideoApplication.logger.i(LOG_TAG + "#initializeAccessEnabler",
                    "AccessEnabler library loaded successfully.");
        }
        catch (AccessEnablerException e)
        {
            AdVideoApplication.logger.e(LOG_TAG + "#initializeAccessEnabler",
                    "Failed to initialize the AccessEnabler library.");
        }
    }

    /**
     * Get the instance of the AccessEnabler.
     * {@link com.hotstar.player.adplayer.manager.EntitlementManager#initializeAccessEnabler(android.app.Application)}
     * must be called first to create the AccessEnabler instance.
     *
     * @return the instance of the AccessEnabler. If {@code null}, the AccessEnabler failed to initialize
     * and is not available to this application.
     */
    protected static AccessEnabler getAccessEnabler()
    {
        return accessEnabler;
    }

    /**
     * Get the instance of the {@link com.hotstar.player.adplayer.crypto.SignatureGenerator}.
     *
     * @return the instance of the SignatureGenerator used in this manager.
     */
    protected static SignatureGenerator getSignatureGenerator()
    {
        return signatureGenerator;
    }

    /**
     * Get the version number of the AccessEnabler library. Returns an empty string if the AccessEnabler
     * is not initialized.
     * @return the version number of the AccessEnabler library, or an empty string if the AccessEnabler library
     * is not initialized.
     */
    public static String getVersion()
    {
        if (accessEnabler != null)
        {
            return accessEnabler.getVersion();
        }
        else
        {
            return "";
        }
    }

    /**
     * Determines if the features of this manager are enabled. If {@code false}, calling the methods of this manager
     * are still valid, however most of the methods will perform no operations.
     *
     * @return {@code true} if this feature manager is enabled, {@code false} otherwise.
     * @see com.hotstar.player.adplayer.manager.EntitlementManagerOn
     */
    public boolean isEnabled()
    {
        return false;
    }

    /**
     * Initiates the authentication workflow. This method should be called when the user attempts to sign in to view
     * content protected by the Primetime PayTV Pass service. At the end of the authentication workflow, a dialog
     * is displayed to the user with the authentication status.
     * <p>Triggers the following callbacks:
     * <ul>
     *     <li>{@link com.hotstar.player.adplayer.manager.EntitlementManager.EntitlementManagerListener#onAuthenticationStatusChange(boolean)}</li>
     *     <li>{@link com.hotstar.player.adplayer.manager.EntitlementManager.EntitlementManagerListener#onDisplayNotificationDialog(com.hotstar.player.adplayer.manager.EntitlementManager.Status, String)}</li>
     * </ul></p>
     * @param caller instance of the Activity calling this method, used to start provider selection and login activities
     *               on behalf of the calling activity.
     */
    public void getAuthentication(Activity caller)
    {
        // do nothing
    }

    /**
     * Performs an authentication check by verifying the authentication token is still valid. This method may be
     * used to update UI features as it does not trigger a UI dialog.
     * <p>Triggers the following callbacks:
     * <ul>
     *     <li>{@link com.hotstar.player.adplayer.manager.EntitlementManager.EntitlementManagerListener#onAuthenticationStatusChange(boolean)}</li>
     * </ul></p>
     */
    public void checkAuthentication()
    {
        // do nothing
    }

    /**
     * Determines if the user is currently authenticated. This method is a convenience method used to update UI
     * features. No calls to the AccessEnabler are made.
     * <p>
     *     This method should not be used when an activity or application resumes as no checks against the
     * authentication token are made. Instead, call {@link EntitlementManager#checkAuthentication()}.
     * </p>
     *
     * @return {@code true} if the user is authenticated, {@code false} otherwise.
     */
    public boolean isAuthenticated()
    {
        return false;
    }


    /**
     * This method is called by the application after the user signs in to the selected content provider. This method
     * retrieves the authentication token from the Primetime PayTV Pass service and is required to complete the
     * authentication workflow.
     *
     * @see com.hotstar.player.adplayer.ui.entitlement.MvpdLoginActivity
     */
    public void getAuthenticationToken()
    {
        // do nothing
    }

    /**
     * This method is called by the application after the user selects an MVPD for sign in. If the user cancels
     * the authentication flow (i.e. pressing the 'Back' button), the application must pass {@code null} to this
     * method to reset the authentication state-machine.
     *
     * @param mvpdId the Mvpd Id selected by the user, or {@code null} if the user cancels the authentication workflow
     *
     * @see com.hotstar.player.adplayer.ui.entitlement.MvpdPickerActivity
     */
    public void setSelectedProvider(String mvpdId)
    {
        // do nothing
    }

    /**
     * Gets the currently selected Mvpd.
     * @return the currently selected Mvpd or {@code null} if the user is not authenticated.
     */
    public Mvpd getSelectedProvider()
    {
        return null;
    }

    /**
     * Check if a {@link com.hotstar.player.adplayer.core.VideoItem} requires authorization with the
     * Primetime PayTV Pass service before playback.
     * @param videoItem the VideoItem to check if authorization is required before playback
     * @return {@code true} if the VideoItem requires authorization with the Primetime PayTV Pass service or
     * {@code false} if the VideoItem may be played without authorization.
     */
    public boolean requiresAuthorization(VideoItem videoItem)
    {
        return false;
    }

    /**
     * Check if an authorization request is currently pending. If an authorization request is pending, it means
     * the user has requested to play a video which requires entitlement.
     * @return {@code true} if an authorization request is pending,
     * {@code false} if no authorization request is currently pending.
     */
    public boolean isAuthorizationPending()
    {
        return false;
    }

    /**
     * Checks if a {@link com.hotstar.player.adplayer.core.VideoItem} is pre-authorized by the
     * Primetime PayTV Pass service. A pre-authorization check is used to decorate the UI and does not entitle
     * the user to playback the VideoItem. For playback, use
     * {@link EntitlementManager#getAuthorization(com.hotstar.player.adplayer.core.VideoItem, android.app.Activity)}.
     *
     * @param videoItem the VideoItem to check for pre-authorization
     * @return {@code true} if the VideoItem is pre-authorized, {@code false} otherwise. If the user is not authenticated,
     * this method always returns {@code false}.
     *
     * @see com.hotstar.player.adplayer.manager.EntitlementManager#setCurrentResources(java.util.ArrayList)
     */
    public boolean isPreauthorized(VideoItem videoItem)
    {
        return false;
    }

    /**
     * Checks if a resource is per-authorized by the Primetime PayTV Pass service. A pre-authorization check is used
     * to decorate the UI and does not entitle the user to playback the resource.
     *
     * @param resourceId the resource ID to check for pre-authorization
     * @return {@code true} if the resource is pre-authorized, {@code false} otherwise. If the user is not authenticated,
     * this method always returns {@code false}.
     *
     * @see com.hotstar.player.adplayer.manager.EntitlementManager#isPreauthorized(com.hotstar.player.adplayer.core.VideoItem)
     * @see com.hotstar.player.adplayer.manager.EntitlementManager#setCurrentResources(java.util.ArrayList)
     */
    public boolean isPreauthorized(String resourceId)
    {
        return false;
    }

    /**
     * Starts the authorization workflow. Must be called every time prior to playback of content protected by
     * Primetime PayTV Pass service. If the user is not authenticated, calling this method automatically starts
     * the authentication workflow.
     * <p>
     *     Triggers callbacks:
     *     <ul>
     *         <li>{@link com.hotstar.player.adplayer.manager.EntitlementManager.EntitlementManagerListener#onAuthorizationSuccess(String, com.hotstar.player.adplayer.core.VideoItem)} - on a successful authorization</li>
     *         <li>{@link com.hotstar.player.adplayer.manager.EntitlementManager.EntitlementManagerListener#onDisplayNotificationDialog(com.hotstar.player.adplayer.manager.EntitlementManager.Status, String)} - on a failed authorization</li>
     *     </ul>
     * </p>
     *
     * @param videoItem the VideoItem to be authorized.
     * @param caller instance of the Activity calling this method, used to start provider selection and login activities
     *               on behalf of the calling activity.
     * @see EntitlementManager#getAuthentication(android.app.Activity)
     */
    public void getAuthorization(VideoItem videoItem, Activity caller)
    {
        // do nothing
    }

    /**
     * Sets the current resources available to the user. These resources are used to perform a pre-authorization check.
     * This method should be called every time the content available to the user changes. It is not necessary to
     * re-call this method if the user signs in/out as the list of current resources persists across
     * authentication state changes.
     *
     * @param videoItems the list of {@link com.hotstar.player.adplayer.core.VideoItem} objects available
     *                   to the user for playback.
     */
    public void setCurrentResources(ArrayList<VideoItem> videoItems)
    {
        // do nothing
    }

    /**
     * Logout from the Primetime PayTV Pass service.
     *
     * @param caller instance of the Activity calling this method, used to start logout activity
     *               on behalf of the calling activity.
     */
    public void logout(Activity caller)
    {
        // do nothing
    }

    /**
     * Registers an event listener to handle callbacks from this manager.
     * @param eventListener {@link com.hotstar.player.adplayer.manager.EntitlementManager.EntitlementManagerListener}
     * instance to handle callbacks from this manager.
     */
    public void addEventListener(EntitlementManagerListener eventListener)
    {
        // do nothing
    }

    /**
     * Deregisters an event listener from this manager.
     * @param eventListener {@link com.hotstar.player.adplayer.manager.EntitlementManager.EntitlementManagerListener}
     * instance to remove from the list of event listeners.
     */
    public void removeEventListener(EntitlementManagerListener eventListener)
    {
        // do nothing
    }

    /**
     * Handler for result returned from authentication activities for provider selection and login. This method
     * should be called within the activity's onActivityResult handler who called
     * {@link com.hotstar.player.adplayer.manager.EntitlementManager#getAuthentication(android.app.Activity)} or
     * {@link com.hotstar.player.adplayer.manager.EntitlementManager#getAuthorization(com.hotstar.player.adplayer.core.VideoItem, android.app.Activity)}
     *
     * @param requestCode Either {@link com.hotstar.player.adplayer.manager.EntitlementManager#MVPD_PICKER_ACTIVITY}
     *                    or {@link com.hotstar.player.adplayer.manager.EntitlementManager#MVPD_LOGIN_ACTIVITY}.
     * @param resultCode The integer result code returned by the authentication flow activity.
     * @param data The result data or {@code null} if the result has no data.
     *
     * @see com.hotstar.player.adplayer.ui.entitlement.MvpdPickerActivity
     * @see com.hotstar.player.adplayer.ui.entitlement.MvpdLoginActivity
     */
    public void onAuthenticationActivityResult(int requestCode, int resultCode, Intent data)
    {
        // do nothing
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    /**
     * Status messages used to communicate authentication and authorization state.
     */
    public enum Status {
        AUTHN_SUCCESS(R.string.entitlement_title_authn_success, R.string.entitlement_authn_success),
        AUTHN_PROVIDER_NOT_SELECTED(R.string.entitlement_title_authn_error, R.string.entitlement_provider_not_selected),
        AUTHN_NOT_AUTHENTICATED(R.string.entitlement_title_authn_error, R.string.entitlement_user_not_authenticated),
        AUTHN_GENERIC_ERROR(R.string.entitlement_title_authn_error, R.string.entitlement_authn_generic_error),
        AUTHN_LOGOUT(R.string.entitlement_title_logout, R.string.entitlement_logout_success),
        AUTHZ_SUCCESS(R.string.entitlement_title_authz_success, R.string.entitlement_authz_success),
        AUTHZ_NOT_AUTHORIZED(R.string.entitlement_title_authz_error, R.string.entitlement_user_not_authorized),
        AUTHZ_COMM_ERROR_TVS(R.string.entitlement_title_authz_error, R.string.entitlement_authz_tvs_error),
        AUTHZ_GENERIC_ERROR(R.string.entitlement_title_authz_error, R.string.entitlement_authz_generic_error),
        CONFIG_ERROR_GENERIC(R.string.entitlement_title_error, R.string.entitlement_config_error);

        private int title;
        private int message;

        private Status(int title, int message)
        {
            this.title = title;
            this.message = message;
        }

        /**
         * Title to be displayed in dialog windows.
         * @return the title to be displayed in dialog windows.
         */
        public int getTitle()
        {
            return title;
        }

        /**
         * Default text to be displayed in dialog windows.
         * @return default text to be displayed in dialog windows.
         */
        public int getMessage()
        {
            return message;
        }
    }

    public interface EntitlementManagerListener
    {
        /**
         * Signals start of background activity. A progress indicator should be displayed to the user.
         */
        public void onProgressStart();

        /**
         * Signals end of background activity. Any displayed progress indicator should be hidden.
         */
        public void onProgressEnd();

        /**
         * Signals that a notification dialog should be displayed to the user.  The notification dialog is used
         * to communicate authentication and authorization status to the user. If no {@code message} is provided,
         * the default string from {@link com.hotstar.player.adplayer.manager.EntitlementManager.Status#getMessage()}
         * should be used.
         *
         * @param status the status code of the notification
         * @param message the message to include in the notification
         *
         * @see com.hotstar.player.adplayer.ui.entitlement.EntitlementDialogFragment
         */
        public void onDisplayNotificationDialog(Status status, String message);

        /**
         * Signals a change in authentication status. Intended to be used to change state of icons or text
         * which display user's authentication state.
         *
         * @param isAuthN true if the user is authenticated, false otherwise
         */
        public void onAuthenticationStatusChange(boolean isAuthN);

        /**
         * Signals the authorization call was successful and the given VideoItem should be played.
         * If an error occurs during authorization,
         * {@link com.hotstar.player.adplayer.manager.EntitlementManager.EntitlementManagerListener#onDisplayNotificationDialog(com.hotstar.player.adplayer.manager.EntitlementManager.Status, String)}
         * is called instead to display an error message to the user.
         *
         * @param resourceId the resource requested for authorization.
         * @param videoItem VideoItem object from the {@link com.hotstar.player.adplayer.manager.EntitlementManager#getAuthorization(com.hotstar.player.adplayer.core.VideoItem, android.app.Activity)} call.
         */
        public void onAuthorizationSuccess(String resourceId, VideoItem videoItem);

        /**
         * Signals the list of pre-authorized resources has updated. This method is called at some point after
         * {@link com.hotstar.player.adplayer.manager.EntitlementManager#setCurrentResources(java.util.ArrayList)}
         * is called.
         */
        public void onPreauthorizedResourcesUpdate();
    }


    /**
     * WebView client used for login and logout activities with content provider (MVPD).
     */
    public static class ProviderWebView extends WebViewClient
    {
        public static final long DEFAULT_TIMEOUT = 10000L;

        private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "ProviderWebView";

        private ProviderWebViewListener callback;
        private CountDownTimer timer;
        private boolean timerStarted;

        /**
         * Create a new WebView object for use with Entitlement login and logout.
         * @param listener handler called when WebView finishes.
         */
        public ProviderWebView(ProviderWebViewListener listener)
        {
            this(listener, 0L);
        }

        /**
         * Create a new WebView object with a hangup timer for use with Entitlement login and logout.
         * @param listener handler called when Webview finishes.
         * @param timeout The number in milliseconds in the future from when the first URL loads in this WebView
         *                until the countdown timer is done and calls {@link com.hotstar.player.adplayer.manager.EntitlementManager.ProviderWebView.ProviderWebViewListener#onFinish()}.
         */
        public ProviderWebView(ProviderWebViewListener listener, long timeout)
        {
            this.callback = listener;

            if (timeout > 0L)
            {
                this.timer = new CountDownTimer(timeout, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // do nothing
                    }

                    @Override
                    public void onFinish() {
                        AdVideoApplication.logger.w(LOG_TAG + ".CountDownTimer#onFinish", "Timeout reached.");
                        ProviderWebView.this.callback.onFinish();
                    }
                };
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
            AdVideoApplication.logger.d(LOG_TAG + "#shouldOverrideUrlLoading", "Loading URL: " + url);

            // if we detect a redirect to our application URL, this is an indication
            // that the authN workflow was completed successfully
            String decodedUrl = null;
            try
            {
                decodedUrl = URLDecoder.decode(AccessEnabler.ADOBEPASS_REDIRECT_URL, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                AdVideoApplication.logger.e(LOG_TAG + "#shouldOverrideUrlLoading",
                        "Error decoding URL. Cancelling WebView. " + e.getMessage());
            }

            // If the URL is redirecting back to Primetime PayTV Pass service or if there was an error, finish the WebView
            if (decodedUrl == null || url.equals(decodedUrl)) {
                // stop timer
                if (timer != null)
                {
                    timer.cancel();
                }

                // the authentication workflow is now complete
                callback.onFinish();
            }

            return false;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            AdVideoApplication.logger.d(LOG_TAG + "#onReceivedSslError", "Ignoring SSL certificate error.");
            handler.proceed();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            AdVideoApplication.logger.d(LOG_TAG + "#onReceivedError", description);
            AdVideoApplication.logger.d(LOG_TAG + "#onReceivedError", failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            AdVideoApplication.logger.d(LOG_TAG + "#onPageStarted", "Page started: " + url);
            super.onPageStarted(view, url, favicon);

            // when the first page loads, start the timer
            if (timer != null && !timerStarted)
            {
                timer.start();
                timerStarted = true;
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            AdVideoApplication.logger.d(LOG_TAG + "#onPageFinished", "Page loaded: " + url);
            super.onPageFinished(view, url);
        }

        // Callback to signal that the Provider's site has finished
        public interface ProviderWebViewListener
        {
            public void onFinish();
        }
    }


}
