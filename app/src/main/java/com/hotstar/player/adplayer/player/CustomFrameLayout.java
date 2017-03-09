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

package com.hotstar.player.adplayer.player;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Custom FrameLayout that offers the possibility to listen for size change
 * events.
 * 
 */
public class CustomFrameLayout extends FrameLayout {
	private final List<OnSizeChangeListener> sizeChangeEventListeners = new ArrayList<OnSizeChangeListener>();

	/**
	 * Notifies the listeners that layout size has changed.
	 */
	public interface OnSizeChangeListener {
		void onSizeChanged();
	}

	public void addOnSizeChangeListener(OnSizeChangeListener listener) {
		sizeChangeEventListeners.add(listener);
	}

	public void removeOnSizeChangeEventListener(OnSizeChangeListener listener) {
		sizeChangeEventListeners.remove(listener);
	}

	/**
	 * Dispatch size changed event.
	 */
	private void dispatchOnSizeChangeEvent() {
		for (OnSizeChangeListener listener : sizeChangeEventListeners) {
			listener.onSizeChanged();
		}
	}

	public CustomFrameLayout(Context context) {
		super(context);
	}

	public CustomFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Notify listeners that size has changed.
		dispatchOnSizeChangeEvent();
	}
}
