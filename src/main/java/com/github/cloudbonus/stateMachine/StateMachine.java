package com.github.cloudbonus.stateMachine;

import java.util.HashMap;
import java.util.Map;

public class StateMachine {
    private final Map<Class<?>, State> states = new HashMap<>();
    private State currentState;

    public void addState(State state) {
        Class<?> type = state.getClass();

        if (isStateExists(type))
            return;

        this.states.put(type, state);
    }

    public void changeState(Class<? extends State> stateClass) {
        if (!isStateExists(stateClass))
            return;

        if (this.currentState instanceof ExitState) {
            ((ExitState) this.currentState).exit();
        }

        this.currentState = this.states.get(stateClass);

        if (this.currentState instanceof EnterState) {
            ((EnterState) this.currentState).enter();
        }
    }

    private boolean isStateExists(Class<?> type) {
        return this.states.containsKey(type);
    }
}
