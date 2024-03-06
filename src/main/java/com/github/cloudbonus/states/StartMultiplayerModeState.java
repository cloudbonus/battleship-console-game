package com.github.cloudbonus.states;

import com.github.cloudbonus.board.ship.ShipPlacementManager;
import com.github.cloudbonus.client.BattleshipGameClientEndpoint;
import com.github.cloudbonus.game.BattleController;
import com.github.cloudbonus.server.BattleshipGameServerEndpoint;
import com.github.cloudbonus.stateMachine.*;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleDisplayManager;
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

        ConsoleDisplayManager.printMultiplayerSetup();
        String selectedMode = UserInteractionManager.getABSelectionFromInput();

        UserInteractionManager.setPortInterpreter();
        System.out.print("Please provide the port number within the range of 1500 to 8000: ");
        int port = UserInteractionManager.getPortFromInput();
        ConsoleDisplayManager.clearConsole();

        BattleController playerBattleController = new BattleController(user);

        if (A_MODE.equals(selectedMode)) {
            BattleshipGameServerEndpoint.startServer(port, playerBattleController);
        } else {
            BattleshipGameClientEndpoint.startClient(port, playerBattleController);
        }
        BattleController.waitForUserInput();
        if (playerBattleController.isMatchFinished()) {
            playerBattleController.printMatchStats();
        }
    }

    private void enterGameOverState() {
        this.stateMachine.changeState(GameOverState.class);
    }
}
