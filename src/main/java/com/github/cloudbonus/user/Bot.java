package com.github.cloudbonus.user;

import com.github.cloudbonus.board.Cell;
public interface Bot {
    Cell generatePosition();
    void clearHitCells();
    void addCellToHitCells(Cell cell);
}
