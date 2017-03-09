package com.hotstar.player.adplayer.advertising;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.view.View;

import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.logging.Log;
import com.adobe.mediacore.logging.Logger;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.timeline.PlacementOpportunity;
import com.adobe.mediacore.timeline.TimelineOperation;
import com.adobe.mediacore.timeline.advertising.Ad;
import com.adobe.mediacore.timeline.advertising.AdAsset;
import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.adobe.mediacore.timeline.advertising.AdBreakPlacement;
import com.adobe.mediacore.timeline.advertising.AdClick;
import com.adobe.mediacore.timeline.advertising.ContentResolver;
import com.adobe.mediacore.timeline.advertising.ContentTracker;
import com.adobe.mediacore.timeline.advertising.PlacementInformation;
import com.adobe.mediacore.utils.NumberUtils;
import com.adobe.mediacore.utils.StringUtils;
import com.auditude.ads.util.StringUtil;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.utils.Clock;
import com.hotstar.player.adplayer.manager.OverlayAdResourceManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class CustomDirectAdBreakResolver extends ContentResolver {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + CustomDirectAdBreakResolver.class.getSimpleName();

    private static final int delayedTimeMiliseconds = 1000;

    private static final String JSON_METADATA_KEY = "json_metadata";
    private static final String NODE_NAME_ADBREAK_TAG = "tag";
    private static final String NODE_NAME_ADBREAK_TIME = "time";
    private static final String NODE_NAME_ADBREAK_REPLACE = "replace";
    private static final String NODE_NAME_ADBREAK_ADLIST = "ad-list";
    private static final String NODE_NAME_ADBREAK_SUBTYPE = "subtype";
    private static final String NODE_NAME_AD_URL = "url";
    private static final String NODE_NAME_AD_DURATION = "duration";
    private static final String NODE_NAME_AD_TAG = "tag";
    private static final String NODE_NAME_CLICK_INFO = "click-info";
    private static final String NODE_NAME_TITLE = "title";
    private static final String NODE_NAME_URL = "url";
    private static final String NODE_NAME_ID = "id";
    private static final int DEFAULT_PREROLL_TIME = 0;
    private static final int DEFAULT_POSTROLL_TIME = -2;

    private final List<TimelineOperation> _positionedAdBreakPlacements;
    private AdBreak _availableAdBreak;
    private Iterator<Ad> _availableAdIterator;
    private long _linearTagIndex;
    private int _linearTimelineId;
    private int _nextAvailableAdId;
    private boolean _isStartedTimeTracking = false;

    private ArrayList<Object> _overlayAdList = new ArrayList<Object>();
    private ArrayList<Object> _vpaidAdList = new ArrayList<>();

    private Clock.ClockEventListener adTrackingTimeEventListener = null;
    private AdBreak _currentAdBreak = null;
    private AdBreak _currentVPaidAdBreak = null;
    private OverlayAdListener _overlayADListener = null;
    private VPaidAdListener _vpaidADListener = null;
    private boolean _prepareShowedAD = false;
    private boolean _prepareShowedVPaidAD = false;

    public CustomDirectAdBreakResolver() {
        this._positionedAdBreakPlacements = new ArrayList();
        this._availableAdBreak = null;
        this._availableAdIterator = null;
        this._linearTagIndex = 0L;
        this._linearTimelineId = 0;
        this._nextAvailableAdId = 3000;
    }

    private void extractOverlayAdList(List adPlacementList) {
        _overlayAdList.clear();

        for (Object object : adPlacementList) {
            if (object.getClass().equals(AdBreakPlacement.class)) {
                AdBreakPlacement breakPlacement = (AdBreakPlacement) object;
                AdBreak adBreak = breakPlacement.getAdBreak();

                Iterator<Ad> iterator = adBreak.adsIterator();
                while (iterator.hasNext()) {
                    Ad ad = iterator.next();
                    String mediaUrl = ad.getPrimaryAsset().getMediaResource().getUrl();
                    String ext = mediaUrl.substring(mediaUrl.lastIndexOf("."));
                    if (ext.equalsIgnoreCase(".png") || (ext.equalsIgnoreCase(".jpg")) || (ext.equalsIgnoreCase(".gif"))) {
                        AdVideoApplication.logger.i(LOG_TAG + "#extractOverlayAdList", "");
                        _overlayAdList.add(breakPlacement);
                    }
                }
            }
        }
    }

    public void setOverlayAdList(ArrayList adList) {
        _overlayAdList.addAll(adList);
    }

    public ArrayList getOverlayAdList (){
        return _overlayAdList;
    }

    public void setVPaidAdList(List<AdBreakPlacement> adList) {
        _vpaidAdList.addAll(adList);
    }

    public ArrayList getVPaidAdList() {
        return _vpaidAdList;
    }

    public void startTimeTracking() {
        _isStartedTimeTracking = true;
    }

    public void stopTimeTracking() {
        _isStartedTimeTracking = false;
    }

    public void updatePlayerCurrentTime(long playerCurrentTime) {
        if (_isStartedTimeTracking == false)
            return;

        // check current ad break
        if (_currentAdBreak != null) {
            long startTime = _currentAdBreak.getTime();
            long duration = _currentAdBreak.getDuration();
            if (playerCurrentTime > (startTime + duration)) {
                // this means current adbreak completed
                // post adbreak completed
                if (_overlayADListener != null)
                    _overlayADListener.onCompleteOverlayAd(_currentAdBreak.getFirstAd().getPrimaryAsset().getMediaResource().getUrl());
                _currentAdBreak = null;
            }
        }
        else {
            // check ads
            for (Object object : _overlayAdList) {
                if (object.getClass().equals(AdBreakPlacement.class)) {
                    AdBreakPlacement breakPlacement = (AdBreakPlacement) object;
                    AdBreak adBreak = breakPlacement.getAdBreak();

                    long startTime = adBreak.getTime();
                    long duration = adBreak.getDuration();

                    if ((playerCurrentTime >= startTime - 5000 - delayedTimeMiliseconds) && (playerCurrentTime < startTime)) {
                        if ((_overlayADListener != null) && (_prepareShowedAD == false)) {
                            _overlayADListener.onBefore5SecsOverlayAd(adBreak.getFirstAd().getPrimaryAsset().getMediaResource().getUrl());
                            _prepareShowedAD = true;
                        }
                    }

                    if ((playerCurrentTime >= startTime - delayedTimeMiliseconds) && (playerCurrentTime < (startTime+duration))) {
                        // this means current adbreak started
                        // post adbreak start
                        if (_overlayADListener != null) {
                            _overlayADListener.onStartOverlayAd(adBreak.getFirstAd().getPrimaryAsset().getMediaResource().getUrl(), startTime, duration);
                            _prepareShowedAD = false;
                        }
                        _currentAdBreak = adBreak;
                        break;
                    }
                }
            }
        }

        // check current vpaid ad break
        if (_currentVPaidAdBreak != null) {
            long startTime = _currentVPaidAdBreak.getTime();
            long duration = _currentVPaidAdBreak.getDuration();
            if (playerCurrentTime > (startTime + duration)) {
                // this means current VPaid Adbreak completed
                // post adbreak completed
                if (_vpaidADListener != null)
                    _vpaidADListener.onCompleteVPaidAd(_currentVPaidAdBreak.getFirstAd().getPrimaryAsset().getMediaResource().getUrl());
                _currentVPaidAdBreak = null;
            }
        }
        else {
            // check ads
            for (Object object : _vpaidAdList) {
                if (object.getClass().equals(AdBreakPlacement.class)) {
                    AdBreakPlacement breakPlacement = (AdBreakPlacement) object;
                    AdBreak adBreak = breakPlacement.getAdBreak();

                    long startTime = adBreak.getTime();
                    long duration = adBreak.getDuration();

                    if ((playerCurrentTime >= startTime - 5000 - delayedTimeMiliseconds) && (playerCurrentTime < startTime)) {
                        if ((_vpaidADListener != null) && (_prepareShowedVPaidAD == false)) {
                            _vpaidADListener.onBefore5SecsVPaidAd(adBreak.getFirstAd().getPrimaryAsset().getMediaResource().getUrl());
                            _prepareShowedVPaidAD = true;
                        }
                    }

                    if ((playerCurrentTime >= startTime - delayedTimeMiliseconds) && (playerCurrentTime < (startTime + duration))) {
                        // this means current adbreak started
                        // post adbreak start
                        if (_vpaidADListener != null) {
                            _vpaidADListener.onStartVPaidAd(adBreak.getFirstAd().getPrimaryAsset().getMediaResource().getUrl(), startTime, duration);
                            _prepareShowedVPaidAD = false;
                        }
                        _currentVPaidAdBreak = adBreak;
                        break;
                    }
                }
            }
        }
    }

    public void prepareOverlayAdList() {
        if (_overlayAdList.size() == 0)
            return;

        for (Object object : _overlayAdList) {
            if (object.getClass().equals(AdBreakPlacement.class)) {
                AdBreakPlacement breakPlacement = (AdBreakPlacement) object;
                AdBreak adBreak = breakPlacement.getAdBreak();

                Iterator<Ad> iterator = adBreak.adsIterator();
                while (iterator.hasNext()) {
                    Ad ad = iterator.next();
                    String mediaUrl = ad.getPrimaryAsset().getMediaResource().getUrl();
                    String ext = mediaUrl.substring(mediaUrl.lastIndexOf("."));
                    if (ext.equalsIgnoreCase(".png") || (ext.equalsIgnoreCase(".jpg"))) {
                        OverlayAdResourceManager.getInstance().preloadImage(mediaUrl);
                    }
                    else if (ext.equalsIgnoreCase(".gif")){
                        OverlayAdResourceManager.getInstance().preloadGif(mediaUrl);
                    }
                }
            }
        }
    }


    protected void doResolveAds(Metadata metaData, PlacementOpportunity placementOpportunity) {
        if(this._positionedAdBreakPlacements.size() == 0 || this._availableAdBreak == null) {
            this.processMetadata(this.extractMetadata(metaData));
        }

        Object placementList;
        if(placementOpportunity.getPlacementInformation() != null && placementOpportunity.getPlacementInformation().getType() != PlacementInformation.Type.SERVER_MAP) {
            if(placementOpportunity.getPlacementInformation().getType() == PlacementInformation.Type.PRE_ROLL) {
                if(this._positionedAdBreakPlacements.size() > 0) {
                    AdBreak adBreak = ((AdBreakPlacement)this._positionedAdBreakPlacements.get(0)).getAdBreak();
                    placementList = new ArrayList();
                    ((List)placementList).add(new AdBreakPlacement(adBreak.cloneFor(placementOpportunity.getPlacementInformation()),
                            placementOpportunity.getPlacementInformation()));
                } else {
                    placementList = Collections.emptyList();
                }
            } else {
                placementList = this.createAdBreakPlacementsFor(placementOpportunity.getPlacementInformation());
            }
        } else {
            placementList = this._positionedAdBreakPlacements;
        }

        // extract OverlayAd List
        this.extractOverlayAdList((List)placementList);
        ((List) placementList).removeAll(_overlayAdList);

        this.notifyResolveComplete((List) placementList);
    }

    protected boolean doCanResolve(PlacementOpportunity var1) {
        return var1.getPlacementInformation().getMode() == PlacementInformation.Mode.INSERT;
    }

    private List<TimelineOperation> createAdBreakPlacementsFor(PlacementInformation var1) {
        AdVideoApplication.logger.i(LOG_TAG + "#createAdBreakPlacementFor", "Input : " + var1.toString());
        ArrayList var2 = new ArrayList();
        long var3 = var1.getDuration();
        if(this._availableAdBreak != null) {
            String var5 = this.getNextLinearTag();
            ArrayList var6 = new ArrayList();

            while(var3 > 0L) {
                Ad var7 = this.getAdForDuration(var3);
                if(var7 != null) {
                    var6.add(var7);
                    var3 -= var7.getDuration();
                } else {
                    var3 = 0L;
                }
            }

            AdBreak var8 = AdBreak.createAdBreak(var6, var1.getTime(), var1.getDuration(), var5);
            AdVideoApplication.logger.i(LOG_TAG + "#createAdBreakPlacementFor", "Output : " + var8.toString());
            var2.add(new AdBreakPlacement(var8, var1));
        }

        AdVideoApplication.logger.i(LOG_TAG + "#createAdBreakPlacementFor", "Number of ad breaks returned : " + String.valueOf(var2.size()));
        return var2;
    }

    private String getNextLinearTag() {
        ++this._linearTagIndex;
        StringBuilder var1 = new StringBuilder();
        var1.append("linear_").append(this._linearTagIndex);
        return var1.toString();
    }

    private Ad getAdForDuration(long var1) {
        boolean var3 = false;

        Ad var4;
        do {
            if(this._availableAdIterator == null) {
                this._availableAdIterator = this._availableAdBreak.adsIterator();
            }

            if(!this._availableAdIterator.hasNext()) {
                if(!var3) {
                    var3 = true;
                    this._availableAdIterator = this._availableAdBreak.adsIterator();
                } else {
                    this._availableAdIterator = null;
                }
            }

            if(this._availableAdIterator == null) {
                return null;
            }

            var4 = (Ad)this._availableAdIterator.next();
        } while(var4.getDuration() > var1);

        return var4;
    }

    protected ContentTracker doProvideAdTracker() {
        return null;
    }

    private Metadata extractMetadata(Metadata var1) {
        return var1 == null?null:(!(var1 instanceof MetadataNode)?null:((MetadataNode)var1).getNode(DefaultMetadataKeys.ADVERTISING_METADATA.getValue()));
    }

    private void processMetadata(Metadata var1) {
        try {
            String var2 = var1.getValue("json_metadata");
            if(var2 != null) {
                List var3 = this.extractAdBreakPlacements(var2);
                ArrayList var4 = new ArrayList();
                Iterator var5 = var3.iterator();

                while(true) {
                    while(var5.hasNext()) {
                        AdBreakPlacement var6 = (AdBreakPlacement)var5.next();
                        if(this.isAlreadyPositioned(var6)) {
                            this._positionedAdBreakPlacements.add(var6);
                        } else {
                            Iterator var7 = var6.getAdBreak().adsIterator();

                            while(var7.hasNext()) {
                                var4.add(var7.next());
                            }
                        }
                    }

                    if(var4.size() > 0) {
                        this._availableAdBreak = AdBreak.createAdBreak(var4, -1L, 0L, "availableAdBreak");
                    }
                    break;
                }
            }
        } catch (JSONException var8) {
            this.handleInvalidMetadata(var8);
        } catch (IllegalArgumentException var9) {
            this.handleInvalidMetadata(var9);
        }

    }

    private void handleInvalidMetadata(Exception var1) {
        AdVideoApplication.logger.w(LOG_TAG + "#handleInvalidMetadata", var1.getMessage());
        MediaPlayerNotification.Error var2 = MediaPlayerNotification.createErrorNotification(MediaPlayerNotification.ErrorCode.AD_RESOLVER_METADATA_INVALID, "Invalid JSON metadata.");
        MetadataNode var3 = new MetadataNode();
        var3.setValue("DESCRIPTION", var1.getMessage());
        var2.setMetadata(var3);
        this.notifyResolveError(var2);
    }

    private boolean isAlreadyPositioned(AdBreakPlacement var1) {
        return var1.getTime() >= 0L;
    }

    private List<AdBreakPlacement> extractAdBreakPlacements(String var1) throws JSONException {
        JSONArray var2 = new JSONArray(var1);
        CustomDirectAdBreakResolver.AdvertisingJSONFactory factory = new CustomDirectAdBreakResolver.AdvertisingJSONFactory();
        List<AdBreakPlacement> adBreakPlacements =  factory.createAdBreakPlacements(var2);

        // add VPAID ad break placements
        this.setVPaidAdList(factory.getVPaidAdBreakPlacements());
        return adBreakPlacements;
    }

    private class AdvertisingJSONFactory {
        private ArrayList<AdBreakPlacement> vpaidBreakPlacements = new ArrayList<>();

        private AdvertisingJSONFactory() {
        }

        public List<AdBreakPlacement> getVPaidAdBreakPlacements() {
            return vpaidBreakPlacements;
        }

        public List<AdBreakPlacement> createAdBreakPlacements(JSONArray var1) throws JSONException {
            ArrayList var2 = new ArrayList();
            int var3 = var1.length();
            if(var3 > 0) {
                for(int var4 = 0; var4 < var3; ++var4) {
                    AdBreakPlacement newAdBreakPlacement = this.createAdBreakPlacement(var1.getJSONObject(var4));
                    if (newAdBreakPlacement != null)
                        var2.add(newAdBreakPlacement);
                }
            }

            return var2;
        }

        public AdBreakPlacement createAdBreakPlacement(JSONObject var1) throws JSONException {
            AdVideoApplication.logger.i(LOG_TAG+"#createAdBreakPlacement: ", "JSONObject = " + var1.toString());

            String tag = var1.getString(CustomDirectAdBreakResolver.NODE_NAME_ADBREAK_TAG);
            long time = NumberUtils.parseNumber(var1.getString(CustomDirectAdBreakResolver.NODE_NAME_ADBREAK_TIME), -1L);
            long replace = NumberUtils.parseNumber(var1.getString(CustomDirectAdBreakResolver.NODE_NAME_ADBREAK_REPLACE), 0L);
            JSONArray jsonAdList = var1.getJSONArray(CustomDirectAdBreakResolver.NODE_NAME_ADBREAK_ADLIST);
            List adList = this.createAds(jsonAdList);

            PlacementInformation.Type placementType = PlacementInformation.Type.MID_ROLL;
            if(time == 0L) {
                placementType = PlacementInformation.Type.PRE_ROLL;
            }

            PlacementInformation placementInformation = new PlacementInformation(placementType, time, replace);

            // detect subtype
            if (var1.has(CustomDirectAdBreakResolver.NODE_NAME_ADBREAK_SUBTYPE) == true)
            {
                AdVideoApplication.logger.i(LOG_TAG + "#createAdBreakPlacement: ", "JSONObject has subtype field");
                String subType = var1.getString(CustomDirectAdBreakResolver.NODE_NAME_ADBREAK_SUBTYPE);
                if (subType.equalsIgnoreCase("vpaid_ad") == true) {
                    vpaidBreakPlacements.add(new AdBreakPlacement(AdBreak.createAdBreak(adList, time, replace, tag), placementInformation));
                    return null;
                }
            }
            return new AdBreakPlacement(AdBreak.createAdBreak(adList, time, replace, tag), placementInformation);
        }

        public List<Ad> createAds(JSONArray var1) throws JSONException {
            ArrayList var2 = new ArrayList();
            int var3 = var1.length();
            if(var3 > 0) {
                for(int var4 = 0; var4 < var3; ++var4) {
                    var2.add(this.createAd(var1.getJSONObject(var4)));
                }
            }

            return var2;
        }

        public Ad createAd(JSONObject jsonObject) throws JSONException {
            long duration = NumberUtils.parseLong(jsonObject.getString(CustomDirectAdBreakResolver.NODE_NAME_AD_DURATION), -1L);
            int timelineId = this.getNextTimelineId();
            AdAsset adAsset = this.createPrimaryAdAsset(jsonObject, duration, timelineId);
            return Ad.createAd(com.adobe.mediacore.MediaResource.Type.HLS, duration, timelineId, adAsset, new ArrayList(), (ContentTracker)null);
        }

        private AdAsset createPrimaryAdAsset(JSONObject jsonObject, long duration, int timelineId) throws JSONException {
            String mediaUrl = jsonObject.getString(CustomDirectAdBreakResolver.NODE_NAME_AD_URL);
            MediaResource var6 = MediaResource.createFromUrl(mediaUrl, (Metadata)null);
            return new AdAsset(timelineId, duration, var6, this.createAdClick(jsonObject), (Metadata)null);
        }

        private AdClick createAdClick(JSONObject var1) throws JSONException {
            if(var1.toString().contains(CustomDirectAdBreakResolver.NODE_NAME_CLICK_INFO)) {
                JSONObject var2 = var1.getJSONArray(CustomDirectAdBreakResolver.NODE_NAME_CLICK_INFO).getJSONObject(0);
                String var3 = var2.getString(CustomDirectAdBreakResolver.NODE_NAME_TITLE);
                String var4 = var2.getString(CustomDirectAdBreakResolver.NODE_NAME_URL);
                String var5 = var2.getString(CustomDirectAdBreakResolver.NODE_NAME_ID);
                return new AdClick(var5, var3, var4);
            } else {
                return new AdClick("", "", "");
            }
        }

        private synchronized int getNextTimelineId() {
            ++_linearTimelineId;
            return _linearTimelineId;
        }
    }

    public void registerOverlayAdListener(OverlayAdListener overlayAdListener) {
        _overlayADListener = overlayAdListener;
    }

    public void registerVPaidAdListener(VPaidAdListener vpaidAdListener) {
        _vpaidADListener = vpaidAdListener;
    }

    public interface OverlayAdListener {
        public void onBefore5SecsOverlayAd(String imageURL);
        public void onStartOverlayAd(String imageURL, long startTime, long duration);
        public void onCompleteOverlayAd(String imageURL);
    }

    public interface VPaidAdListener {
        public void onBefore5SecsVPaidAd(String vpaidURL);
        public void onStartVPaidAd(String vpaidURL, long startTime, long duration);
        public void onCompleteVPaidAd(String vpaidURL);
    }
}