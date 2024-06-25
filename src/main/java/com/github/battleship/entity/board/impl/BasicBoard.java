package com.github.battleship.entity.board.impl;

import com.github.battleship.entity.board.Board;
import com.github.battleship.entity.board.cell.Cell;
import com.github.battleship.entity.board.cell.CellType;
import com.github.battleship.entity.board.ship.ShipType;
import com.github.battleship.game.util.CellConverter;
import lombok.Getter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;

@Getter
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class BasicBoard extends Board {

    private final Map<ShipType, Integer> remainingShips = new TreeMap<>();

    public BasicBoard() {
        for (ShipType shipType : ShipType.values()) {
            this.remainingShips.put(shipType, shipType.getNumShips());
        }
    }

    public int getRemainingShipsSum() {
        return this.remainingShips.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public Cell updatePosition(Cell position) {
        Cell cell = super.getPosition(position.getX(), position.getY());
        cell.setCellType(position.getCellType());

        if (position.getCellType() == CellType.SUNK) {
            int[][] directions = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};

            for (int[] direction : directions) {
                int newX = position.getX() + direction[0];
                int newY = position.getY() + direction[1];

                if (newX >= 0 && newX < BOARD_SIZE && newY >= 0 && newY < BOARD_SIZE) {
                    Cell neighbour = super.getPosition(newX, newY);

                    if (neighbour.getCellType() != CellType.SUNK) {
                        neighbour.setCellType(CellType.MISS);
                    }
                }
            }
        }
        return cell;
    }

    public void resetMap() {
        for (ShipType shipType : ShipType.values()) {
            this.remainingShips.put(shipType, shipType.getNumShips());
        }
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                super.getPosition(i, j).setCellType(CellType.WATER);
            }
        }
    }

    public boolean hasAttacked(String position) {
        Cell cell = CellConverter.createCellFromInput(position);
        return super.getPosition(cell.getX(), cell.getY()).getCellType() != CellType.WATER;
    }
    public String getShipsState() {
        StringBuilder sb = new StringBuilder();

        sb.append("Remaining Opponent's Ships\n");
        sb.append("───────────────────────────────\n");
        for (ShipType shipType : ShipType.values()) {
            String shipIcon = this.remainingShips.get(shipType) > 0 ? CellType.SHIP.getSymbol() : CellType.SUNK.getSymbol();
            String shipCount = this.remainingShips.get(shipType) > 0 ? String.valueOf(remainingShips.get(shipType)) : "No";
            sb.append(String.format("%-" +(12+(9*shipType.getShipLength())) + "s ---   %-2s remaining%n", shipIcon.repeat(shipType.getShipLength()), shipCount));
        }
        return sb.toString();
    }

    @Override
    public String getRemainingShipsAfterGame() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<ShipType, Integer> entry : this.remainingShips.entrySet()) {
            if (entry.getValue() > 0) {
                String shipIcon = CellType.SHIP.getSymbol();
                sb.append(shipIcon.repeat(entry.getKey().getShipLength())).append(" x").append(entry.getValue()).append(" ");
            }
        }

        return sb.toString();
    }


    public void updateShipsOnBoard(ShipType shipType) {
        this.remainingShips.put(shipType, remainingShips.get(shipType) - 1);
    }
}
