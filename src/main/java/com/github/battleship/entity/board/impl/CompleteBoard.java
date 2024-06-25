package com.github.battleship.entity.board.impl;

import com.github.battleship.entity.board.Board;
import com.github.battleship.entity.board.cell.Cell;
import com.github.battleship.entity.board.cell.CellType;
import com.github.battleship.entity.board.ship.Ship;
import com.github.battleship.entity.board.ship.ShipType;
import com.github.battleship.entity.board.util.Observer;
import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CompleteBoard extends Board implements Observer {
    private final List<Ship> ships;
    private Ship lastDestroyedShip;

    public CompleteBoard() {
        super();
        this.ships = new ArrayList<>();
    }

    public int getRemainingShipsCount() {
        return this.ships.size();
    }

    public void resetMap() {
        this.ships.forEach(ship -> ship.getPosition().forEach(pos -> pos.setCellType(CellType.WATER)));
        this.ships.clear();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                super.getPosition(i, j).setCellType(CellType.WATER);
            }
        }
    }

    @Override
    public Cell updatePosition(Cell opponentTarget) {
        Cell cell = super.getPosition(opponentTarget.getX(), opponentTarget.getY());
        CellType state = cell.getCellType();
        if (state == CellType.SHIP) {
            cell.setCellType(CellType.HIT);
            cell.notifyObserver();
        } else cell.setCellType(opponentTarget.getCellType());
        return new Cell(cell.getX(), cell.getY(), cell.getCellType());
    }

    public boolean hasShipsOnBoard() {
        return this.ships.isEmpty();
    }

    public void addShip(Ship ship) {
        ship.attach(this);
        this.ships.add(ship);
    }

    @Override
    public void update() {
        for (Ship ship : this.ships) {
            if (ship.isShipDestroyed()) {
                this.lastDestroyedShip = ship;
                for (Cell position : ship.getPosition()) {
                    updateMapAroundDestroyedShip(position);
                }
                this.ships.remove(ship);
                break;
            }
        }
    }

    private void updateMapAroundDestroyedShip(Cell cell) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] direction : directions) {
            int newX = cell.getX() + direction[0];
            int newY = cell.getY() + direction[1];
            if (newX >= 0 && newX < BOARD_SIZE && newY >= 0 && newY < BOARD_SIZE) {
                Cell neighbour = super.getPosition(newX, newY);
                if (neighbour.getCellType() == CellType.WATER) {
                    neighbour.setCellType(CellType.MISS);
                }
            }
        }
    }

    public int getTotalShipsPlacedOfThisType(ShipType shipType) {
        int count = 0;
        for (Ship s : this.ships) {
            if (s.getShipType() == shipType) {
                count++;
            }
        }
        return count;
    }

    @Override
    public String getRemainingShipsAfterGame() {
        Map<String, Integer> shipCounts = new TreeMap<>();
        StringBuilder sb = new StringBuilder();

        for (Ship ship : this.ships) {
            String shipIcon = getShipIcon(ship);
            shipCounts.put(shipIcon, shipCounts.getOrDefault(shipIcon, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : shipCounts.entrySet()) {
            if (entry.getValue() > 0) {
                sb.append(entry.getKey()).append(" x").append(entry.getValue()).append(" ");
            }
        }

        return sb.toString();
    }

    private String getShipIcon(Ship ship) {
        int length = ship.getShipType().getShipLength();
        return CellType.SHIP.getSymbol().repeat(length);
    }
}
