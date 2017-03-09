package com.hotstar.player.events;


import com.hotstar.player.webservice.ErrorType;

public class HotStarApiErrorEvent
{
	Throwable mThrowable;
	ErrorType mErrorType;

	public HotStarApiErrorEvent(ErrorType errorType, Throwable throwable)
	{
		mErrorType = errorType;
		mThrowable = throwable;
	}

	public Throwable getThrowable()
	{
		return mThrowable;
	}

	public ErrorType getErrorType()
	{
		return mErrorType;
	}

	@Override
	public String toString()
	{
		return "HotStarApiErrorEvent{" + "mErrorType=" + mErrorType + ", mErrorMessage='" + mThrowable + '\'' +'}';
	}
}
