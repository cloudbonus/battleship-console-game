package com.github.battleship.game.sm.state.impl;

import com.github.battleship.entity.player.Human;
import com.github.battleship.game.sm.StateMachine;
import com.github.battleship.game.sm.state.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameOverState implements State {

    private final StateMachine stateMachine;
    private final Human humanPlayer;


    @Override
    public void enter() {
        resetUser();
        enterGameModeSelectionState();
    }

    private void resetUser(){
        if (this.humanPlayer.getBattleController().isMatchFinished()) {
            this.humanPlayer.getBattleController().printMatchStats();
        }

        this.humanPlayer.getBattleController().reset();
        this.humanPlayer.getLeftBoard().resetMap();
        this.humanPlayer.getRightBoard().resetMap();
    }

    private void enterGameModeSelectionState(){
        this.stateMachine.changeState(GameModeSelectionState.class);
    }
}
