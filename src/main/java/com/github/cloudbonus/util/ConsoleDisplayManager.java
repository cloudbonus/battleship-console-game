package com.github.cloudbonus.util;

import com.github.cloudbonus.board.ship.ShipType;
import com.github.cloudbonus.user.User;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ConsoleDisplayManager {
    public enum AnsiColor {
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        PURPLE("\u001B[35m"),
        RESET("\u001B[0m");

        private final String code;

        AnsiColor(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return code;
        }
    }

    public static void printHeader() {
        clearConsole();
        System.out.println(createGameHeader());
    }

    public static void printGameModeMenu() {
        String message = """
                Please select your preferred game mode:
                A. Singleplayer (play alone against the computer)
                B. Multiplayer (play against another player)
                            
                Enter your choice (A or B):\s""";
        System.out.print(message);
    }

    public static void printOrientationMenu() {
        String message = "Choose the orientation of the ship (H for horizontal, V for vertical): ";
        System.out.print(message);
    }
    public static void printEmptyRows(int numRows) {
        for(int i = 0; i < numRows; i++) {
            System.out.println();
        }
    }

    public static void printShipPlacementModeMenu() {
        String message = """
                Please choose the ship placement mode:
                A. Automatic (let the computer place the ships for you)
                B. Manual (manually place the ships on the board)
                            
                Enter your choice (A or B):\s""";
        System.out.print(message);
    }

    public static void printMultiplayerMenu() {
        String message = """
                Please select your role in the multiplayer game:
                A. Host (Start the server)
                B. Client (Connect to the server)
                                           
                Please enter your choice (A or B):\s""";
        System.out.print(message);
    }

    public static void printPositionInputMessage() {
        System.out.print("Please input the position (use only A-P for letters and 1-16 for numbers, e.g., A13): ");
    }

    public static void welcomeUser(String userName) {
        System.out.printf("Welcome, %s%s%s! Congratulations on joining the Battleship game!\n\n", AnsiColor.GREEN, userName, AnsiColor.RESET);
    }

    public static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("bash", "-c", "clear").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void printPlacementMessage(ShipType shipType, int totalShipsPlacedOfThisType, int totalShipsPlaced) {
        String shipName = ShipType.convertShipTypeToNormalString(shipType);
        int shipSize = shipType.getShipLength();
        int totalShipsOfThisType = shipType.getNumShips();
        int totalShips = ShipType.sizeAllShips();
        String message = String.format("You are placing a %s of size %d. After this, you will have %d more of this type to place,\nand %d more ships in total to place.\n",
                shipName, shipSize, totalShipsOfThisType - totalShipsPlacedOfThisType - 1, totalShips - totalShipsPlaced - 1);
        System.out.println(message);
    }

    private static String createGameHeader() {

        String text = """
                 ____       _______ _______ _      ______  _____ _    _ _____ _____\s
                |  _ \\   /\\|__   __|__   __| |    |  ____|/ ____| |  | |_   _|  __  \\ \s
                | |_) | /  \\  | |     | |  | |    | |__  | (___ | |__| | | | | |__) |
                |  _ < / /\\ \\ | |     | |  | |    |  __|  \\___ \\|  __  | | | |  ___/
                | |_) / ____ \\| |     | |  | |____| |____ ____) | |  | |_| |_| |   \s
                |____/_/    \\_\\_|     |_|  |______|______|_____/|_|  |_|_____|_|   \s
                """;
        int totalLength = 109;
        int padding = (totalLength - 69) / 2;
        String paddingSpaces = " ".repeat(padding);

        return Arrays.stream(text.split("\\n"))
                .map(line -> paddingSpaces + line + paddingSpaces)
                .collect(Collectors.joining("\n")) + "\n";
    }

    private static String createBoardNamesHeader(String userName, String opponentName) {
        String firstPlayerName = String.format("   %s%s%s", AnsiColor.GREEN, userName, AnsiColor.RESET);
        String secondPlayerName = String.format("   %s%s%s", AnsiColor.RED, opponentName, AnsiColor.RESET);
        return String.format("\n%-44s   %s%n", firstPlayerName, secondPlayerName);
    }

    private static String createRemainingShipsHeader(int userCount, int opponentCount) {
        String firstPlayerCount = String.format("   Remaining ships: %s%d%s", AnsiColor.GREEN, userCount, AnsiColor.RESET);
        String secondPlayerCount = String.format("   Remaining ships: %s%d%s", AnsiColor.RED, opponentCount, AnsiColor.RESET);
        return String.format("%-44s   %s%n", firstPlayerCount, secondPlayerCount);
    }

    private static String createBattleBody(String[]... args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args[0].length; i++) {
            if (args.length > 2 && i < args[2].length) {
                sb.append(String.format("%-35s   %-35s   %s%n", args[0][i], args[1][i], args[2][i]));
            } else if (i == 12) {
                sb.append(String.format("%-35s   %-35s   %s%n", args[0][i], args[1][i], getTurnMessage(args[3][0])));
            } else {
                sb.append(String.format("%-35s   %s%n", args[0][i], args[1][i]));
            }
        }
        return sb.toString();
    }

    public static String getGameInfo(User user, String opponentName, int totalTurns) {
        StringBuilder sb = new StringBuilder();
        String gameHeader = createGameHeader();
        sb.append(gameHeader);

        String boardNamesHeader = createBoardNamesHeader(user.getName(), opponentName);
        sb.append(boardNamesHeader);

        int userCount = user.getLeftBoard().getRemainingShipsCount();
        int opponentCount = user.getRightBoard().getRemainingShipsSum();
        String remainingShipsHeader = createRemainingShipsHeader(userCount, opponentCount);
        sb.append(remainingShipsHeader);

        String[] board1 = user.getLeftBoard().getState().split("\n");
        String[] board2 = user.getRightBoard().getState().split("\n");
        String[] remainingShips = user.getRightBoard().getShipsState().split("\n");

        String boardsAndShips = createBattleBody(board1, board2, remainingShips, new String[]{String.valueOf(totalTurns)});
        sb.append(boardsAndShips);

        return sb.toString();
    }

    public static void printGameSetup(User user) {
        printHeader();
        welcomeUser(user.getName());
        printGameModeMenu();
        UserInteractionManager.setABSelectionInterpreter();
    }

    public static void printMultiplayerSetup() {
        printHeader();
        printMultiplayerMenu();
        UserInteractionManager.setABSelectionInterpreter();
    }

    public static void printShipPlacementSetup() {
        clearConsole();
        printHeader();
        printShipPlacementModeMenu();
        UserInteractionManager.setABSelectionInterpreter();
    }

    public static String getSunkMessage() {
        return String.format("%sSUNK%s", AnsiColor.RED, AnsiColor.RESET);
    }

    public static String getMissMessage() {
        return String.format("%sMISSED%s", AnsiColor.BLUE, AnsiColor.RESET);
    }

    public static String getHitMessage() {
        return String.format("%sHIT%s", AnsiColor.YELLOW, AnsiColor.RESET);
    }

    public static String getPlayerTurnMessage() {
        return String.format("%sYOUR TURN%s", AnsiColor.GREEN, AnsiColor.RESET);
    }

    public static String getPositionMessage(String position) {
        return String.format("%s%s%s", AnsiColor.PURPLE, position, AnsiColor.RESET);
    }

    public static String getOpponentTurnMessage(String opponentName) {
        return String.format("%s%s'S TURN%s", AnsiColor.RED, opponentName.toUpperCase(), AnsiColor.RESET);
    }

    public static String getOpponentAttackMessage(String opponentName, String message) {
        return String.format("%s attacked %s", opponentName, getPositionMessage(message));
    }

    public static String getPlayerShipStatus(String shipName, int shipSize) {
        return String.format("Your %s of size %d has been %s", shipName, shipSize, getSunkMessage());
    }

    public static String getPlayerImminentAttackWarning(String opponentName) {
        return String.format("%s %s! Stay prepared for another imminent attack", opponentName, getHitMessage());
    }

    public static String getOpponentMissMessage(String opponentName) {
        return String.format("%s %s", opponentName, getMissMessage());
    }

    public static String getPlayerShipSunkMessage(String shipName, int shipSize) {
        return String.format("You have %s %s of size %d", getSunkMessage(), shipName, shipSize);
    }

    public static String getPlayerMissMessage() {
        return String.format("You %s", getMissMessage());
    }

    public static String getPlayerHitMessage() {
        return String.format("You %s! Congratulations, you can attack once more", getHitMessage());
    }

    public static String getTurnMessage(String totalTurns) {
        String s = String.format("MATCH TURN: %s%s%s", AnsiColor.PURPLE, totalTurns, AnsiColor.RESET);
        s = String.format("%31s", s);
        return String.format("%-31s", s);
    }

    public static void printMatchResult(boolean flag, String name) {
        AnsiColor color = flag ? AnsiColor.RED : AnsiColor.GREEN;
        String winnerName = flag ? name : "You";
        System.out.printf("%sGame finished. %s win.%s\n\n", color, winnerName, AnsiColor.RESET);
    }

    public static String getPlayerMatchStatus(boolean flag) {
        AnsiColor color = flag ? AnsiColor.RED : AnsiColor.GREEN;
        String status = flag ? "LOST" : "WIN";
        return String.format("%s%s%s", color, status, AnsiColor.RESET);
    }

    public static void getMatchTimes(String matchStartTime, String matchEndTime, Duration duration) {
        String startTime = String.format("Match start time: %s\n", matchStartTime);
        String endTime = String.format("Match end time: %s\n", matchEndTime);
        String elapsedTime = String.format("Total match time: %d hours, %d minutes\n", duration.toHours(), duration.toMinutes());
        System.out.println(startTime + endTime + elapsedTime);
    }

    public static void printSessionClosure(String id, String message) {
        System.out.printf("Session %s closed because of %s\n", id, message);
    }

    public static String getReasonMessage() {
        return "game finished";
    }

    public static String getWatchOnlyMessage() {
        return String.format("\n%sYou are allowed only to watch host game%s\n", AnsiColor.YELLOW, AnsiColor.RESET);
    }

    public static void printStatsMessage() {
        System.out.printf("%sStats of the Match%s\n\n", AnsiColor.YELLOW, AnsiColor.RESET);
    }

    public static void printEfficiency(long efficiency) {
        System.out.printf("Efficiency: %d%%\n", efficiency);
    }

    public static void printPlayerStats(String playerName, boolean hasLost, String remainingShips) {
        String ships = hasLost ? "no ships left" : remainingShips;
        System.out.printf("Player: %s\nStatus: %s\nRemaining ships: %s\n", playerName, getPlayerMatchStatus(hasLost), ships);
    }
}
