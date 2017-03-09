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

package com.hotstar.player.adplayer.core;

import android.os.Handler;

import com.hotstar.player.adplayer.AdVideoApplication;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ContentRequest {
	private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "ContentRequest";
	private final int HTTP_STATUS_CODE_OK = 200;

	private final List<OnCompleteListener> onCompleteListeners = new CopyOnWriteArrayList<OnCompleteListener>();
	private final List<OnErrorListener> onErrorListeners = new CopyOnWriteArrayList<OnErrorListener>();

	private final DefaultHttpClient httpClient = new DefaultHttpClient();
	private final HttpContext localContext = new BasicHttpContext();
	protected HttpGet httpGet;

	public interface OnCompleteListener {
		public void onComplete(String response);
	}

	public void addOnCompleteListener(OnCompleteListener listener) {
		onCompleteListeners.add(listener);
	}

	public void removeOnCompleteListener(OnCompleteListener listener) {
		onCompleteListeners.remove(listener);
	}

	private void dispatchCompleteEvent(String response) {
		for (OnCompleteListener listener : onCompleteListeners) {
			listener.onComplete(response);
		}
	}

	public interface OnErrorListener {
		public void onError(String error);
	}

	public void addOnErrorListener(OnErrorListener listener) {
		onErrorListeners.add(listener);
	}

	public void removeOnErrorListener(OnErrorListener listener) {
		onErrorListeners.remove(listener);
	}

	private void dispatchErrorEvent(String error) {
		for (OnErrorListener listener : onErrorListeners) {
			listener.onError(error);
		}
	}

	private final Handler handler = new Handler();

	public void doRequest(final String targetUrl) {
		new Thread(new Runnable() {
			@Override
			public void run()
			{
				try
				{
					httpGet = new HttpGet(targetUrl);

					HttpResponse httpResponse = httpClient.execute(httpGet, localContext);
					final int statusCode = httpResponse.getStatusLine().getStatusCode();

					if (statusCode != HTTP_STATUS_CODE_OK) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								dispatchErrorEvent("Server status error: " + statusCode);
							}
						});
					}
					else {
						final String response = getResponseContent(httpResponse);
						if (response == null || response.isEmpty()) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									dispatchErrorEvent("No response from server");
								}
							});
						}
						else {
							handler.post(new Runnable() {
								@Override
								public void run() {
									dispatchCompleteEvent(response);
								}
							});
						}
					}

					// Consume content.
					httpResponse.getEntity().consumeContent();

				}
				catch (Exception e) {
					AdVideoApplication.logger.e(LOG_TAG + "#doRequest()", e.toString());
					final String errMessage = e.getMessage();
					handler.post(new Runnable() {
						@Override
						public void run() {
							dispatchErrorEvent(errMessage);
						}
					});
				}
			}
		}).start();
	}

	private String getResponseContent(HttpResponse httpResponse) throws IOException {
		String line;
		StringBuilder total = new StringBuilder();
		InputStream is = httpResponse.getEntity().getContent();

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		while ((line = rd.readLine()) != null) {
			total.append(line);
		}

		// Return full string
		return total.toString();
	}

	/**
	 * Releases allocated resources.
	 */
	public void shutdown() {
		if (httpClient != null) {
			httpClient.getConnectionManager().shutdown();
		}
	}
}
