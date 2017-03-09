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

package com.hotstar.player.adplayer.logging;

import com.adobe.mediacore.logging.LogEntry;
import com.adobe.mediacore.logging.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class InMemoryLogger implements Logger {
	private List<LogEntry> logEntries = new ArrayList<LogEntry>();

	private int DEFAULT_MAX_ENTRY_COUNT = 1000;
	private int maxEntryCount = DEFAULT_MAX_ENTRY_COUNT;

	private Verbosity maxVerbosityLevel = Verbosity.INFO;

	private final static SimpleDateFormat sdf = new SimpleDateFormat(
			"dd/MM/yyyy HH:mm:ss");

	private void addEntry(LogEntry logEntry) {
		if (logEntries.size() >= maxEntryCount) {
			logEntries.remove(logEntries.size() - 1);
		}

		logEntries.add(0, logEntry);
	}

	@Override
	public void setCapacity(int maxEntryCount) {
		this.maxEntryCount = maxEntryCount;
	}

	@Override
	public List<LogEntry> getEntries() {
		List<LogEntry> entries = new ArrayList<LogEntry>();
		entries.addAll(logEntries);

		return entries;
	}

	@Override
	public void clear() {
		logEntries.clear();
	}

	@Override
	public void i(String logTag, String message) {
		if (maxVerbosityLevel.getLevel() < Verbosity.INFO.getLevel())
			return;

		addEntry(new LogEntry(now(), message, Logger.Verbosity.INFO, logTag));
		android.util.Log.i(logTag, message);
	}

	@Override
	public void d(String logTag, String message) {
		if (maxVerbosityLevel.getLevel() < Verbosity.DEBUG.getLevel())
			return;

		addEntry(new LogEntry(now(), message, Logger.Verbosity.DEBUG, logTag));
		android.util.Log.d(logTag, message);
	}

	@Override
	public void w(String logTag, String message) {
		if (maxVerbosityLevel.getLevel() < Verbosity.WARN.getLevel())
			return;

		addEntry(new LogEntry(now(), message, Logger.Verbosity.WARN, logTag));
		android.util.Log.w(logTag, message);
	}

	@Override
	public void w(String logTag, String message, Exception exception) {
		if (maxVerbosityLevel.getLevel() < Verbosity.WARN.getLevel())
			return;

		addEntry(new LogEntry(now(), message, Verbosity.WARN, logTag));
		android.util.Log.e(logTag, message, exception);
	}

	@Override
	public void e(String logTag, String message) {
		if (maxVerbosityLevel.getLevel() < Verbosity.ERROR.getLevel())
			return;

		addEntry(new LogEntry(now(), message, Logger.Verbosity.ERROR, logTag));
		android.util.Log.e(logTag, message);
	}

	@Override
	public void e(String logTag, String message, Exception exception) {
		if (maxVerbosityLevel.getLevel() < Verbosity.ERROR.getLevel())
			return;

		addEntry(new LogEntry(now(), message, Logger.Verbosity.ERROR, logTag));
		android.util.Log.e(logTag, message, exception);
	}

	@Override
	public void setVerbosityLevel(Verbosity level) {
		this.maxVerbosityLevel = level;
	}

	private String now() {
		Calendar cal = Calendar.getInstance();
		return sdf.format(cal.getTime());
	}

}
