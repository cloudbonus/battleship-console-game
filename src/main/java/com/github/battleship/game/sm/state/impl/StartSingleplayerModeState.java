package com.github.battleship.game.sm.state.impl;

import com.github.battleship.entity.board.ship.util.ShipPlacementManager;
import com.github.battleship.entity.player.Bot;
import com.github.battleship.entity.player.Human;
import com.github.battleship.game.BattleController;
import com.github.battleship.game.GameStatistics;
import com.github.battleship.game.sm.StateMachine;
import com.github.battleship.game.sm.state.State;
import com.github.battleship.game.util.ConsoleDisplayManager;
import com.github.battleship.game.util.UserInteractionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartSingleplayerModeState implements State {

    private final StateMachine stateMachine;
    private final Human humanPlayer;
    private final Bot botPlayer;
    private final ShipPlacementManager shipPlacementManager;

    @Override
    public void enter(){
        try {
            play();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void play() throws InterruptedException {
        this.shipPlacementManager.setupShips(this.humanPlayer.getLeftBoard());

        UserInteractionManager.setPositionInterpreter();

        this.shipPlacementManager.setBoard(this.botPlayer.getLeftBoard());
        this.shipPlacementManager.placeShipsAutomatically();

        this.humanPlayer.getBattleController().setOpponentName(this.botPlayer.getName());
        this.botPlayer.getBattleController().setOpponentName(this.humanPlayer.getName());


        boolean gameOn = true;

        GameStatistics.startGameTime();
        while (gameOn) {
            gameOn = playTurn(this.humanPlayer.getBattleController(), this.botPlayer.getBattleController());
            if (gameOn) {
                gameOn = playTurn(this.botPlayer.getBattleController(), this.humanPlayer.getBattleController());
            }
        }
        GameStatistics.endGameTime();
        BattleController.waitForUserInput();

        this.botPlayer.reset();

        enterGameOverState();
    }

    private boolean playTurn(BattleController attacker, BattleController defender) {
        String position = attacker.attack();
        while (!"END_TURN".equals(position)) {
            String attackResponse = defender.processAttack(position);
            position = attacker.processCellState(attackResponse);
            if (ConsoleDisplayManager.getReasonMessage().equals(position)) {
                String name = attacker.getUserName();
                boolean isOutputOff = attacker.isDisableConsoleOutput();
                ConsoleDisplayManager.printMatchResult(isOutputOff, name);
                return false;
            }
        }
        return true;
    }

    private void enterGameOverState(){
        this.stateMachine.changeState(GameOverState.class);
    }
}
