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

package com.hotstar.player.adplayer.drm;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.hotstar.player.R;

/**
 * Dialog box to inform the user that a DRM error has occurred, and guide them
 * on how to resolve the problem.
 * 
 */
public class DRMErrorDialog extends Dialog implements
		android.view.View.OnClickListener {

	public Activity activity;
	public Button okButton;

	private String mMessage;

	/**
	 * Construct a new DRMErrorDialog using the given activity. The dialog
	 * displays an error message passed in as "message" in the args bundle. The
	 * parent activity is finished when user clicks on the button to dismiss the
	 * dialog.
	 * 
	 * @param activity
	 */
	public DRMErrorDialog(Activity activity) {
		super(activity);
		this.activity = activity;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_drm_error);
		okButton = (Button) findViewById(R.id.okButton);
		okButton.setOnClickListener(this);
		TextView text = (TextView) findViewById(R.id.txt_drm_message);
		text.setText(mMessage);
		*/
	}

	@Override
	public void onClick(View v) {
		/*
		switch (v.getId()) {
		case R.id.okButton:
			break;
		default:
			break;
		}
		activity.finish();
		dismiss();
		*/
	}

	public void setArguments(Bundle args) {

		mMessage = args.getString("message");

	}

}
