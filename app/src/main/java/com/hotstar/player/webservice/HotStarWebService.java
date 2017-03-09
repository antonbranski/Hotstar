package com.hotstar.player.webservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hotstar.player.HotStarApplication;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.events.BusProvider;
import com.hotstar.player.events.HotStarApiErrorEvent;
import com.hotstar.player.events.HotStarUserInfoFailureEvent;
import com.hotstar.player.events.HotStarUserInfoGotEvent;
import com.hotstar.player.model.HotStarUserInfo;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public class HotStarWebService {
    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + HotStarWebService.class.getSimpleName();
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    private static HotStarWebService instance = null;

    private HotStarApi mHotStarAPI;
    private Gson mGson;

    public static HotStarWebService getInstance() {
        if (instance == null)
            instance = new HotStarWebService();

        return instance;
    }

    public HotStarWebService()
    {
		/* setup BwfApi with Retrofit */
        mGson = new GsonBuilder()
                .setDateFormat(DATE_FORMAT)
                .create();

        mHotStarAPI = new RestAdapter.Builder()
                .setClient(new OkClient(OkHttpProvider.get().getClient()))
                .setEndpoint(Api.SERVER)
                .setConverter(new GsonConverter(mGson))
                .setLogLevel(RestAdapter.LogLevel.BASIC/*BwfApp.DEBUG ? RestAdapter.LogLevel.BASIC : RestAdapter.LogLevel.NONE*/)
                .build()
                .create(HotStarApi.class);

        BusProvider.get().register(this);
    }

    public Gson getGson()
    {
        return mGson;
    }

    public synchronized void getUserInfo(String username, String password)
    {
        final String acUsername = username;
        final String acPassword = password;
        new Thread(new Runnable() {
            @Override
            public void run() {
                AdVideoApplication.logger.i(LOG_TAG + "#getUserInfo", "GetUserInfo Thread is called");
                try {
                    HotStarUserInfo userInfo = mHotStarAPI.getUserInfo(acUsername, acPassword);
                    if (userInfo != null && userInfo.authStatus.equalsIgnoreCase("failed")) {
                        HotStarUserInfoFailureEvent event = new HotStarUserInfoFailureEvent("Authentication failed");
                        BusProvider.get().post(event);
                    } else {
                        HotStarUserInfoGotEvent event = new HotStarUserInfoGotEvent(userInfo);
                        BusProvider.get().post(event);
                    }

                } catch (RetrofitError e) {
                    e.printStackTrace();
                    HotStarApiErrorEvent event = new HotStarApiErrorEvent(ErrorType.HOTSTAR_SERVICE_API, e);
                    BusProvider.get().post(event);
                }
            }
        }).start();
    }


}