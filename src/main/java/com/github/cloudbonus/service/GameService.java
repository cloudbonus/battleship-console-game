package com.github.cloudbonus.service;

import com.github.cloudbonus.board.Cell;
import com.github.cloudbonus.board.CellState;
import com.github.cloudbonus.board.Ship;
import com.github.cloudbonus.user.Bot;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.github.cloudbonus.board.CellState.DESTROYED;
import static com.github.cloudbonus.board.CellState.SEIZED_SHOT;

public class GameService {
    @Setter
    @Getter
    private User user;
    @Setter
    @Getter
    private String opponentName;
    private Cell position;
    private boolean disableConsoleOutput = false;

    public String getUserName() {
        return user.getName();
    }

    public String printGameInfo() {
        if (!disableConsoleOutput) {
            ConsoleInformationManager.clearConsole();
        }
        return ConsoleInformationManager.printGameInfo(user, opponentName);
    }

    public void disableConsoleOutput() {
        this.disableConsoleOutput = true;
    }

    private void println(String message) {
        if (!disableConsoleOutput) {
            System.out.println(message);
        }
    }

    private void printf(String format, Object... args) {
        if (!disableConsoleOutput) {
            System.out.printf(format, args);
        }
    }

    public String attack(String message) {
        String info = printGameInfo();
        println(info);
        println("Game info:");
        if ("END_TURN".equals(message)) {
            printf("%s missed!\n", opponentName);
        }
        println("Your turn");
        String position = user.attackOpponent();
        this.position = ConsoleInformationManager.createCellFromInput(position);
        return position;
    }

    public String processAttack(String message) {
        Cell cell = user.giveResponse(ConsoleInformationManager.createCellFromInput(message));
        String info = printGameInfo();
        println(info);
        println("Game info:");
        printf("%s's turn\n", opponentName);
        printf("%s attacked %s\n", opponentName, message);
        if (user.hasLost()) {
            return "LOST";
        } else if (cell.getCellState() == DESTROYED) {
            Ship lastDestroyedShip = user.getLeftBoard().getLastDestroyedShip();
            int size = lastDestroyedShip.getPosition().size();
            String end = ConsoleInformationManager.createInputFromCell(lastDestroyedShip.getPosition().get(size - 1));
            String start = ConsoleInformationManager.createInputFromCell(lastDestroyedShip.getPosition().get(0));
            printf("Your ship of size %s has been destroyed\n", lastDestroyedShip.getSize());
            return String.format("SHIP_%s_%s_%d", start, end, lastDestroyedShip.getSize());
        } else {
            if (cell.getCellState() == SEIZED_SHOT) {
                printf("%s hit! Stay prepared for another imminent attack\n", opponentName);
            }
            return cell.getCellState().name();
        }
    }

    public String processCellState(String message) {
        if (message.startsWith("SHIP_")) {
            processShipMessage(message);
        } else {
            processNonShipMessage(message);
        }
        String info = printGameInfo();
        println(info);
        println("Game info:");
        return processTurn(message);
    }

    private void processShipMessage(String message) {
        String[] parts = message.split("_");
        String secondValue = parts[1];
        String thirdValue = parts[2];
        String fourthValue = parts[3];

        if (user instanceof Bot) ((Bot) user).clearHitCells();

        if (secondValue.equals(thirdValue)) {
            updateBoardWithCellState("DESTROYED");
        } else {
            List<Cell> cells = ConsoleInformationManager.generateSequence(secondValue, thirdValue);
            cells.forEach(cell -> user.updateRightBoard(cell));
            printf("You have destroyed a ship of size %s\n", fourthValue);
        }
        updateShipsOnBoard(fourthValue);
    }

    private void processNonShipMessage(String message) {
        updateBoardWithCellState(message);
    }

    private void updateBoardWithCellState(String message) {
        position.setCellState(CellState.valueOf(message));

        if (user instanceof Bot && position.getCellState() == SEIZED_SHOT) {
            ((Bot) user).addCellToHitCells(position);
        }

        user.updateRightBoard(position);
    }

    private void updateShipsOnBoard(String fourthValue) {
        user.getRightBoard().updateShipsOnBoard(Integer.parseInt(fourthValue));
    }

    private String processTurn(String message) {
        if (!message.startsWith("SHIP_") && position.getCellState() != SEIZED_SHOT) {
            println("You missed!");
            printf("%s's turn\n", opponentName);
            return "END_TURN";
        } else {
            println("Your turn");
            if (message.startsWith("SHIP_")) {
                printf("You have destroyed a ship of size %s\n", message.split("_")[3]);
            }
            println("You hit! Congratulations, you can attack once more");
            String position_proxy = user.attackOpponent();
            position = ConsoleInformationManager.createCellFromInput(position_proxy);
            return position_proxy;
        }
    }
}
