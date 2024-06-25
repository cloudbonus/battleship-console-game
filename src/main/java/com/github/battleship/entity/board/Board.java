package com.github.battleship.entity.board;

import com.github.battleship.entity.board.cell.Cell;
import com.github.battleship.entity.board.cell.CellType;

import java.util.ArrayList;
import java.util.List;

public abstract class Board {
    public static final int BOARD_SIZE = 16;
    private final Cell[][] boardGrid;

    public Board() {
        this.boardGrid = new Cell[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell position = new Cell(i, j, CellType.WATER);
                this.boardGrid[i][j] = position;
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
                state.append(this.boardGrid[x][y].getCellType().getSymbol()).append(' ');
            }
            state.append("\n");
        }
        return state.toString();
    }

    public List<Cell> getEmptyCells() {
        List<Cell> emptyCells = new ArrayList<>();
        for (Cell[] cells : this.boardGrid) {
            for (Cell cell : cells) {
                if (cell != null && cell.getCellType() == CellType.WATER) {
                    emptyCells.add(cell);
                }
            }
        }
        return emptyCells;
    }

    public Cell getPosition(int x, int y) {
        return this.boardGrid[x][y];
    }

    public abstract Cell updatePosition(Cell position);
    public abstract String getRemainingShipsAfterGame();
}
