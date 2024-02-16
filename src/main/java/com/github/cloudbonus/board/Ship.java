package com.github.cloudbonus.board;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.github.cloudbonus.board.CellState.*;

public class Ship implements Observer, Subject {
    private Observer observer;
    @Getter
    private final int size;
    @Getter
    private final List<Cell> position;

    public void setPosition(Cell cell) {
        cell.attach(this);
        position.add(cell);
    }

    private boolean isShipDestroyed;

    public boolean isShipDestroyed() {
        return isShipDestroyed;
    }

    public Ship(int size) {
        this.size = size;
        this.position = new ArrayList<>();
    }

    @Override
    public void update() {
        for (Cell cell : position) {
            if (cell.getCellState() != SEIZED_SHOT) {
                return;
            }
        }
        isShipDestroyed = true;
        position.forEach(cell -> cell.setCellState(DESTROYED));
        notifyObserver();
    }

    @Override
    public void attach(Observer observer) {
        if(!Objects.isNull(observer)){
            this.observer = observer;
        }
    }

    @Override
    public void notifyObserver() {
        observer.update();
    }

}
