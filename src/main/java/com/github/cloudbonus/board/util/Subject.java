package com.github.cloudbonus.board.util;

public interface Subject {
    void attach(Observer observer);
    void notifyObserver();
}

