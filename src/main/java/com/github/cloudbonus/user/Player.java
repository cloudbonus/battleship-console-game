package com.github.cloudbonus.user;

import com.github.cloudbonus.board.Cell;

public interface Player {
    Cell attackOpponent(Player opponent);
    Cell giveResponse(Cell position);
    boolean hasLost();
}
