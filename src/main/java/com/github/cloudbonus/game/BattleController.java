package com.github.cloudbonus.game;

import com.github.cloudbonus.board.cell.Cell;
import com.github.cloudbonus.board.cell.CellType;
import com.github.cloudbonus.board.ship.Ship;
import com.github.cloudbonus.board.ship.ShipType;
import com.github.cloudbonus.user.Bot;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.CellConverter;
import com.github.cloudbonus.util.ConsoleInformationManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


public class BattleController {
    @Setter
    @Getter
    private User user;
    @Setter
    @Getter
    private String opponentName;
    private Cell position;
    @Getter
    private boolean disableConsoleOutput = false;

    public String getUserName() {
        return this.user.getName();
    }

    public String printGameInfo() {
        if (!this.disableConsoleOutput) {
            ConsoleInformationManager.clearConsole();
        }
        return ConsoleInformationManager.printGameInfo(this.user, this.opponentName);
    }

    public void disableConsoleOutput() {
        this.disableConsoleOutput = true;
    }

    private void println(String message) {
        if (!this.disableConsoleOutput) {
            System.out.println(message);
        }
    }

    public String attack() {
        String info = printGameInfo();
        println(info);
        println(ConsoleInformationManager.getPlayerTurnMessage());
        String position = this.user.attackOpponent();
        this.position = CellConverter.createCellFromInput(position);
        return position;
    }

    public String processAttack(String message) {
        Cell cell = this.user.giveResponse(CellConverter.createCellFromInput(message));
        String info = printGameInfo();
        println(info);
        println(ConsoleInformationManager.getOpponentTurnMessage(this.opponentName));
        println(ConsoleInformationManager.getOpponentAttackMessage(this.opponentName, message));
        return handleCellType(cell);
    }

    private String handleCellType(Cell cell) {
        if (cell.getCellType() == CellType.SUNK) {
            return handleSunkCell();
        } else {
            return handleNonSunkCell(cell);
        }
    }

    private String handleSunkCell() {
        Ship lastDestroyedShip = this.user.getLeftBoard().getLastDestroyedShip();
        int shipSize = lastDestroyedShip.getShipType().getShipLength();
        String end = CellConverter.createInputFromCell(lastDestroyedShip.getPosition().get(shipSize - 1));
        String start = CellConverter.createInputFromCell(lastDestroyedShip.getPosition().get(0));
        String shipName = ShipType.convertShipTypeToNormalString(lastDestroyedShip.getShipType());

        println(ConsoleInformationManager.getPlayerShipStatus(shipName, shipSize));
        if (this.user.hasLost()) {
            return String.format("LOST/%s/%s/%s", start, end, lastDestroyedShip.getShipType());
        }
        return String.format("SUNK/%s/%s/%s", start, end, lastDestroyedShip.getShipType());
    }

    private String handleNonSunkCell(Cell cell) {
        if (cell.getCellType() == CellType.HIT) {
            println(ConsoleInformationManager.getPlayerImminentAttackWarning(this.opponentName));
        } else {
            println(ConsoleInformationManager.getOpponentMissMessage(this.opponentName));
        }
        return cell.getCellType().name();
    }

    public String processCellState(String message) {
        if (message.startsWith("SUNK") || message.startsWith("LOST")) {
            processSunkMessage(message);
        } else {
            processNonShipMessage(message);
        }
        String info = printGameInfo();
        println(info);
        return processTurn(message);
    }

    private void processSunkMessage(String message) {
        String[] parts = message.split("/");
        String secondValue = parts[1];
        String thirdValue = parts[2];
        String fourthValue = parts[3];

        if (this.user instanceof Bot) ((Bot) this.user).clearHitCells();

        if (secondValue.equals(thirdValue)) {
            updateBoardWithCellState(CellType.SUNK.name());
        } else {
            List<Cell> cells = CellConverter.generateCellSequence(secondValue, thirdValue);
            cells.forEach(cell -> this.user.updateRightBoard(cell));
        }
        updateShipsOnBoard(fourthValue);
    }

    private void processNonShipMessage(String message) {
        updateBoardWithCellState(message);
    }

    private void updateBoardWithCellState(String message) {
        this.position.setCellType(CellType.valueOf(message));

        if (this.user instanceof Bot && this.position.getCellType() == CellType.HIT) {
            ((Bot) this.user).addCellToHitCells(this.position);
        }

        this.user.updateRightBoard(this.position);
    }

    private void updateShipsOnBoard(String fourthValue) {
        this.user.getRightBoard().updateShipsOnBoard(ShipType.valueOf(fourthValue));
    }

    private String processTurn(String message) {
        println(ConsoleInformationManager.getPlayerTurnMessage());

        if (!message.startsWith("LOST") && !message.startsWith("SUNK") && this.position.getCellType() != CellType.HIT) {
            println(ConsoleInformationManager.getPlayerMissMessage());
            return "END_TURN";
        }

        if (message.startsWith("SUNK") || message.startsWith("LOST")) {
            ShipType shipType = ShipType.valueOf(message.split("/")[3]);
            int shipSize = shipType.getShipLength();
            String shipName = ShipType.convertShipTypeToNormalString(shipType);
            println(ConsoleInformationManager.getPlayerShipSunkMessage(shipName, shipSize));
        }

        if (message.startsWith("LOST")) {
            return ConsoleInformationManager.getReasonMessage();
        }

        println(ConsoleInformationManager.getPlayerHitMessage());
        String position_proxy = this.user.attackOpponent();
        this.position = CellConverter.createCellFromInput(position_proxy);
        return position_proxy;
    }
}
