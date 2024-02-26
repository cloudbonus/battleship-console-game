package com.github.cloudbonus.client;

import com.github.cloudbonus.board.*;
import com.github.cloudbonus.service.GameService;
import com.github.cloudbonus.user.User;
import com.github.cloudbonus.util.ConsoleInformationManager;
import jakarta.websocket.*;
import lombok.Setter;
import org.glassfish.tyrus.client.ClientManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class BattleshipGameClientEndpoint {
    @Setter
    private static GameService gameService;
    private static CountDownLatch latch;

    @OnOpen
    public void onOpen(Session session) {
        try {
            System.out.println("---connecting to the server");
            String message = String.format("NAME_%s", gameService.getUserName());
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if (message.length() < 100) {
            if (message.startsWith("NAME_")) {
                gameService.setOpponentName(message.substring(5));
            }

            if (message.equals("LOST")) {
                handleLostMessage(session);
            } else if (message.matches("^[A-P][1-9]$|^[A-P]1[0-6]$")) {
                handleAttackMessage(message, session);
            } else if (CellState.isCellState(message) || message.startsWith("SHIP_")) {
                handleCellStateMessage(message, session);
            } else {
                handleDefaultMessage(message, session);
            }
        } else {
            ConsoleInformationManager.clearConsole();
            System.out.println(message);
        }
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
        latch.countDown();
    }

    public static void startClient(int port, User user) {
        latch = new CountDownLatch(1);

        ClientManager client = ClientManager.createClient();
        try {
            String args = String.format("ws://localhost:%s/websockets/game", port);
            GameService gameService = new GameService();
            gameService.setUser(user);
            BattleshipGameClientEndpoint.setGameService(gameService);
            try (Session ignored = client.connectToServer(BattleshipGameClientEndpoint.class, new URI(args))) {
                latch.await();
            }
        } catch (DeploymentException | URISyntaxException | InterruptedException | IOException e) {
            System.err.println("Failed to connect to the server: " + e.getMessage());
        } finally {
            latch.countDown();
        }
    }
}