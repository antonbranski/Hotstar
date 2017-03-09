package com.hotstar.player.events;

import com.hotstar.player.model.TransitionExtendFragmentModel;

public class GoToExtendFragmentEvent
{
    TransitionExtendFragmentModel model = null;

    public GoToExtendFragmentEvent(TransitionExtendFragmentModel model) {
        this.model = model;
    }

    public TransitionExtendFragmentModel getModel() {
        return model;
    }
}