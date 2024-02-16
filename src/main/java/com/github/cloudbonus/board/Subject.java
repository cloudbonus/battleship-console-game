package com.github.cloudbonus.board;

interface Subject {
    void attach(Observer observer);
    void notifyObserver();
}

