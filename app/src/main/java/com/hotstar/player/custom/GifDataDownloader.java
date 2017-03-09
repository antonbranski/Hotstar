package com.hotstar.player.custom;

import android.os.AsyncTask;
import android.util.Log;

import com.hotstar.player.adplayer.AdVideoApplication;

public class GifDataDownloader extends AsyncTask<String, Void, byte[]> {

    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME  + GifDataDownloader.class.getSimpleName();

    public GifDataDownloader() {
    }

    @Override
    protected byte[] doInBackground(final String... params) {
        final String gifUrl = params[0];

        if (gifUrl == null)
            return null;

        byte[] gif = null;
        try {
            gif = ByteArrayHttpClient.get(gifUrl);
        } catch (OutOfMemoryError e) {
            AdVideoApplication.logger.e(LOG_TAG + "#doInBackground: " + gifUrl, e.getMessage());
        }

        return gif;
    }
}
