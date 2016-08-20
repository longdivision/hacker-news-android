package com.hexforhn.hex.activity.story;

import com.hexforhn.hex.util.statemachine.StateMachine;
import com.hexforhn.hex.util.statemachine.StateMachineHandler;

public class StoryState implements StateMachineHandler<StoryState.State> {
    public enum State {
        INACTIVE, LOADING, LOADED, UNAVAILABLE
    }
    public enum Event {
        RESTORED, LOAD_REQUESTED, LOAD_SUCCEEDED, LOAD_FAILED
    }

    private StoryStateHandler mHandler;
    private StateMachine<State, Event> mStateMachine;

    public StoryState(StoryStateHandler handler) {
        mHandler = handler;
        mStateMachine = new StateMachine<>(this, State.INACTIVE);
        mStateMachine.addTransition(Event.RESTORED, State.INACTIVE, State.LOADED);
        mStateMachine.addTransition(Event.LOAD_REQUESTED, State.INACTIVE, State.LOADING);
        mStateMachine.addTransition(Event.LOAD_REQUESTED, State.UNAVAILABLE, State.LOADING);
        mStateMachine.addTransition(Event.LOAD_REQUESTED, State.LOADED, State.LOADING);
        mStateMachine.addTransition(Event.LOAD_SUCCEEDED, State.LOADING, State.LOADED);
        mStateMachine.addTransition(Event.LOAD_FAILED, State.LOADING, State.UNAVAILABLE);
    }

    public boolean sendEvent(Event event) {
        return mStateMachine.sendEvent(event);
    }

    @Override
    public void onEnterState(State state) {
        switch(state) {
            case LOADING:
                mHandler.onEnterLoading();
                break;
            case LOADED:
                mHandler.onEnterLoaded();
                break;
            case UNAVAILABLE:
                mHandler.onEnterUnavailable();
                break;
        }
    }
}
