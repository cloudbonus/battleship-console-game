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
        User user = HumanPlayerProvider.getInstance();
        ConsoleInformationManager.printGameSetup(user);
    }

    private void chooseGameMode() {
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
