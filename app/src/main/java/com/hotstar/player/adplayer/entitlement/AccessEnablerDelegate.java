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

package com.hotstar.player.adplayer.entitlement;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.adobe.adobepass.accessenabler.api.IAccessEnablerDelegate;
import com.adobe.adobepass.accessenabler.models.Event;
import com.adobe.adobepass.accessenabler.models.MetadataKey;
import com.adobe.adobepass.accessenabler.models.MetadataStatus;
import com.adobe.adobepass.accessenabler.models.Mvpd;
import com.adobe.adobepass.accessenabler.utils.Utils;
import com.hotstar.player.adplayer.AdVideoApplication;

import java.io.IOException;
import java.util.ArrayList;

public class AccessEnablerDelegate implements IAccessEnablerDelegate
{
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME
            + AccessEnablerDelegate.class.getSimpleName();

    private Handler handler;

    public final int SET_REQUESTOR_COMPLETE = 0;
    public final int SET_AUTHN_STATUS = 1;
    public final int SET_TOKEN = 2;
    public final int TOKEN_REQUEST_FAILED = 3;
    public final int SELECTED_PROVIDER = 4;
    public final int DISPLAY_PROVIDER_DIALOG = 5;
    public final int NAVIGATE_TO_URL = 6;
    public final int SEND_TRACKING_DATA = 7;
    public final int SET_METADATA_STATUS = 8;
    public final int PREAUTHORIZED_RESOURCES = 9;

    public AccessEnablerDelegate(Handler handler) {
        this.handler = handler;
    }

    public static final String BUNDLED_OP_CODE = "op_code";
    public static final String BUNDLED_MESSAGE = "message";

    private Bundle createMessagePayload(int opCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLED_OP_CODE, opCode);
        if (message != null) {
            bundle.putString(BUNDLED_MESSAGE, message);
        }

        return bundle;
    }

    public static final String BUNDLED_REQUESTOR_STATUS = "status";

    @Override
    public void setRequestorComplete(int status) {
        String message = "setRequestorComplete(" + status + ")";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SET_REQUESTOR_COMPLETE, message);
        bundle.putInt(BUNDLED_REQUESTOR_STATUS, status);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static final String BUNDLED_AUTHN_STATUS = "status";
    public static final String BUNDLED_AUTHN_ERROR_CODE = "err_code";

    @Override
    public void setAuthenticationStatus(int status, String errorCode) {
        String message = "setAuthenticationStatus(" + status + ", " + errorCode + ")";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SET_AUTHN_STATUS, message);
        bundle.putInt(BUNDLED_AUTHN_STATUS, status);
        bundle.putString(BUNDLED_AUTHN_ERROR_CODE, errorCode) ;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static final String BUNDLED_AUTHZ_TOKEN = "token";
    public static final String BUNDLED_AUTHZ_RESOURCE_ID = "resource_id";
    public static final String BUNDLED_AUTHZ_ERROR_CODE = "err_code";
    public static final String BUNDLED_AUTHZ_ERROR_DESCRIPTION = "err_description";

    @Override
    public void setToken(String token, String resourceId) {
        String message = "setToken(" + token + ", " + resourceId + ")";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SET_TOKEN, message);
        bundle.putString(BUNDLED_AUTHZ_RESOURCE_ID, resourceId) ;
        bundle.putString(BUNDLED_AUTHZ_TOKEN, token) ;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void tokenRequestFailed(String resourceId, String errorCode, String errorDescription) {
        String message = "tokenRequestFailed(" + resourceId + ", " + errorCode + ", " + errorDescription + ")";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(TOKEN_REQUEST_FAILED, message);
        bundle.putString(BUNDLED_AUTHZ_RESOURCE_ID, resourceId) ;
        bundle.putString(BUNDLED_AUTHZ_ERROR_CODE, errorCode) ;
        bundle.putString(BUNDLED_AUTHZ_ERROR_DESCRIPTION, errorDescription) ;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static final String BUNDLED_SELECTED_PROVIDER = "selected_mvpd";

    @Override
    public void selectedProvider(Mvpd mvpd) {
        String message;
        if (mvpd != null) {
            message = "selectedProvider(" + mvpd.getId() + ")";
        } else {
            message = "selectedProvider(null)";
        }
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SELECTED_PROVIDER, message);
        bundle.putSerializable(BUNDLED_SELECTED_PROVIDER, mvpd);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static final String BUNDLED_MVPD_DATA = "mvpd_data";

    @Override
    public void displayProviderDialog(ArrayList<Mvpd> mvpds) {
        String message = "displayProviderDialog(" + mvpds.size() + " mvpds)";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(DISPLAY_PROVIDER_DIALOG, message);

        // serialize the MVPD objects
        ArrayList<String> serializedData = new ArrayList<String>();
        for (Mvpd mvpd : mvpds) {
            try {
                serializedData.add(mvpd.serialize());
            } catch (IOException e) {
                AdVideoApplication.logger.e(LOG_TAG, e.toString());
            }
        }

        bundle.putStringArrayList(BUNDLED_MVPD_DATA, serializedData);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static final String BUNDLED_PREAUTHORIZED_RESOURCES = "resources";

    @Override
    public void preauthorizedResources(ArrayList<String> resources) {
        String message = "preauthorizedResources(" + Utils.joinStrings(resources, ", ") + ")";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(PREAUTHORIZED_RESOURCES, message);
        bundle.putStringArrayList(BUNDLED_PREAUTHORIZED_RESOURCES, resources);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static final String BUNDLED_URL = "url";

    @Override
    public void navigateToUrl(String url) {
        String message = "navigateToUrl(" + url + ")";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(NAVIGATE_TO_URL, message);
        bundle.putString(BUNDLED_URL, url);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static final String BUNDLED_TRACKING_EVENT_TYPE = "event_type";
    public static final String BUNDLED_TRACKING_EVENT_DATA = "event_data";

    @Override
    public void sendTrackingData(Event event, ArrayList<String> data) {
        String message = "sendTrackingData(" + Utils.joinStrings(data, "|") + ", " + event.getType() + ")";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SEND_TRACKING_DATA, message);
        bundle.putInt(BUNDLED_TRACKING_EVENT_TYPE, event.getType());
        bundle.putStringArrayList(BUNDLED_TRACKING_EVENT_DATA, data);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public static final String BUNDLED_METADATA_KEY = "key";
    public static final String BUNDLED_METADATA_RESULT = "result";

    @Override
    public void setMetadataStatus(MetadataKey key, MetadataStatus result) {
        String message = "setMetadataStatus(" + key.getKey() + ", " + result + ")";
        AdVideoApplication.logger.i(LOG_TAG, message);

        // signal the fact that the AccessEnabler work is done
        Message msg = handler.obtainMessage();
        Bundle bundle = createMessagePayload(SET_METADATA_STATUS, message);
        bundle.putSerializable(BUNDLED_METADATA_KEY, key);
        bundle.putSerializable(BUNDLED_METADATA_RESULT, result);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

}

