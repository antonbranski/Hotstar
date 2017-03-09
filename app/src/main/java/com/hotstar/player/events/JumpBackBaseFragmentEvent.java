package com.hotstar.player.events;

import com.hotstar.player.model.TransitionBaseFragmentModel;

public class JumpBackBaseFragmentEvent {

    TransitionBaseFragmentModel model = null;

    public JumpBackBaseFragmentEvent(TransitionBaseFragmentModel model) {
        this.model = model;
    }

    public TransitionBaseFragmentModel getModel() {
        return model;
    }
}