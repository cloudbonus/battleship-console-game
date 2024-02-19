package com.github.cloudbonus;

import com.github.cloudbonus.stateMachine.*;
import com.github.cloudbonus.states.*;

public class App
{
    private static StateMachine stateMachine;

    public static void main( String[] args ) {
        createAndSetupStateMachine();
        enterPrepareGameState();
    }



    private static void createAndSetupStateMachine(){
        stateMachine = new StateMachine();

        PrepareGameState prepareGameState = new PrepareGameState(stateMachine);
        GameModeSelectionState gameModeSelectionState = new GameModeSelectionState(stateMachine);
        PrepareBoardModeState prepareBoardModeState = new PrepareBoardModeState(stateMachine);
        StartMultiplayerModeState startMultiplayerModeState = new StartMultiplayerModeState(stateMachine);
        GameLoopState gameLoopState = new GameLoopState(stateMachine);
        GameOverState gameOverState = new GameOverState(stateMachine);

        stateMachine.addState(prepareGameState);
        stateMachine.addState(gameModeSelectionState);
        stateMachine.addState(prepareBoardModeState);
        stateMachine.addState(startMultiplayerModeState);
        stateMachine.addState(gameLoopState);
        stateMachine.addState(gameOverState);
    }

    private static void enterPrepareGameState(){
        stateMachine.changeState(PrepareGameState.class);
    }
}