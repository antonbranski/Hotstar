package com.hotstar.player.webservice;

import com.hotstar.player.HotStarApplication;
import com.hotstar.player.adplayer.AdVideoApplication;

public class ConnectorUrlRequest {
    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + ConnectorUrlRequest.class.getSimpleName();

    public static String buildURL(String url) {
        String newURL = url;
        newURL += "/?";
        newURL += "userName=";
        newURL += HotStarApplication.getInstance().getUsername();
        newURL += "&location=Metro&deviceType=S5";

        AdVideoApplication.logger.i(LOG_TAG + "#buildURL", newURL);
        return newURL;
    }

    public static String buildURL(String url, String location, String deviceType) {
        String newURL = url;
        newURL += "/?";
        newURL += "userName=";
        newURL += HotStarApplication.getInstance().getUsername();
        newURL += "&location=";
        newURL += location;
        newURL += "&deviceType=";
        newURL += deviceType;

        AdVideoApplication.logger.i(LOG_TAG + "#buildURL", newURL);
        return newURL;
    }
}