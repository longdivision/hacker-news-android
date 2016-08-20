package com.hexforhn.hex.activity.frontpage;

import com.hexforhn.hex.util.statemachine.StateMachine;
import com.hexforhn.hex.util.statemachine.StateMachineHandler;

public class FrontPageState implements StateMachineHandler<FrontPageState.State> {
    public enum State {
        INACTIVE, LOADING, LOADED, REFRESHING, NETWORK_UNAVAILABLE
    }
    public enum Event {
        RESTORED, LOAD_REQUESTED, LOAD_SUCCEEDED, LOAD_FAILED
    }

    private FrontPageStateHandler mHandler;
    private StateMachine<State, Event> mStateMachine;

    public FrontPageState(FrontPageStateHandler handler) {
        mHandler = handler;
        mStateMachine = new StateMachine<>(this, State.INACTIVE);
        mStateMachine.addTransition(Event.RESTORED, State.INACTIVE, State.LOADED);
        mStateMachine.addTransition(Event.LOAD_REQUESTED, State.INACTIVE, State.LOADING);
        mStateMachine.addTransition(Event.LOAD_REQUESTED, State.NETWORK_UNAVAILABLE, State.LOADING);
        mStateMachine.addTransition(Event.LOAD_REQUESTED, State.LOADED, State.REFRESHING);
        mStateMachine.addTransition(Event.LOAD_REQUESTED, State.NETWORK_UNAVAILABLE, State.REFRESHING);
        mStateMachine.addTransition(Event.LOAD_SUCCEEDED, State.LOADING, State.LOADED);
        mStateMachine.addTransition(Event.LOAD_SUCCEEDED, State.REFRESHING, State.LOADED);
        mStateMachine.addTransition(Event.LOAD_FAILED, State.LOADING, State.NETWORK_UNAVAILABLE);
        mStateMachine.addTransition(Event.LOAD_FAILED, State.REFRESHING, State.NETWORK_UNAVAILABLE);
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
            case REFRESHING:
                mHandler.onEnterRefresh();
                break;
            case NETWORK_UNAVAILABLE:
                mHandler.onEnterUnavailable();
                break;
        }
    }
}
