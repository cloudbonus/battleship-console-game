package com.github.cloudbonus.board;

import lombok.Data;

import java.util.Objects;
@Data
public class Cell implements Subject {
    private Observer observer;
    private int x;
    private int y;
    private CellState cellState;

    public Cell(){}
    public Cell(int x, int y, CellState cellState) {
        this.x = x;
        this.y = y;
        this.cellState = cellState;
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
