package com.github.cloudbonus.states;

import com.github.cloudbonus.board.ShipPlacementManager;
import com.github.cloudbonus.stateMachine.*;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class PrepareBoardModeState implements EnterState {
    public PrepareBoardModeState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }

    private final StateMachine stateMachine;

    @Override
    public void enter(){
        prepareBoard();
        enterGameLoopState();
    }

    private void prepareBoard() {
        ShipPlacementManager manager = new ShipPlacementManager();
        User firstUser = HumanPlayerProvider.getInstance();

        ConsoleInformationManager.printGameMode();
        manager.setBoard(firstUser.getLeftBoard());
        String selectedMode = UserInteractionManager.getABSelectionFromInput();
        manager.placeShipsOnBoard(selectedMode);
    }

    private void enterGameLoopState(){
        stateMachine.changeState(GameLoopState.class);
    }
}
