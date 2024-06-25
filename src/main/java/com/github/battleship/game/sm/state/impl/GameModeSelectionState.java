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
public class GameModeSelectionState implements State {

    private static final String A_MODE = "A";

    private final StateMachine stateMachine;
    private final Human humanPlayer;

    @Override
    public void enter(){
        chooseGameMode();
    }

    private void chooseGameMode() {
        ConsoleDisplayManager.printGameSetup(this.humanPlayer);

        String selectedMode = UserInteractionManager.getABSelectionFromInput();

        if (A_MODE.equals(selectedMode)) {
            enterStartSingleplayerModeState();
        } else {
            enterStartMultiplayerModeState();
        }
    }

    private void enterStartSingleplayerModeState(){
        this.stateMachine.changeState(StartSingleplayerModeState.class);
    }

    private void enterStartMultiplayerModeState(){
        this.stateMachine.changeState(StartMultiplayerModeState.class);
    }
}
