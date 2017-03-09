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

/*
 *  AdOverlay is a fragment class which contains the UI Component for displaying the Ad information such as 
 *  Ad remaining time,remaining ads in break, ad time,ad duration etc.
 *  Ad Overlay is displayed on top the player view in the player fragment.
 */

package com.hotstar.player.adplayer.player;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.adobe.mediacore.timeline.advertising.Ad;
import com.adobe.mediacore.timeline.advertising.AdBreak;
import com.hotstar.player.R;

import java.util.Iterator;

public class AdOverlay {

	private final int MS_IN_SECOND = 1000;

	private final ViewGroup container;
	private final TextView txtAdRemainingTime = null;
	private final TextView txtRemainingAdsInBreak = null;

	private long currentAdBreakSize;
	private long currentAdTime;
	private long currentAdIndex;
	private long currentAdDuration;

	public AdOverlay(ViewGroup container) {
		this.container = container;

		/*
		txtAdRemainingTime = (TextView) container
				.findViewById(R.id.txtAdRemainingTime);
		txtRemainingAdsInBreak = (TextView) container
				.findViewById(R.id.txtRemainingAdsInBreak);

		hide();
		*/
	}

	private void hide() {
		/* not used
		this.container.setVisibility(View.INVISIBLE);
		*/
	}

	private void show() {
		/* not used
		this.container.setVisibility(View.VISIBLE);
		*/
	}

	public void startAdBreak(AdBreak adBreak) {
		/*
		show();
		hideAdProgress();

		currentAdBreakSize = adBreak.size();
		currentAdIndex = 0;
		*/
	}

	public void startAd(AdBreak adBreak, Ad ad) {
		/*
		Iterator<Ad> it = adBreak.adsIterator();

		currentAdIndex = 0;
		while (it.hasNext()) {
			if (it.next().getId() == ad.getId()) {
				break;
			}
			currentAdIndex++;
		}

		currentAdTime = 0;
		currentAdDuration = (ad.getDuration() / MS_IN_SECOND);

		txtRemainingAdsInBreak.setText((currentAdIndex + 1) + "/" + currentAdBreakSize);
		txtAdRemainingTime.setText(currentAdTime + "/" + currentAdDuration);

		showAdProgress();
		*/
	}

	public void stopAd(AdBreak adBreak, Ad ad) {
		/*
		hideAdProgress();

		currentAdTime = 0;
		currentAdDuration = 0;
		*/
	}

	public void stopAdBreak(AdBreak adBreak) {
		/*
		currentAdBreakSize = 0;
		hide();
		*/
	}

	public void notifyClockTick() {
		/*
		currentAdTime++;

		txtRemainingAdsInBreak.setText((currentAdIndex + 1) + "/" + currentAdBreakSize);
		txtAdRemainingTime.setText(currentAdTime + "/" + currentAdDuration);
		*/
	}

	private void showAdProgress() {
		/* not used
		txtRemainingAdsInBreak.setVisibility(View.VISIBLE);
		txtAdRemainingTime.setVisibility(View.INVISIBLE);
		*/
	}

	private void hideAdProgress() {
		/* not used
		txtRemainingAdsInBreak.setVisibility(View.INVISIBLE);
		txtAdRemainingTime.setVisibility(View.INVISIBLE);
		*/
	}
}
