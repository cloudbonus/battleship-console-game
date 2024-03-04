package com.github.cloudbonus.states;

import com.github.cloudbonus.board.BasicBoard;
import com.github.cloudbonus.board.CompleteBoard;
import com.github.cloudbonus.board.ship.ShipPlacementManager;
import com.github.cloudbonus.game.BattleController;
import com.github.cloudbonus.stateMachine.EnterState;
import com.github.cloudbonus.stateMachine.StateMachine;
import com.github.cloudbonus.user.BotPlayer;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class StartSingleplayerModeState implements EnterState {
    public StartSingleplayerModeState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }
    private static final long sleepTime = 1000;
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

        BattleController playerBattleController = new BattleController();
        playerBattleController.setUser(this.user);
        playerBattleController.setOpponentName(bot.getName());

        BattleController botBattleController = new BattleController();
        botBattleController.setUser(setupBot(manager));
        botBattleController.setOpponentName(this.user.getName());
        botBattleController.disableConsoleOutput();

        boolean gameOn = true;

        while (gameOn) {
            gameOn = playTurn(playerBattleController, botBattleController);
            Thread.sleep(sleepTime);
            if (gameOn) {
                gameOn = playTurn(botBattleController, playerBattleController);
            }
            Thread.sleep(sleepTime);
        }
        enterGameOverState();
    }

    private boolean playTurn(BattleController attacker, BattleController defender) throws InterruptedException {
        String position = attacker.attack();
        do {
            String attackResponse = defender.processAttack(position);
            if (attackResponse.startsWith("LOST")) {
                attacker.processCellState(attackResponse);

                String name = attacker.getUserName();
                boolean isOutputOff = attacker.isDisableConsoleOutput();

                ConsoleInformationManager.printMatchResult(isOutputOff, name);
                return false;
            }
            position = attacker.processCellState(attackResponse);
            Thread.sleep(sleepTime);
        } while (!"END_TURN".equals(position));
        return true;
    }

    private void enterGameOverState(){
        this.stateMachine.changeState(GameOverState.class);
    }
}
