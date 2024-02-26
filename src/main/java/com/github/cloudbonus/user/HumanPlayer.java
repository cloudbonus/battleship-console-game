package com.github.cloudbonus.user;

import com.github.cloudbonus.board.Cell;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class HumanPlayer extends User {

    @Override
    public String attackOpponent() {
        String target;
        while (true) {
            try {
                ConsoleInformationManager.printPositionInputMessage();
                UserInteractionManager.setPositionInterpreter();
                target = UserInteractionManager.createPositionFromInputOnline();
                if (getRightBoard().hasAttacked(target)) {
                    throw new IllegalArgumentException("You’ve already shot this cell");
                }
                break;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
        return target;
    }

    @Override
    public Cell giveResponse(Cell position) {
        return super.getLeftBoard().updatePosition(position);
    }

    @Override
    public void updateRightBoard(Cell cell) {
        super.getRightBoard().updatePosition(cell);
    }

    @Override
    public boolean hasLost() {
        return super.getLeftBoard().hasShipsOnBoard();
    }
}
