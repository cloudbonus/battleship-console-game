package com.github.cloudbonus.states;

import com.github.cloudbonus.stateMachine.EnterState;
import com.github.cloudbonus.stateMachine.StateMachine;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.user.User;

public class GameOverState implements EnterState {
    public GameOverState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }

    private final StateMachine stateMachine;
    @Override
    public void enter() {
        resetUser();
        enterPrepareGameState();
    }

    private void resetUser(){
        User firstUser = HumanPlayerProvider.getInstance();
        firstUser.getLeftBoard().resetMap();
        firstUser.getRightBoard().resetMap();
    }

    private void enterPrepareGameState(){
        stateMachine.changeState(PrepareGameState.class);
    }
}
