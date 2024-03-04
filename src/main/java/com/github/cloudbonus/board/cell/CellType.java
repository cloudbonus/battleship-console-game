package com.github.cloudbonus.board.cell;

import com.github.cloudbonus.util.ConsoleInformationManager;
import lombok.Getter;

@Getter
public enum CellType {
    WATER(ConsoleInformationManager.AnsiColor.BLUE + "." + ConsoleInformationManager.AnsiColor.RESET),
    MISS("×"),
    SHIP(ConsoleInformationManager.AnsiColor.YELLOW + "☐" + ConsoleInformationManager.AnsiColor.RESET),
    HIT(ConsoleInformationManager.AnsiColor.RED + "☒" + ConsoleInformationManager.AnsiColor.RESET),
    SUNK(ConsoleInformationManager.AnsiColor.RED + "☒" + ConsoleInformationManager.AnsiColor.RESET);


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