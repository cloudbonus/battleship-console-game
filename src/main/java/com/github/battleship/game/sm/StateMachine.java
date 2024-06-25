package com.github.battleship.game.sm;

import com.github.battleship.game.sm.state.State;

/**
 * @author Raman Haurylau
 */
public interface StateMachine {
    void addState(State state);
    void changeState(Class<? extends State> stateClass);
}
