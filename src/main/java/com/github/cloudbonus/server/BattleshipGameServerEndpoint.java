package com.github.cloudbonus.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.cloudbonus.board.cell.CellType;
import com.github.cloudbonus.game.BattleController;
import com.github.cloudbonus.util.ConsoleInformationManager;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Setter;
import org.glassfish.tyrus.server.Server;

@ServerEndpoint(value = "/game")
public class BattleshipGameServerEndpoint {
    @Setter
    private static BattleController battleController;
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
            if (message.startsWith("NAME")) {
                handleNameMessage(message, session);
            } else if (message.startsWith("LOST")) {
                handleLostMessage(message, session);
            } else if (message.matches("^[A-P][1-9]$|^[A-P]1[0-6]$")) {
                handleAttackMessage(message, session);
            } else if (CellType.isCellState(message) || message.startsWith("SUNK")) {
                handleCellStateMessage(message, session);
            } else {
                handleDefaultMessage(session);
            }

            String info = ConsoleInformationManager.printGameInfo(battleController.getUser(), battleController.getOpponentName());
            info += ConsoleInformationManager.getWatchOnlyMessage();
            for (Session s : sessions) {
                if (!(boolean) s.getUserProperties().get("canPlay")) {
                    s.getBasicRemote().sendText(info);
                }
            }
        }
    }

    private void handleNameMessage(String message, Session session) throws IOException {
        battleController.setOpponentName(message.substring(5));
        String info = battleController.printGameInfo();
        System.out.println(info);
        System.out.println(ConsoleInformationManager.getOpponentTurnMessage(battleController.getOpponentName()));
        session.getBasicRemote().sendText("NAME_" + battleController.getUserName());
    }

    private void handleLostMessage(String message, Session session) {
        String reason = battleController.processCellState(message);
        ConsoleInformationManager.printMatchResult(false, battleController.getOpponentName());
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, reason));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAttackMessage(String message, Session session) throws IOException {
        String response = battleController.processAttack(message);
        session.getBasicRemote().sendText(response);
    }

    private void handleCellStateMessage(String message, Session session) throws IOException {
        String response = battleController.processCellState(message);
        if ("END_TURN".equals(response)) {
            session.getBasicRemote().sendText("END_TURN");
        } else {
            session.getBasicRemote().sendText(response);
        }
    }

    private void handleDefaultMessage(Session session) throws IOException {
        String position = battleController.attack();
        session.getBasicRemote().sendText(position);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        boolean hasLost = battleController.getUser().hasLost();
        if (hasLost) {
            ConsoleInformationManager.printMatchResult(true, battleController.getOpponentName());
        }
        ConsoleInformationManager.printSessionClosure(session.getId(), closeReason.getReasonPhrase());
        connectedClients--;
        isFinished = true;
    }

    public static void startServer(int port, BattleController battleController) {
        Server server;
        BattleshipGameServerEndpoint.setBattleController(battleController);
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
