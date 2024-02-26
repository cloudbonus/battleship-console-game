package com.github.cloudbonus.server;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.cloudbonus.board.*;
import com.github.cloudbonus.service.GameService;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Setter;
import org.glassfish.tyrus.server.Server;

@ServerEndpoint(value = "/game")
public class BattleshipGameServerEndpoint {
    @Setter
    private static GameService gameService;
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

            String info = ConsoleInformationManager.printGameInfo(gameService.getUser(), gameService.getOpponentName());
            info = info + "\nYou are allowed only to watch host game";
            for (Session s : sessions) {
                if (!(boolean) s.getUserProperties().get("canPlay")) {
                    s.getBasicRemote().sendText(info);
                }
            }
        }
    }

    private void handleNameMessage(String message, Session session) throws IOException {
        gameService.setOpponentName(message.substring(5));
        String info = gameService.printGameInfo();
        System.out.println(info);
        System.out.println("Game info:");
        System.out.printf("%s's turn\n", gameService.getOpponentName());
        session.getBasicRemote().sendText("NAME_" + gameService.getUserName());
    }

    private void handleLostMessage(Session session) {
        System.out.println("Quitting the game");
        try {
            String message = String.format("Game finished. %s won.", gameService.getUserName());
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAttackMessage(String message, Session session) throws IOException {
        String response = gameService.processAttack(message);
        if ("LOST".equals(response)) {
            handleLostMessage(session);
        } else session.getBasicRemote().sendText(response);
    }

    private void handleCellStateMessage(String message, Session session) throws IOException {
        String response = gameService.processCellState(message);
        if ("END_TURN".equals(response)) {
            session.getBasicRemote().sendText("END_TURN");
        } else {
            session.getBasicRemote().sendText(response);
        }
    }

    private void handleDefaultMessage(String message, Session session) throws IOException {
        String position = gameService.attack(message);
        session.getBasicRemote().sendText(position);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.printf("Session %s closed because of %s\n", session.getId(), closeReason);
        connectedClients--;
        isFinished = true;
    }

    public static void startServer(int port, User user) {
        Server server;
        GameService gameService = new GameService();
        gameService.setUser(user);
        BattleshipGameServerEndpoint.setGameService(gameService);
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
