package com.github.cloudbonus.client;

import com.github.cloudbonus.board.cell.CellType;
import com.github.cloudbonus.game.BattleController;
import com.github.cloudbonus.game.GameStatistics;
import com.github.cloudbonus.util.ConsoleDisplayManager;
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
    private static BattleController playerBattleController;
    private static CountDownLatch latch;

    @OnOpen
    public void onOpen(Session session) {
        try {
            System.out.println("---connecting to the server");
            String message = String.format("NAME_%s", playerBattleController.getUserName());
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        if (message.length() < 100) {
            if (message.startsWith("NAME")) {
                GameStatistics.startGameTime();
                playerBattleController.setOpponentName(message.substring(5));
            }

            if (message.startsWith("LOST")) {
                handleLostMessage(message, session);
            } else if (message.matches("^[A-P][1-9]$|^[A-P]1[0-6]$")) {
                handleAttackMessage(message, session);
            } else if (CellType.isCellState(message) || message.startsWith("SUNK")) {
                handleCellStateMessage(message, session);
            } else {
                handleDefaultMessage(session);
            }
        } else {
            ConsoleDisplayManager.clearConsole();
            System.out.println(message);
        }
    }

    private void handleLostMessage(String message, Session session) {
        GameStatistics.endGameTime();
        String reason = playerBattleController.processCellState(message);
        playerBattleController.printMatchResult();
        playerBattleController.finishMatch();
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, reason));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleAttackMessage(String message, Session session) throws IOException {
        String response = playerBattleController.processAttack(message);
        session.getBasicRemote().sendText(response);
    }

    private void handleCellStateMessage(String message, Session session) throws IOException {
        String response = playerBattleController.processCellState(message);
        if ("END_TURN".equals(response)) {
            session.getBasicRemote().sendText("END_TURN");
        } else {
            session.getBasicRemote().sendText(response);
        }
    }

    private void handleDefaultMessage(Session session) throws IOException {
        String position = playerBattleController.attack();
        session.getBasicRemote().sendText(position);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        boolean hasLost = playerBattleController.hasLostMatch();
        if (hasLost) {
            GameStatistics.endGameTime();
            playerBattleController.printMatchResult();
            playerBattleController.finishMatch();
        }
        GameStatistics.endGameTime();
        ConsoleDisplayManager.printSessionClosure(session.getId(), closeReason.getReasonPhrase());
        latch.countDown();
    }

    public static void startClient(int port, BattleController battleController) {
        latch = new CountDownLatch(1);
        ClientManager client = ClientManager.createClient();
        try {
            String args = String.format("ws://localhost:%s/websockets/game", port);
            BattleshipGameClientEndpoint.setPlayerBattleController(battleController);
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