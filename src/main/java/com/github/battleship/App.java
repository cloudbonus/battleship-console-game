package com.github.battleship;

import com.github.battleship.game.sm.StateMachine;
import com.github.battleship.game.sm.state.State;
import com.github.battleship.game.sm.state.impl.GameModeSelectionState;
import com.github.battleship.game.sm.state.impl.GameOverState;
import com.github.battleship.game.sm.state.impl.PrepareGameState;
import com.github.battleship.game.sm.state.impl.StartMultiplayerModeState;
import com.github.battleship.game.sm.state.impl.StartSingleplayerModeState;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.github.battleship")
public class App {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(App.class);
        StateMachine stateMachine = context.getBean(StateMachine.class);

        State prepareGameState = context.getBean(PrepareGameState.class);
        State gameModeSelectionState = context.getBean(GameModeSelectionState.class);
        State startMultiplayerModeState = context.getBean(StartMultiplayerModeState.class);
        State startSingleplayerModeState = context.getBean(StartSingleplayerModeState.class);
        State gameOverState = context.getBean(GameOverState.class);

        stateMachine.addState(prepareGameState);
        stateMachine.addState(gameModeSelectionState);
        stateMachine.addState(startMultiplayerModeState);
        stateMachine.addState(startSingleplayerModeState);
        stateMachine.addState(gameOverState);

        stateMachine.changeState(PrepareGameState.class);
    }
}