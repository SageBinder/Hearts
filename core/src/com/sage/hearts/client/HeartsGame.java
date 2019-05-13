package com.sage.hearts.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.sage.hearts.client.game.GameState;
import com.sage.hearts.client.screens.*;

public class HeartsGame extends Game {
    public static final Color BACKGROUND_COLOR = new Color(0, 0.2f, 0.11f, 1);

    private GameState gameState;

    private Screen startScreen,
            createGameScreen,
            joinGameScreen,
            optionsScreen,
            lobbyScreen,
            gameScreen;

    @Override
    public void create() {
        gameState = new GameState(this);
        startScreen = new StartScreen(this, gameState);
        createGameScreen = new CreateGameScreen(this, gameState);
        joinGameScreen = new JoinGameScreen(this, gameState);
        optionsScreen = new OptionsScreen(this, gameState);
        lobbyScreen = new LobbyScreen(this, gameState);
        gameScreen = new GameScreen(this, gameState);
        setScreen(startScreen);
    }

    public void showStartScreen() {
        setScreen(startScreen);
    }

    public void showCreateGameScreen() {
        setScreen(createGameScreen);
    }

    public void showJoinGameScreen() {
        setScreen(joinGameScreen);
    }

    public void showOptionsScreen() {
        setScreen(optionsScreen);
    }

    public void showLobbyScreen() {
        setScreen(lobbyScreen);
    }

    public void showGameScreen() {
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
    }
}
