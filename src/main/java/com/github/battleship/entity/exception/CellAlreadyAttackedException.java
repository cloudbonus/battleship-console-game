package com.github.battleship.entity.exception;

import com.github.battleship.game.util.ConsoleDisplayManager;

public class CellAlreadyAttackedException extends Exception {
    public CellAlreadyAttackedException(String message) {
        super(ConsoleDisplayManager.AnsiColor.RED + message + ConsoleDisplayManager.AnsiColor.RESET);
    }
}
