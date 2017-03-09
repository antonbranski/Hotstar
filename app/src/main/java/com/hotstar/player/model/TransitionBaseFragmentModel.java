package com.hotstar.player.model;

import com.hotstar.player.fragments.BaseFragment;

public class TransitionBaseFragmentModel extends TransitionFragmentModel{

    public TransitionBaseFragmentModel(int targetSubfragmentType) {
        super(null, TransitionFragmentModel.INVALID, TransitionFragmentModel.INVALID, BaseFragment.BASE_FRAGMENT, targetSubfragmentType);
    }
}