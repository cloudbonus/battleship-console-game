package com.github.battleship.entity.board.ship;

import lombok.Getter;

@Getter
public enum ShipType {

    PATROL_BOAT(6, 1),
    DESTROYER(5, 2),
    SUBMARINE(4, 3),
    CRUISER(3, 4),
    BATTLESHIP(2, 5),
    CARRIER(1, 6);

    private final int numShips;
    private final int shipLength;

    ShipType(int numShips, int shipLength) {
        this.numShips = numShips;
        this.shipLength = shipLength;
    }

    public static int sizeAllShips() {
        int sum = 0;
        for (ShipType type : ShipType.values()) {
            sum += type.numShips;
        }
        return sum;
    }

    public static String convertShipTypeToNormalString(ShipType type) {
        return switch (type) {
            case PATROL_BOAT -> "Patrol Boat";
            case DESTROYER -> "Destroyer";
            case SUBMARINE -> "Submarine";
            case CRUISER -> "Cruiser";
            case BATTLESHIP -> "Battleship";
            case CARRIER -> "Carrier";
        };
    }
}
