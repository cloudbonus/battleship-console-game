package com.github.battleship.entity.exception;

import com.github.battleship.game.util.ConsoleDisplayManager;

public class InvalidShipPlacementException extends Exception {
    public InvalidShipPlacementException(String message) {
        super(ConsoleDisplayManager.AnsiColor.RED + message + ConsoleDisplayManager.AnsiColor.RESET);
    }
}
