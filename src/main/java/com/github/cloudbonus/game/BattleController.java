package com.github.cloudbonus.game;

import com.github.cloudbonus.board.cell.Cell;
import com.github.cloudbonus.board.cell.CellType;
import com.github.cloudbonus.board.ship.Ship;
import com.github.cloudbonus.board.ship.ShipType;
import com.github.cloudbonus.user.Bot;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.CellConverter;
import com.github.cloudbonus.util.ConsoleDisplayManager;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class BattleController {
    private final User user;
    @Setter
    private String opponentName;
    private Cell position;
    private final GameStatistics gameStatistics;
    private static final long sleepTime = 2000;
    @Getter
    private boolean disableConsoleOutput = false;
    @Getter
    private boolean isMatchFinished = false;

    public BattleController(User user) {
        this.user = user;
        this.gameStatistics = new GameStatistics();
    }

    public String getUserName() {
        return this.user.getName();
    }

    public boolean hasLostMatch() {
        return this.user.hasLost();
    }

    public void finishMatch() {
        this.isMatchFinished = true;
    }

    private String getGameInfo() {
        if (!this.disableConsoleOutput) {
            ConsoleDisplayManager.clearConsole();
        }
        int totalTurns = gameStatistics.getTotalTurns();
        return ConsoleDisplayManager.getGameInfo(this.user, this.opponentName, totalTurns);
    }

    public String getGameInfoForSpectators() {
        return getGameInfo() + ConsoleDisplayManager.getWatchOnlyMessage();
    }

    public void disableConsoleOutput() {
        this.disableConsoleOutput = true;
    }


    public void waitOpponentTurn() {
        this.gameStatistics.incrementOpponentTotalShots();
        String info = getGameInfo();
        println(info);
        println(ConsoleDisplayManager.getOpponentTurnMessage(this.opponentName));
    }

    public String attack() {
        this.gameStatistics.incrementPlayerTotalShots();
        String info = getGameInfo();
        println(info);
        println(ConsoleDisplayManager.getPlayerTurnMessage());
        ConsoleDisplayManager.printEmptyRows(3);
        return executeAttack();
    }

    private String executeAttack() {
        String position = this.user.attackOpponent();
        this.position = CellConverter.createCellFromInput(position);
        return position;
    }

    public String processAttack(String message) {
        Cell cell = this.user.giveResponse(CellConverter.createCellFromInput(message));

        if (cell.getCellType() != CellType.MISS) {
            this.gameStatistics.incrementOpponentHitShots();
            if (!this.user.hasLost()) {
                this.gameStatistics.incrementOpponentTotalShots();
            }
        }

        String info = getGameInfo();
        println(info);
        println(ConsoleDisplayManager.getOpponentTurnMessage(this.opponentName));
        println(ConsoleDisplayManager.getOpponentAttackMessage(this.opponentName, message));
        return processCellType(cell);
    }

    private String processCellType(Cell cell) {
        if (cell.getCellType() == CellType.SUNK) {
            return processSunkCell();
        } else {
            return processNonSunkCell(cell);
        }
    }

    private String processSunkCell() {
        Ship lastDestroyedShip = this.user.getLeftBoard().getLastDestroyedShip();

        int shipSize = lastDestroyedShip.getShipType().getShipLength();
        String end = CellConverter.createInputFromCell(lastDestroyedShip.getPosition().get(shipSize - 1));
        String start = CellConverter.createInputFromCell(lastDestroyedShip.getPosition().get(0));
        String shipName = ShipType.convertShipTypeToNormalString(lastDestroyedShip.getShipType());

        println(ConsoleDisplayManager.getPlayerShipStatus(shipName, shipSize));
        println(ConsoleDisplayManager.getPlayerImminentAttackWarning(this.opponentName));
        if (this.user.hasLost()) {
            return String.format("LOST/%s/%s/%s", start, end, lastDestroyedShip.getShipType());
        }
        return String.format("SUNK/%s/%s/%s", start, end, lastDestroyedShip.getShipType());
    }

    private String processNonSunkCell(Cell cell) {
        if (cell.getCellType() == CellType.HIT) {
            println(ConsoleDisplayManager.getPlayerImminentAttackWarning(this.opponentName));
        } else {
            println(ConsoleDisplayManager.getOpponentMissMessage(this.opponentName));
        }

        return cell.getCellType().name();
    }

    public String processCellState(String message) {
        if (message.startsWith("SUNK") || message.startsWith("LOST")) {
            processSunkMessage(message);
        } else {
            processNonShipMessage(message);
        }

        if (!"MISS".equals(message) && !message.startsWith("LOST")) {
            this.gameStatistics.incrementPlayerTotalShots();
        }

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
            cells.forEach(this.user::updateRightBoard);
        }

        this.user.getRightBoard().updateShipsOnBoard(ShipType.valueOf(fourthValue));
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

    private String processTurn(String message) {
        println(ConsoleDisplayManager.getPlayerTurnMessage());

        if ("MISS".equals(message)) {
            println(ConsoleDisplayManager.getPlayerMissMessage());

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            waitOpponentTurn();
            return "END_TURN";
        }

        gameStatistics.incrementPlayerHitShots();

        boolean triggerForAdditionalRows = true;

        if (message.startsWith("SUNK") || message.startsWith("LOST")) {
            ShipType shipType = ShipType.valueOf(message.split("/")[3]);
            int shipSize = shipType.getShipLength();
            String shipName = ShipType.convertShipTypeToNormalString(shipType);
            println(ConsoleDisplayManager.getPlayerShipSunkMessage(shipName, shipSize));
            triggerForAdditionalRows = false;
        }

        if (message.startsWith("LOST")) {
            return ConsoleDisplayManager.getReasonMessage();
        }

        println(ConsoleDisplayManager.getPlayerHitMessage());

        if (triggerForAdditionalRows){
            ConsoleDisplayManager.printEmptyRows(2);
        }
        else ConsoleDisplayManager.printEmptyRows(1);

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
        ConsoleDisplayManager.printHeader();
        ConsoleDisplayManager.printStatsMessage();
        gameStatistics.getMatchTimes();

        ConsoleDisplayManager.printPlayerStats(this.user.getName(), this.user.hasLost(), this.user.getLeftBoard().getRemainingShipsAfterGame());
        ConsoleDisplayManager.printEfficiency(gameStatistics.getPlayerHitEfficiency());

        ConsoleDisplayManager.printPlayerStats(this.opponentName, !this.user.hasLost(), this.user.getRightBoard().getRemainingShipsAfterGame());
        ConsoleDisplayManager.printEfficiency(gameStatistics.getOpponentHitEfficiency());
        waitForUserInput();
    }

    public void printMatchResult() {
        boolean flag = this.user.hasLost();
        if (flag) {
            ConsoleDisplayManager.printMatchResult(true, this.opponentName);
        }
        ConsoleDisplayManager.printMatchResult(flag, this.user.getName());
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

    private void println(String message) {
        if (!this.disableConsoleOutput) {
            System.out.println(message);
        }
    }
}
