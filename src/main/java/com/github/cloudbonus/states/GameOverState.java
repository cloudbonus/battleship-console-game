package com.github.cloudbonus.states;

import com.github.cloudbonus.game.GameStatistics;
import com.github.cloudbonus.server.BattleshipGameServerEndpoint;
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
        GameStatistics.resetTurns();
        BattleshipGameServerEndpoint.resetServer();
        enterGameModeSelectionState();
    }

    private void resetUser(){
        User user = HumanPlayerProvider.getInstance();
        user.getLeftBoard().resetMap();
        user.getRightBoard().resetMap();
    }

    private void enterGameModeSelectionState(){
        this.stateMachine.changeState(GameModeSelectionState.class);
    }
}
