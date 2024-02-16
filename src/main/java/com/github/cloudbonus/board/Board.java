package com.github.cloudbonus.board;

import java.util.ArrayList;
import java.util.List;

import static com.github.cloudbonus.board.CellState.EMPTY;

public abstract class Board {
    public static final int BOARD_SIZE = 16;
    private final Cell[][] boardGrid;

    

    public Board() {
        this.boardGrid = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell position = new Cell(i, j, EMPTY);
                setPosition(position);
            }
        }
    }

    public String getState() {
        StringBuilder state = new StringBuilder();
        state.append(String.format("%1s", "   A B C D E F G H I J K L M N O P\n"));
        state.append(String.format("%1s", "  ┌───────────────────────────────\n"));
        for (int x = 0; x < BOARD_SIZE; x++) {
            state.append(String.format("%2d|", x + 1));
            for (int y = 0; y < BOARD_SIZE; y++) {
                state.append(getPosition(x, y).getCellState().getSymbol()).append(' ');
            }
            state.append("\n");
        }
        return state.toString();
    }

    public Cell getPosition(int x, int y){
        return boardGrid[x][y];
    }

    public void setPosition(Cell position) {
        boardGrid[position.getX()][position.getY()] = position;
    }

    public abstract Cell updatePosition(Cell position);

    public List<Cell> getEmptyCells(){
        List<Cell> emptyCells = new ArrayList<>();
        for (Cell[] cells : boardGrid) {
            for (Cell cell : cells) {
                if (cell != null && cell.getCellState() == EMPTY) {
                    emptyCells.add(cell);
                }
            }
        }
        return emptyCells;
    }
}
