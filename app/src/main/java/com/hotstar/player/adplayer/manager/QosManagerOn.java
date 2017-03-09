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

package com.hotstar.player.adplayer.manager;

import java.util.ArrayList;

import android.content.Context;
import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.qos.PlaybackInformation;
import com.adobe.mediacore.qos.QOSProvider;
import com.hotstar.player.adplayer.config.IQosConfig;

/**
 * 
 * The QosManager handles the QoS functionality
 * 
 */
public class QosManagerOn extends QosManager {

	private IQosConfig qosConfig;
	private MediaPlayer mediaPlayer;
	private QOSProvider qosProvider;
	private ArrayList<QosItem> qosItems = new ArrayList<QosItem>();
	private final ArrayList<QosManagerEventListener> eventListeners = new ArrayList<QosManagerEventListener>();

	public QosManagerOn(IQosConfig qosConfig, MediaPlayer mediaPlayer) {
		this.qosConfig = qosConfig;
		this.mediaPlayer = mediaPlayer;
	}

	@Override
	public void addEventListener(QosManagerEventListener eventListener) {
		eventListeners.add(eventListener);
	}

	@Override
	public void removeEventListener(QosManagerEventListener eventListener) {
		eventListeners.remove(eventListener);
	}

	/**
	 * 
	 * Create QOS provider to track QOS statistics by getting device info and
	 * attaching to the media player.
	 * 
	 * @param context
	 *            the context the QOS provider needs to retrieve device specific
	 *            information
	 */
	@Override
	public void createQOSProvider(Context context) {
		qosProvider = new QOSProvider(context);
		qosProvider.attachMediaPlayer(mediaPlayer);
	}

	/**
	 * Destroy QOS provider by detaching it from the media player
	 */
	@Override
	public void destroyQOSProvider() {
		if (qosProvider != null) {
			qosProvider.detachMediaPlayer();
			qosProvider = null;
		}
	}

	/**
	 * Update the latest QOS information from QOS provider
	 */
	@Override
	public void updateQosInformation() {
		if (qosProvider == null)
			return;

		PlaybackInformation playbackInformation = qosProvider
				.getPlaybackInformation();

		if (playbackInformation == null) {
			return;
		}

		setQosItem("Frame rate", (int) playbackInformation.getFrameRate()
				+ " (" + (int) playbackInformation.getDroppedFrameCount()
				+ " dropped)");
		setQosItem("Bitrate", (int) playbackInformation.getBitrate());
		setQosItem("Buffering time",
				(long) playbackInformation.getBufferingTime());
		setQosItem("Buffer length",
				(long) playbackInformation.getBufferLength());
		setQosItem("Buffer time", (long) playbackInformation.getBufferTime());
		setQosItem("Empty buffer count",
				(int) playbackInformation.getEmptyBufferCount());
		setQosItem("Time to load", (int) playbackInformation.getTimeToLoad());
		setQosItem("Time to start", (int) playbackInformation.getTimeToStart());
		setQosItem("Time to first frame",
				(int) playbackInformation.getTimeToFirstFrame());
		setQosItem("Time to prepare",
				(int) playbackInformation.getTimeToPrepare());
		setQosItem("Last seek time",
				(int) playbackInformation.getLastSeekTime());
	}

	/**
	 * Set the QOS item in the QOS item list with given name and value
	 * 
	 * @param name
	 *            the name of QOS item
	 * @param value
	 *            the value of the QOS item
	 */
	private void setQosItem(String name, Object value) {
		boolean found = false;
		for (QosItem item : qosItems) {
			if (item.getName().equalsIgnoreCase(name)) {
				item.setValue(value);
				found = true;
			}
		}
		if (!found)
			qosItems.add(new QosItem(name, value));

		if (qosConfig.isQosVisible()) {
			for (QosManager.QosManagerEventListener listener : eventListeners) {
				listener.onQosUpdate(qosItems);
			}
		}
	}

}
