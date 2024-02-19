package com.github.cloudbonus.states;

import com.github.cloudbonus.stateMachine.*;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class GameModeSelectionState implements EnterState {
    public GameModeSelectionState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }

    private static final String A_MODE = "A";

    private final StateMachine stateMachine;
    @Override
    public void enter(){
        printGameSetup();
        chooseGameMode();
    }

    private void printGameSetup(){
        User firstUser = HumanPlayerProvider.getInstance();
        ConsoleInformationManager.printGameSetup(firstUser);
    }

    private void chooseGameMode() {
        String selectedMode = UserInteractionManager.getABSelectionFromInput();

        if (A_MODE.equals(selectedMode)) {
            enterPrepareBoardModeState();
        } else {
            enterStartMultiplayerPlayerMode();
        }
    }

    private void enterPrepareBoardModeState(){
        stateMachine.changeState(PrepareBoardModeState.class);
    }

    private void enterStartMultiplayerPlayerMode(){
        stateMachine.changeState(StartMultiplayerModeState.class);
    }
}