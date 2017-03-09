package com.hotstar.player.events;


import com.hotstar.player.model.TransitionBaseFragmentModel;

public class BackToBaseFragmentEvent {

    TransitionBaseFragmentModel model = null;

    public BackToBaseFragmentEvent(TransitionBaseFragmentModel model) {
        this.model = model;
    }

    public TransitionBaseFragmentModel getModel() {
        return model;
    }
}