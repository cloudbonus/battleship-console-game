package com.github.cloudbonus.util;

import java.util.Scanner;

public class UserInteractionManager {
    private static final Scanner scanner = new Scanner(System.in);
    private static Interpreter interpreter;

    public static void setPositionInterpreter() {
        interpreter = new PositionInterpreter();
    }

    public static void setOrientationInterpreter() {
        interpreter = new OrientationInterpreter();
    }

    public static void setABSelectionInterpreter() {
        interpreter = new ABSelectionInterpreter();
    }

    public static void setPortInterpreter() {
        interpreter = new PortInterpreter();
    }

    public static String createPositionFromInput() {
        String input;
        while (true) {
            input = getInputFromUser();
            if (isValidPosition(input)) {
                break;
            } else {
                String message= "Input does not match the pattern. Try again: ";
                System.out.printf("\n%s%s%s", ConsoleInformationManager.AnsiColor.YELLOW, message, ConsoleInformationManager.AnsiColor.RESET);
            }
        }
        System.out.println();
        return input;
    }

    public static boolean getShipOrientationFromInput() {
        boolean isVertical;
        while (true) {
            String input = getInputFromUser();
            if (isValidPosition(input)) {
                isVertical = input.equals("V");
                break;
            } else {
                String message= "Input does not match the pattern. Try again: ";
                System.out.printf("\n%s%s%s", ConsoleInformationManager.AnsiColor.YELLOW, message, ConsoleInformationManager.AnsiColor.RESET);
            }
        }
        return isVertical;
    }

    public static String getABSelectionFromInput() {
        while (true) {
            String input = getInputFromUser();
            if (isValidPosition(input)) {
                return input;
            } else {
                String message= "Input does not match the pattern. Try again: ";
                System.out.printf("\n%s%s%s", ConsoleInformationManager.AnsiColor.YELLOW, message, ConsoleInformationManager.AnsiColor.RESET);
            }
        }
    }

    public static int getPortFromInput() {
        while (true) {
            String input = getInputFromUser();
            if (isValidPosition(input)) {
                return Integer.parseInt(input);
            } else {
                String message= "Input does not match the pattern. Try again: ";
                System.out.printf("\n%s%s%s", ConsoleInformationManager.AnsiColor.YELLOW, message, ConsoleInformationManager.AnsiColor.RESET);
            }
        }
    }

    public static String getInputNameFromUser() {
        System.out.println("Hello, how should I address you?");
        System.out.print("Please enter: ");
        return scanner.nextLine();
    }

    private static String getInputFromUser() {
        return scanner.nextLine();
    }

    private static boolean isValidPosition(String input) {
        InputContext context = new InputContext(input);
        return interpreter.interpret(context);
    }

}

record InputContext(String input) {
}

interface Interpreter {
    boolean interpret(InputContext context);
}

class PositionInterpreter implements Interpreter {
    @Override
    public boolean interpret(InputContext context) {
        String input = context.input();
        return input.matches("^[A-P][1-9]$|^[A-P]1[0-6]$");
    }
}

class OrientationInterpreter implements Interpreter {
    @Override
    public boolean interpret(InputContext context) {
        String input = context.input();
        return input.matches("^H$|^V$");
    }
}

class ABSelectionInterpreter implements Interpreter {
    @Override
    public boolean interpret(InputContext context) {
        String input = context.input();
        return input.matches("^A$|^B$");
    }
}

class PortInterpreter implements Interpreter {
    @Override
    public boolean interpret(InputContext context) {
        String input = context.input();
        return input.matches("^(1[5-9]\\d{2}|[2-7]\\d{3}|8000)$");
    }
}

