package com.hotstar.player.events;

import com.hotstar.player.adplayer.core.VideoItem;

import java.util.ArrayList;

public class ConnectorErrEvent {

	String mErrorMessage = null;

	public ConnectorErrEvent(String errorMessage){
		mErrorMessage = errorMessage;
	}

	public String getMessage() {
		return mErrorMessage;
	}
}
