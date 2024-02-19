package com.github.cloudbonus.client;

import com.github.cloudbonus.board.*;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import jakarta.websocket.*;
import lombok.Setter;
import org.glassfish.tyrus.client.ClientManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.github.cloudbonus.board.CellState.DESTROYED;
import static com.github.cloudbonus.board.CellState.SEIZED_SHOT;

@ClientEndpoint
public class BattleshipGameClientEndpoint
{
    @Setter private static User user;
    private static String opponentName;
    private static Cell position;
    private static CountDownLatch latch;
    @OnOpen
    public void onOpen(Session session) {
        try
        {
            System.out.println("---connecting to the server");
            session.getBasicRemote().sendText("NAME_" + user.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if(message.length() < 100) {
            if (message.startsWith("NAME_")) {
                opponentName = message.substring(5);
            } if (message.equals("LOST")) {
                handleLostMessage(session);
            } else if (message.matches("^[A-P][1-9]$|^[A-P]1[0-6]$")) {
                handleAttackMessage(message, session);
            } else if (CellState.isCellState(message) || message.startsWith("SHIP_")) {
                handleCellStateMessage(message, session);
            } else {
                handleDefaultMessage(message, session);
            }
        }
        else {
            ConsoleInformationManager.clearConsole();
            System.out.println(message);
        }
    }

    private void handleLostMessage(Session session) {
        System.out.println("Quitting the game");
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Game finished. " + user.getName() + " won."));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAttackMessage(String message, Session session) throws IOException {
        Cell cell = user.giveResponse(ConsoleInformationManager.createCellFromInput(message));
        String info = ConsoleInformationManager.printGameInfo(user, opponentName);
        System.out.println(info);
        System.out.println("Game info:");
        System.out.printf("%s's turn\n", opponentName);
        System.out.printf("%s attacked %s\n", opponentName, message);
        processAttackResult(cell, session);
    }

    private void processAttackResult(Cell cell, Session session) throws IOException {
        if (user.hasLost()) {
            handleLostMessage(session);
        } else if (cell.getCellState() == DESTROYED) {
            Ship lastDestroyedShip = user.getLeftBoard().getLastDestroyedShip();
            int size = lastDestroyedShip.getPosition().size();
            String end = ConsoleInformationManager.createInputFromCell(lastDestroyedShip.getPosition().get(size - 1));
            String start = ConsoleInformationManager.createInputFromCell(lastDestroyedShip.getPosition().get(0));
            System.out.printf("Your ship of size %s has been destroyed\n", lastDestroyedShip.getSize());
            session.getBasicRemote().sendText("SHIP_" + start + "_" + end + "_" + lastDestroyedShip.getSize());
        } else {
            System.out.printf("%s hit! Stay prepared for another imminent attack\n", opponentName);
            session.getBasicRemote().sendText(cell.getCellState().name());
        }
    }

    private void handleCellStateMessage(String message, Session session) throws IOException {
        if (message.startsWith("SHIP_")) {
            handleShipMessage(message);
        } else {
            handleNonShipMessage(message);
        }
        String info = ConsoleInformationManager.printGameInfo(user, opponentName);
        System.out.println(info);
        System.out.println("Game info:");
        handleTurn(message, session);
    }

    private void handleShipMessage(String message) {
        String[] parts = message.split("_");
        String secondValue = parts[1];
        String thirdValue = parts[2];
        String fourthValue = parts[3];

        if (secondValue.equals(thirdValue)) {
            updateBoardWithCellState("DESTROYED");
        } else {
            List<Cell> cells = ConsoleInformationManager.generateSequence(secondValue, thirdValue);
            cells.forEach(cell -> user.updateRightBoard(cell));
            updateShipsOnBoard(fourthValue);
            System.out.printf("You have destroyed a ship of size %s\n", fourthValue);
        }
    }

    private void handleNonShipMessage(String message) {
        updateBoardWithCellState(message);
    }

    private void updateBoardWithCellState(String message) {
        position.setCellState(CellState.valueOf(message));
        user.updateRightBoard(position);
    }

    private void updateShipsOnBoard(String fourthValue) {
        user.getRightBoard().updateShipsOnBoard(Integer.parseInt(fourthValue));
    }

    private void handleTurn(String message, Session session) throws IOException {
        if (!message.startsWith("SHIP_") && position.getCellState() != SEIZED_SHOT && position.getCellState() != DESTROYED) {
            System.out.println("You missed!");
            System.out.printf("%s's turn\n", opponentName);
            session.getBasicRemote().sendText("END_TURN");
        } else {
            System.out.println("Your turn");
            if (message.startsWith("SHIP_")) {
                System.out.printf("You have destroyed a ship of size %s\n", message.split("_")[3]);
            }
            System.out.println("You hit! Congratulations, you can attack once more");
            String position_proxy = user.attackOpponentOnline();
            position = ConsoleInformationManager.createCellFromInput(position_proxy);
            session.getBasicRemote().sendText(position_proxy);
        }
    }

    private void handleDefaultMessage(String message, Session session) throws IOException {
        String info = ConsoleInformationManager.printGameInfo(user, opponentName);
        System.out.println(info);
        System.out.println("Game info:");
        if ("END_TURN".equals(message)) {
            System.out.printf("%s missed!\n", opponentName);
        }
        System.out.println("Your turn");
        String position_proxy = user.attackOpponentOnline();
        position = ConsoleInformationManager.createCellFromInput(position_proxy);
        session.getBasicRemote().sendText(position_proxy);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.printf("Session %s closed because of %s\n", session.getId(), closeReason);
        latch.countDown();
    }

    public static void startClient(int port, User user) {
        latch = new CountDownLatch(1);

        ClientManager client = ClientManager.createClient();
        try {
            String args = String.format("ws://localhost:%s/websockets/game", port);
            BattleshipGameClientEndpoint.setUser(user);
            client.connectToServer(BattleshipGameClientEndpoint.class, new URI(args));
            latch.await();

        } catch (DeploymentException | URISyntaxException | InterruptedException | IOException e) {
            System.err.println("Failed to connect to the server: " + e.getMessage());
        } finally {
            latch.countDown();
        }
    }
}