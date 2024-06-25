package com.github.battleship.game.sm.state.impl;

import com.github.battleship.entity.board.ship.util.ShipPlacementManager;
import com.github.battleship.entity.player.Human;
import com.github.battleship.game.BattleController;
import com.github.battleship.game.client.BattleshipGameClientEndpoint;
import com.github.battleship.game.server.BattleshipGameServerEndpoint;
import com.github.battleship.game.sm.StateMachine;
import com.github.battleship.game.sm.state.State;
import com.github.battleship.game.util.ConsoleDisplayManager;
import com.github.battleship.game.util.UserInteractionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartMultiplayerModeState implements State {

    private static final String A_MODE = "A";

    private final StateMachine stateMachine;
    private final Human humanPlayer;
    private final ShipPlacementManager shipPlacementManager;

    @Override
    public void enter() {
        startMultiplayerGame();
        enterGameOverState();
    }

    private void startMultiplayerGame() {
        this.shipPlacementManager.setupShips(this.humanPlayer.getLeftBoard());

        ConsoleDisplayManager.printMultiplayerSetup();

        String selectedMode = UserInteractionManager.getABSelectionFromInput();

        UserInteractionManager.setPortInterpreter();

        System.out.print("Please provide the port number within the range of 1500 to 8000: ");
        int port = UserInteractionManager.getPortFromInput();

        ConsoleDisplayManager.clearConsole();

        if (A_MODE.equals(selectedMode)) {
            BattleshipGameServerEndpoint.startServer(port, this.humanPlayer.getBattleController());
        } else {
            BattleshipGameClientEndpoint.startClient(port, this.humanPlayer.getBattleController());
        }

        BattleController.waitForUserInput();
        BattleshipGameServerEndpoint.resetServer();
    }

    private void enterGameOverState() {
        this.stateMachine.changeState(GameOverState.class);
    }
}
