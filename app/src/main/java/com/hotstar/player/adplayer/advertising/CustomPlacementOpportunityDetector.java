/*
 * ************************************************************************
 *
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the 
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a 
 * source other than Adobe, then your use, modification, or distribution of it requires the prior 
 * written permission of Adobe.
 *
 **************************************************************************
 */
package com.hotstar.player.adplayer.advertising;

import com.hotstar.player.adplayer.AdVideoApplication;
import com.adobe.mediacore.PlacementOpportunityDetector;
import com.adobe.mediacore.timeline.PlacementOpportunity;
import com.adobe.mediacore.metadata.DefaultMetadataKeys;
import com.adobe.mediacore.metadata.Metadata;
import com.adobe.mediacore.metadata.MetadataNode;
import com.adobe.mediacore.metadata.TimedMetadata;
import com.adobe.mediacore.timeline.advertising.PlacementInformation;
import com.adobe.mediacore.utils.NumberUtils;
import com.adobe.mediacore.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which monitors the playback timeline and detects ad placement
 * opportunities.
 * 
 * The packager will scan the playlist files for special "cues" and detect if
 * they contain any ad placement information. An example of a "cue" entry for
 * HLS streams
 * #EXT-X-CUE:TYPE=SpliceOut,ID=7687,TIME=578123.41,DURATION=30.0,AVAIL
 * -NUM=0,AVAILS-EXPECTED=0,PROGRAM-ID=0
 * 
 */
public class CustomPlacementOpportunityDetector implements
		PlacementOpportunityDetector {
	private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME
			+ CustomPlacementOpportunityDetector.class.getSimpleName();

	private static final String OPPORTUNITY_DURATION_KEY = "DURATION";
	private static final String OPPORTUNITY_ID_KEY = "CAID";
	private static final String OPPORTUNITY_TAG_NAME = "#EXT-X-CUE-OUT";

	private static final String PSDK_AVAIL_DURATION_KEY = "PSDK_AVAIL_DURATION";
	private static final String PSDK_ASSET_ID_KEY = "ASSET_ID";

	@Override
	public List<PlacementOpportunity> process(
			List<TimedMetadata> timedMetadataList, Metadata metadata) {
		AdVideoApplication.logger.i(LOG_TAG + "#process",
				"Processing [" + timedMetadataList.size() + "] timed metadata, in order to provide placement opportunities.");
		List<PlacementOpportunity> opportunities = new ArrayList<PlacementOpportunity>();

		for (TimedMetadata timedMetadata : timedMetadataList) {
			if (isPlacementOpportunity(timedMetadata)) {

				// The airingId (CAID) is in another tag. Iterate through the
				// timedMetadata list and find
				// the CAID associated with this ad cue point.
				String airingId = getAiringIdForTime(timedMetadata.getTime(), timedMetadataList);
				PlacementOpportunity opportunity = createPlacementOpportunity(timedMetadata, airingId, metadata);
				if (opportunity != null)
				{
					AdVideoApplication.logger.i(LOG_TAG + "#process",
							"Created new placement opportunity at time " + opportunity.getPlacementInformation().getTime()
									+ ", with a duration of " + opportunity.getPlacementInformation().getDuration() + "ms.");
					opportunities.add(opportunity);
				}
				else
				{
					AdVideoApplication.logger.w(LOG_TAG + "#process",
							"Ad placement opportunity creation has failed. Probably has invalid metadata."
									+ " opportunity time = " + String.valueOf(timedMetadata.getTime())
									+ ", metadata: " + timedMetadata.getMetadata() + "].");
				}
			}
			else {
				AdVideoApplication.logger.w(LOG_TAG + "#process",
						"Ad placement opportunity creation has failed. Probably has invalid metadata."
								+ " opportunity time = " + String.valueOf(timedMetadata.getTime())
								+ ", metadata: " + timedMetadata.getMetadata() + "].");
			}
		}

		return opportunities;
	}

	protected boolean isPlacementOpportunity(TimedMetadata timedMetadata) {
		Metadata metadata = timedMetadata.getMetadata();
		return timedMetadata.getName().equals(OPPORTUNITY_TAG_NAME) && metadata != null && metadata.containsKey(OPPORTUNITY_DURATION_KEY);
	}

	protected PlacementOpportunity createPlacementOpportunity(TimedMetadata timedMetadata, String airingId, Metadata metadata) {
		long time = timedMetadata.getTime();
		long duration = 0;

		Metadata rawMetadata = timedMetadata.getMetadata();
		if (rawMetadata.containsKey(OPPORTUNITY_DURATION_KEY)) {
			duration = NumberUtils.parseNumber(rawMetadata.getValue(OPPORTUNITY_DURATION_KEY), 0) * 1000;
		}

		if (duration <= 0) {
			return null;
		}

		// The custom params to be sent to the ad provider.
		MetadataNode customParams = new MetadataNode();
		customParams.setValue(PSDK_AVAIL_DURATION_KEY, String.valueOf(duration / 1000));
		customParams.setValue(PSDK_ASSET_ID_KEY, String.valueOf(getAiringIdAsLong(airingId)));
		((MetadataNode) metadata).setNode(DefaultMetadataKeys.CUSTOM_PARAMETERS.getValue(), customParams);

		return new PlacementOpportunity(
				String.valueOf(timedMetadata.getId()),
				new PlacementInformation(PlacementInformation.Type.MID_ROLL, time, duration), metadata);
	}

	private long getAiringIdAsLong(String airingId) {
		long value = 0;
		if (StringUtils.isEmpty(airingId)) {
			return value;
		}
		try {
			// Example: 0x00000000AABBDDEE to 00000000AABBDDEE and parse it to
			// long
			value = Long.parseLong(airingId.substring(2), 16);
			AdVideoApplication.logger.i(LOG_TAG + "#getAiringIdAsLong",
					"Converted airingId [" + airingId + "] to long value: "
							+ value + ".");
		} catch (NumberFormatException e) {
			AdVideoApplication.logger.w(LOG_TAG + "#getAiringIdAsLong",
					"Unable to convert airingId [" + airingId
							+ "] to long value.");
		}
		return value;
	}

	/**
	 * Iterate through the timedMetadataList and find the CAID (airing ID)
	 * associated with the provided local time.
	 * 
	 * @param time
	 *            the local time in the stream
	 * @param timedMetadataList
	 *            the list of timed metadata to iterate through
	 * @return the airing id
	 */
	private String getAiringIdForTime(long time,
			List<TimedMetadata> timedMetadataList) {

		for (TimedMetadata timedMetadata : timedMetadataList) {
			if (timedMetadata.getTime() == time
					&& timedMetadata.getMetadata() != null
					&& timedMetadata.getMetadata().containsKey(
							OPPORTUNITY_ID_KEY)) {
				return timedMetadata.getMetadata().getValue(OPPORTUNITY_ID_KEY);
			}
		}
		return null;
	}
}
