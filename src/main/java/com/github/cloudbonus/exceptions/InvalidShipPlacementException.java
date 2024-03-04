package com.github.cloudbonus.exceptions;

import com.github.cloudbonus.util.ConsoleInformationManager;

public class InvalidShipPlacementException extends Exception {
    public InvalidShipPlacementException(String message) {
        super(ConsoleInformationManager.AnsiColor.RED + message + ConsoleInformationManager.AnsiColor.RESET);
    }
}
