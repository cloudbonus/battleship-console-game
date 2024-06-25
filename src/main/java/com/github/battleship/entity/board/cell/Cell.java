package com.github.battleship.entity.board.cell;

import com.github.battleship.entity.board.util.Observer;
import com.github.battleship.entity.board.util.Subject;
import lombok.Data;

import java.util.Objects;

@Data
public class Cell implements Subject {

    private Observer observer;
    private CellType cellType;
    private int x;
    private int y;

    public Cell() {
    }

    public Cell(int x, int y, CellType cellType) {
        this.x = x;
        this.y = y;
        this.cellType = cellType;
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
