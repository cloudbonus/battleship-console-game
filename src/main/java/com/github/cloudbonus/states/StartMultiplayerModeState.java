package com.github.cloudbonus.states;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.github.cloudbonus.board.ship.ShipPlacementManager;
import com.github.cloudbonus.client.BattleshipGameClientEndpoint;
import com.github.cloudbonus.game.BattleController;
import com.github.cloudbonus.server.BattleshipGameServerEndpoint;
import com.github.cloudbonus.stateMachine.*;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class StartMultiplayerModeState implements EnterState {
    public StartMultiplayerModeState(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    private static final String A_MODE = "A";

    private final StateMachine stateMachine;

    @Override
    public void enter() {
        startMultiplayerGame();
        enterGameOverState();
    }

    private void startMultiplayerGame() {
        User user = HumanPlayerProvider.getInstance();

        ShipPlacementManager manager = new ShipPlacementManager();
        manager.setupShips(user.getLeftBoard());

        ConsoleInformationManager.printMultiplayerSetup();
        String selectedMode = UserInteractionManager.getABSelectionFromInput();

        UserInteractionManager.setPortInterpreter();
        System.out.println("Please provide the port number within the range of 1500 to 8000: ");
        int port = UserInteractionManager.getPortFromInput();
        ConsoleInformationManager.clearConsole();

        BattleController battleController = new BattleController();
        battleController.setUser(user);

        if (A_MODE.equals(selectedMode)) {
            BattleshipGameServerEndpoint.startServer(port, battleController);
        } else {
            BattleshipGameClientEndpoint.startClient(port, battleController);
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Press any key to continue...");
            reader.readLine();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void enterGameOverState() {
        this.stateMachine.changeState(GameOverState.class);
    }
}
