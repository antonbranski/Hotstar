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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.api.AccessEnablerException;
import com.adobe.adobepass.accessenabler.models.Event;
import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.adobe.adobepass.accessenabler.utils.Utils;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.utils.StringUtils;
import com.adobe.mobile.Analytics;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.entitlement.AccessEnablerDelegate;
import com.hotstar.player.adplayer.entitlement.EntitlementMetadata;
import com.hotstar.player.adplayer.core.VideoItem;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntitlementManagerOn extends EntitlementManager
{

    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "EntitlementManagerOn";

    // handles messages from AccessEnablerDelegate
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            if (bundle != null)
            {
                int opCode = bundle.getInt(AccessEnablerDelegate.BUNDLED_OP_CODE);
                messageHandlers[opCode].handle(bundle);
            }
        }
    };

    private final AccessEnabler accessEnabler;

    // listeners to handle callbacks from this manager
    private final ArrayList<EntitlementManagerListener> eventListeners = new ArrayList<EntitlementManagerListener>();

    // map of VideoItem to resource id during authorization calls
    // maintains only one item at a time.
    private final Map<String, VideoItem> videoItemMap = new HashMap<String, VideoItem>(2, 0.75f);

    // list of current resources. passed to AccessEnabler.checkPreauthorizedResources once user is authenticated
    private final Set<String> currentResources = new HashSet<String>();

    // list of preauthorized resources. empty if user is not authenticated.
    private final ArrayList<String> preauthorizedResources = new ArrayList<String>();

    // currently selected provider, may be null if user is not authenticated
    private Mvpd selectedProvider;

    // Calling activity for get authentication and get authorization requests
    private Activity callingActivity;

    // flag if we do not want to display authentication dialogs to the user
    // this is true if checkAuthentication is called as it is not from a user request
    // and if authentication was triggered by getAuthorization
    private boolean hideAuthNDialog;

    // flag to wait for setSelectedProvider callback when user is first authenticated
    // used to retrieve MVPD object so logo is displayed on authentication success
    private boolean waitForSelectedProvider;

    // flag if authentication flow is currently in progress.
    // block all calls to access enabler if authentication is pending.
    private boolean isAuthenticationInProgress;

    // flag if user is authenticated.
    private boolean isAuthenticated;

    // flag if call to setRequestor was made.
    private boolean setRequestorCalled;
    // flag if call to setRequestor was successful.
    private boolean setRequestorSuccessful;

    public EntitlementManagerOn()
    {

        if (EntitlementManager.getAccessEnabler() == null)
        {
            // AccessEnabler library failed to load, disable Primetime PayTV Pass Integration
            accessEnabler = null;
            AdVideoApplication.logger.e(LOG_TAG, "AccessEnabler library failed to load. Primetime PayTV Pass features disabled.");
        }
        else
        {
            // set AccessEnabler delegate
            accessEnabler = EntitlementManager.getAccessEnabler();
            accessEnabler.setDelegate(new AccessEnablerDelegate(handler));

            // establish the identity of the Programmer
            setRequestor(true);
        }
    }


    /**
     * Establish the identity of the Programmer within the Primetime PayTV Pass system. This should be performed
     * only once during the application's lifecycle.
     * @param useUI if true, UI dialogs are displayed to the user. if false, the method call occurs in the background
     *              without and UI dialogs.
     */
    public void setRequestor(boolean useUI)
    {
        // Programmer's requestor id
        String requestorId = EntitlementManager.REQUESTOR_ID;

        if (!StringUtils.isEmpty(requestorId))
        {
            // array of Primetime PayTV Pass endpoints
            ArrayList<String> spUrls = new ArrayList<String>();
            spUrls.add(EntitlementManager.PAYTV_PASS_URI);

            try
            {
                /* Signing of the requestor id is included here for demonstration purposes only.
                Do not sign your requestor id locally within the application.
                Instead, call to a trusted network server to sign the requestor id.
                */
                String signedRequestorId = EntitlementManager.getSignatureGenerator().generateSignature(requestorId);
                AdVideoApplication.logger.d(LOG_TAG + "#setRequestor", "Signed Requestor ID: " + signedRequestorId);
                accessEnabler.setRequestor(requestorId, signedRequestorId, spUrls);
                setRequestorCalled = true;

                if (useUI)
                {
                    for (EntitlementManagerListener listener : eventListeners)
                    {
                        listener.onProgressStart();
                    }
                }
            }
            catch (AccessEnablerException e)
            {
                AdVideoApplication.logger.e(LOG_TAG + "#setRequestor", "Failed to digitally sign the requestor id. " + e.getMessage());
                setRequestorSuccessful = false;

                if (useUI)
                {
                    for (EntitlementManagerListener listener : eventListeners)
                    {
                        listener.onProgressEnd();
                        listener.onDisplayNotificationDialog(Status.CONFIG_ERROR_GENERIC, "");
                    }
                }
            }
        }
        else
        {
            AdVideoApplication.logger.w(LOG_TAG + "#setRequestor", "Requestor ID cannot be empty.");
        }
    }

    /**
     * Check if the call to setRequestor was made and was successful. If setRequestor was not called, it will be
     * called and {@code true} is returned, as the AccessEnabler will queue the request and execute it after a
     * successful setRequestor call.  If setRequestor was called but failed, the application is instructed
     * to display an error message to the user.
     * <p>
     *     This method is intended to be called before any user initiated AccessEnabler calls are performed, such
     *     as getAuthentication or checkAuthorization.
     * </p>
     *
     * @return {@code false} if the call to setRequestor failed, {@code true} if setRequestor succeeded or was not called.
     */
    private boolean isRequestorSuccess()
    {
        if (!setRequestorCalled)
        {
            setRequestor(true);
            return true;
        }
        else if (!setRequestorSuccessful)
        {
            // if call was made but failed, display error to user
            for (EntitlementManagerListener listener : eventListeners)
            {
                listener.onDisplayNotificationDialog(Status.CONFIG_ERROR_GENERIC, "");
            }

            return false;
        }

        return true;
    }

    @Override
    public void getAuthentication(Activity caller)
    {
        // ignore call if authentication is in progress
        if (isRequestorSuccess())
        {
            if (!isAuthenticationInProgress)
            {
                callingActivity = caller;

                // set flag to block other calls while authentication flow is in progress
                isAuthenticationInProgress = true;
                accessEnabler.getAuthentication();
            }
            else
            {
                AdVideoApplication.logger.d(LOG_TAG + "#getAuthenticaiton", "Authentication flow in progress. Call ignored.");
            }
        }
    }

    @Override
    public void checkAuthentication()
    {
        // ignore call if authentication is in progress
        if (!isAuthenticationInProgress)
        {
            hideAuthNDialog = true;
            accessEnabler.checkAuthentication();
        }
        else
        {
            AdVideoApplication.logger.d(LOG_TAG + "#checkAuthentication", "Authentication flow in progress. Call ignored.");
        }
    }

    @Override
    public boolean isAuthenticated()
    {
        return isAuthenticated;
    }

    @Override
    public void setSelectedProvider(String mvpdId)
    {
        accessEnabler.setSelectedProvider(mvpdId);
    }

    @Override
    public Mvpd getSelectedProvider()
    {
        return selectedProvider;
    }

    @Override
    public void getAuthenticationToken()
    {
        accessEnabler.getAuthenticationToken();
    }

    @Override
    public boolean requiresAuthorization(VideoItem videoItem)
    {
        EntitlementMetadata metadata = getEntitlementMetadata(videoItem);
        return metadata != null && metadata.hasResourceId();
    }

    @Override
    public boolean isAuthorizationPending()
    {
        return !videoItemMap.isEmpty();
    }

    @Override
    public boolean isPreauthorized(VideoItem videoItem)
    {
        EntitlementMetadata metadata = getEntitlementMetadata(videoItem);
        return metadata != null && metadata.hasResourceId() && isPreauthorized(metadata.getChannelTitle());
    }

    @Override
    public boolean isPreauthorized(String resourceId)
    {
        return resourceId != null && preauthorizedResources.contains(resourceId);
    }

    @Override
    public void getAuthorization(VideoItem videoItem, Activity caller)
    {
        if (isRequestorSuccess())
        {
            if (!isAuthenticationInProgress)
            {
                EntitlementMetadata metadata = getEntitlementMetadata(videoItem);
                if (metadata == null || !metadata.hasResourceId())
                {
                    throw new IllegalArgumentException("Entitlement resource ID is empty.");
                }

                String resourceId = metadata.getResourceId();

                if (videoItemMap.isEmpty())
                {
                    // add video item to map so we can retrieve it on successful authorization
                    videoItemMap.put(resourceId, videoItem);

                    callingActivity = caller;
                    // using checkAuthorization over getAuthorization is preferred
                    accessEnabler.checkAuthorization(resourceId);
                }
                else
                {
                    // if an authorization request is currently pending, ignore this request
                    AdVideoApplication.logger.w(LOG_TAG, "Authorization request is pending. Request ignored for " + resourceId);
                }
            }
            else
            {
                AdVideoApplication.logger.d(LOG_TAG + "#getAuthorization", "Authentication flow in progress. Call ignored.");
            }
        }
    }

    @Override
    public void setCurrentResources(ArrayList<VideoItem> videoItems)
    {
        currentResources.clear();

        for (VideoItem item : videoItems)
        {
            EntitlementMetadata metadata = getEntitlementMetadata(item);
            if (metadata != null && metadata.hasResourceId())
            {
                // pre-authorization is a channel level check
                // so use channel title instead of entire resource id
                currentResources.add(metadata.getChannelTitle());
            }
        }

        preauthorizedResources.clear();
        if (!currentResources.isEmpty() && isAuthenticated())
        {
            ArrayList<String> list = new ArrayList<String>(currentResources);
            accessEnabler.checkPreauthorizedResources(list);
        }
        else
        {
            // pre-authorized resource list cleared, but we are not calling AccessEnabler, so update listeners of change
            for (EntitlementManagerListener listener : eventListeners)
            {
                listener.onPreauthorizedResourcesUpdate();
            }
        }
    }

    /**
     * Extracts the resource id from the metadata of the given {@link com.hotstar.player.adplayer.core.VideoItem}.
     *
     * @param videoItem the VideoItem from which to retrieve an entitlement resource id
     * @return the resource id from the given VideoItem, or {@code null} if no resource id was found.
     */
    private static EntitlementMetadata getEntitlementMetadata(VideoItem videoItem)
    {
        Metadata metadata = videoItem.getAdvertisingMetadata();
        if (metadata != null
                && metadata instanceof MetadataNode
                && ((MetadataNode)metadata).containsNode(EntitlementMetadata.ENTITLEMENT_METADATA))
        {
            return (EntitlementMetadata)((MetadataNode)metadata).getNode(EntitlementMetadata.ENTITLEMENT_METADATA);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void logout(Activity caller)
    {
        if (!isAuthenticationInProgress)
        {
            callingActivity = caller;
            accessEnabler.logout();
        }
        else
        {
            AdVideoApplication.logger.d(LOG_TAG + "#logout", "Authentication flow in progress. Call ignored.");
        }
    }

    @Override
    public boolean isEnabled()
    {
        return accessEnabler != null;
    }

    @Override
    public void addEventListener(EntitlementManagerListener eventListener)
    {
        eventListeners.add(eventListener);
    }

    @Override
    public void removeEventListener(EntitlementManagerListener eventListener)
    {
        eventListeners.remove(eventListener);
    }

    @Override
    public void onAuthenticationActivityResult(int requestCode, int resultCode, Intent data)
    {
        String logTag = LOG_TAG + "#onAuthenticationActivityResult";

        /*
        switch (requestCode)
        {
            case (MVPD_PICKER_ACTIVITY):

                switch (resultCode)
                {
                    case (Activity.RESULT_OK):
                        String mvpdId = data.getStringExtra(MvpdPickerActivity.EXTRA_MVPD);

                        if (mvpdId != null)
                        {
                            AdVideoApplication.logger.d(logTag, "Selected: " + mvpdId);

                            // user has selected an MVPD: call setSelectedProvider()
                            accessEnabler.setSelectedProvider(mvpdId);
                        }
                        else
                        {
                            AdVideoApplication.logger.w(logTag, "Selected MVPD is null. Cancel authentication flow.");
                            accessEnabler.setSelectedProvider(null);
                        }
                    break;

                    case (Activity.RESULT_CANCELED):
                        AdVideoApplication.logger.d(logTag, "Selection canceled by user.");

                        // abort the authN flow.
                        accessEnabler.setSelectedProvider(null);
                    break;

                    default:
                        AdVideoApplication.logger.w(logTag, "Unrecognized result code for MVPD Picker Activity result.");
                        accessEnabler.setSelectedProvider(null);

                }
            break;

            case (MVPD_LOGIN_ACTIVITY):
                switch (resultCode)
                {
                    case (Activity.RESULT_OK):
                        // retrieve the authentication token
                        accessEnabler.getAuthenticationToken();
                    break;

                    case (Activity.RESULT_CANCELED):
                        AdVideoApplication.logger.d(logTag, "Login canceled by user.");

                        // abort the authN flow.
                        accessEnabler.setSelectedProvider(null);
                    break;

                    default:
                        AdVideoApplication.logger.w(logTag, "Unrecognized result code for MVPD Login Activity result.");
                        accessEnabler.setSelectedProvider(null);

                }
            break;

            default:
                AdVideoApplication.logger.w(logTag, "Unrecognized request code '" + requestCode + "'");
        }
        */
    }

    public interface MessageHandler {
        void handle(Bundle bundle);
    }

    // Array of callbacks for android.os.Handler#handleMessage which process messages from the AccessEnablerDelegate
    // The index in the array matches the optCode defined in AccessEnablerDelegate
    private MessageHandler[] messageHandlers = new MessageHandler[] {
            new MessageHandler() { public void handle(Bundle bundle) { handleSetRequestor(bundle); } },             //  0 SET_REQUESTOR_COMPLETE
            new MessageHandler() { public void handle(Bundle bundle) { handleSetAuthnStatus(bundle); } },           //  1 SET_AUTHN_STATUS
            new MessageHandler() { public void handle(Bundle bundle) { handleSetToken(bundle); } },                 //  2 SET_TOKEN
            new MessageHandler() { public void handle(Bundle bundle) { handleSetTokenRequestFailed(bundle); } },    //  3 TOKEN_REQUEST_FAILED
            new MessageHandler() { public void handle(Bundle bundle) { handleSelectedProvider(bundle); } },         //  4 SELECTED_PROVIDER
            new MessageHandler() { public void handle(Bundle bundle) { handleDisplayProviderDialog(bundle); } },    //  5 DISPLAY_PROVIDER_DIALOG
            new MessageHandler() { public void handle(Bundle bundle) { handleNavigateToUrl(bundle); } },            //  6 NAVIGATE_TO_URL
            new MessageHandler() { public void handle(Bundle bundle) { handleSendTrackingData(bundle); } },         //  7 SEND_TRACKING_DATA
            new MessageHandler() { public void handle(Bundle bundle) { handleSetMetadataStatus(bundle); } },        //  8 SET_METADATA_STATUS
            new MessageHandler() { public void handle(Bundle bundle) { handlePreauthorizedResources(bundle); } },   //  9 PREAUTHORIZED_RESOURCES
    };

    private void handleMessage(Bundle bundle)
    {
        String message = bundle.getString(AccessEnablerDelegate.BUNDLED_MESSAGE);
        AdVideoApplication.logger.d(LOG_TAG, message);
    }

    private void handleSetAuthnStatus(Bundle bundle) {
        handleMessage(bundle);

        // clear is authentication in progress flag
        isAuthenticationInProgress = false;

        // extract the status code
        int status = bundle.getInt(AccessEnablerDelegate.BUNDLED_AUTHN_STATUS);
        String errCode = bundle.getString(AccessEnablerDelegate.BUNDLED_AUTHN_ERROR_CODE);

        // update authentication flag
        isAuthenticated = (status == AccessEnabler.ACCESS_ENABLER_STATUS_SUCCESS);

        if (isAuthenticated)
        {
            // if a video item is in the queue, authentication flow initiated from authorization request
            // continue authorization flow
            if (!videoItemMap.isEmpty())
            {
                // only one item in the map is expected
                String resourceId = videoItemMap.keySet().iterator().next();
                accessEnabler.checkAuthorization(resourceId);
                hideAuthNDialog = true;
            }

            // if we are supposed to display a success dialog, wait for MVPD object to display logo
            if (selectedProvider == null && !hideAuthNDialog)
            {
                waitForSelectedProvider = true;
                hideAuthNDialog = true;
            }
            // get and store selected provider
            accessEnabler.getSelectedProvider();

            // update pre-authorized list, but only if we need to
            if (!currentResources.isEmpty() && preauthorizedResources.isEmpty())
            {
                accessEnabler.checkPreauthorizedResources(new ArrayList<String>(currentResources));
            }

        }
        else
        {
            // not authorized, so clear flags and lists
            selectedProvider = null;
            videoItemMap.clear();
            preauthorizedResources.clear();

            // update listeners that there are no pre-authorized resources
            for (EntitlementManagerListener listener : eventListeners)
            {
                listener.onPreauthorizedResourcesUpdate();
            }
        }

        // update status change listeners
        for (EntitlementManagerListener listener : eventListeners)
        {
            listener.onAuthenticationStatusChange(status == AccessEnabler.ACCESS_ENABLER_STATUS_SUCCESS);
        }

        if (!hideAuthNDialog)
        {
            Status resultCode = Status.AUTHN_GENERIC_ERROR;

            switch (status)
            {
                case (AccessEnabler.ACCESS_ENABLER_STATUS_SUCCESS):

                    resultCode = Status.AUTHN_SUCCESS;


                case (AccessEnabler.ACCESS_ENABLER_STATUS_ERROR):

                    if (AccessEnabler.PROVIDER_NOT_SELECTED_ERROR.equals(errCode))
                    {
                        // provider not selected error triggered by user cancelling authentication flow
                        // no need to display dialog to user.
                        break;
                    }
                    else if (AccessEnabler.USER_NOT_AUTHENTICATED_ERROR.equals((errCode)))
                    {
                        resultCode = Status.AUTHN_NOT_AUTHENTICATED;
                    }

                    for (EntitlementManagerListener listener : eventListeners)
                    {
                        listener.onDisplayNotificationDialog(resultCode, errCode);
                    }
                    break;

                default:
                    AdVideoApplication.logger.e(LOG_TAG + "#handleSetAuthnStatus", "Unknown status code: " + status);
                    break;
            }
        }
        else
        {
            hideAuthNDialog = false;
        }
    }

    private void handleSetRequestor(Bundle bundle) {
        handleMessage(bundle);

        // dismiss progress dialog
        for (EntitlementManagerListener listener : eventListeners)
        {
            listener.onProgressEnd();
        }

        int status = bundle.getInt(AccessEnablerDelegate.BUNDLED_REQUESTOR_STATUS);

        switch (status)
        {
            case AccessEnabler.ACCESS_ENABLER_STATUS_SUCCESS:
                setRequestorSuccessful = true;
                break;

            case AccessEnabler.ACCESS_ENABLER_STATUS_ERROR:
                setRequestorSuccessful = false;

                AdVideoApplication.logger.e(LOG_TAG + "#handleSetRequestor", "Unable to connect to requestor!");

                // display a dialog to the user that the configuration failed
                for (EntitlementManagerListener listener : eventListeners)
                {
                    listener.onDisplayNotificationDialog(Status.CONFIG_ERROR_GENERIC, "");
                }

                break;

            default:
                AdVideoApplication.logger.e(LOG_TAG + "#handleSetRequestor", "Unknown status code: " + status);
                break;
        }
    }

    private void handleSetToken(Bundle bundle) {
        handleMessage(bundle);

        String resourceId = bundle.getString(AccessEnablerDelegate.BUNDLED_AUTHZ_RESOURCE_ID);
        String token = bundle.getString(AccessEnablerDelegate.BUNDLED_AUTHZ_TOKEN);

        String error = "";
        Status code;

        VideoItem videoItem = videoItemMap.remove(resourceId);
        if (videoItem == null)
        {
            // shouldn't be in this state, but check for it anyway
            AdVideoApplication.logger.w(LOG_TAG + "#handleSetToken", "Authorization successful but null video item returned.");
            code = Status.AUTHZ_GENERIC_ERROR;
        }
        else if (token == null || token.trim().length() == 0)
        {
            // verify token is not null
            AdVideoApplication.logger.w(LOG_TAG + "#handleSetToken", "Authorization token is null or empty!");
            code = Status.AUTHZ_GENERIC_ERROR;
        }
        else
        {
            try
            {
                // display loading dialog
                for (EntitlementManagerListener listener : eventListeners)
                {
                    listener.onProgressStart();
                }

                // call TVS to verify short media token
                error = new MediaTokenValidatorTask().execute(resourceId, token).get();

                if (MediaTokenValidatorTask.TVS_SUCCESS.equals(error))
                {
                    AdVideoApplication.logger.i(LOG_TAG + "#handleSetToken", "Authorization token successfully verified by TVS.");
                    code = Status.AUTHZ_SUCCESS;
                    error = "";
                }
                else
                {
                    AdVideoApplication.logger.w(LOG_TAG + "#handleSetToken", "Error occured while validating media token! " + error);
                    code = Status.AUTHZ_COMM_ERROR_TVS;
                    error = "";
                }
            }
            catch (Exception e)
            {
                AdVideoApplication.logger.e(LOG_TAG + "#handleSetToken", "Exception validating media token! " + e.getMessage());
                code = Status.AUTHZ_COMM_ERROR_TVS;
            }

            // hide loading dialog
            for (EntitlementManagerListener listener : eventListeners)
            {
                listener.onProgressEnd();
            }
        }

        switch (code)
        {
            case AUTHZ_SUCCESS:
                for (EntitlementManagerListener listener : eventListeners)
                {
                    // authorization successful
                    listener.onAuthorizationSuccess(resourceId, videoItem);
                }

                break;

            default:
                for (EntitlementManagerListener listener : eventListeners)
                {
                    // authorization failed
                    listener.onDisplayNotificationDialog(code, error);
                }
                break;
        }
    }

    private void handleSetTokenRequestFailed(Bundle bundle) {
        handleMessage(bundle);

        String resourceId = bundle.getString(AccessEnablerDelegate.BUNDLED_AUTHZ_RESOURCE_ID);
        String errorCode = bundle.getString(AccessEnablerDelegate.BUNDLED_AUTHZ_ERROR_CODE);
        String errorDescription = bundle.getString(AccessEnablerDelegate.BUNDLED_AUTHZ_ERROR_DESCRIPTION);

        VideoItem videoItem = videoItemMap.get(resourceId);
        if (AccessEnabler.USER_NOT_AUTHENTICATED_ERROR.equals(errorCode) && videoItem != null)
        {
            // if the user is not authenticated, start the authentication flow
            // keep the existing video item in the map as it is needed after a successful authentication
            AdVideoApplication.logger.d(LOG_TAG + "#handleSetTokenRequestFailed", "User is not authenticated. Starting authentication flow.");
            getAuthentication(callingActivity);
        }
        else
        {
            Status code;

            // display the error to the user
            // the below code can be simplified as the result is the same for most of the statements,
            // however it is here to demonstrate the possible values for the error code
            if (AccessEnabler.USER_NOT_AUTHORIZED_ERROR.equals(errorCode))
            {
                code = Status.AUTHZ_NOT_AUTHORIZED;
            }
            else if (AccessEnabler.GENERIC_AUTHORIZATION_ERROR.equals(errorCode))
            {
                code = Status.AUTHZ_GENERIC_ERROR;
            }
            else if (AccessEnabler.INTERNAL_AUTHORIZATION_ERROR.equals(errorCode))
            {
                code = Status.AUTHZ_GENERIC_ERROR;
            }
            else
            {
                code = Status.AUTHZ_GENERIC_ERROR;
            }

            // authorization failed for the given resource, remove item from list
            videoItemMap.remove(resourceId);
            for (EntitlementManagerListener listener : eventListeners)
            {
                listener.onDisplayNotificationDialog(code, errorDescription);
            }
        }
    }

    private void handleSelectedProvider(Bundle bundle) {
        handleMessage(bundle);
        selectedProvider = (Mvpd) bundle.getSerializable(AccessEnablerDelegate.BUNDLED_SELECTED_PROVIDER);

        // if an "authentication success" dialog wasn't displayed to the user from handleSetAuthNStatus
        // then display one now that we have the selected provider object
        if (waitForSelectedProvider)
        {
            waitForSelectedProvider = false;
            for (EntitlementManagerListener listener : eventListeners)
            {
                listener.onDisplayNotificationDialog(Status.AUTHN_SUCCESS, "");
            }

        }
    }

    private void handleDisplayProviderDialog(Bundle bundle) {
        handleMessage(bundle);

        /*
        if (callingActivity != null)
        {
            // start MvpdPickerActivity on behalf of calling activity
            Intent intent = new Intent(callingActivity, MvpdPickerActivity.class);
            intent.putExtra(AccessEnablerDelegate.BUNDLED_MVPD_DATA, bundle);
            callingActivity.startActivityForResult(intent, MVPD_PICKER_ACTIVITY);
        }
        else
        {
            AdVideoApplication.logger.e(LOG_TAG + "#handleDisplayProviderDialog", "Calling activity is null. Cannot start provider picker activity.");
        }
        */
    }

    private void handleNavigateToUrl(Bundle bundle) {
        handleMessage(bundle);

        String targetUrl = bundle.getString(AccessEnablerDelegate.BUNDLED_URL);

        /*
        if (targetUrl.indexOf(AccessEnabler.SP_URL_PATH_GET_AUTHENTICATION) > 0)
        {
            if (callingActivity != null)
            {
                // start MvpdLoginActivity on behalf of calling activity
                Intent intent = new Intent(callingActivity, MvpdLoginActivity.class);
                intent.putExtra(MvpdLoginActivity.EXTRA_TARGET_URL, targetUrl);
                callingActivity.startActivityForResult(intent, MVPD_LOGIN_ACTIVITY);
            }
            else
            {
                AdVideoApplication.logger.e(LOG_TAG + "#handleNavigateToUrl", "Calling activity is null. Cannot start login activity.");
            }

        }
        else if (targetUrl.indexOf(AccessEnabler.SP_URL_PATH_LOGOUT) > 0)
        {
            // display loading dialog as logout is performed in the background
            for (EntitlementManagerListener listener : eventListeners)
            {
                listener.onProgressStart();
            }

            // create hidden WebView to perform logout
            final WebView webView = new WebView(callingActivity);
            // enable JavaScript support
            WebSettings browserSettings = webView.getSettings();
            browserSettings.setJavaScriptEnabled(true);
            browserSettings.setJavaScriptCanOpenWindowsAutomatically(true);

            // setup WebView client (same as used for login but with different callback)
            EntitlementManager.ProviderWebView.ProviderWebViewListener webViewListener =
                    new EntitlementManager.ProviderWebView.ProviderWebViewListener() {
                        @Override
                        public void onFinish() {

                            for (EntitlementManagerListener listener : eventListeners)
                            {
                                // dismiss loading dialog
                                listener.onProgressEnd();
                                // display logout confirmation
                                listener.onDisplayNotificationDialog(Status.AUTHN_LOGOUT, "");
                            }

                            // destroy logout WebView
                            webView.destroy();

                        }
                    };

            // use a timeout to prevent hangs when communicating with MVPD's logout endpoint
            EntitlementManager.ProviderWebView webViewClient = new EntitlementManager.ProviderWebView(webViewListener, EntitlementManager.ProviderWebView.DEFAULT_TIMEOUT);
            webView.setWebViewClient(webViewClient);

            webView.loadUrl(targetUrl);

            // check authentication to trigger ui updates
            // technically, at this point the user is already logged out from the Adobe Primetime PayTV Pass service,
            // the web view is used to further logout the user from the selected MVPD.
            checkAuthentication();

        }
        */
    }

    private void handlePreauthorizedResources(Bundle bundle) {
        handleMessage(bundle);
        ArrayList<String> bundledResources = bundle.getStringArrayList(AccessEnablerDelegate.BUNDLED_PREAUTHORIZED_RESOURCES);

        if (bundledResources != null)
        {
            preauthorizedResources.clear();
            preauthorizedResources.addAll(bundledResources);

            for (EntitlementManagerListener listener : eventListeners)
            {
                listener.onPreauthorizedResourcesUpdate();
            }
        }
    }

    private void handleSendTrackingData(Bundle bundle) {
        handleMessage(bundle);

        // extract the event type and the event data
        int eventType = bundle.getInt(AccessEnablerDelegate.BUNDLED_TRACKING_EVENT_TYPE);
        ArrayList<String> data = bundle.getStringArrayList(AccessEnablerDelegate.BUNDLED_TRACKING_EVENT_DATA);

        if (data == null)
        {
            AdVideoApplication.logger.w(LOG_TAG + "#handleSendTrackingData", "Tracking data received from bundle is null.");
            return;
        }

        String eventName;
        int index = 0;

        Map<String, Object> contextData = new HashMap<String, Object>();

        switch (eventType)
        {
            case (Event.EVENT_MVPD_SELECTION):
                eventName = "a.media.pass.event.MvpdSelection";
                contextData.put("a.media.pass.MvpdId", data.get(index++));
            break;

            case (Event.EVENT_AUTHN_DETECTION):
                eventName = "a.media.pass.event.AuthenticationDetection";

                contextData.put("a.media.pass.Successful", data.get(index++));
                contextData.put("a.media.pass.MvpdId", data.get(index++));
                contextData.put("a.media.pass.Guid", data.get(index++));
                contextData.put("a.media.pass.Cached", data.get(index++));
            break;

            case (Event.EVENT_AUTHZ_DETECTION):
                eventName = "a.media.pass.event.AuthorizationDetection";

                contextData.put("a.media.pass.Successful", data.get(index++));
                contextData.put("a.media.pass.MvpdId", data.get(index++));
                contextData.put("a.media.pass.Guid", data.get(index++));
                contextData.put("a.media.pass.Cached", data.get(index++));
                contextData.put("a.media.pass.Error", data.get(index++));
                contextData.put("a.media.pass.ErrorDetails", data.get(index++));
            break;

            default:
                AdVideoApplication.logger.w(LOG_TAG + "#handleSendTrackingData", "Unknown tracking event.");
                return;
        }

        // don't track device type from Access Enabler. the Adobe Mobile Library tracks this metric automatically
        // contextData.put("a.media.pass.DeviceName", data.get(index++));
        index++;
        contextData.put("a.media.pass.ClientType", data.get(index));

        // don't track OS from Access Enabler. the Adobe Mobile Library tracks this metric automatically
        // contextData.put("a.media.pass.OSVersion", data.get(index));

        // send action data to Adobe Analytics
        Analytics.trackAction(eventName, contextData);

    }

    private void handleSetMetadataStatus(Bundle bundle) {
        handleMessage(bundle);
    }


    /**
     * Task to communicate with the Media Token Validator Service asynchronously over HTTP. This task is used after
     * a successful authorization call and a short-term media token is received.  The TVS takes the short-term media
     * token and the resource ID and returns either success or failure. If success, the token and resource ID is valid
     * access may be granted to the user to view the media.  If failure, the user may not have access to the media.
     *
     * <p>
     *     To use, call {@code String result = new MediaTokenValidatorTask().execute(resourceId, token).get()}
     *     The result will be one of:
     *     <ul>
     *         <li>{@link com.hotstar.player.adplayer.manager.EntitlementManagerOn.MediaTokenValidatorTask#TVS_SUCCESS} - valid token, user granted access</li>
     *         <li>{@link com.hotstar.player.adplayer.manager.EntitlementManagerOn.MediaTokenValidatorTask#TVS_ERROR} - an exception occurred, user not granted access</li>
     *         <li>any other error string - error message from TVS, user not granted access</li>
     *     </ul>
     * </p>
     */
    private static class MediaTokenValidatorTask extends AsyncTask<String, Void, String>
    {
        /* Successful response from TVS */
        public static final String TVS_SUCCESS = "tvs_success";
        /* Error occurred communicating with TVS */
        public static final String TVS_ERROR = "tvs_error";

        @Override
        protected String doInBackground(String... params) {
            try
            {
                String resource = params[0];
                String token = params[1];

                // build request
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(EntitlementManager.TVS_URL);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("mediaToken", Utils.base64Encode(token.getBytes())));
                nameValuePairs.add(new BasicNameValuePair("resource", Utils.base64Encode(resource.getBytes())));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // execute request
                HttpResponse response = httpclient.execute(httppost);

                // 200 status => token valid
                int code = response.getStatusLine().getStatusCode();
                if (code == HttpStatus.SC_OK)
                {
                    return TVS_SUCCESS;
                }

                // retrieve error message
                InputStream is = response.getEntity().getContent();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, length);
                }
                return new String(baos.toByteArray());
            }
            catch (Exception e)
            {
                AdVideoApplication.logger.e(LOG_TAG + ".MediaTokenValidatorTask#doInBackground", e.getMessage());
                return TVS_ERROR;
            }
        }
    }

}
