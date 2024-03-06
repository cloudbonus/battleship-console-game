package com.github.cloudbonus.game;

import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameStatistics {
    @Getter
    private static int totalTurns = 0;
    private int playerShots = 0;
    @Getter
    private int opponentShots = 0;
    private static LocalDateTime gameStartTime;
    private static LocalDateTime gameEndTime;

    public static void incrementTotalTurns() {
        totalTurns++;
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

    public String calculateGameDuration() {
        Duration duration = Duration.between(gameStartTime, gameEndTime);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

        String startTime = String.format("Game start time: %s\n", gameStartTime.format(formatter));
        String endTime = String.format("Game end time: %s\n", gameEndTime.format(formatter));
        String elapsedTime = String.format("Total game time: %d hours, %d minutes\n", duration.toHours(), duration.toMinutesPart());

        return startTime + endTime + elapsedTime;
    }

    public static void resetTurns() {
        totalTurns = 0;
    }

    public String calculatePlayerEfficiency() {
        double efficiency = ((double) this.playerShots / totalTurns) * 100;
        return String.format("Efficiency: %d%%\n", Math.round(efficiency));
    }
    public String calculateOpponentEfficiency() {
        double efficiency = ((double) this.opponentShots / totalTurns) * 100;
        return String.format("Efficiency: %d%%\n", Math.round(efficiency));
    }
}
