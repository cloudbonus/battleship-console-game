package com.github.cloudbonus.exceptions;

import com.github.cloudbonus.util.ConsoleDisplayManager;

public class CellAlreadyAttackedException extends Exception {
    public CellAlreadyAttackedException(String message) {
        super(ConsoleDisplayManager.AnsiColor.RED + message + ConsoleDisplayManager.AnsiColor.RESET);
    }
}
