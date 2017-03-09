package com.hotstar.player.adplayer.core;

import android.graphics.Bitmap;
import android.text.method.HideReturnsTransformationMethod;

import java.io.Serializable;

public class OverlayAdItem implements Serializable {

    public static final int OVERLAY_AD_IMAGE_UNLOADED = 0;
    public static final int OVERLAY_AD_IMAGE_LOADED = 1;

    private int adStartTime = 0;
    private int adDuration = 0;
    private int adReplace = 0;
    private String adTag = null;
    private String adListTag = null;
    private String adUrl = null;
    private int adImageLoadedStatus = OVERLAY_AD_IMAGE_UNLOADED;
    private Bitmap adBitmap = null;

    public int getAdStartTime() {
        return adStartTime;
    }

    public int getAdDuration() {
        return adDuration;
    }

    public int getAdReplace() {
        return adReplace;
    }

    public String getAdTag() {
        return adTag;
    }

    public String getAdUrl() {
        return adUrl;
    }

    public String getAdListTag() {
        return adListTag;
    }

    public int getAdImageLoadedStatus () {
        return adImageLoadedStatus;
    }

    public Bitmap getAdBitmap() {
        return adBitmap;
    }

    public void setAdStartTime(int _adStartTime) {
        adStartTime = _adStartTime;
    }

    public void setAdDuration(int _adDuration) {
        adDuration = _adDuration;
    }

    public void setAdReplace(int _adReplace) {
        adReplace = _adReplace;
    }

    public void setAdTag(String _adTag) {
        adTag = _adTag;
    }

    public void setAdListTag(String _adListTag) {
        adListTag = _adListTag;
    }

    public void setAdUrl(String _adUrl) {
        adUrl = _adUrl;
    }

    public void setAdImageLoadedStatus(int _adImageLoadedStatus) {
        adImageLoadedStatus = _adImageLoadedStatus;
    }

    public void setAdBitmap(Bitmap _adBitmap) {
        adBitmap = _adBitmap;
    }

}
