package com.github.cloudbonus.board.ship;

import com.github.cloudbonus.board.cell.CellType;
import com.github.cloudbonus.board.util.Observer;
import com.github.cloudbonus.board.util.Subject;
import com.github.cloudbonus.board.cell.Cell;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Ship implements Observer, Subject {
    private Observer observer;
    @Getter
    private final ShipType shipType;
    @Getter
    private final List<Cell> position;
    private boolean isShipDestroyed;

    public void setPosition(Cell cell) {
        cell.attach(this);
        position.add(cell);
    }

    public boolean isShipDestroyed() {
        return this.isShipDestroyed;
    }

    public Ship(ShipType shipType) {
        this.shipType = shipType;
        this.position = new ArrayList<>();
    }

    @Override
    public void update() {
        for (Cell cell : this.position) {
            if (cell.getCellType() != CellType.HIT) {
                return;
            }
        }
        this.isShipDestroyed = true;
        this.position.forEach(cell -> cell.setCellType(CellType.SUNK));
        notifyObserver();
    }

    @Override
    public void attach(Observer observer) {
        if (!Objects.isNull(observer)) {
            this.observer = observer;
        }
    }

    @Override
    public void notifyObserver() {
        this.observer.update();
    }

}
