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

/*
 *  PlayerClickableAdFragment class contains the UI Component for clickable ad and also
 *  the callback for ad user interaction.
 */

package com.hotstar.player.adplayer.player;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.hotstar.player.R;
import android.support.v4.app.Fragment;

public class PlayerClickableAdFragment extends Fragment {
	private ViewGroup viewGroup;
	private Button button;
	OnAdUserInteraction callback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		viewGroup = (ViewGroup) inflater.inflate(
				R.layout.fragment_player_clickable_ad, container, false);
		button = (Button) viewGroup.findViewById(R.id.clickButton);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				callback.onAdClick();
			}
		});
		viewGroup.setVisibility(View.INVISIBLE);
		return viewGroup;
	}

	public void hide() {
		viewGroup.setVisibility(View.INVISIBLE);
	}

	public void show() {
		viewGroup.setVisibility(View.VISIBLE);
	}

	public interface OnAdUserInteraction {
		public void onAdClick();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			callback = (OnAdUserInteraction) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAdUserInteraction");
		}
	}
}
