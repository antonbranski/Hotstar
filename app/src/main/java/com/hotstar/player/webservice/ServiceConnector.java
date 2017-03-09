package com.hotstar.player.webservice;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.adobe.ave.MediaErrorCode;
import com.adobe.mediacore.MediaPlayerItem;
import com.adobe.mediacore.MediaPlayerItemLoader;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.core.ContentRequest;
import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.adplayer.core.VideoItemParser;
import com.hotstar.player.adplayer.manager.DrmManager;

import java.util.ArrayList;

public class ServiceConnector {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + ServiceConnector.class.getSimpleName();

    private Activity mActivity = null;
    private ResponseListener mListener = null;
    private String contentURL = "";

    public ServiceConnector(Activity activity) {
        mActivity = activity;
    }

    public void doRequest(String contentUrl, ResponseListener listener) {
        mListener = listener;
        this.contentURL = contentUrl;

        ContentRequest contentRequest = new ContentRequest();
        contentRequest.addOnCompleteListener(new ContentRequest.OnCompleteListener()
        {
            @Override
            public void onComplete(String response)
            {
                try
                {
                    ArrayList<VideoItem> liveContentList = new ArrayList<VideoItem>();
                    ArrayList<VideoItem> vodContentList = new ArrayList<VideoItem>();
                    String vcmsType = AdVideoApplication.SETTINGS_VCMS_TYPE;

                    if (vcmsType != null && !vcmsType.isEmpty())
                    {
                        VideoItemParser videoItemParser = new VideoItemParser(response, vcmsType);
                        liveContentList.addAll(videoItemParser.getLiveContentList());
                        vodContentList.addAll(videoItemParser.getVodContentList());

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext());
                        if (sharedPreferences.getBoolean(DrmManager.SETTINGS_DRM_PRE_CACHE, false))
                        {
                            ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                            contentList.addAll(videoItemParser.getLiveContentList());
                            contentList.addAll(videoItemParser.getVodContentList());

                            /**
                             * This is a bad practice to preload DRM license for
                             * every single item. This is only an example of
                             * preloading DRM license. It is recommended to preload
                             * only the items you need to.
                             */
                            for (VideoItem item : contentList)
                            {
                                DrmManager.preLoadDrmLicenses(item.getUrl(), new MediaPlayerItemLoader.LoaderListener() {
                                    @Override
                                    public void onLoadComplete(MediaPlayerItem item) {
                                        AdVideoApplication.logger.w(LOG_TAG + "::DRMPreload#onLoadComplete", item.getResource().getUrl());
                                    }

                                    @Override
                                    public void onError(MediaErrorCode errorCode, String s) {
                                        AdVideoApplication.logger.e(LOG_TAG + "::DRMPreload#onError", s);
                                    }
                                }, mActivity.getApplicationContext());
                            }
                        }

                        // add content list to entitlement manager for preauthorization resource check
                        // ArrayList<VideoItem> contentList = new ArrayList<VideoItem>();
                        // contentList.addAll(vodContentList);
                        // contentList.addAll(liveContentList);
                        // entitlementManager.setCurrentResources(contentList);

                        if (mListener != null) {
                            mListener.onComplete(vodContentList, liveContentList);
                        }
                    }
                }
                catch (Exception e) {
                    AdVideoApplication.logger.e(LOG_TAG + "::ContentRequest.OnCompleteListener#onComplete",
                            "Error parsing the catalog content list(" + ServiceConnector.this.contentURL + "): " + e.getMessage());
                    // Toast.makeText(mActivity, "Invalid JSON format", Toast.LENGTH_SHORT).show();
                    if (mListener != null) {
                        mListener.onErrorOccurred("Invalid JSON format");
                    }
                }

                AdVideoApplication.logger.i(LOG_TAG + "::ContentRequest.OnCompleteListener#onComplete", "Catalog content successfully loaded.");
            }}
        );
        contentRequest.addOnErrorListener(new ContentRequest.OnErrorListener() {
            @Override
            public void onError(String error) {
                AdVideoApplication.logger.e(LOG_TAG + "::ContentRequest.OnErrorListener#onError",
                        "Error retrieving the catalog content list(" + ServiceConnector.this.contentURL  + "): " + error);
                if (mListener != null) {
                    mListener.onErrorOccurred(error);
                }
                // Toast.makeText(mActivity, "Failed to load catalog content.", Toast.LENGTH_SHORT).show();
            }
        });
        contentRequest.doRequest(contentUrl);
    }

    public interface ResponseListener {
        public void onComplete(ArrayList<VideoItem> vodContentList, ArrayList<VideoItem> liveContentList);
        public void onErrorOccurred(String errMessage);
    }
}