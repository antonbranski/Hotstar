package com.hotstar.player.custom;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class ByteArrayHttpClient {
    private static final String TAG = "ByteArrayHttpClient";
    private static OkHttpClient client = new OkHttpClient();

    public static byte[] get(final String urlString) {
        InputStream in = null;
        try {
            final String decodedUrl = URLDecoder.decode(urlString, "UTF-8");
            final URL url = new URL(decodedUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            in = connection.getInputStream();
            return streamToBytes(in);
        } catch (final MalformedURLException e) {
            Log.d(TAG, "Malformed URL", e);
        } catch (final OutOfMemoryError e) {
            Log.d(TAG, "Out of memory", e);
        } catch (final UnsupportedEncodingException e) {
            Log.d(TAG, "Unsupported encoding", e);
        } catch (final IOException e) {
            Log.d(TAG, "IO exception", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException ignored) {
                }
            }
        }
        return null;
    }

    private static byte[] streamToBytes(InputStream is) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = is.read(buffer)) >= 0) {
                os.write(buffer, 0, len);
            }
        } catch (java.io.IOException e) {
        }
        return os.toByteArray();
    }

}