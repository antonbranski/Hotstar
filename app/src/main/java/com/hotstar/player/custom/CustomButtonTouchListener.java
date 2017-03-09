package com.hotstar.player.custom;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

@SuppressLint ("NewApi")
public class CustomButtonTouchListener implements OnTouchListener {

    private static CustomButtonTouchListener globalInstance;

    private CustomButtonTouchListener () {
        // TODO Auto-generated constructor stub
    }

    public static CustomButtonTouchListener getInstance () {
        if (globalInstance == null)
            globalInstance = new CustomButtonTouchListener ();
        return globalInstance;
    }

    @Override
    public boolean onTouch (View arg0, MotionEvent arg1) {
        // TODO Auto-generated method stub
        if (arg1.getAction () == MotionEvent.ACTION_DOWN) {
            if (Build.VERSION.SDK_INT > 10)
                arg0.setAlpha (0.5f);
        } else {
            if (Build.VERSION.SDK_INT > 10)
                arg0.setAlpha (1.0f);
        }
        return false;
    }
}
