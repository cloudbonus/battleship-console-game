package com.github.battleship.game.sm.state.impl;


import com.github.battleship.entity.player.Human;
import com.github.battleship.game.sm.StateMachine;
import com.github.battleship.game.sm.state.State;
import com.github.battleship.game.util.ConsoleDisplayManager;
import com.github.battleship.game.util.UserInteractionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrepareGameState implements State {

    private final StateMachine stateMachine;

    private final Human humanPlayer;

    @Override
    public void enter() {
        ConsoleDisplayManager.printHeader();
        setupUser();
        enterGameModeSelectionState();
    }

    private void setupUser() {
        String userName = UserInteractionManager.getInputNameFromUser();
        this.humanPlayer.setName(userName);
    }

    private void enterGameModeSelectionState() {
        this.stateMachine.changeState(GameModeSelectionState.class);
    }
}