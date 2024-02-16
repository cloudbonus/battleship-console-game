package com.github.cloudbonus.board;

import lombok.Getter;

@Getter
public enum CellState {
    EMPTY('.'),
    SHOT('×'),
    SEIZED('☐'),
    SEIZED_SHOT('☒'),
    DESTROYED('☒');


    private final char symbol;

    CellState(char symbol) {
        this.symbol = symbol;
    }

    public static boolean isCellState(String message) {
        try {
            CellState.valueOf(message);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}