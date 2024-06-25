package com.github.battleship.entity.player;

import com.github.battleship.entity.board.cell.Cell;

public interface Bot extends Player{
    Cell generatePosition();
    void clearHitCells();
    void addCellToHitCells(Cell cell);
    void reset();
}
