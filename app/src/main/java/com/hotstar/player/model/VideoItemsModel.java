package com.hotstar.player.model;

import com.hotstar.player.adplayer.core.VideoItem;

import java.util.ArrayList;

public class VideoItemsModel
{
    ArrayList<VideoItem> mVideoItems;

    public VideoItemsModel(ArrayList<VideoItem> videoItems) {
        mVideoItems = new ArrayList<VideoItem>();
        mVideoItems.addAll(videoItems);
    }

    public ArrayList<VideoItem> getVideoItems()
    {
        return mVideoItems;
    }
}
