package com.github.battleship.game.util;

import com.github.battleship.entity.board.cell.Cell;
import com.github.battleship.entity.board.cell.CellType;

import java.util.ArrayList;
import java.util.List;

public class CellConverter {
    public static String createInputFromCell(Cell cell) {
        char y = (char) ('A' + cell.getY());
        int x = cell.getX() + 1;
        return "" + y + x;
    }

    public static Cell createCellFromInput(String input) {
        var coordinates = getCoordinatesFromInput(input);
        return new Cell(coordinates[0], coordinates[1], CellType.MISS);
    }

    public static Cell createDestroyedCellFromInput(String input) {
        var coordinates = getCoordinatesFromInput(input);
        return new Cell(coordinates[0], coordinates[1], CellType.SUNK);
    }

    private static int[] getCoordinatesFromInput(String input) {
        var y = input.charAt(0) - 'A';
        String numberStr = input.substring(1);
        var x = Integer.parseInt(numberStr) - 1;
        return new int[]{x, y};
    }

    public static List<Cell> generateCellSequence(String start, String end) {
        List<Cell> sequence = new ArrayList<>();
        char startChar = start.charAt(0);
        char endChar = end.charAt(0);
        int startNum = Integer.parseInt(start.substring(1));
        int endNum = Integer.parseInt(end.substring(1));

        for (char i = startChar; i <= endChar; i++) {
            for (int j = startNum; j <= endNum; j++) {
                sequence.add(createDestroyedCellFromInput(String.valueOf(i) + j));
            }
        }
        return sequence;
    }
}
