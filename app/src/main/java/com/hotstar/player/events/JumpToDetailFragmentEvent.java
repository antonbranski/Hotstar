package com.hotstar.player.events;

import com.hotstar.player.model.TransitionDetailFragmentModel;

public class JumpToDetailFragmentEvent{

    TransitionDetailFragmentModel model = null;

    public JumpToDetailFragmentEvent(TransitionDetailFragmentModel model) {
        this.model = model;
    }

    public TransitionDetailFragmentModel getModel() {
        return model;
    }
}