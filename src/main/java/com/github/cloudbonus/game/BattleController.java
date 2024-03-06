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

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
    private static final long sleepTime = 2000;
    private final GameStatistics gameStatistics;
    @Getter
    private boolean isMatchFinished = false;

    public BattleController() {
        this.gameStatistics = new GameStatistics();
    }
    public String getUserName() {
        return this.user.getName();
    }

    public void toEndGame() {
        this.isMatchFinished = true;
    }

    private String getGameInfo() {
        if (!this.disableConsoleOutput) {
            ConsoleInformationManager.clearConsole();
        }
        return ConsoleInformationManager.printGameInfo(this.user, this.opponentName);
    }

    private void incrementTotalTurns() {
        if (!this.disableConsoleOutput) {
            GameStatistics.incrementTotalTurns();
        }
    }

    public void disableConsoleOutput() {
        this.disableConsoleOutput = true;
    }

    private void println(String message) {
        if (!this.disableConsoleOutput) {
            System.out.println(message);
        }
    }

    public void waitOpponentTurn() {
        incrementTotalTurns();
        String info = getGameInfo();
        println(info);
        println(ConsoleInformationManager.getOpponentTurnMessage(this.opponentName));
    }

    public String attack() {
        incrementTotalTurns();
        String info = getGameInfo();
        println(info);
        println(ConsoleInformationManager.getPlayerTurnMessage());
        return executeAttack();
    }

    private String executeAttack() {
        String position = this.user.attackOpponent();
        this.position = CellConverter.createCellFromInput(position);
        return position;
    }

    public String processAttack(String message) {
        Cell cell = this.user.giveResponse(CellConverter.createCellFromInput(message));
        if (cell.getCellType() != CellType.MISS && !this.user.hasLost()) {
            incrementTotalTurns();
        }
        this.gameStatistics.incrementOpponentHitShots();
        String info = getGameInfo();
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
        println(ConsoleInformationManager.getPlayerImminentAttackWarning(this.opponentName));
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

        if (!"MISS".equals(message) && !message.startsWith("LOST")) incrementTotalTurns();

        String info = getGameInfo();
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

        if ("MISS".equals(message)) {
            println(ConsoleInformationManager.getPlayerMissMessage());

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            waitOpponentTurn();
            return "END_TURN";
        }

        gameStatistics.incrementPlayerHitShots();

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

        if (disableConsoleOutput) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return executeAttack();
    }

    public void printMatchStats() {
        ConsoleInformationManager.printHeader();
        ConsoleInformationManager.printStatsMessage();
        System.out.println(gameStatistics.calculateGameDuration());

        printStats(this.user.getName(), this.user.hasLost(), this.user.getLeftBoard().getRemainingShipsAfterGame());
        System.out.println(gameStatistics.calculatePlayerEfficiency());

        printStats(this.opponentName, !this.user.hasLost(), this.user.getRightBoard().getRemainingShipsAfterGame());
        System.out.println(gameStatistics.calculateOpponentEfficiency());
        waitForUserInput();
    }

    private void printStats(String playerName, boolean hasLost, String remainingShips) {
        System.out.println("Player: " + playerName);
        System.out.println("Status: " + ConsoleInformationManager.getMatchStatus(hasLost));
        System.out.println("Remaining ships: " + (hasLost ? "no ships left" : remainingShips));
    }

    public static void waitForUserInput() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Press any key to continue...");
            reader.readLine();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
