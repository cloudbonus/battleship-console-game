package com.github.cloudbonus.service;

import com.github.cloudbonus.board.Cell;
import com.github.cloudbonus.board.CellState;
import com.github.cloudbonus.board.Ship;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.github.cloudbonus.board.CellState.DESTROYED;
import static com.github.cloudbonus.board.CellState.SEIZED_SHOT;

public class GameService {
    @Setter
    private User user;
    @Setter
    @Getter
    private String opponentName;
    private Cell position;

    public String getUserName() {
        return user.getName();
    }

    public String printGameInfo() {
        return ConsoleInformationManager.printGameInfo(user, opponentName);
    }
    public String attack(String message) {
        ConsoleInformationManager.clearConsole();
        String info = ConsoleInformationManager.printGameInfo(user, opponentName);
        System.out.println(info);
        System.out.println("Game info:");
        if ("END_TURN".equals(message)) {
            System.out.printf("%s missed!\n", opponentName);
        }
        System.out.println("Your turn");
        String position = user.attackOpponentOnline();
        this.position = ConsoleInformationManager.createCellFromInput(position);
        return position;
    }

    public String processAttack(String message) {
        Cell cell = user.giveResponse(ConsoleInformationManager.createCellFromInput(message));
        ConsoleInformationManager.clearConsole();
        String info = ConsoleInformationManager.printGameInfo(user, opponentName);
        System.out.println(info);
        System.out.println("Game info:");
        System.out.printf("%s's turn\n", opponentName);
        System.out.printf("%s attacked %s\n", opponentName, message);
        if (user.hasLost()) {
            return "LOST";
        } else if (cell.getCellState() == DESTROYED) {
            Ship lastDestroyedShip = user.getLeftBoard().getLastDestroyedShip();
            int size = lastDestroyedShip.getPosition().size();
            String end = ConsoleInformationManager.createInputFromCell(lastDestroyedShip.getPosition().get(size - 1));
            String start = ConsoleInformationManager.createInputFromCell(lastDestroyedShip.getPosition().get(0));
            System.out.printf("Your ship of size %s has been destroyed\n", lastDestroyedShip.getSize());
            return String.format("SHIP_%s_%s_%d", start, end, lastDestroyedShip.getSize());
        } else {
            System.out.printf("%s hit! Stay prepared for another imminent attack\n", opponentName);
            return cell.getCellState().name();
        }
    }

    public String processCellState(String message) {
        if (message.startsWith("SHIP_")) {
            handleShipMessage(message);
        } else {
            handleNonShipMessage(message);
        }
        ConsoleInformationManager.clearConsole();
        String info = ConsoleInformationManager.printGameInfo(user, opponentName);
        System.out.println(info);
        System.out.println("Game info:");
        return handleTurn(message);
    }

    private void handleShipMessage(String message) {
        String[] parts = message.split("_");
        String secondValue = parts[1];
        String thirdValue = parts[2];
        String fourthValue = parts[3];

        if (secondValue.equals(thirdValue)) {
            updateBoardWithCellState("DESTROYED");
        } else {
            List<Cell> cells = ConsoleInformationManager.generateSequence(secondValue, thirdValue);
            cells.forEach(cell -> user.updateRightBoard(cell));
            updateShipsOnBoard(fourthValue);
            System.out.printf("You have destroyed a ship of size %s\n", fourthValue);
        }
    }

    private void handleNonShipMessage(String message) {
        updateBoardWithCellState(message);
    }

    private void updateBoardWithCellState(String message) {
        position.setCellState(CellState.valueOf(message));
        user.updateRightBoard(position);
    }

    private void updateShipsOnBoard(String fourthValue) {
        user.getRightBoard().updateShipsOnBoard(Integer.parseInt(fourthValue));
    }

    private String handleTurn(String message) {
        if (!message.startsWith("SHIP_") && position.getCellState() != SEIZED_SHOT && position.getCellState() != DESTROYED) {
            System.out.println("You missed!");
            System.out.printf("%s's turn\n", opponentName);
            return "END_TURN";
        } else {
            System.out.println("Your turn");
            if (message.startsWith("SHIP_")) {
                System.out.printf("You have destroyed a ship of size %s\n", message.split("_")[3]);
            }
            System.out.println("You hit! Congratulations, you can attack once more");
            String position_proxy = user.attackOpponentOnline();
            position = ConsoleInformationManager.createCellFromInput(position_proxy);
            return position_proxy;
        }
    }
}
