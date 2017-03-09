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

package com.hotstar.player.adplayer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A clock class using the timer thread to keep track of time. This is used to
 * run simultaneously with the video playback to update the control bar and QOS
 * information
 */
public final class Clock {
	private final long interval;
	private final String name;
	private boolean isRunning;

	private final List<ClockEventListener> clockEventListeners = new ArrayList<ClockEventListener>();

	private Timer timer;

	/**
	 * Interface definition of a set of callback to be invoked during as timer
	 * goes.
	 */
	public interface ClockEventListener {

		/**
		 * Called each time the clock ticks (based on the interval)
		 * 
		 * @param name
		 *            the name associated with the clock
		 */
		void onTick(String name);
	}

	public void addClockEventListener(ClockEventListener listener) {
		clockEventListeners.add(listener);
	}

	public void removeClockEventListener(ClockEventListener listener) {
		clockEventListeners.remove(listener);
	}

	/**
	 * Dispatch on tick event
	 */
	private void dispatchClockEvent() {
		for (ClockEventListener listener : clockEventListeners) {
			listener.onTick(name);
		}
	}

	/**
	 * Constructor for a clock object
	 * 
	 * @param name
	 *            the identifier of the clock
	 * @param interval
	 *            the time in milliseconds the timer takes for each tick
	 */
	public Clock(String name, long interval) {
		this.name = name;
		this.interval = interval;
	}

	/**
	 * Start the clock
	 */
	public void start() {
		if (isRunning)
			return;
		isRunning = true;
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				dispatchClockEvent();
			}
		}, 0, interval);
	}

	/**
	 * Stop the clock
	 */
	public void stop() {
		if (!isRunning)
			return;
		isRunning = false;
		timer.cancel();
	}

	/**
	 * Determine if the clock is running
	 * 
	 * @return true if the clock is running, false otherwise
	 */
	public boolean isRunning() {
		return isRunning;
	}

}
