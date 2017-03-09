package com.hotstar.player.events;

import com.hotstar.player.adplayer.core.VideoItem;

import java.util.ArrayList;

public class HotStarUserInfoFailureEvent {

    String mErrorMessage = null;

    public HotStarUserInfoFailureEvent(String errorMessage){
        mErrorMessage = errorMessage;
    }

    public String getMessage() {
        return mErrorMessage;
    }
}
