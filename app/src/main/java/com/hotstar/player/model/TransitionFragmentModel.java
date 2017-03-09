package com.hotstar.player.model;

import com.hotstar.player.adplayer.core.VideoItem;

public class TransitionFragmentModel {

    protected final static int INVALID = -1;

    private VideoItem videoItem = null;
    private int currentFragmentType = INVALID;
    private int currentFragmentSubType = INVALID;
    private int targetFragmentType = INVALID;
    private int targetFragmentSubType = INVALID;

    public TransitionFragmentModel(VideoItem videoItem,
                                   int currentFragmentType,
                                   int currentFragmentSubType,
                                   int targetFragmentType,
                                   int targetFragmentSubType) {
        this.videoItem = videoItem;
        this.currentFragmentType = currentFragmentType;
        this.currentFragmentSubType = currentFragmentSubType;
        this.targetFragmentType = targetFragmentType;
        this.targetFragmentSubType = targetFragmentSubType;
    }

    public VideoItem getVideoItem() {
        return this.videoItem;
    }

    public int getCurrentFragmentType() {
        return this.currentFragmentType;
    }

    public int getCurrentFragmentSubType() {
        return this.currentFragmentSubType;
    }

    public int getTargetFragmentType() {
        return this.targetFragmentType;
    }

    public int getTargetFragmentSubType() {
        return this.targetFragmentSubType;
    }
}