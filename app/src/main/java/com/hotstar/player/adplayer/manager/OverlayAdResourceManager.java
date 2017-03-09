package com.hotstar.player.adplayer.manager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.felipecsl.gifimageview.library.GifImageView;
import com.hotstar.player.R;
import com.hotstar.player.adplayer.AdVideoApplication;
import com.hotstar.player.custom.ByteArrayHttpClient;
import com.nineoldandroids.animation.Animator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class OverlayAdResourceManager implements ComponentCallbacks2 {
    private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME  + OverlayAdResourceManager.class.getSimpleName();

    private static OverlayAdResourceManager instance = null;
    private BitmapLruCache bmpCache;
    private LruCache<String, WeakReference<ByteBuffer>> gifCache;
    private Context mContext = null;

    public static OverlayAdResourceManager createInstance(Context context) {
        if (instance == null)
            instance = new OverlayAdResourceManager(context);

        return instance;
    }

    public static OverlayAdResourceManager getInstance() {
        return instance;
    }

    public OverlayAdResourceManager(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int maxKb = am.getMemoryClass() * 1024;
        int limitKb = maxKb / 16; // 1/8th of total ram
        bmpCache = new BitmapLruCache(limitKb);
        gifCache = new LruCache<String, WeakReference<ByteBuffer>>(limitKb);
        mContext = context;
    }

    public void display(String url, ImageView imageview) {
        Bitmap image = bmpCache.get(url);
        if (image != null) {
            AdVideoApplication.logger.i(LOG_TAG + "#display(cached) : Image URL", url);
            imageview.setImageBitmap(image);
            imageview.clearAnimation();
            fade_left_to_right(imageview);
        }
        else {
            AdVideoApplication.logger.i(LOG_TAG + "#display(non-cached) : Image URL", url);
            new SetImageTask(imageview).execute(url);
        }
    }

    public void show(ImageView imageView) {
        if (imageView == null)
            return;

        imageView.clearAnimation();
        fade_left_to_right(imageView);
    }

    public void hide(ImageView imageView) {
        if (imageView == null)
            return;

        fade_right_to_left(imageView);
    }

    public void preloadImage(String url) {
        AdVideoApplication.logger.i(LOG_TAG + "#preloadImage: ", url);
        Bitmap image = bmpCache.get(url);
        if (image == null) {
            new SetImageTask(null).execute(url);
        }
    }

    public void display(String url, GifImageView gifView) {
        WeakReference<ByteBuffer> wrappedReference = gifCache.get(url);
        if (wrappedReference == null) {
            AdVideoApplication.logger.i(LOG_TAG + "#display(non-cached) : Gif URL", url);
            new SetGifViewTask(gifView).execute(url);
            return;
        }

        ByteBuffer wrapped = wrappedReference.get();
        if (wrapped == null) {
            AdVideoApplication.logger.i(LOG_TAG + "#display(non-cached) : Gif URL", url);
            new SetGifViewTask(gifView).execute(url);
        }
        else {
            AdVideoApplication.logger.i(LOG_TAG + "#display(cached) : Gif URL", url);
            if (gifView != null) {
                gifView.setBytes(wrapped.array());
                gifView.startAnimation();
                fade_left_to_right(gifView);
            }
        }
    }

    public void show(GifImageView gifImageView) {
        if (gifImageView == null)
            return;

        gifImageView.startAnimation();
        fade_left_to_right(gifImageView);
    }

    public void hide(GifImageView gifImageView) {
        if (gifImageView == null)
            return;

        gifImageView.stopAnimation();
        fade_right_to_left(gifImageView);
    }

    public void preloadGif(String url) {
        AdVideoApplication.logger.i(LOG_TAG + "#preloadGif: ", url);
        WeakReference<ByteBuffer> wrapped_weakreference = gifCache.get(url);
        if (wrapped_weakreference == null) {
            new SetGifViewTask(null).execute(url);
        }
        else {
            ByteBuffer wrapped = wrapped_weakreference.get();
            if (wrapped == null) {
                new SetGifViewTask(null).execute(url);
            }
        }
    }

    /**
     * fade right to left
     */
    public void fade_right_to_left(View view) {
        final View targetView = view;
        YoYo.with(Techniques.FadeOutLeft).duration(1000)
                .interpolate(new AccelerateDecelerateInterpolator())
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        targetView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(view);
    }

    /**
     * fade left to right
     */
    public void fade_left_to_right(View view) {
        final View targetView = view;
        YoYo.with(Techniques.FadeInLeft).duration(1000)
                .interpolate(new AccelerateDecelerateInterpolator())
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        targetView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(view);
    }

    private class BitmapLruCache extends LruCache<String, Bitmap> {

        public BitmapLruCache(int maxSize) {
            super(maxSize);
        }
    }

    /**
     * Set Image Task for ImageView
     *
     */
    private class SetImageTask extends AsyncTask<String, Void, Integer> {
        private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME  + SetImageTask.class.getSimpleName();

        private ImageView imageview;
        private Bitmap bmp;

        public SetImageTask(ImageView imageView) {
            this.imageview = imageView;
        }
        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {
                bmp = getBitmapFromURL(url);
                if (bmp != null) {
                    bmpCache.put(url, bmp);
                }
                else {
                    return 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if ((result == 1) && (imageview != null)){
                imageview.setImageBitmap(bmp);
                imageview.clearAnimation();
                fade_left_to_right(imageview);
            }
            super.onPostExecute(result);
        }

        private Bitmap getBitmapFromURL(String src) {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                AdVideoApplication.logger.i(LOG_TAG + "#getBitmapFromURL", e.getMessage());
                return null;
            }
        }

    }

    /**
     * Set GifView Task for GifView
     *
     */
    private class SetGifViewTask extends AsyncTask<String, Void, Integer> {
        private final String LOG_TAG = AdVideoApplication.LOG_APP_NAME  + SetGifViewTask.class.getSimpleName();
        private GifImageView gifImageView;
        private ByteBuffer wrappedBytes;

        public SetGifViewTask(GifImageView gifView) {
            this.gifImageView = gifView;
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {
                byte[] gifBytes = getGifBytesFromURL(url);
                if (gifBytes != null) {
                    wrappedBytes = ByteBuffer.wrap(gifBytes);
                    gifCache.put(url, new WeakReference<ByteBuffer>(wrappedBytes));
                }
                else {
                    return 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if ((result == 1) && (gifImageView != null)){
                gifImageView.setBytes(wrappedBytes.array());
                gifImageView.startAnimation();
                fade_left_to_right(gifImageView);
            }
            super.onPostExecute(result);
        }

        private byte[] getGifBytesFromURL(String src) {
            try {
                if (src == null)
                    return null;

                byte[] gif = null;
                try {
                    gif = ByteArrayHttpClient.get(src);
                } catch (OutOfMemoryError e) {
                    AdVideoApplication.logger.e(LOG_TAG + "#getGifBytesFromURL: " + src, e.getMessage());
                }

                return gif;
            } catch (OutOfMemoryError e) {
                AdVideoApplication.logger.i(LOG_TAG + "#getGifBytesFromURL", e.getMessage());
                return null;
            }
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(int level) {
        if (level >= TRIM_MEMORY_MODERATE) {
            bmpCache.evictAll();
        }
        else if (level >= TRIM_MEMORY_BACKGROUND) {
            bmpCache.trimToSize(bmpCache.size() / 2);
        }
    }
}