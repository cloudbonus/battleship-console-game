package com.github.cloudbonus.board;

import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.github.cloudbonus.board.Board.BOARD_SIZE;
import static com.github.cloudbonus.board.CellState.SEIZED;

public class ShipPlacementManager {
    private final Random random = new Random();
    private CompleteBoard board;
    private static final int[] SHIP_SIZES = {1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 6};

    public void setBoard(Board board) {
        this.board = (CompleteBoard) board;
    }

    public void placeShipsOnBoard(String selectedMode) {
        if ("A".equals(selectedMode)) {
            placeShipsAutomatically();
        } else {
            placeShipsManually();
        }
    }

    public void placeShipsAutomatically() {
        boolean allShipsPlaced;
        do {
            this.board.resetMap();
            allShipsPlaced = true;
            for (int shipSize : SHIP_SIZES) {
                if (!placeShipIfPossible(shipSize)) {
                    allShipsPlaced = false;
                    break;
                }
            }
        } while (!allShipsPlaced);
    }

    private void placeShipsManually() {
        ConsoleInformationManager.printMap(this.board);
        for (int shipSize : SHIP_SIZES) {
            placeShipManually(shipSize);
            ConsoleInformationManager.printMap(this.board);
        }
    }

    private void placeShipManually(int shipSize) {
        while (true) {
            try {
                Cell position = getPositionFromUser();
                boolean isVertical = getOrientationFromUser();
                System.out.println("The ship's size is " + shipSize);
                placeShip(new Ship(shipSize), position.getX(), position.getY(), isVertical);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private Cell getPositionFromUser() {
        ConsoleInformationManager.printPositionInputMessage();
        UserInteractionManager.setPositionInterpreter();
        return UserInteractionManager.createPositionFromInput();
    }

    private boolean getOrientationFromUser() {
        UserInteractionManager.setOrientationInterpreter();
        ConsoleInformationManager.printOrientationMenu();
        return UserInteractionManager.getShipOrientationFromInput();
    }

    private boolean placeShipIfPossible(int shipSize) {
        List<Integer> positions = getRandomPositions();
        boolean[] orientations = {true, false};

        for (int x : positions) {
            for (int y : positions) {
                for (boolean isVertical : orientations) {
                    try {
                        Ship ship = new Ship(shipSize);
                        placeShip(ship, x, y, isVertical);
                        return true;
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
        return false;
    }

        private List<Integer> getRandomPositions() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            positions.add(i);
        }
        Collections.shuffle(positions, random);
        return positions;
    }

    private void placeShip(Ship ship, int x, int y, boolean isVertical) {
        for (int i = 0; i < ship.getSize(); i++) {
            int nx = x + (isVertical ? i : 0);
            int ny = y + (isVertical ? 0 : i);
            if (nx >= BOARD_SIZE || ny >= BOARD_SIZE) {
                throw new IllegalArgumentException("The ship goes beyond the board boundaries.");
            }
            validateCellForPlacement(nx, ny);
            Cell currentCell = this.board.getPosition(nx, ny);
            ship.setPosition(currentCell);
        }
        ship.getPosition().forEach(pos -> pos.setCellState(SEIZED));
        this.board.addShip(ship);
    }

    private void validateCellForPlacement(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE && this.board.getPosition(nx, ny).getCellState() != CellState.EMPTY) {
                    throw new IllegalArgumentException("Unable to place the ship");
                }
            }
        }
    }
}

