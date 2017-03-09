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

package com.hotstar.player.adplayer.utils.http;

import android.os.Handler;
import android.os.Message;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class AsyncHttpConnection implements Runnable {
	public static final int DEFAULT_CONNECTION_TIMEOUT = 30;

	public static final int DID_START = 0;
	public static final int DID_ERROR = 1;
	public static final int DID_SUCCEED = 2;

	enum HttpMethod {
		GET, POST
	}

	private String _url;
	private HttpMethod _method;
	private Handler _handler;
	private String _data;
	private int _timeout = 0;
	private HttpClient _httpClient;
	private boolean _isCanceled = false;
	private Map<String, String> _headers;

	private AsyncHttpConnection(HttpMethod method, String url,
			Map<String, String> headers, String data, int timeout,
			Handler handler) {
		_method = method;
		_url = url;
		_data = data;
		_timeout = timeout;
		_handler = handler;

		_isCanceled = false;
		_headers = headers;
	}

	private static void createConnection(HttpMethod method, String url,
			Map<String, String> headers, String data, int timeout,
			Handler handler) {
		AsyncHttpConnection connection = new AsyncHttpConnection(method, url,
				headers, data, timeout, handler);
		ConnectionManager.getInstance().push(connection);
	}

	public static void createGetConnection(String url,
			Map<String, String> headers, Handler handler) {
		createConnection(HttpMethod.GET, url, headers, null,
				DEFAULT_CONNECTION_TIMEOUT, handler);
	}

	public static void createGetConnection(String url,
			Map<String, String> headers, int timeout, Handler handler) {
		createConnection(HttpMethod.GET, url, headers, null, timeout, handler);
	}

	public static void createPostConnection(String url,
			Map<String, String> headers, String data, Handler handler) {
		createConnection(HttpMethod.POST, url, headers, data,
				DEFAULT_CONNECTION_TIMEOUT, handler);
	}

	public static void createPostConnection(String url,
			Map<String, String> headers, String data, int timeout,
			Handler handler) {
		createConnection(HttpMethod.POST, url, headers, data, timeout, handler);
	}

	public void cancel() {
		_isCanceled = true;
	}

	public void run() {
		_handler.sendMessage(Message.obtain(_handler,
				AsyncHttpConnection.DID_START));

		HttpParams httpParameters = new BasicHttpParams();
		// Set the timeout in milliseconds until a connection is established.
		HttpConnectionParams.setConnectionTimeout(httpParameters, _timeout);
		// Set the default socket timeout (SO_TIMEOUT)
		// in milliseconds which is the timeout for waiting for _data.
		HttpConnectionParams.setSoTimeout(httpParameters, _timeout);

		_httpClient = new DefaultHttpClient(httpParameters);
		try {
			HttpResponse response = null;
			switch (_method) {
			case GET:
				HttpGet httpGet = new HttpGet(_url);
				for (Map.Entry<String, String> header : _headers.entrySet()) {
					httpGet.setHeader(header.getKey(), header.getValue());
				}
				response = _httpClient.execute(httpGet);
				break;
			case POST:
				HttpPost httpPost = new HttpPost(_url);
				for (Map.Entry<String, String> header : _headers.entrySet()) {
					httpPost.setHeader(header.getKey(), header.getValue());
				}
				httpPost.setEntity(new StringEntity(_data));
				response = _httpClient.execute(httpPost);
				break;
			default:
				throw new IllegalArgumentException("Unsupported HTTP method: "
						+ _method.name());
			}

			processEntity(response.getEntity());
		} catch (Exception e) {
			_handler.sendMessage(Message.obtain(_handler,
					AsyncHttpConnection.DID_ERROR, e));
		}
		ConnectionManager.getInstance().didComplete(this);
	}

	private void processEntity(HttpEntity entity) throws IllegalStateException,
			IOException {
		if (_isCanceled) {
			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				entity.getContent()));

		String str;
		StringBuffer buff = new StringBuffer();
		while ((str = br.readLine()) != null) {
			buff.append(str);
		}
		br.close();

		Message message = Message
				.obtain(_handler, DID_SUCCEED, buff.toString());
		_handler.sendMessage(message);
	}
}
