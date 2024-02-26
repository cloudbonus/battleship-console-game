package com.github.cloudbonus.util;

import com.github.cloudbonus.board.*;
import com.github.cloudbonus.user.User;

import java.util.ArrayList;
import java.util.List;


public class ConsoleInformationManager {

    public static void printHeader() {
        clearConsole();
        String text = "BATTLESHIP GAME";
        int totalLength = 109;
        int padding = (totalLength - text.length()) / 2;

        String header = """
                ###########################################################################################################
                        
                %s%s%s
                        
                ###########################################################################################################
                """.formatted(" ".repeat(padding), text, " ".repeat(padding));

        System.out.println(header);
    }

    public static void printGameModeMenu() {
        String mes = """
                Please select your preferred game mode:
                A. Singleplayer (play alone against the computer)
                B. Multiplayer (play against another player)
                            
                Enter your choice (A or B):
                """;
        System.out.println(mes);
    }

    public static void printOrientationMenu() {
        String mes = "Choose the orientation of the ship (H for horizontal, V for vertical):";
        System.out.println(mes);
    }

    public static void printShipPlacementModeMenu() {
        String mes = """
                Please choose the ship placement mode:
                A. Automatic (let the computer place the ships for you)
                B. Manual (manually place the ships on the board)
                            
                Enter your choice (A or B):
                """;
        System.out.println(mes);
    }

    public static void printMultiplayerMenu() {
        String mes = """
                Please select your role in the multiplayer game:
                                           
                A. Host (Start the server)
                B. Client (Connect to the server)
                                           
                Please enter your choice (A or B):
                """;
        System.out.println(mes);
    }

    public static void printPositionInputMessage() {
        System.out.println();
        System.out.println("Please input the position (use only A-P for letters and 1-16 for numbers, e.g., A13):");
    }

    public static void welcomeUser(String userName) {
        System.out.println("Welcome, " + userName + "! Congratulations on joining the Battleship game!");
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

    public static String createInputFromCell(Cell cell) {
        char y = (char) ('A' + cell.getY());
        int x = cell.getX() + 1;
        return "" + y + x;
    }

    public static Cell createCellFromInput(String input) {
        var coordinates = getCoordinatesFromInput(input);
        return new Cell(coordinates[0], coordinates[1], CellState.SHOT);
    }

    public static Cell recreateCellFromInput(String input) {
        var coordinates = getCoordinatesFromInput(input);
        return new Cell(coordinates[0], coordinates[1], CellState.DESTROYED);
    }

    private static int[] getCoordinatesFromInput(String input) {
        var y = input.charAt(0) - 'A';
        String numberStr = input.substring(1);
        var x = Integer.parseInt(numberStr) - 1;
        return new int[]{x, y};
    }

    public static List<Cell> generateSequence(String start, String end) {
        List<Cell> sequence = new ArrayList<>();
        char startChar = start.charAt(0);
        char endChar = end.charAt(0);
        int startNum = Integer.parseInt(start.substring(1));
        int endNum = Integer.parseInt(end.substring(1));

        for (char i = startChar; i <= endChar; i++) {
            for (int j = startNum; j <= endNum; j++) {
                sequence.add(recreateCellFromInput(String.valueOf(i) + j));
            }
        }
        return sequence;
    }

    public static String printGameInfo(User user, String opponentName) {
        StringBuilder sb = new StringBuilder();

        String text = "BATTLESHIP GAME";
        int totalLength = 109;
        int padding = (totalLength - text.length()) / 2;

        String header = """
                ###########################################################################################################
                        
                %s%s%s
                        
                ###########################################################################################################
                """.formatted(" ".repeat(padding), text, " ".repeat(padding));

        sb.append(header);

        String firstPlayerName = "   " + user.getName();
        String secondPlayerName = "   " + opponentName;
        String boardNames = String.format("%-35s   %s%n", firstPlayerName, secondPlayerName);
        sb.append(boardNames);

        String firstPlayerCount = String.format("   Remaining ships: %d", user.getLeftBoard().getRemainingShipsCount());
        String secondPlayerCount = String.format("   Remaining ships: %d", user.getRightBoard().getRemainingShipsCount());
        String remainingShipsCount = String.format("%-35s   %s%n", firstPlayerCount, secondPlayerCount);
        sb.append(remainingShipsCount);

        String[] board1 = user.getLeftBoard().getState().split("\n");
        String[] board2 = user.getRightBoard().getState().split("\n");
        String[] remainingShips = user.getRightBoard().getRemainingShips().split("\n");


        for (int i = 0; i < board1.length; i++) {
            if (i < remainingShips.length) {
                sb.append(String.format("%-35s   %-35s   %s%n", board1[i], board2[i], remainingShips[i]));
            } else {
                sb.append(String.format("%-35s   %s%n", board1[i], board2[i]));
            }
        }
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

    public static void printGameMode() {
        clearConsole();
        printHeader();
        printShipPlacementModeMenu();
        UserInteractionManager.setABSelectionInterpreter();
    }
}

