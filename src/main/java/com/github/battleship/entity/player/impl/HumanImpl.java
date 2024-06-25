package com.github.battleship.entity.player.impl;

import com.github.battleship.entity.board.impl.BasicBoard;
import com.github.battleship.entity.board.impl.CompleteBoard;
import com.github.battleship.entity.board.cell.Cell;
import com.github.battleship.entity.exception.CellAlreadyAttackedException;
import com.github.battleship.entity.player.Human;
import com.github.battleship.game.BattleController;
import com.github.battleship.game.util.ConsoleDisplayManager;
import com.github.battleship.game.util.UserInteractionManager;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
public class HumanImpl implements Human {

    private String name;

    @Autowired
    private CompleteBoard leftBoard;

    @Autowired
    private BasicBoard rightBoard;

    @Autowired
    private BattleController battleController;

    public void setName(String name) {
        this.name = name;
        battleController.setPlayer(this);
    }

    @Override
    public String attackOpponent() {
        String target;
        while (true) {
            try {
                UserInteractionManager.setPositionInterpreter();
                ConsoleDisplayManager.printPositionInputMessage();
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
        return this.getLeftBoard().updatePosition(position);
    }

    @Override
    public void updateRightBoard(Cell cell) {
        this.getRightBoard().updatePosition(cell);
    }

    @Override
    public boolean hasLost() {
        return this.getLeftBoard().hasShipsOnBoard();
    }
}
