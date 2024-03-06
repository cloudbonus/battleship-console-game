package com.github.cloudbonus.states;

import com.github.cloudbonus.board.BasicBoard;
import com.github.cloudbonus.board.CompleteBoard;
import com.github.cloudbonus.board.ship.ShipPlacementManager;
import com.github.cloudbonus.game.BattleController;
import com.github.cloudbonus.game.GameStatistics;
import com.github.cloudbonus.stateMachine.EnterState;
import com.github.cloudbonus.stateMachine.StateMachine;
import com.github.cloudbonus.user.BotPlayer;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.util.ConsoleDisplayManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class StartSingleplayerModeState implements EnterState {
    public StartSingleplayerModeState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }
    private final StateMachine stateMachine;
    private User user;
    @Override
    public void enter(){
        this.user = HumanPlayerProvider.getInstance();
        ShipPlacementManager manager = new ShipPlacementManager();
        manager.setupShips(this.user.getLeftBoard());

        try {
            play(manager);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private User setupBot(ShipPlacementManager manager) {
        User bot  = new BotPlayer();
        bot.setName("Bot Alex");

        CompleteBoard leftBoard = new CompleteBoard();
        BasicBoard rightBoard = new BasicBoard();
        bot.setLeftBoard(leftBoard);
        bot.setRightBoard(rightBoard);

        manager.setBoard(bot.getLeftBoard());
        manager.placeShipsAutomatically();
        return bot;
    }

    private void play(ShipPlacementManager manager) throws InterruptedException {
        UserInteractionManager.setPositionInterpreter();
        User bot = setupBot(manager);

        BattleController playerBattleController = new BattleController(user);
        playerBattleController.setOpponentName(bot.getName());

        BattleController botBattleController = new BattleController(bot);
        botBattleController.setOpponentName(this.user.getName());
        botBattleController.disableConsoleOutput();

        boolean gameOn = true;

        GameStatistics.startGameTime();
        while (gameOn) {
            gameOn = playTurn(playerBattleController, botBattleController);
            if (gameOn) {
                gameOn = playTurn(botBattleController, playerBattleController);
            }
        }
        GameStatistics.endGameTime();
        BattleController.waitForUserInput();
        playerBattleController.printMatchStats();
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
