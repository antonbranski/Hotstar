package com.hotstar.player.adplayer.advertising;

import android.content.Context;
import android.os.Handler;
import com.adobe.mediacore.MediaPlayerNotification;
import com.adobe.mediacore.MediaResource;
import com.adobe.mediacore.MediaPlayerNotification.Error;
import com.adobe.mediacore.MediaPlayerNotification.ErrorCode;
import com.adobe.mediacore.MediaResource.MimeType;
import com.adobe.mediacore.logging.Log;
import com.adobe.mediacore.logging.Logger;
import com.adobe.mediacore.metadata.AuditudeSettings;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.timeline.NopTimelineOperation;
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
import com.adobe.mediacore.timeline.advertising.PlacementInformation.Mode;
import com.adobe.mediacore.timeline.advertising.PlacementInformation.Type;
import com.adobe.mediacore.timeline.advertising.auditude.AuditudeRequest;
import com.adobe.mediacore.timeline.advertising.auditude.AuditudeTracker;
import com.adobe.mediacore.timeline.advertising.customadmarkers.CustomRangeHelper;
import com.adobe.mediacore.utils.ReplacementTimeRange;
import com.adobe.mediacore.utils.StringUtils;
import com.auditude.ads.AuditudeAdUnitDelegate;
import com.auditude.ads.event.AdClickThroughEvent;
import com.auditude.ads.event.AdPluginErrorEvent;
import com.auditude.ads.event.AdPluginEvent;
import com.auditude.ads.event.AdProgressEvent;
import com.auditude.ads.event.AuditudePluginEventListener;
import com.auditude.ads.event.LinearAdEvent;
import com.auditude.ads.event.NonLinearAdEvent;
import com.auditude.ads.event.OnPageEvent;
import com.auditude.ads.model.Asset;
import com.auditude.ads.model.IClick;
import com.auditude.ads.model.media.MediaFile;
import com.auditude.ads.model.smil.Group;
import com.auditude.ads.model.smil.Ref;
import com.auditude.ads.model.smil.Group.PrefetchCompleteListener;
import com.auditude.ads.response.AdResponse;
import com.hotstar.player.adplayer.AdVideoApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CustomAuditudeAdResolver extends ContentResolver implements PrefetchCompleteListener {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + CustomAuditudeAdResolver.class.getSimpleName();

    private final Logger _logger;
    private static final int MIN_INIT_REQUEST_INTERVAL = 1200;
    private static final int POST_ROLL_DEFAULT_TIME = 10000000;
    private static final int PRE_ROLL_DEFAULT_TIME = 0;
    private static final int MINIMUM_AD_DURATION = 5;
    private static final int MINIMUM_AD_LOTS = 1;
    private static final int AUDITUDE_TIME_SCALE = 1000;
    private final int EMPTY_AD_ID;
    private AuditudeAdUnitDelegate auditudeAdResolver;
    private final String REPACKAGING_MIME_TYPE;
    private PlacementOpportunity _placementOpportunity;
    private AuditudeRequest _auditudeRequest;
    private boolean creativeRepackagingEnabled;
    private String userAgent;
    private Handler handler;
    private long lastRequestTime;
    private int loadingGroupCount;
    private Context _resolverContext;
    private AuditudeTracker _auditudeTracker;
    private LinkedList<AuditudeRequest> _requestQueue;
    private final AuditudePluginEventListener pluginEventListener;

    public CustomAuditudeAdResolver() {
        this._logger = Log.getLogger(this.LOG_TAG);
        this.EMPTY_AD_ID = 0;
        this.REPACKAGING_MIME_TYPE = "application/x-mpegURL";
        this._placementOpportunity = null;
        this._auditudeRequest = null;
        this.creativeRepackagingEnabled = false;
        this.userAgent = "";
        this.handler = new Handler();
        this.loadingGroupCount = 0;
        this._requestQueue = new LinkedList();

        this.pluginEventListener = new AuditudePluginEventListener() {
            public void onAdPluginEvent(AdPluginEvent adPluginEvent) {
                if("initComplete".equals(adPluginEvent.getType())) {
                    CustomAuditudeAdResolver.this.loadAdBreaks(adPluginEvent);
                    CustomAuditudeAdResolver.this.startConsumer();
                }

            }

            public void onAdPluginErrorEvent(AdPluginErrorEvent adPluginErrorEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#onAdPluginErrorEvent", "An error ocurred while resolving ads");
                MediaPlayerNotification.Error var2 = MediaPlayerNotification.createErrorNotification(MediaPlayerNotification.ErrorCode.AD_RESOLVER_RESOLVE_FAIL, "Auditude plugin failed to resolve ad.");
                MetadataNode var3 = new MetadataNode();
                var3.setValue("NATIVE_ERROR_CODE", adPluginErrorEvent.getType());
                var2.setMetadata(var3);
                CustomAuditudeAdResolver.this.notifyResolveError(var2);
                CustomAuditudeAdResolver.this.startConsumer();
            }

            public void onAdClickEvent(AdClickThroughEvent adClickThroughEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#AdClickThroughEvent", "Event: " + adClickThroughEvent);
            }

            public void onAdProgressEvent(AdProgressEvent adProgressEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#AdProgressEvent", "Event: " + adProgressEvent);
            }

            public void onLinearAdEvent(LinearAdEvent linearAdEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#LinearAdEvent", "Event: " + linearAdEvent);
            }

            public void onNonLinearAdEvent(NonLinearAdEvent nonLinearAdEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#NonLinearAdEvent", "Event: " + nonLinearAdEvent);
            }

            public void onOnPageAdEvent(OnPageEvent onPageEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#OnPageEvent", "Event: " + onPageEvent);
            }
        };
        this.initializeAdUnit();
    }

    public CustomAuditudeAdResolver(Context context) {
        this._logger = Log.getLogger(this.LOG_TAG);
        this.EMPTY_AD_ID = 0;
        this.REPACKAGING_MIME_TYPE = "application/x-mpegURL";
        this._placementOpportunity = null;
        this._auditudeRequest = null;
        this.creativeRepackagingEnabled = false;
        this.userAgent = "";
        this.handler = new Handler();
        this.loadingGroupCount = 0;
        this._requestQueue = new LinkedList();

        this.pluginEventListener = new AuditudePluginEventListener() {
            public void onAdPluginEvent(AdPluginEvent adPluginEvent) {
                if("initComplete".equals(adPluginEvent.getType())) {
                    CustomAuditudeAdResolver.this.loadAdBreaks(adPluginEvent);
                    CustomAuditudeAdResolver.this.startConsumer();
                }

            }

            public void onAdPluginErrorEvent(AdPluginErrorEvent adPluginErrorEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#onAdPluginErrorEvent", "An error ocurred while resolving ads");
                MediaPlayerNotification.Error errorNotification = MediaPlayerNotification.createErrorNotification(MediaPlayerNotification.ErrorCode.AD_RESOLVER_RESOLVE_FAIL, "Auditude plugin failed to resolve ad.");
                MetadataNode metadataNode = new MetadataNode();
                metadataNode.setValue("NATIVE_ERROR_CODE", adPluginErrorEvent.getType());
                errorNotification.setMetadata(metadataNode);
                CustomAuditudeAdResolver.this.notifyResolveError(errorNotification);
                CustomAuditudeAdResolver.this.startConsumer();
            }

            public void onAdClickEvent(AdClickThroughEvent adClickThroughEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#AdClickThroughEvent", "Event: " + adClickThroughEvent);
            }

            public void onAdProgressEvent(AdProgressEvent adProgressEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#AdProgressEvent", "Event: " + adProgressEvent);
            }

            public void onLinearAdEvent(LinearAdEvent linearAdEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#LinearAdEvent", "Event: " + linearAdEvent);
            }

            public void onNonLinearAdEvent(NonLinearAdEvent nonLinearAdEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#NonLinearAdEvent", "Event: " + nonLinearAdEvent);
            }

            public void onOnPageAdEvent(OnPageEvent onPageEvent) {
                AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#OnPageEvent", "Event: " + onPageEvent);
            }
        };
        this._resolverContext = context;
        this.initializeAdUnit();
    }

    private void initializeAdUnit() {
        this.auditudeAdResolver = new AuditudeAdUnitDelegate();
        this.auditudeAdResolver.setProperty("repackageCreativeFormat", "application/x-mpegURL");
        this.auditudeAdResolver.setProperty("applicationContext", this._resolverContext);
        this.auditudeAdResolver.setPluginEventListener(this.pluginEventListener);
    }

    protected synchronized void doResolveAds(Metadata metadata, PlacementOpportunity placementOpportunity) {
        if(placementOpportunity.getPlacementInformation().getType().equals(PlacementInformation.Type.CUSTOM_TIME_RANGES)) {
            this.processReplacementRanges(placementOpportunity, new CustomRangeHelper(metadata));
        } else {
            this.execResolveAds(metadata, placementOpportunity);
        }

        this.startConsumer();
    }

    protected boolean doCanResolve(PlacementOpportunity placementOpportunity) {
        AuditudeSettings auditudeSettings = this.extractAuditudeSettings((MetadataNode)placementOpportunity.getMetadata());
        return placementOpportunity.getPlacementInformation().getMode() == PlacementInformation.Mode.INSERT
                || placementOpportunity.getPlacementInformation().getMode() == PlacementInformation.Mode.REPLACE && auditudeSettings != null;
    }

    private void startConsumer() {
        if(!this._requestQueue.isEmpty()) {
            long currentTimeMillis = System.currentTimeMillis();
            long delayTimeMillis;
            if(1200L > currentTimeMillis - this.lastRequestTime) {
                delayTimeMillis = 1200L - (currentTimeMillis - this.lastRequestTime);
            } else {
                delayTimeMillis = 0L;
            }

            this.lastRequestTime = currentTimeMillis + delayTimeMillis;
            this.handler.postDelayed(new Runnable() {
                public void run() {
                    if(!CustomAuditudeAdResolver.this._requestQueue.isEmpty()) {
                        CustomAuditudeAdResolver.this._auditudeRequest = (AuditudeRequest)CustomAuditudeAdResolver.this._requestQueue.poll();

                        try {
                            CustomAuditudeAdResolver.this.issueAdResolvingRequest(CustomAuditudeAdResolver.this._auditudeRequest.getAuditudeSettings(),
                                    (PlacementOpportunity)CustomAuditudeAdResolver.this._auditudeRequest.getPlacementOpportunities().get(0),
                                    CustomAuditudeAdResolver.this._auditudeRequest.getAvailCustomParams(),
                                    CustomAuditudeAdResolver.this._auditudeRequest.getPlacementInformations());
                        }
                        catch (IllegalArgumentException var4) {
                            AdVideoApplication.logger.i(CustomAuditudeAdResolver.this.LOG_TAG + "#startConsumer", String.valueOf(var4.getMessage()));
                            MediaPlayerNotification.Error errorNotification = MediaPlayerNotification.createErrorNotification(MediaPlayerNotification.ErrorCode.AD_RESOLVER_METADATA_INVALID, "Invalid ad metadata.");
                            MetadataNode metadataNode = new MetadataNode();
                            metadataNode.setValue("DESCRIPTION", var4.getMessage());
                            errorNotification.setMetadata(metadataNode);
                            CustomAuditudeAdResolver.this.notifyResolveError(errorNotification);
                        }

                    }
                }
            }, delayTimeMillis);
        }
    }

    private void processReplacementRanges(PlacementOpportunity placementOpportunity, CustomRangeHelper customRangeHelper) {
        this._placementOpportunity = placementOpportunity;

        try {
            Metadata metadata = placementOpportunity.getMetadata();
            MetadataNode metadataNode = customRangeHelper.extractCustomTimeRangeMetadata();
            List var5 = customRangeHelper.extractCustomTimeRanges(metadataNode);
            List var6 = customRangeHelper.mergeRanges(var5);
            AuditudeSettings auditudeSettings = this.extractAuditudeSettings((MetadataNode)metadata);
            MetadataNode var8 = ((MetadataNode)metadata).getNode(DefaultMetadataKeys.CUSTOM_PARAMETERS.getValue());
            AuditudeRequest var9 = new AuditudeRequest(auditudeSettings, placementOpportunity, var8);
            if(((ReplacementTimeRange)var6.get(0)).getBegin() != 0L) {
                var9.addPlacement(new PlacementInformation(PlacementInformation.Type.PRE_ROLL, PlacementInformation.Mode.REPLACE, 0L, -1L));
            }

            for(int var10 = 0; var10 < var6.size(); ++var10) {
                ReplacementTimeRange var11 = (ReplacementTimeRange)var6.get(var10);
                PlacementInformation.Type var12 = var11.getBegin() == 0L? PlacementInformation.Type.PRE_ROLL: PlacementInformation.Type.MID_ROLL;
                Long var13 = Long.valueOf(var11.getReplacementDuration() < 0L?-1L:var11.getReplacementDuration());
                var9.addPlacement(new PlacementInformation(var12, PlacementInformation.Mode.REPLACE, var11.getBegin(), var13.longValue()));
            }

            this._requestQueue.add(var9);
        } catch (Exception var14) {
            this.handleInvalidMetadata(var14);
        }

    }

    private void handleInvalidMetadata(Exception e) {
        AdVideoApplication.logger.i(LOG_TAG + "#handleInvalidMetadata", String.valueOf(e.getMessage()));
        MediaPlayerNotification.Error errorNotification = MediaPlayerNotification.createErrorNotification(MediaPlayerNotification.ErrorCode.AD_RESOLVER_METADATA_INVALID, "Invalid ad metadata.");
        MetadataNode metadataNode = new MetadataNode();
        metadataNode.setValue("DESCRIPTION", e.getMessage());
        errorNotification.setMetadata(metadataNode);
        this.notifyResolveError(errorNotification);
    }

    private void execResolveAds(Metadata metadata, PlacementOpportunity placementOpportunity) {
        this._placementOpportunity = placementOpportunity;
        if(!(metadata instanceof MetadataNode)) {
            throw new IllegalArgumentException("The provided metadata is not valid for Auditude resolver");
        }
        else {
            AuditudeSettings auditudeSettings = this.extractAuditudeSettings((MetadataNode)metadata);
            MetadataNode metadataNode = ((MetadataNode)metadata).getNode(DefaultMetadataKeys.CUSTOM_PARAMETERS.getValue());
            this._requestQueue.add(new AuditudeRequest(auditudeSettings, placementOpportunity, metadataNode));
        }
    }

    protected ContentTracker doProvideAdTracker() {
        if(this._auditudeTracker == null) {
            this._auditudeTracker = new AuditudeTracker(this.auditudeAdResolver.getReportingEngine());
        }

        return this._auditudeTracker;
    }

    private AuditudeSettings extractAuditudeSettings(MetadataNode metadataNode) {
        MetadataNode childNode = null;
        if(metadataNode.containsNode(DefaultMetadataKeys.AUDITUDE_METADATA_KEY.getValue())) {
            childNode = metadataNode.getNode(DefaultMetadataKeys.AUDITUDE_METADATA_KEY.getValue());
        }

        if(metadataNode.containsKey(DefaultMetadataKeys.ADVERTISING_METADATA.getValue())) {
            childNode = metadataNode.getNode(DefaultMetadataKeys.ADVERTISING_METADATA.getValue());
        }

        if(childNode != null && childNode instanceof AuditudeSettings) {
            return (AuditudeSettings)childNode;
        } else {
            throw new IllegalArgumentException("No AuditudeSettings metdata or compatible metadata have been found.");
        }
    }

    private void issueAdResolvingRequest(AuditudeSettings auditudeSettings, PlacementOpportunity placementOpportunity, Metadata metadata, List<PlacementInformation> placementInformationList) {
        List var5 = this.getMediaIds(auditudeSettings);
        String domainString = auditudeSettings.getDomain();
        if(StringUtils.isEmpty(domainString)) {
            AdVideoApplication.logger.i(LOG_TAG + "#initAuditudeAdResolver()", "Auditude domain parameter cannot be null or empty.");
            throw new IllegalArgumentException("Auditude domain parameter cannot be null or empty.");
        }
        else {
            int zoneId;
            try {
                zoneId = Integer.parseInt(auditudeSettings.getZoneId());
            }
            catch (NumberFormatException numberException) {
                throw new IllegalArgumentException("Auditude zoneId parameter must be a valid number.", numberException);
            }

            this.creativeRepackagingEnabled = auditudeSettings.isCreativeRepackagingEnabled();
            HashMap customParams = this.getCustomParams(auditudeSettings, placementOpportunity.getPlacementInformation(), metadata, placementInformationList);
            this.auditudeAdResolver.setProperty("repackageCreativeFormat", "application/x-mpegURL");
            this.auditudeAdResolver.setProperty("repackageCreativeEnabled", Boolean.valueOf(this.creativeRepackagingEnabled));
            this.userAgent = auditudeSettings.getUserAgent();
            if(!StringUtils.isEmpty(this.userAgent)) {
                this.auditudeAdResolver.setProperty("userAgent", this.userAgent);
            }

            this.auditudeAdResolver.init(domainString, var5, zoneId, customParams, 10);
        }
    }

    private List<String> getMediaIds(AuditudeSettings auditudeSettings) {
        ArrayList arrayList = new ArrayList();
        String mediaId = auditudeSettings.getMediaId();
        String defaultMediaId = auditudeSettings.getDefaultMediaId();
        if(StringUtils.isEmpty(mediaId)) {
            AdVideoApplication.logger.i(LOG_TAG + "#getMediaIds()", "Auditude asset ID parameter cannot be null or empty.");
            throw new IllegalArgumentException("Auditude asset ID parameter cannot be null or empty.");
        }
        else {
            arrayList.add(mediaId);
            if(!StringUtils.isEmpty(defaultMediaId)) {
                arrayList.add(defaultMediaId);
            }

            return arrayList;
        }
    }

    private HashMap<String, Object> getCustomParams(AuditudeSettings auditudeSettings, PlacementInformation placementInformation, Metadata metadata, List<PlacementInformation> placementInformationList) {
        HashMap userHashMap = new HashMap();
        Metadata targetingParameters = auditudeSettings.getTargetingParameters();
        if(targetingParameters != null) {
            Set targetKeySet = targetingParameters.keySet();
            Iterator it = targetKeySet.iterator();

            while(it.hasNext()) {
                String keyString = (String)it.next();
                String valueString = targetingParameters.getValue(keyString);
                if(!StringUtils.isEmpty(keyString) && !StringUtils.isEmpty(valueString)) {
                    userHashMap.put(keyString, valueString);
                }
            }
        }

        HashMap newHashMap = new HashMap();
        newHashMap.put("userData", userHashMap);
        String timeLineString = this.getTimeLineString(placementInformationList);
        if(timeLineString != null) {
            newHashMap.put("auditudeTimeline", timeLineString);
        }

        HashMap var17 = new HashMap();
        Metadata customParameters = auditudeSettings.getCustomParameters();
        Set var11;
        Iterator var12;
        String var13;
        String var14;
        if(customParameters != null) {
            var11 = customParameters.keySet();
            var12 = var11.iterator();

            while(var12.hasNext()) {
                var13 = (String)var12.next();
                var14 = customParameters.getValue(var13);
                if(!StringUtils.isEmpty(var13) && var14 != null) {
                    var17.put(var13, var14);
                }
            }
        }

        if(metadata != null) {
            var11 = metadata.keySet();
            var12 = var11.iterator();

            while(var12.hasNext()) {
                var13 = (String)var12.next();
                var14 = metadata.getValue(var13);
                if(!StringUtils.isEmpty(var13) && !StringUtils.isEmpty(var14)) {
                    var17.put(var13, var14);
                }
            }
        }

        if(!var17.containsKey("PSDK_AVAIL_DURATION") && placementInformation != null && placementInformation.getDuration() != -1L) {
            long var19 = placementInformation.getDuration() / 1000L;
            var17.put("PSDK_AVAIL_DURATION", String.valueOf(var19));
        }

        newHashMap.put("auditudePassThroughParams", var17);
        AdVideoApplication.logger.i(LOG_TAG + "#getCustomParams", "Auditude custom params: " + newHashMap);
        return newHashMap;
    }

    private String getTimeLineString(List<PlacementInformation> placementInformationList) {
        String timelineString = null;
        if(placementInformationList != null && placementInformationList.size() > 0) {
            if(((PlacementInformation)placementInformationList.get(0)).getType() == PlacementInformation.Type.SERVER_MAP) {
                return null;
            }

            if(((PlacementInformation)placementInformationList.get(0)).getType() == PlacementInformation.Type.PRE_ROLL
                    && placementInformationList.size() == 1
                    && ((PlacementInformation)placementInformationList.get(0)).getDuration() == -1L) {
                return null;
            }

            StringBuilder stringBuilder = new StringBuilder();

            for(int index = 0; index < placementInformationList.size(); ++index) {
                if(index > 0) {
                    stringBuilder.append("&tl=");
                }

                stringBuilder.append(serializePlacement((PlacementInformation)placementInformationList.get(index), false));
                if(placementInformationList.size() > 1) {
                    PlacementInformation var5;
                    if(index < placementInformationList.size() - 1) {
                        var5 = new PlacementInformation(PlacementInformation.Type.MID_ROLL, PlacementInformation.Mode.INSERT,
                                ((PlacementInformation)placementInformationList.get(index)).getTime(),
                                ((PlacementInformation)placementInformationList.get(index + 1)).getTime() - ((PlacementInformation)placementInformationList.get(index)).getTime());
                        stringBuilder.append("&tl=").append(serializePlacement(var5, true));
                    } else if(((PlacementInformation)placementInformationList.get(index)).getType() != PlacementInformation.Type.POST_ROLL) {
                        var5 = new PlacementInformation(PlacementInformation.Type.MID_ROLL, PlacementInformation.Mode.INSERT,
                                ((PlacementInformation)placementInformationList.get(index)).getTime(),
                                10000000L);
                        stringBuilder.append("&tl=").append(serializePlacement(var5, true));
                    }
                }
            }

            timelineString = stringBuilder.toString();
        }

        return timelineString;
    }

    private static String serializePlacement(PlacementInformation placementInformation, boolean bFlag) {
        long var2 = -1L;
        long var4 = 1L;
        if(placementInformation.getDuration() != -1L) {
            var2 = placementInformation.getDuration() / 1000L;
            var4 = var2 / 5L;
        }

        if(placementInformation.getDuration() == 0L) {
            var4 = 0L;
        } else if(var4 < 1L) {
            var4 = 1L;
        }

        StringBuilder var6 = new StringBuilder();
        var6.append(bFlag?"n":"l").append(",");
        if(var2 == -1L) {
            var6.append("*").append(",");
        } else {
            var6.append(String.valueOf(var2)).append(",");
        }

        if(var2 == -1L) {
            var6.append("*").append(",");
        } else if(bFlag) {
            var6.append("0");
        } else {
            var6.append(String.valueOf(var4)).append(",");
        }

        if(!bFlag) {
            if(placementInformation.getType() == PlacementInformation.Type.PRE_ROLL) {
                var6.append("p");
            } else if(placementInformation.getType() == PlacementInformation.Type.POST_ROLL) {
                var6.append("t");
            } else {
                var6.append("m");
            }
        }

        return var6.toString();
    }

    public void onPrefetchComplete(ArrayList<AdPluginEvent> var1) {
        --this.loadingGroupCount;
        if(this.loadingGroupCount <= 0) {
            List var3 = this.extractAdBreakPlacements();
            Object var2 = new ArrayList();
            if(this._placementOpportunity != null && this._placementOpportunity.getPlacementInformation() != null && this._auditudeRequest != null) {
                List var4 = this._auditudeRequest.getPlacementInformations();
                if(var3 != null && var3.size() > 0 && var3.get(0) instanceof NopTimelineOperation) {
                    var2 = var3;
                } else if(this._placementOpportunity.getPlacementInformation().getType() != PlacementInformation.Type.PRE_ROLL && (((PlacementInformation)var4.get(0)).getType() != PlacementInformation.Type.PRE_ROLL || var4.size() != 1 || ((PlacementInformation)var4.get(0)).getDuration() != -1L)) {
                    var2 = var3;
                } else {
                    AdVideoApplication.logger.i(LOG_TAG + "#createAdBreakFor", "Input : " + this._placementOpportunity.toString());
                    var2 = new ArrayList();
                    if(var3.size() > 0) {
                        AdBreakPlacement var5 = (AdBreakPlacement)var3.get(0);
                        AdBreak var6 = var5.getAdBreak().cloneFor(this._placementOpportunity.getPlacementInformation());
                        AdVideoApplication.logger.i(LOG_TAG + "#createAdBreakFor", "Output : " + var6.toString());
                        PlacementInformation var7 = this.createPlacementInformation(var6);
                        AdBreakPlacement var8 = new AdBreakPlacement(var6, var7);
                        ((List)var2).add(var8);
                    }
                }
            }

            AdVideoApplication.logger.i(LOG_TAG + "#createAdBreakFor", "Number of ad breaks returned : " + String.valueOf(((List) var2).size()));
            this.notifyResolveComplete((List)var2);
        }

    }

    private void loadAdBreaks(AdPluginEvent var1) {
        AdResponse var2 = this.auditudeAdResolver.getAdResponse();
        ArrayList var3 = var2.getBreaks();
        this.loadingGroupCount = var3.size();
        if(this.loadingGroupCount > 0) {
            AdVideoApplication.logger.i(LOG_TAG + "#loadAdBreaks", "Loading ad breaks. Creative repackaging is: " + (this.creativeRepackagingEnabled ? "enabled" : "disabled") + ".");
            Iterator var4 = var3.iterator();

            while(var4.hasNext()) {
                Group var5 = (Group)var4.next();
                var5.load(this, Boolean.valueOf(this.creativeRepackagingEnabled), (HashMap)null);
            }
        } else {
            this.onPrefetchComplete((ArrayList)null);
        }

    }

    private List<TimelineOperation> extractAdBreakPlacements() {
        ArrayList var1 = new ArrayList();
        AdResponse var2 = this.auditudeAdResolver.getAdResponse();
        ArrayList var3 = var2.getBreaks();
        Iterator var4 = var3.iterator();

        while(var4.hasNext()) {
            Group var5 = (Group)var4.next();
            ArrayList var6 = new ArrayList();
            ArrayList var7 = var5.getRefs();
            Iterator var8 = var7.iterator();

            while(var8.hasNext()) {
                Ref var9 = (Ref)var8.next();
                if(var9 != null && var9.getAd() != null && var9.getPrimaryAsset() != null) {
                    AuditudeSettings var10 = new AuditudeSettings();
                    var10.setData(var9);
                    Asset var11 = var9.getPrimaryAsset();
                    AdAsset var12 = this.extractPrimaryAdAsset(var11, var10);
                    if(var12 != null) {
                        ArrayList var13 = new ArrayList();
                        var6.add(Ad.createAd(com.adobe.mediacore.MediaResource.Type.HLS, (long)var11.getDurationInMillis(), this.extractId(var9.getAd().getID()), var12, var13, this.getAdTracker()));
                    } else {
                        AdVideoApplication.logger.i(LOG_TAG + "#extractAdBreaks", "Ad will be skipped. Auditude response contains a primary asset that is not supported");
                    }
                }
            }

            if(var6.size() > 0) {
                AdBreak var14 = AdBreak.createAdBreak(var6, (long)var5.getStartTime(), 0L, String.valueOf(var5.getIndex()));
                PlacementInformation var15 = this.createPlacementInformation(var14);
                var1.add(new AdBreakPlacement(var14, var15));
            } else {
                var1.add(new NopTimelineOperation());
            }
        }

        return var1;
    }

    private AdAsset extractPrimaryAdAsset(Asset asset, Metadata metadata) {
        AdClick adClick = this.extractAdClick(asset);
        return this.extractAdAsset(asset, metadata, adClick);
    }

    private AdAsset extractAdAsset(Asset asset, Metadata metadata, AdClick adClick) {
        ArrayList mediaFiles = asset.getMediaFiles();
        if(mediaFiles == null) {
            AdVideoApplication.logger.i(LOG_TAG + "#extractAdAsset", "Received invalid response from Auditude. Asset has no media files associated with it");
            return null;
        }
        else {
            Iterator it = mediaFiles.iterator();
            MediaFile mediaFile;
            do {
                if(!it.hasNext()) {
                    return null;
                }

                mediaFile = (MediaFile)it.next();
            } while(!this.isMediaFileSupported(mediaFile));

            return this.createAdAsset(mediaFile.source, mediaFile.id, (long)asset.getDurationInMillis(), metadata, adClick);
        }
    }

    private boolean isMediaFileSupported(MediaFile mediaFile) {
        if(mediaFile != null && mediaFile.mimeType != null) {
            if(mediaFile.mimeType.equalsIgnoreCase(MediaResource.MimeType.HLS_MIME_TYPE.getValue())) {
                return true;
            }
            else {
                String ext = StringUtils.getFileExtension(mediaFile.source);
                return ext != null && ext.equalsIgnoreCase(com.adobe.mediacore.MediaResource.Type.HLS.getValue());
            }
        }
        else {
            return false;
        }
    }

    private AdAsset createAdAsset(String var1, String var2, long var3, Metadata metadata, AdClick adClick) {
        MediaResource var7 = MediaResource.createFromUrl(var1, metadata);
        return new AdAsset(this.extractId(var2), var3, var7, adClick, metadata);
    }

    private int extractId(String var1) {
        int var2 = 0;
        if(var1 != null) {
            try {
                var2 = Integer.parseInt(var1);
            } catch (NumberFormatException var4) {
                AdVideoApplication.logger.i(LOG_TAG + "#extractId", "Number format exception when parsing id from auditude. Will use default values");
            }
        }

        return var2;
    }

    private AdClick extractAdClick(Asset asset) {
        IClick var2 = asset.getClick();
        return var2 == null ? new AdClick("", "", "") : new AdClick(var2.getID(), var2.getTitle(), var2.getUrl());
    }

    private PlacementInformation createPlacementInformation(AdBreak adBreak) {
        if(this._placementOpportunity.getPlacementInformation() != null
                && this._placementOpportunity.getPlacementInformation().getType() != PlacementInformation.Type.SERVER_MAP
                && !this._placementOpportunity.getPlacementInformation().getType().equals(PlacementInformation.Type.CUSTOM_TIME_RANGES)) {
            return new PlacementInformation(this._placementOpportunity.getPlacementInformation().getType(),
                    this._placementOpportunity.getPlacementInformation().getTime(),
                    this._placementOpportunity.getPlacementInformation().getDuration());
        }
        else {
            PlacementInformation.Type var2 = PlacementInformation.Type.MID_ROLL;
            if(adBreak.getTime() == 0L) {
                var2 = PlacementInformation.Type.PRE_ROLL;
            } else if(adBreak.getTime() == 10000000L) {
                var2 = PlacementInformation.Type.POST_ROLL;
            }

            return new PlacementInformation(var2, adBreak.getTime(), 0L);
        }
    }
}
