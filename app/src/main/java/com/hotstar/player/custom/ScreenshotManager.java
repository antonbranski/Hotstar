package com.hotstar.player.custom;

import android.graphics.Bitmap;
import android.view.View;

public class ScreenshotManager {
    public static Bitmap takeScreenShot(View v) {
        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return bitmap;
    }
}