/*******************************************************************************
 * ADOBE CONFIDENTIAL
 *  ___________________
 *
 *  Copyright 2014 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe Systems Incorporated and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe Systems Incorporated and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe Systems Incorporated.
 ******************************************************************************/

package com.hotstar.player.adplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import com.hotstar.player.adplayer.AdVideoApplication;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Asynchronously download an image and, post execute, update that image in an image view.
 * <p>
 *     To use, from OnCreate or getView method, call
 * {@code new DownloadImageTask(imageViewObject).execute(imageUrlString)}
 * </p>
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
{
    private static final String LOG_TAG = AdVideoApplication.LOG_APP_NAME + "DownloadImageTask";

    private static final int cacheSize = 1024 * 1024; // 1 MB cache
    private static LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };

    private ImageView imageView;

    public DownloadImageTask(ImageView image)
    {
        this.imageView = image;
    }

    protected Bitmap doInBackground(String... urls)
    {
        String imageUrl = urls[0];
        Bitmap bitmap;

        // check memory cache for bitmap first
        if ((bitmap = memoryCache.get(imageUrl)) == null)
        {

            try
            {
                URL url = new URL(imageUrl);

                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                InputStream in = new BufferedInputStream(connection.getInputStream());
                bitmap = BitmapFactory.decodeStream(in);
            }
            catch (SocketTimeoutException timeout)
            {
                AdVideoApplication.logger.e(LOG_TAG + "#doInBackground",
                        "Timeout occured while downloading image from " + imageUrl + ". " + timeout.getMessage());
            }
            catch (Exception e)
            {
                AdVideoApplication.logger.e(LOG_TAG + "#doInBackground",
                        "Error downloading image from " + imageUrl + ". " + e.getMessage());
            }

            // add bitmap to memory cache
            synchronized (memoryCache)
            {
                if (bitmap != null && memoryCache.get(imageUrl) == null)
                {
                    memoryCache.put(imageUrl, bitmap);
                }
            }

        }

        return bitmap;
    }

    protected void onPostExecute(Bitmap result)
    {
        if (result != null)
        {
            imageView.setImageBitmap(result);
        }

    }
}

