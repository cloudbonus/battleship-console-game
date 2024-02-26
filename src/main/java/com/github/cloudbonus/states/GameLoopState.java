package com.github.cloudbonus.states;

import com.github.cloudbonus.board.BasicBoard;
import com.github.cloudbonus.board.CompleteBoard;
import com.github.cloudbonus.board.ShipPlacementManager;
import com.github.cloudbonus.service.GameService;
import com.github.cloudbonus.stateMachine.EnterState;
import com.github.cloudbonus.stateMachine.StateMachine;
import com.github.cloudbonus.user.BotPlayer;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.util.UserInteractionManager;

public class GameLoopState implements EnterState {
    public GameLoopState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }
    private static final long sleepTime = 1000;
    private final StateMachine stateMachine;
    private User firstUser;
    @Override
    public void enter(){
        firstUser = HumanPlayerProvider.getInstance();
        ShipPlacementManager manager = new ShipPlacementManager();

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
        User player = firstUser;
        User bot = setupBot(manager);

        GameService playerGameService = new GameService();
        playerGameService.setUser(player);
        playerGameService.setOpponentName(bot.getName());

        GameService botGameService = new GameService();
        botGameService.setUser(bot);
        botGameService.setOpponentName(player.getName());
        botGameService.disableConsoleOutput();

        boolean gameOn = true;

        while (gameOn) {
            gameOn = playTurn(playerGameService, botGameService);
            Thread.sleep(sleepTime);
            if (gameOn) {
                gameOn = playTurn(botGameService, playerGameService);
            }
            Thread.sleep(sleepTime);
        }
        enterGameOverState();
    }

    private boolean playTurn(GameService attacker, GameService defender) throws InterruptedException {
        String position = attacker.attack("Start"); // Player 1
        do {
            String attackResponse = defender.processAttack(position); // Player 2
            if ("LOST".equals(attackResponse)) {
                System.out.printf("Game finished. %s won.%n", attacker.getUserName());
                return false;
            }
            position = attacker.processCellState(attackResponse);
            Thread.sleep(sleepTime);
        } while (!"END_TURN".equals(position));
        return true;
    }

    private void enterGameOverState(){
        stateMachine.changeState(GameOverState.class);
    }
}
