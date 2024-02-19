package com.github.cloudbonus.states;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.github.cloudbonus.board.ShipPlacementManager;
import com.github.cloudbonus.client.BattleshipGameClientEndpoint;
import com.github.cloudbonus.server.BattleshipGameServerEndpoint;
import com.github.cloudbonus.stateMachine.*;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class StartMultiplayerModeState implements EnterState {
    public StartMultiplayerModeState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }
    
    private static final String A_MODE = "A";

    private StateMachine stateMachine;
    @Override
    public void enter(){
        startMultiplayerGame();
        enterGameModeSelectionState();
    }

    private void startMultiplayerGame() {
        ShipPlacementManager manager = new ShipPlacementManager();
        User firstUser = HumanPlayerProvider.getInstance();

        ConsoleInformationManager.printGameMode();
        manager.setBoard(firstUser.getLeftBoard());

        String placementMode = UserInteractionManager.getABSelectionFromInput();
        manager.placeShipsOnBoard(placementMode);

        ConsoleInformationManager.printMultiplayerSetup();
        String selectedMode = UserInteractionManager.getABSelectionFromInput();
        System.out.println("Please provide the port number within the range of 1500 to 8000: ");
        UserInteractionManager.setPortInterpreter();
        int port = UserInteractionManager.getPortFromInput();
        ConsoleInformationManager.clearConsole();

        if (A_MODE.equals(selectedMode)) {
            BattleshipGameServerEndpoint.startServer(port, firstUser);
        } else {
            BattleshipGameClientEndpoint.startClient(port, firstUser);
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Press any key to continue...");
            reader.readLine();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        firstUser.getLeftBoard().resetMap();
        firstUser.getRightBoard().resetMap();        
    }

    private void enterGameModeSelectionState(){
        stateMachine.changeState(GameModeSelectionState.class);
    }
}
