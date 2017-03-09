package com.hotstar.player.events;

import com.hotstar.player.adplayer.core.VideoItem;
import com.hotstar.player.model.VideoItemsModel;

import java.util.ArrayList;

public class LoadedUsecase5VideoItemEvent
{
    VideoItemsModel model = null;

    public LoadedUsecase5VideoItemEvent(VideoItemsModel model)
    {
        this.model = model;
    }

    public ArrayList<VideoItem> getVideoItems() {
        if (this.model == null)
            return null;

        return this.model.getVideoItems();
    }
}
