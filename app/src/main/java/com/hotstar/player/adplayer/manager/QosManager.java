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
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

/**
 * 
 * The QosManager handles the QoS functionality
 * 
 */
public class QosManager implements IManager {

	/**
	 * QosEventListener: listeners that can respond appropriately to QosEvents
	 */
	public interface QosManagerEventListener {
		public void onQosUpdate(ArrayList<QosItem> qosItems);
	}

	/**
	 * Internal objects and methods
	 */
	public static class QosItem {
		private String name;
		private Object value;
		private int id;
		private static final AtomicInteger uniqueId = new AtomicInteger();

		public QosItem(String name, Object value) {
			this.name = name;
			this.value = value;

			// This is thread safe.
			this.id = uniqueId.getAndIncrement();
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value.toString();
		}

		public int getId() {
			return id;
		}

		public void setValue(Object value) {
			this.value = value;
		}
	}

	public void addEventListener(
			QosManager.QosManagerEventListener eventListener) {

	}

	public void removeEventListener(
			QosManager.QosManagerEventListener qosEventListener) {

	}

	/**
	 * 
	 * Create QOS provider to track QOS statistics by getting device info and
	 * attaching to the media player. This method should be sub-classed to
	 * provide an implementation.
	 * 
	 */
	public void createQOSProvider(Context context) {

	}

	/**
	 * Destroy QOS provider by detaching it from the media player
	 */
	public void destroyQOSProvider() {

	}

	/**
	 * Empty call to update QOS information
	 */
	public void updateQosInformation() {

	}

	@Override
	public void destroy() {
	}

	public static final String SETTINGS_QOS_VISIBILITY = "settings_qos_visibility";
	public final static boolean DEFAULT_QOS_VISIBILITY = true;
}
