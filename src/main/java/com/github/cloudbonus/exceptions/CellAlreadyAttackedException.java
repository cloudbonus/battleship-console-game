package com.github.cloudbonus.exceptions;

import com.github.cloudbonus.util.ConsoleInformationManager;

public class CellAlreadyAttackedException extends Exception {
    public CellAlreadyAttackedException(String message) {
        super(ConsoleInformationManager.AnsiColor.RED + message + ConsoleInformationManager.AnsiColor.RESET);
    }
}
