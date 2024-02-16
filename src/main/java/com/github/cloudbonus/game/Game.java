package com.github.cloudbonus.game;

import com.github.cloudbonus.board.*;
import com.github.cloudbonus.client.BattleshipGameClientEndpoint;
import com.github.cloudbonus.server.BattleshipGameServerEndpoint;
import com.github.cloudbonus.user.BotPlayer;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;
import lombok.Getter;
import lombok.Setter;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import static com.github.cloudbonus.board.CellState.*;

@Setter
@Getter
public class Game {
    private static final String A_MODE = "A";

    private User firstUser;

    public void setupGame() {
        ConsoleInformationManager.printGameSetup(getFirstUser());
        String selectedMode = UserInteractionManager.getABSelectionFromInput();
        chooseGameMode(selectedMode);
    }

    private void chooseGameMode(String selectedMode) {
        ShipPlacementManager manager = new ShipPlacementManager();
        if (A_MODE.equals(selectedMode)) {
            startSinglePlayerGame(manager);
            try {
                play(manager);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            startMultiplayerGame(manager);
        }
    }

    private void startSinglePlayerGame(ShipPlacementManager manager) {
        ConsoleInformationManager.printGameMode();
        manager.setBoard(getFirstUser().getLeftBoard());
        String selectedMode = UserInteractionManager.getABSelectionFromInput();
        manager.placeShipsOnBoard(selectedMode);
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

    public void play(ShipPlacementManager manager) throws InterruptedException {
        UserInteractionManager.setPositionInterpreter();
        User player = firstUser;
        User bot = setupBot(manager);


        boolean gameOn = true;
        while (gameOn) {
            gameOn = playTurn(player, bot);
            Thread.sleep(2000);
            if (gameOn) {
                gameOn = playTurn(bot, player);
            }
            Thread.sleep(2000);
        }
        firstUser.getLeftBoard().resetMap();
        firstUser.getRightBoard().resetMap();
    }

    private boolean playTurn(User attacker, User defender) throws InterruptedException {
        while (true) {
            ConsoleInformationManager.printGameStatus(firstUser, defender.getName());
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
                Thread.sleep(2000);
            }
        }
        return true;
    }

    private void startMultiplayerGame(ShipPlacementManager manager) {
        ConsoleInformationManager.printGameMode();
        manager.setBoard(getFirstUser().getLeftBoard());

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
        setupGame();
    }
}


