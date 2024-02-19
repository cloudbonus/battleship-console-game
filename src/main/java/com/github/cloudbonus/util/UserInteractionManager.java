package com.github.cloudbonus.util;

import com.github.cloudbonus.board.Cell;
import lombok.Getter;

import java.util.Scanner;

import static com.github.cloudbonus.util.ConsoleInformationManager.createCellFromInput;

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

    public static Cell createPositionFromInput() {
        Cell cell;
        while (true) {
            String input = getInputFromUser();
            if (isValidPosition(input)) {
                cell = createCellFromInput(input);
                break;
            } else System.out.println("Input does not match the pattern");
        }
        System.out.println();
        return cell;
    }

    public static String createPositionFromInputOnline() {
        String input;
        while (true) {
            input = getInputFromUser();
            if (isValidPosition(input)) {
                break;
            } else System.out.println("Input does not match the pattern");
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
                System.out.println("Input does not match the pattern");
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
                System.out.println("Input does not match the pattern");
            }
        }
    }

    public static int getPortFromInput() {
        while (true) {
            String input = getInputFromUser();
            if (isValidPosition(input)) {
                return Integer.parseInt(input);
            } else {
                System.out.println("Input does not match the pattern");
            }
        }
    }

    public static String getInputNameFromUser() {
        System.out.println("Hello, how should I address you?");
        System.out.println("Please enter:");
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

@Getter
class InputContext {
    private final String input;

    public InputContext(String input) {
        this.input = input;
    }

}

interface Interpreter {
    boolean interpret(InputContext context);
}

class PositionInterpreter implements Interpreter {
    @Override
    public boolean interpret(InputContext context) {
        String input = context.getInput();
        return input.matches("^[A-P][1-9]$|^[A-P]1[0-6]$");
    }
}

class OrientationInterpreter implements Interpreter {
    @Override
    public boolean interpret(InputContext context) {
        String input = context.getInput();
        return input.matches("^H$|^V$");
    }
}

class ABSelectionInterpreter implements Interpreter {
    @Override
    public boolean interpret(InputContext context) {
        String input = context.getInput();
        return input.matches("^A$|^B$");
    }
}

class PortInterpreter implements Interpreter {
    @Override
    public boolean interpret(InputContext context) {
        String input = context.getInput();
        return input.matches("^(1[5-9]\\d{2}|[2-7]\\d{3}|8000)$");
    }
}

