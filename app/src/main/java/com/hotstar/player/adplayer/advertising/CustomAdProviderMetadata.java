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

import java.io.Serializable;
import java.util.ArrayList;

import com.adobe.mediacore.metadata.AdvertisingMetadata;
import com.adobe.mediacore.metadata.Metadata;

public class CustomAdProviderMetadata extends AdvertisingMetadata {
	private static final long serialVersionUID = 1L;
	public static final String CUSTOM_AD_PROVIDER_METADATA_KEY = "CUSTOM_AD_PROVIDER_METADATA_KEY";

	private String domain;
	private Metadata metadata;
	private ArrayList<AdPattern> adPatternList;

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setAdPatternList(ArrayList<AdPattern> adPatternList) {
		this.adPatternList = adPatternList;
	}

	public void setContentMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public String getDomain() {
		return domain;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public ArrayList<AdPattern> getAdPatternList() {
		return adPatternList;
	}

	public static class AdPattern implements Serializable {
		private static final long serialVersionUID = 1L;
		private Long time;
		private Long count;
		private Long duration;

		public AdPattern(Long time, Long count, Long duration) {
			this.time = time;
			this.count = count;
			this.duration = duration;
		}

		public Long getTime() {
			return time;
		}

		public Long getCount() {
			return count;
		}

		public Long getDuration() {
			return duration;
		}
	}
}
