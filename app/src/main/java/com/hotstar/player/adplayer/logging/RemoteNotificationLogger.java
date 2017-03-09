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

import android.os.Handler;
import android.os.Message;
import com.adobe.mediacore.MediaPlayer;
import com.adobe.mediacore.Version;
import com.adobe.mediacore.session.NotificationHistory;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.adplayer.utils.DeviceInfo;
import com.hotstar.player.adplayer.utils.http.AsyncHttpConnection;

import java.util.*;

public class RemoteNotificationLogger {
	private final static String LOG_TAG = AdVideoApplication.LOG_APP_NAME
			+ RemoteNotificationLogger.class.getSimpleName();

	private final String PLATFORM = "Android";

	public static final String DEFAULT_HOST = "127.0.0.1";
	public static final int DEFAULT_PORT = 3000;

	private long LOGGING_TIMER_PERIOD = 1000;
	private int LOGGING_REQUEST_TIMEOUT = 10 * 1000;

	private final MediaPlayer _mediaPlayer;

	private Handler _handler = new Handler();

	private List<Listener> _listeners = new ArrayList<Listener>();

	private Runnable _internalTimer;
	private boolean _shouldStopInternalTimer, _internalTimerRunning;

	private String _host;
	private String _tag;
	private int _port;

	private long lastLoggedNotificationIndex;

	public interface Listener {
		void onError(String errMsg);
	}

	public RemoteNotificationLogger(MediaPlayer mediaPlayer, String host,
			int port) {
		_mediaPlayer = mediaPlayer;
		_host = host;
		_port = port;
		_tag = computeDefaultSessionTag();

		_internalTimer = new Runnable() {
			@Override
			public void run() {
				sendNewDataToServer(_host, _port);
				if (!_shouldStopInternalTimer) {
					_handler.postDelayed(_internalTimer, LOGGING_TIMER_PERIOD);
				}
			}
		};
	}

	private void startInternalTimer() {
		if (_internalTimerRunning) {
			return;
		}
		_shouldStopInternalTimer = false;
		_internalTimerRunning = true;

		AdVideoApplication.logger.i(LOG_TAG + "#startInternalTimer",
				"Starting the logging timer.");
		_handler.postDelayed(_internalTimer, LOGGING_TIMER_PERIOD);
	}

	private void stopInternalTimer() {
		_shouldStopInternalTimer = true;
		_internalTimerRunning = false;
		if (_handler != null) {
			AdVideoApplication.logger.i(LOG_TAG + "#stopInternalTimer",
					"Stopping the logging timer.");
			_handler.removeCallbacks(_internalTimer);
		}
	}

	public void startLogging() {
		startInternalTimer();
	}

	public void stopLogging() {
		stopInternalTimer();
	}

	public String getHost() {
		return _host;
	}

	public void setHost(String host) {
		_host = host;
	}

	public int getPort() {
		return _port;
	}

	public void setPort(int port) {
		_port = port;
	}

	public void setSessionTag(String tag) {
		if (tag == null) {
			dispatchError("Session-tag cannot be null or empty string. Using the default log session tag.");
		} else {
			try {
				_tag = computeSessionTag(tag);
				return;
			} catch (RuntimeException exception) {
				dispatchError(exception.getMessage());
				// compute the default value for the log-session tag
			}
		}

		_tag = computeDefaultSessionTag();
	}

	public String getSessionTag() {
		return _tag;
	}

	public void addListener(Listener listener) {
		_listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		for (Listener aListener : _listeners) {
			if (aListener == listener) {
				_listeners.remove(listener);
			}
		}
	}

	private void dispatchError(String errMsg) {
		for (Listener listener : _listeners) {
			listener.onError(errMsg);
		}
	}

	private void sendNewDataToServer(String host, int port) {
		String postParams = getNewLogData();

		if (postParams == null) {
			return;
		}

		final AsyncHttpHandler handler = new AsyncHttpHandler();

		String url = "http://" + host + ":" + port + "/logs";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json; charset=utf-8");
		AsyncHttpConnection.createPostConnection(url, headers, postParams,
				LOGGING_REQUEST_TIMEOUT, handler);
	}

	private String computeDefaultSessionTag() {
		return PLATFORM + "-" + computeSessionTimeStamp();
	}

	private String computeSessionTimeStamp() {
		return System.currentTimeMillis() + "";
	}

	private String translateSpecialToken(String token) {
		if (token.equals("platform")) {
			return PLATFORM;
		}

		if (token.equals("timestamp")) {
			return computeSessionTimeStamp();
		}

		throw new RuntimeException("Unsupported special token: " + token
				+ ". Using the default session tag value.");
	}

	private String computeSessionTag(String rawTag) {
		StringTokenizer st = new StringTokenizer(rawTag, "{}", true);
		StringBuilder sb = new StringBuilder();
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			if (token.equals("{")) {
				String specialToken = st.nextToken();
				sb.append(translateSpecialToken(specialToken));

				specialToken = st.nextToken();
				if (!specialToken.equals("}")) {
					throw new RuntimeException(
							"Invalid format for the session-tag string. Using the default session tag value.");
				}
			} else {
				sb.append(token);
			}
		}

		return sb.toString();
	}

	private String getNewLogData() {
		NotificationHistory notificationHistory = _mediaPlayer
				.getNotificationHistory();
		if (notificationHistory == null) {
			return null;
		}

		List<NotificationHistory.Item> items = notificationHistory
				.getNotifications();

		if (items.size() == 0) {
			return null;
		}

		long newestIndex = items.get(0).getIndex();
		StringBuilder sb = new StringBuilder("{");

		sb.append("\"session\":{");
		sb.append("\"name\":").append("\"").append(_tag).append("\",");
		sb.append("\"device\":{");
		sb.append("\"model\":").append("\"").append(DeviceInfo.getDeviceName())
				.append("\",");
		sb.append("\"os\":").append("\"").append(DeviceInfo.getOSVersion())
				.append("\"");
		sb.append("}, \"stream\":{");
		sb.append("\"url\":").append("\"")
				.append(_mediaPlayer.getCurrentItem().getResource().getUrl())
				.append("\",");
		sb.append("\"type\":").append("\"")
				.append(_mediaPlayer.getCurrentItem().getResource().getType())
				.append("\"");
		sb.append("},\"version\":{");
		sb.append("\"psdk\":").append("\"").append(Version.getDescription())
				.append("\",");
		sb.append("\"ave\":").append("\"").append(Version.getAVEVersion())
				.append("\"");
		sb.append("}},");

		sb.append("\"logItems\":[");
		if (newestIndex > lastLoggedNotificationIndex) {
			for (NotificationHistory.Item item : items) {
				if (item.getIndex() > lastLoggedNotificationIndex) {
					sb.append(item).append(",");
				}
			}

			sb.deleteCharAt(sb.length() - 1);
		} else {
			return null;
		}

		sb.append("]}");

		lastLoggedNotificationIndex = newestIndex;

		return sb.toString();
	}

	static class AsyncHttpHandler extends Handler {
		@Override
		public void handleMessage(Message message) {

			switch (message.what) {
			case AsyncHttpConnection.DID_START:
				break;

			case AsyncHttpConnection.DID_SUCCEED:
				break;

			case AsyncHttpConnection.DID_ERROR:
				AdVideoApplication.logger.i(LOG_TAG + "#logToServer",
						"Error sending data to logging server.");
				break;
			}
		}
	}
}
