package com.github.cloudbonus.user;

import com.github.cloudbonus.board.cell.Cell;
import com.github.cloudbonus.exceptions.CellAlreadyAttackedException;
import com.github.cloudbonus.util.ConsoleInformationManager;
import com.github.cloudbonus.util.UserInteractionManager;

public class HumanPlayer extends User {

    @Override
    public String attackOpponent() {
        String target;
        while (true) {
            try {
                UserInteractionManager.setPositionInterpreter();
                ConsoleInformationManager.printPositionInputMessage();
                target = UserInteractionManager.createPositionFromInput();
                if (getRightBoard().hasAttacked(target)) {
                    throw new CellAlreadyAttackedException("You’ve already shot this cell");
                }
                break;
            } catch (CellAlreadyAttackedException e) {
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
