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

        states.put(type, state);
    }

    public void changeState(Class<? extends State> stateClass) {
        if (!isStateExists(stateClass))
            return;

        if (currentState instanceof ExitState) {
            ((ExitState) currentState).exit();
        }

        currentState = states.get(stateClass);

        if (currentState instanceof EnterState) {
            ((EnterState) currentState).enter();
        }
    }

    private boolean isStateExists(Class<?> type) {
        return states.containsKey(type);
    }
}
