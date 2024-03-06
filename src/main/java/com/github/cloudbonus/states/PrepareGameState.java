package com.github.cloudbonus.states;

import com.github.cloudbonus.board.BasicBoard;
import com.github.cloudbonus.board.CompleteBoard;
import com.github.cloudbonus.stateMachine.*;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.user.HumanPlayerProvider;
import com.github.cloudbonus.util.ConsoleDisplayManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class PrepareGameState implements EnterState{
    public PrepareGameState(StateMachine stateMachine){
        this.stateMachine = stateMachine;
    }

    private final StateMachine stateMachine;
    @Override
    public void enter(){
        ConsoleDisplayManager.printHeader();
        
        createUser();
        setupUser();
        enterGameModeSelectionState();
    }

    private void createUser(){
        HumanPlayerProvider.getInstance();
    }

    private void setupUser(){
        User user = HumanPlayerProvider.getInstance();
        
        String userName = UserInteractionManager.getInputNameFromUser();
        CompleteBoard leftBoard = new CompleteBoard();
        BasicBoard rightBoard = new BasicBoard();
        
        user.setName(userName);
        user.setLeftBoard(leftBoard);
        user.setRightBoard(rightBoard);
    }

    private void enterGameModeSelectionState(){
        this.stateMachine.changeState(GameModeSelectionState.class);
    }
}