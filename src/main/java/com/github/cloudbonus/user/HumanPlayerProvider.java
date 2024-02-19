package com.github.cloudbonus.user;


public class HumanPlayerProvider {
    private static User instance;

    private HumanPlayerProvider(){}

    public static User getInstance() {
        if (instance == null) {
            instance = new HumanPlayer();
        }
        return instance;
    }
}
