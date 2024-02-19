package com.github.cloudbonus.board;

import com.github.cloudbonus.util.ConsoleInformationManager;
import lombok.Getter;

import java.util.*;
import java.util.stream.Stream;

import static com.github.cloudbonus.board.CellState.EMPTY;

@Getter
public class BasicBoard extends Board{
    private final List<Integer> remainingShipSizes = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 6));
    public int getRemainingShipsCount() {
        return remainingShipSizes.size();
    }

    @Override
    public Cell updatePosition(Cell position) {
        Cell cell = super.getPosition(position.getX(), position.getY());
        cell.setCellState(position.getCellState());

        if (position.getCellState() == CellState.DESTROYED) {
            int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
            int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

            for (int i = 0; i < 8; i++) {
                int newX = position.getX() + dx[i];
                int newY = position.getY() + dy[i];

                if (newX >= 0 && newX < BOARD_SIZE && newY >= 0 && newY < BOARD_SIZE) {
                    Cell neighbour = super.getPosition(newX, newY);

                    if (neighbour.getCellState() != CellState.DESTROYED) {
                        neighbour.setCellState(CellState.SHOT);
                    }
                }
            }
        }
        return cell;
    }
    public void resetMap() {
        remainingShipSizes.clear();
        Stream.of(1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 5, 5, 6).forEach(remainingShipSizes::add);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                getPosition(i, j).setCellState(EMPTY);
            }
        }
    }
    public boolean hasAttacked(String position) {
        Cell cell = ConsoleInformationManager.createCellFromInput(position);
        return super.getPosition(cell.getX(), cell.getY()).getCellState() != EMPTY;
    }

    public String getRemainingShips() {
        Map<Integer, Integer> countMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        for (Integer size : remainingShipSizes) {
            countMap.put(size, countMap.getOrDefault(size, 0) + 1);
        }
        sb.append("Remaining Opponent's Ships\n");
        sb.append("───────────────────────────────\n");
        for (int i = 1; i <= 6; i++) {
            String shipIcon = countMap.containsKey(i) ? "☐" : "☒";
            String shipCount = countMap.containsKey(i) ? String.valueOf(countMap.get(i)) : "No";
            sb.append(String.format("%-6s - %s remaining%n", shipIcon.repeat(i), shipCount));
        }

        return sb.toString();
    }

    public void updateShipsOnBoard(int ship) {
        remainingShipSizes.remove(Integer.valueOf(ship));
    }

    public Integer getMaxRemainingShipSize() {
        return Collections.max(remainingShipSizes);
    }
}
