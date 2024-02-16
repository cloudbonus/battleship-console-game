package com.github.cloudbonus.board;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


import static com.github.cloudbonus.board.CellState.*;

@Getter
public class CompleteBoard extends Board implements Observer {
    private final List<Ship> ships;
    private Ship lastDestroyedShip;

    public CompleteBoard() {
        super();
        ships = new ArrayList<>();
    }

    public int getRemainingShipsCount() {
        return ships.size();
    }
    public void resetMap() {
        ships.forEach(ship -> ship.getPosition().forEach(pos -> pos.setCellState(EMPTY)));
        ships.clear();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                getPosition(i, j).setCellState(EMPTY);
            }
        }
    }

    @Override
    public Cell updatePosition(Cell opponentTarget) {
        Cell cell = super.getPosition(opponentTarget.getX(), opponentTarget.getY());
        CellState state = cell.getCellState();
        if (state == CellState.SEIZED) {
            cell.setCellState(SEIZED_SHOT);
            cell.notifyObserver();
            return new Cell(cell.getX(), cell.getY(), cell.getCellState());
        } else if (state == EMPTY) {
            cell.setCellState(opponentTarget.getCellState());
            return new Cell(cell.getX(), cell.getY(), cell.getCellState());
        } else {
            throw new IllegalArgumentException("You’ve already shot this cell");
        }
    }

    public boolean hasShipsOnBoard() {
        return getShips().isEmpty();
    }

    public void addShip(Ship ship) {
        ship.attach(this);
        getShips().add(ship);
    }

    @Override
    public void update() {
        getShips().stream()
                .filter(Ship::isShipDestroyed)
                .findFirst()
                .ifPresent(ship -> {
                    lastDestroyedShip = ship;
                    ship.getPosition().forEach(this::updateMapAroundDestroyedShip);
                    getShips().remove(ship);
                });
    }

    private void updateMapAroundDestroyedShip(Cell cell) {
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < 8; i++) {
            int newX = cell.getX() + dx[i];
            int newY = cell.getY() + dy[i];
            if (newX >= 0 && newX < BOARD_SIZE && newY >= 0 && newY < BOARD_SIZE) {
                Cell neighbour = super.getPosition(newX, newY);
                if (neighbour.getCellState() == CellState.EMPTY) {
                    neighbour.setCellState(CellState.SHOT);
                }
            }
        }
    }
}
