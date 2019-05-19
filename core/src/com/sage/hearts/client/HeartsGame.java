package com.sage.hearts.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
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

    private Screen startScreen,
            createGameScreen,
            joinGameScreen,
            optionsScreen,
            lobbyScreen,
            gameScreen,
            playgroundScreen;

    @Override
    public void create() {
        Gdx.graphics.setTitle("❤️❤️❤️");
        Timer titleTimer = new Timer();
        titleTimer.scheduleTask(new Task() {
            private int count = 0;
            private final int numHeartsInTitle = 3;
            @Override
            public void run() {
                count = (count + 1) % numHeartsInTitle;
                StringBuilder title = new StringBuilder();
                for(int i = 0; i < numHeartsInTitle; i++) {
                    title.append(i == count ? "\uD83D\uDC94" : "❤️");
                }
                Gdx.graphics.setTitle(title.toString());
            }
        }, 0.5f, 0.5f);

        gameState = new GameState(this);
        startScreen = new StartScreen(this);
        createGameScreen = new CreateGameScreen(this);
        joinGameScreen = new JoinGameScreen(this);
        optionsScreen = new OptionsScreen(this);
        lobbyScreen = new LobbyScreen(this);
        gameScreen = new GameScreen(this);
        playgroundScreen = new PlaygroundScreen(this);
        System.out.println("before setScreen");
        setScreen(startScreen);
        System.out.println("ok bye");
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

    public void showPlaygroundScreen() {
        setScreen(playgroundScreen);
    }

    public void joinGame(String serverIP, int port, String name) {
        System.out.println("join gmae");
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

    public void closeGameServer() {
        if(server != null) {
            server.close();
        }
    }

    public ClientConnection getClientConnection() {
        return clientConnection;
    }

    public GameState getGameState() {
        return gameState;
    }

    @Override
    public void dispose() {
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
