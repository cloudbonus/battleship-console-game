package com.github.cloudbonus;

import com.github.cloudbonus.stateMachine.*;
import com.github.cloudbonus.states.*;

public class App {
    private static StateMachine stateMachine;

    public static void main(String[] args) {
        createAndSetupStateMachine();
        enterPrepareGameState();
    }


    private static void createAndSetupStateMachine() {
        stateMachine = new StateMachine();

        PrepareGameState prepareGameState = new PrepareGameState(stateMachine);
        GameModeSelectionState gameModeSelectionState = new GameModeSelectionState(stateMachine);
        StartMultiplayerModeState startMultiplayerModeState = new StartMultiplayerModeState(stateMachine);
        StartSingleplayerModeState startSingleplayerModeState = new StartSingleplayerModeState(stateMachine);
        GameOverState gameOverState = new GameOverState(stateMachine);

        stateMachine.addState(prepareGameState);
        stateMachine.addState(gameModeSelectionState);
        stateMachine.addState(startMultiplayerModeState);
        stateMachine.addState(startSingleplayerModeState);
        stateMachine.addState(gameOverState);
    }

    private static void enterPrepareGameState() {
        stateMachine.changeState(PrepareGameState.class);
    }
}