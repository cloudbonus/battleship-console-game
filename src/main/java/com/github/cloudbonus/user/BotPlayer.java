package com.github.cloudbonus.user;

import com.github.cloudbonus.board.Board;
import com.github.cloudbonus.board.Cell;
import com.github.cloudbonus.board.CellState;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.github.cloudbonus.board.CellState.*;

public class BotPlayer extends User implements Bot {
    private final Random random = new Random();
    private final List<Cell> hitCells = new ArrayList<>();
    @Override
    public Cell generatePosition() {
        if (!hitCells.isEmpty()) {
            return generatePositionAlongShip();
        }
        return generateSmartPosition();
    }

    private Cell generatePositionAlongShip() {
        if (hitCells.size() == 1) {
            return generatePositionAroundHit(hitCells.get(0));
        } else {
            hitCells.sort(Comparator.comparing(Cell::getX).thenComparing(Cell::getY));
            Cell firstCell = hitCells.get(0);
            Cell lastCell = hitCells.get(hitCells.size() - 1);

            if (firstCell.getX() == lastCell.getX()) {
                return generatePositionAboveOrBelow(firstCell, lastCell);
            } else {
                return generatePositionToLeftOrRight(firstCell, lastCell);
            }
        }
    }


    private Cell generatePositionAboveOrBelow(Cell firstCell, Cell lastCell) {
        int x = firstCell.getX();
        int yAbove = firstCell.getY() - 1;
        int yBelow = lastCell.getY() + 1;
        List<Cell> possiblePositions = new ArrayList<>();
        if (isValidPosition(x, yAbove) && !isCellAttacked(x, yAbove)) {
            possiblePositions.add(new Cell(x, yAbove, SHOT));
        }
        if (isValidPosition(x, yBelow) && !isCellAttacked(x, yBelow)) {
            possiblePositions.add(new Cell(x, yBelow, SHOT));
        }

        return possiblePositions.get(random.nextInt(possiblePositions.size()));
    }

    private Cell generatePositionToLeftOrRight(Cell firstCell, Cell lastCell) {
        int y = firstCell.getY();
        int xLeft = firstCell.getX() - 1;
        int xRight = lastCell.getX() + 1;

        List<Cell> possiblePositions = new ArrayList<>();
        if (isValidPosition(xLeft, y) && !isCellAttacked(xLeft, y)) {
            possiblePositions.add(new Cell(xLeft, y, SHOT));
        }
        if (isValidPosition(xRight, y) && !isCellAttacked(xRight, y)) {
            possiblePositions.add(new Cell(xRight, y, SHOT));
        }
        return possiblePositions.get(random.nextInt(possiblePositions.size()));
    }

    private Cell generatePositionAroundHit(Cell cell) {
        List<Cell> possiblePositions = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] direction : directions) {
            int newX = cell.getX() + direction[0];
            int newY = cell.getY() + direction[1];

            if (isValidPosition(newX, newY) && !isCellAttacked(newX, newY)) {
                possiblePositions.add(new Cell(newX, newY, SHOT));
            }
        }

        if (possiblePositions.isEmpty()) {
            return null;
        } else {
            return possiblePositions.get(random.nextInt(possiblePositions.size()));
        }
    }


    private Cell generateSmartPosition() {
        int largestShipSize = super.getRightBoard().getMaxRemainingShipSize();
        int emptyCells = super.getRightBoard().getEmptyCells().size();
        if (emptyCells > 128) {
            return generateRandomPosition();
        }
        Cell maxEmptyCell = getMaxEmptyArea();
        if (maxEmptyCell != null) {
            return maxEmptyCell;
        }
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (canPlaceShip(x, y, largestShipSize)) {
                    return new Cell(x, y, SHOT);
                }
            }
        }
        return generateRandomPosition();
    }

    private Cell getMaxEmptyArea() {
        int maxCount = 0;
        Cell maxCell = null;
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                int count = countEmptyCellsAround(x, y);
                if (count > maxCount) {
                    maxCount = count;
                    maxCell = new Cell(x, y, SHOT);
                }
            }
        }
        return maxCell;
    }

    private int countEmptyCellsAround(int x, int y) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newX = x + i;
                int newY = y + j;
                if (isValidPosition(newX, newY) && getRightBoard().getPosition(newX, newY).getCellState() == EMPTY) {
                    count++;
                }
            }
        }
        return count;
    }
//    private Cell generateSmartPosition() {
//        int largestShipSize = ((BasicBoard) super.getRightBoard()).getMaxRemainingShipSize();
//        int emptyCells = super.getRightBoard().getEmptyCells().size();
//        if (emptyCells > 128) {
//            return generateRandomPosition();
//        }
//        for (int x = 0; x < Board.BOARD_SIZE; x++) {
//            for (int y = 0; y < Board.BOARD_SIZE; y++) {
//                if (canPlaceShip(x, y, largestShipSize)) {
//                    return new Cell(x, y, SHOT);
//                }
//            }
//        }
//        return generateRandomPosition();
//    }
    private boolean canPlaceShip(int x, int y, int shipSize) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] direction : directions) {
            for (int i = 0; i < shipSize; i++) {
                int newX = x + direction[0] * i;
                int newY = y + direction[1] * i;
                if (!isValidPosition(newX, newY) || isCellAttacked(newX, newY)) {
                    break;
                }
                if (i == shipSize - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < Board.BOARD_SIZE && y >= 0 && y < Board.BOARD_SIZE;
    }

    private boolean isCellAttacked(int x, int y) {
        CellState state = getRightBoard().getPosition(x, y).getCellState();
        return state == SHOT || state == SEIZED_SHOT || state == DESTROYED;
    }

    private Cell generateRandomPosition() {
        int x, y;
        do {
            x = random.nextInt(Board.BOARD_SIZE);
            y = random.nextInt(Board.BOARD_SIZE);
        } while (isCellAttacked(x, y));
        return new Cell(x, y, SHOT);
    }

    @Override
    public boolean hasLost() {
        return super.getLeftBoard().hasShipsOnBoard();
    }

    @Override
    public Cell attackOpponent(Player opponent) {
        Cell response;
        while (true) {
            try {
                Cell target = generatePosition();
                response = opponent.giveResponse(target);
                updateRightBoard(response);
                if (response.getCellState() == SEIZED_SHOT) {
                    hitCells.add(response);
                }
                if (response.getCellState() == DESTROYED) {
                    hitCells.clear();
                }
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        return response;
    }

    @Override
    public String attackOpponentOnline() {
        return null;
    }

    public void updateRightBoard(Cell cell) {
        super.getRightBoard().updatePosition(cell);
    }

    @Override
    public Cell giveResponse(Cell position) {
        return super.getLeftBoard().updatePosition(position);
    }
}

