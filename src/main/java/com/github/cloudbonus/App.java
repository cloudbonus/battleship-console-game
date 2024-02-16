package com.github.cloudbonus;

import com.github.cloudbonus.board.BasicBoard;
import com.github.cloudbonus.board.CompleteBoard;
import com.github.cloudbonus.game.Game;
import com.github.cloudbonus.user.HumanPlayer;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class App
{
    public static void main( String[] args ) {
        ConsoleInformationManager.printHeader();
        String userName = UserInteractionManager.getInputNameFromUser();
        User user = new HumanPlayer();
        user.setName(userName);

        CompleteBoard leftBoard = new CompleteBoard();
        BasicBoard rightBoard = new BasicBoard();
        user.setLeftBoard(leftBoard);
        user.setRightBoard(rightBoard);

        Game game = new Game();
        game.setFirstUser(user);
        game.setupGame();
    }
}
