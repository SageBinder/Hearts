package com.sage.hearts.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Timer;
import com.dosse.upnp.UPnP;
import com.sage.hearts.client.game.GameState;
import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.client.network.ClientConnection;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.client.screens.*;
import com.sage.hearts.server.Server;

import java.io.IOException;

public class HeartsGame extends Game {
    public static final Color BACKGROUND_COLOR = new Color(0, 0.2f, 0.11f, 1);

    private GameState gameState;
    private ClientConnection clientConnection;
    private Server server;
    private boolean successfullyOpenedServerPort = false;

    private Screen startScreen,
            createGameScreen,
            joinGameScreen,
            lobbyScreen,
            gameScreen,
            playgroundScreen;

    private Timer titleTimer;

    @Override
    public void create() {
        Gdx.graphics.setTitle("❤️❤️❤️");
        titleTimer = new Timer();
        titleTimer.scheduleTask(new Timer.Task() {
            private int count = 0;
            private final int numHeartsInTitle = 3;
            @Override
            public void run() {
                count = (count + 1) % numHeartsInTitle;
                StringBuilder title = new StringBuilder();
                for(int i = 0; i < numHeartsInTitle; i++) {
                    title.append(i == count ? "\uD83D\uDC94" : "❤️"); // Unicode for broken heart emoji
                }
                Gdx.graphics.setTitle(title.toString());
            }
        }, 0.5f, 0.5f);

        gameState = new GameState(this);
        startScreen = new StartScreen(this);
        createGameScreen = new CreateGameScreen(this);
        joinGameScreen = new JoinGameScreen(this);
        lobbyScreen = new LobbyScreen(this);
        gameScreen = new GameScreen(this);
        playgroundScreen = new PlaygroundScreen(this);
        setScreen(startScreen);

        Runtime.getRuntime().addShutdownHook(new Thread(this::closeGameServer));
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

    public void showLobbyScreen() {
        setScreen(lobbyScreen);
    }

    public void showGameScreen() {
        setScreen(gameScreen);
    }

    public void showPlaygroundScreen() {
        setScreen(playgroundScreen);
    }

    public void joinGame(String serverIP, int port, String name) {
        clientConnection = new ClientConnection(serverIP, port, name, this);
        clientConnection.start();
        ClientPacket namePacket = new ClientPacket(ClientCode.NAME);
        namePacket.data.put("name", name);
        try {
            clientConnection.sendPacket(namePacket);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void startGameServer(int port) {
        closeGameServer();

        this.server = new Server(port); // If server couldn't be started it will throw an exception
        server.start();
    }

    public void openServerPort() {
        if(server == null) {
            return;
        }

        if(!UPnP.isMappedTCP(server.port)) {
            successfullyOpenedServerPort = UPnP.openPortTCP(server.port);
        } else {
            successfullyOpenedServerPort = true;
        }
    }

    public boolean successfullyOpenedServerPort() {
        return successfullyOpenedServerPort;
    }

    public void closeGameServer() {
        if(server != null) {
            new Thread(() -> UPnP.closePortTCP(server.port)).start();
            server.close();
            server = null;
            successfullyOpenedServerPort = false;
        }
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void startTitleTimer() {
        titleTimer.start();
    }

    public void stopTitleTimer() {
        titleTimer.stop();
    }

    @Override
    public void dispose() {
        startScreen.dispose();
        createGameScreen.dispose();
        joinGameScreen.dispose();
        lobbyScreen.dispose();
        gameScreen.dispose();
        playgroundScreen.dispose();
        closeGameServer();
    }

    public static void clearScreen() {
        Gdx.gl.glClearColor(HeartsGame.BACKGROUND_COLOR.r,
                HeartsGame.BACKGROUND_COLOR.g,
                HeartsGame.BACKGROUND_COLOR.b,
                HeartsGame.BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT
                | GL20.GL_DEPTH_BUFFER_BIT
                | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
    }
}
