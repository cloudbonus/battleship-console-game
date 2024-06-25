package com.github.battleship.entity.board.util;

public interface Subject {
    void attach(Observer observer);
    void notifyObserver();
}

