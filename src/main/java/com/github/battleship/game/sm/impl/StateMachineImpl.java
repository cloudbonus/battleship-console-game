package com.github.battleship.game.sm.impl;

import com.github.battleship.game.sm.StateMachine;
import com.github.battleship.game.sm.state.State;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StateMachineImpl implements StateMachine {

    private final Map<Class<?>, State> states = new HashMap<>();

    public void addState(State state) {
        Class<?> type = state.getClass();

        if (isStateExists(type))
            return;

        this.states.put(type, state);
    }

    public void changeState(Class<? extends State> stateClass) {
        if (!isStateExists(stateClass)) {
            return;
        }

        this.states.get(stateClass).enter();
    }

    private boolean isStateExists(Class<?> type) {
        return this.states.containsKey(type);
    }
}
