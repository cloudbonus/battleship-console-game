package com.github.cloudbonus.states;

import com.github.cloudbonus.board.BasicBoard;
import com.github.cloudbonus.board.Cell;
import com.github.cloudbonus.board.CompleteBoard;
import com.github.cloudbonus.board.Ship;
import com.github.cloudbonus.board.ShipPlacementManager;
import com.github.cloudbonus.stateMachine.EnterState;
import com.github.cloudbonus.stateMachine.StateMachine;
import com.github.cloudbonus.user.BotPlayer;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

import static com.github.cloudbonus.board.CellState.*;

public class GameLoopState implements EnterState {
    public GameLoopState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }

    private static final long sleepTime = 2000;
    
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

        boolean gameOn = true;

        while (gameOn) {
            gameOn = playTurn(player, bot);
            Thread.sleep(sleepTime);
            if (gameOn) {
                gameOn = playTurn(bot, player);
            }
            Thread.sleep(sleepTime);
        }

        enterGameOverState();
    }

    private boolean playTurn(User attacker, User defender) throws InterruptedException {
        while (true) {
            ConsoleInformationManager.printGameStatus(firstUser, "Bot Alex");
            System.out.println("Game info:");
            System.out.printf("%s's turn%n", attacker.getName());
            Cell cell = attacker.attackOpponent(defender);

            if (defender.hasLost()) {
                System.out.printf("Player %s has lost. Congratulations %s%n", defender.getName(), attacker.getName());
                return false;
            }
            System.out.printf("%s attacked %s%n", attacker.getName(), ConsoleInformationManager.createInputFromCell(cell));

            if (cell.getCellState() != SEIZED_SHOT && cell.getCellState() != DESTROYED) {
                System.out.printf("%s missed!%n", attacker.getName());
                break;
            } else {
                System.out.printf("%s hit! Congratulations, you can attack once more%n", attacker.getName());
                if (cell.getCellState() == DESTROYED) {
                    Ship lastDestroyedShip = defender.getLeftBoard().getLastDestroyedShip();
                    System.out.printf("%s's ship of size %s has been destroyed%n", defender.getName(), lastDestroyedShip.getSize());
                    lastDestroyedShip.getPosition().forEach(shipCellPosition -> attacker.getRightBoard().updatePosition(shipCellPosition));
                    attacker.getRightBoard().updateShipsOnBoard(lastDestroyedShip.getSize());
                }
                Thread.sleep(sleepTime);
            }
        }
        return true;
    }

    private void enterGameOverState(){
        stateMachine.changeState(GameOverState.class);
    }
}
