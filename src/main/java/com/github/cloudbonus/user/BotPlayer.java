package com.github.cloudbonus.user;

import com.github.cloudbonus.board.Board;
import com.github.cloudbonus.board.cell.Cell;
import com.github.cloudbonus.board.cell.CellType;
import com.github.cloudbonus.board.ship.ShipType;
import com.github.cloudbonus.exceptions.CellAlreadyAttackedException;
import com.github.cloudbonus.util.CellConverter;

import java.util.*;

public class BotPlayer extends User implements Bot {
    private final Random random = new Random();
    private final List<Cell> hitCells = new ArrayList<>();

    @Override
    public String attackOpponent() {
        String target;
        while (true) {
            try {
                target = CellConverter.createInputFromCell(generatePosition());
                if (getRightBoard().hasAttacked(target)) {
                    throw new CellAlreadyAttackedException("You’ve already shot this cell");
                }
                break;
            } catch (CellAlreadyAttackedException e) {
                System.out.println(e.getMessage());
            }
        }
        return target;
    }

    @Override
    public Cell generatePosition() {
        if (!this.hitCells.isEmpty()) {
            return generatePositionAlongShip();
        }
        return generateSmartPosition();
    }

    @Override
    public Cell giveResponse(Cell position) {
        return super.getLeftBoard().updatePosition(position);
    }

    @Override
    public void updateRightBoard(Cell cell) {
        super.getRightBoard().updatePosition(cell);
    }

    @Override
    public boolean hasLost() {
        return super.getLeftBoard().hasShipsOnBoard();
    }

    @Override
    public void addCellToHitCells(Cell cell) {
        this.hitCells.add(cell);
    }

    @Override
    public void clearHitCells() {
        this.hitCells.clear();
    }

    private Cell generatePositionAlongShip() {
        if (this.hitCells.size() == 1) {
            return generatePositionAroundHit(this.hitCells.get(0));
        } else {
            this.hitCells.sort(Comparator.comparing(Cell::getX).thenComparing(Cell::getY));
            Cell firstCell = this.hitCells.get(0);
            Cell lastCell = this.hitCells.get(this.hitCells.size() - 1);

            if (firstCell.getX() == lastCell.getX()) {
                return generatePosition(firstCell, lastCell, true);
            } else {
                return generatePosition(firstCell, lastCell, false);
            }
        }
    }

    private Cell generatePosition(Cell firstCell, Cell lastCell, boolean isVertical) {
        int fixedCoordinate = isVertical ? firstCell.getX() : firstCell.getY();
        int variableCoordinateOne = isVertical ? firstCell.getY() - 1 : firstCell.getX() - 1;
        int variableCoordinateTwo = isVertical ? lastCell.getY() + 1 : lastCell.getX() + 1;
        List<Cell> possiblePositions = new ArrayList<>();

        int xCoordinate, yCoordinate;

        for (int variableCoordinate : new int[]{variableCoordinateOne, variableCoordinateTwo}) {
            if (isVertical) {
                xCoordinate = fixedCoordinate;
                yCoordinate = variableCoordinate;
            } else {
                xCoordinate = variableCoordinate;
                yCoordinate = fixedCoordinate;
            }

            if (isValidAndNotAttacked(xCoordinate, yCoordinate)) {
                possiblePositions.add(new Cell(xCoordinate, yCoordinate, CellType.MISS));
            }
        }

        return possiblePositions.get(this.random.nextInt(possiblePositions.size()));
    }

    private Cell generatePositionAroundHit(Cell cell) {
        List<Cell> possiblePositions = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] direction : directions) {
            int newX = cell.getX() + direction[0];
            int newY = cell.getY() + direction[1];

            if (isValidAndNotAttacked(newX, newY)) {
                possiblePositions.add(new Cell(newX, newY, CellType.MISS));
            }
        }

        if (possiblePositions.isEmpty()) {
            return null;
        } else {
            return possiblePositions.get(this.random.nextInt(possiblePositions.size()));
        }
    }

    private Cell generateSmartPosition() {
        int emptyCells = super.getRightBoard().getEmptyCells().size();
        if (emptyCells > 64) {
            return generateRandomPosition();
        }

        int maxPotential = 0;
        Cell bestCell = null;


        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                int potential = calculatePotential(x, y);
                if (potential > maxPotential) {
                    maxPotential = potential;
                    bestCell = new Cell(x, y, CellType.MISS);
                }
            }
        }

        return bestCell != null ? bestCell : generateRandomPosition();
    }

    private int calculatePotential(int x, int y) {
        int potential = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        Map<ShipType, Integer> remainingShipSizes = super.getRightBoard().getRemainingShips();
        for (int[] direction : directions) {
            for (ShipType shipType : remainingShipSizes.keySet()) {
                int length = 0;
                while (true) {
                    int newX = x + direction[0] * length;
                    int newY = y + direction[1] * length;

                    if (!isValidPosition(newX, newY) || isCellAttacked(newX, newY)) {
                        break;
                    }

                    length++;
                }

                if (length >= shipType.getShipLength()) {
                    potential += length;
                }
            }
        }

        return potential;
    }

    private Cell generateRandomPosition() {
        int x, y;
        do {
            x = this.random.nextInt(Board.BOARD_SIZE);
            y = this.random.nextInt(Board.BOARD_SIZE);
        } while (isCellAttacked(x, y));
        return new Cell(x, y, CellType.MISS);
    }

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < Board.BOARD_SIZE && y >= 0 && y < Board.BOARD_SIZE;
    }

    private boolean isCellAttacked(int x, int y) {
        CellType state = super.getRightBoard().getPosition(x, y).getCellType();
        return state == CellType.MISS || state == CellType.HIT || state == CellType.SUNK;
    }

    private boolean isValidAndNotAttacked(int x, int y) {
        return isValidPosition(x, y) && !isCellAttacked(x, y);
    }
}



