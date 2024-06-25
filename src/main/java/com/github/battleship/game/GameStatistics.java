package com.github.battleship.game;

import com.github.battleship.game.util.ConsoleDisplayManager;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
public class GameStatistics {

    private int playerShots = 0;
    private int playerTotalShots = 0;
    private int opponentShots = 0;
    private int opponentTotalShots = 0;

    private static LocalDateTime gameStartTime;
    private static LocalDateTime gameEndTime;

    public void reset() {
        this.playerShots = 0;
        this.playerTotalShots = 0;
        this.opponentShots = 0;
        this.opponentTotalShots = 0;
    }

    public int getTotalTurns() {
        return this.playerTotalShots + this.opponentTotalShots;
    }

    public void incrementPlayerTotalShots() {
        this.playerTotalShots++;
    }

    public void incrementOpponentTotalShots() {
        this.opponentTotalShots++;
    }

    public void incrementPlayerHitShots() {
        this.playerShots++;
    }

    public void incrementOpponentHitShots() {
        this.opponentShots++;
    }

    public static void startGameTime() {
        gameStartTime = LocalDateTime.now();
    }

    public static void endGameTime() {
        gameEndTime = LocalDateTime.now();
    }

    public void getMatchTimes() {
        Duration duration = Duration.between(gameStartTime, gameEndTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        String startTime = gameStartTime.format(formatter);
        String endTime = gameEndTime.format(formatter);
        ConsoleDisplayManager.getMatchTimes(startTime, endTime, duration);
    }

    public long getPlayerHitEfficiency() {
        return Math.round(((double) this.playerShots / this.playerTotalShots) * 100);
    }

    public long getOpponentHitEfficiency() {
        return Math.round(((double) this.opponentShots / this.opponentTotalShots) * 100);
    }
}
