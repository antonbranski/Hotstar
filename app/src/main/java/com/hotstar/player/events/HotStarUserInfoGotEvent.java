package com.hotstar.player.events;

import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.model.HotStarUserInfo;

import java.util.ArrayList;

public class HotStarUserInfoGotEvent {

    HotStarUserInfo userInfo = null;

    public HotStarUserInfoGotEvent(HotStarUserInfo userInfo){
        this.userInfo = userInfo;
    }

    public HotStarUserInfo getUserInfo() {
        return userInfo;
    }
}
