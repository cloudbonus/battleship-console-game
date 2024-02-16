package com.github.cloudbonus.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.github.cloudbonus.board.*;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Setter;
import org.glassfish.tyrus.server.Server;

import static com.github.cloudbonus.board.CellState.DESTROYED;
import static com.github.cloudbonus.board.CellState.SEIZED_SHOT;


@ServerEndpoint(value = "/game")
public class BattleshipGameServerEndpoint {
    @Setter
    private static User user;
    private static String opponentName;
    private static Cell position;
    private static volatile boolean isFinished = false;
    private static int connectedClients = 0;
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        connectedClients++;
        if (connectedClients == 1) {
            session.getUserProperties().put("canPlay", true);
        } else {
            session.getUserProperties().put("canPlay", false);
        }
        sessions.add(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        boolean canPlay = (boolean) session.getUserProperties().get("canPlay");
        if (canPlay) {
            if (message.startsWith("NAME_")) {
                handleNameMessage(message, session);
            } else if (message.equals("LOST")) {
                handleLostMessage(session);
            } else if (message.matches("^[A-P][1-9]$|^[A-P]1[0-6]$")) {
                handleAttackMessage(message, session);
            } else if (CellState.isCellState(message) || message.startsWith("SHIP_")) {
                handleCellStateMessage(message, session);
            } else {
                handleDefaultMessage(message, session);
            }
            String info = ConsoleInformationManager.getGameInfo(user, opponentName);
            for (Session s : sessions) {
                if (!(boolean) s.getUserProperties().get("canPlay")) {
                    s.getBasicRemote().sendText(info);
                }
            }
        }
    }

    private void handleNameMessage(String message, Session session) throws IOException {
        opponentName = message.substring(5);
        session.getBasicRemote().sendText("NAME_" + user.getName());
        ConsoleInformationManager.printGameInfo(user, opponentName);
        System.out.printf("%s's turn\n", opponentName);
    }

    private void handleLostMessage(Session session) {
        System.out.println("Quitting the game\n");
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Game finished. " + user.getName() + " won."));
            isFinished = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAttackMessage(String message, Session session) throws IOException {
        Cell cell = user.giveResponse(ConsoleInformationManager.createCellFromInput(message));
        ConsoleInformationManager.printGameInfo(user, opponentName);
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
        ConsoleInformationManager.printGameInfo(user, opponentName);
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
        ConsoleInformationManager.printGameInfo(user, opponentName);
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
        connectedClients--;
        isFinished = true;
    }

    public static void startServer(int port, User user) {
        Server server;
        setUser(user);
        server = new Server("localhost", port, "/websockets", null, BattleshipGameServerEndpoint.class);
        try {
            server.start();
            System.out.println("---server is running and waiting players");
            while (!isFinished) {
                Thread.onSpinWait();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            server.stop();
        }
    }
}
