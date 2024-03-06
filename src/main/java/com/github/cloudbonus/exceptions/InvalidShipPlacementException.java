package com.github.cloudbonus.exceptions;

import com.github.cloudbonus.util.ConsoleDisplayManager;

public class InvalidShipPlacementException extends Exception {
    public InvalidShipPlacementException(String message) {
        super(ConsoleDisplayManager.AnsiColor.RED + message + ConsoleDisplayManager.AnsiColor.RESET);
    }
}
