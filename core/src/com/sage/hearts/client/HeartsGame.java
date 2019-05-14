package com.sage.hearts.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.dosse.upnp.UPnP;
import com.sage.hearts.client.game.GameState;
import com.sage.hearts.client.network.ClientConnection;
import com.sage.hearts.client.screens.*;
import com.sage.hearts.server.Server;

public class HeartsGame extends Game {
    public static final Color BACKGROUND_COLOR = new Color(0, 0.2f, 0.11f, 1);

    private GameState gameState;
    private ClientConnection clientConnection = null;
    private Server server = null;

    private Screen startScreen,
            createGameScreen,
            joinGameScreen,
            optionsScreen,
            lobbyScreen,
            gameScreen;

    @Override
    public void create() {
        Gdx.graphics.setTitle("❤︎❤︎❤︎");

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

    public void joinGame(String serverIP, int port, String name) {
        clientConnection = new ClientConnection(serverIP, port, name, this);
    }

    public void startGameServer(int port) {
        if(server != null) {
            server.close();
            if(server.port != port) {
                new Thread(() -> UPnP.closePortTCP(server.port)).start();
            }
        }

        if(!UPnP.isMappedTCP(port)) {
            UPnP.openPortTCP(port); // TODO: Warning message or something if UPnP fails
        }

        this.server = new Server(port);
        server.start();
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    @Override
    public void dispose() {
    }
}
