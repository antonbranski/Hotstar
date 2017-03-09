package com.hotstar.player.events;

import com.hotstar.player.adplayer.core.VideoItem;

import java.util.ArrayList;

public class PlayerFailureEvent {

    String mErrorMessage = null;

    public PlayerFailureEvent(String errorMessage){
        mErrorMessage = errorMessage;
    }

    public String getMessage() {
        return mErrorMessage;
    }
}
