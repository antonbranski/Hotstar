package com.hotstar.player.events;

import com.hotstar.player.model.TransitionExtendFragmentModel;

public class BackToExtendFragmentEvent {

    TransitionExtendFragmentModel model = null;

    public BackToExtendFragmentEvent(TransitionExtendFragmentModel model) {
        this.model = model;
    }

    public TransitionExtendFragmentModel getModel() {
        return model;
    }
}