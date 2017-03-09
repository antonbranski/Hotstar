package com.hotstar.player.events;

import com.hotstar.player.model.TransitionDetailFragmentModel;

public class GoToDetailFragmentEvent{

    TransitionDetailFragmentModel model = null;

    public GoToDetailFragmentEvent(TransitionDetailFragmentModel model) {
        this.model = model;
    }

    public TransitionDetailFragmentModel getModel() {
        return model;
    }
}