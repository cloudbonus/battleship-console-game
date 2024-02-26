package com.github.cloudbonus.user;

import com.github.cloudbonus.board.Cell;

public interface Player {
    String attackOpponent();
    Cell giveResponse(Cell position);
    boolean hasLost();
}
