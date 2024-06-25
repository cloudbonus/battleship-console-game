package com.github.battleship.entity.player;

import com.github.battleship.entity.board.impl.BasicBoard;
import com.github.battleship.entity.board.impl.CompleteBoard;
import com.github.battleship.entity.board.cell.Cell;
import com.github.battleship.game.BattleController;


public interface Player {
    String attackOpponent();
    Cell giveResponse(Cell position);
    boolean hasLost();
    void updateRightBoard(Cell cell);

    void setBattleController(BattleController battleController);
    BattleController getBattleController();

    void setName(String name);
    String getName();

    void setLeftBoard(CompleteBoard leftBoard);
    CompleteBoard getLeftBoard();

    void setRightBoard(BasicBoard basicBoard);
    BasicBoard getRightBoard();
}
