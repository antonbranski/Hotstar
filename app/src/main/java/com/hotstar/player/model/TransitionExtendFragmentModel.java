package com.hotstar.player.model;

public class TransitionExtendFragmentModel extends TransitionFragmentModel{

    public TransitionExtendFragmentModel(int targetFragmentType) {
        super(null, TransitionFragmentModel.INVALID, TransitionFragmentModel.INVALID, targetFragmentType, TransitionFragmentModel.INVALID);
    }

    public TransitionExtendFragmentModel(int targetFragmentType, int targetSubFragmentType) {
        super(null, TransitionFragmentModel.INVALID, TransitionFragmentModel.INVALID, targetFragmentType, targetSubFragmentType);
    }


}