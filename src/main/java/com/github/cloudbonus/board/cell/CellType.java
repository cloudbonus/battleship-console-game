package com.github.cloudbonus.board.cell;

import com.github.cloudbonus.util.ConsoleDisplayManager;
import lombok.Getter;

@Getter
public enum CellType {
    WATER(ConsoleDisplayManager.AnsiColor.BLUE + "." + ConsoleDisplayManager.AnsiColor.RESET),
    MISS("×"),
    SHIP(ConsoleDisplayManager.AnsiColor.YELLOW + "☐" + ConsoleDisplayManager.AnsiColor.RESET),
    HIT(ConsoleDisplayManager.AnsiColor.RED + "☒" + ConsoleDisplayManager.AnsiColor.RESET),
    SUNK(ConsoleDisplayManager.AnsiColor.RED + "☒" + ConsoleDisplayManager.AnsiColor.RESET);


    private final String symbol;

    CellType(String symbol) {
        this.symbol = symbol;
    }

    public static boolean isCellState(String message) {
        try {
            CellType.valueOf(message);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}