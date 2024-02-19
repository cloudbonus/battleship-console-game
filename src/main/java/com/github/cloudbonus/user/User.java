package com.github.cloudbonus.user;

import com.github.cloudbonus.board.BasicBoard;
import com.github.cloudbonus.board.Cell;
import com.github.cloudbonus.board.CompleteBoard;
import lombok.Data;



@Data
public abstract class User implements Player{
    private String name;
    private CompleteBoard leftBoard;
    private BasicBoard rightBoard;
    public abstract String attackOpponentOnline();
    public abstract void updateRightBoard(Cell cell);
}
