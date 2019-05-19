package com.sage.hearts.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.client.game.GameState;
import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.client.network.ClientConnection;
import com.sage.hearts.client.network.ClientPacket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

public class LobbyScreen implements Screen, InputProcessor {
    private static int MAX_NAME_CHARS = 24;

    private HeartsGame game;
    private GameState gameState;
    private ClientConnection client;

    private Viewport viewport;
    private float textProportion = 1 / 7f;
    private float viewportScale = 5f;

    private Stage stage;
    private Table table;
    private Label gameIPLabel;
    private Label messageLabel;
    private Table playersListTable;
    private TextButton startGameButton;
    private Label gameStateMessageLabel;

    private Label.LabelStyle playerLabelStyle;
    private TextButton.TextButtonStyle changePointsButtonStyle;

    private FreeTypeFontGenerator.FreeTypeFontParameter playerLabelFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter changePointsButtonFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter gameIPLabelFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter messageLabelFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter startGameButtonFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter gameStateMessageLabelFontParameter;

    private FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Regular.ttf"));

    private final Color hostColor = new Color(1f, 1f, 0f, 1f);

    public LobbyScreen(HeartsGame game) {
        this.game = game;
        this.gameState = game.getGameState();
        this.client = game.getClientConnection();

        viewportSetup();
        fontSetup();
        uiSetup();
    }

    private void viewportSetup() {
        float viewportHeight = Gdx.graphics.getHeight() * viewportScale;
        float viewportWidth = Gdx.graphics.getWidth() * viewportScale;
        viewport = new ExtendViewport(viewportWidth, viewportHeight);
        viewport.setWorldSize(viewportWidth, viewportHeight);
    }

    private void fontSetup() {
        int textSize = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Regular.ttf"));

        playerLabelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        playerLabelFontParameter.size = textSize;
        playerLabelFontParameter.incremental = true;

        changePointsButtonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        changePointsButtonFontParameter.size = textSize;
        changePointsButtonFontParameter.incremental = true;

        gameIPLabelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        gameIPLabelFontParameter.size = textSize;
        gameIPLabelFontParameter.incremental = true;

        messageLabelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        messageLabelFontParameter.size = textSize;
        messageLabelFontParameter.incremental = true;

        startGameButtonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        startGameButtonFontParameter.size = textSize;
        startGameButtonFontParameter.incremental = true;

        gameStateMessageLabelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        gameStateMessageLabelFontParameter.size = textSize;
        gameStateMessageLabelFontParameter.incremental = true;
    }

    private void uiSetup() {
        var playerLabelFont = fontGenerator.generateFont(playerLabelFontParameter);
        var changePointsButtonFont = fontGenerator.generateFont(changePointsButtonFontParameter);
        var gameIPLabelFont = fontGenerator.generateFont(gameIPLabelFontParameter);
        var messageLabelFont = fontGenerator.generateFont(messageLabelFontParameter);
        var startGameButtonFont = fontGenerator.generateFont(startGameButtonFontParameter);
        var gameStateMessageLabelFont = fontGenerator.generateFont(gameStateMessageLabelFontParameter);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        playerLabelStyle = skin.get(Label.LabelStyle.class);
        playerLabelStyle.font = playerLabelFont;
        playerLabelStyle.font.getData().markupEnabled = true;

        changePointsButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        changePointsButtonStyle.font = changePointsButtonFont;

        var gameIPLabelStyle = skin.get(Label.LabelStyle.class);
        gameIPLabelStyle.font = gameIPLabelFont;
        gameIPLabelStyle.font.getData().markupEnabled = true;

        var messageLabelStyle = skin.get(Label.LabelStyle.class);
        messageLabelStyle.font = messageLabelFont;
        messageLabelStyle.font.getData().markupEnabled = true;

        var startGameButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        startGameButtonStyle.font = startGameButtonFont;

        var gameStateMessageLabelStyle = skin.get(Label.LabelStyle.class);
        gameStateMessageLabelStyle.font = gameStateMessageLabelFont;
        gameStateMessageLabelStyle.font.getData().markupEnabled = true;

        // Creating UI elements:
        gameIPLabel = new Label("IP Label", gameIPLabelStyle);
        gameIPLabel.setAlignment(Align.center);

        messageLabel = new Label("", messageLabelStyle);
        messageLabel.setAlignment(Align.center);

        playersListTable = new Table();
        playersListTable.align(Align.center);

        startGameButton = new TextButton("", startGameButtonStyle);
        startGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                try {
                    System.out.println(client == null);
                    client.sendPacket(new ClientPacket(ClientCode.START_GAME));
                } catch(IOException e) {
                    messageLabel.setText("Error connecting to server. Maybe you lost connection?");
                    return;
                }
                messageLabel.setText("");
            }
        });

        gameStateMessageLabel = new Label("", messageLabelStyle);
        gameStateMessageLabel.setAlignment(Align.center);

        // Organizing UI elements into main table:
        table = new Table();
        table.setFillParent(true);

        table.row();
        table.add(gameIPLabel).padBottom(viewport.getWorldHeight() * 0.1f);

        table.row();
        table.add(playersListTable).align(Align.center).maxWidth(viewport.getWorldWidth() / 2f);

        table.row().padTop(viewport.getWorldHeight() * 0.1f);
        table.add(startGameButton);

        table.row();
        table.add(gameStateMessageLabel);

        stage = new Stage(viewport);
        stage.addActor(table);
    }

    private void inputProcessorsSetup() {
        var multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        client = game.getClientConnection();
        if(client.serverIP.equals("127.0.0.1")) {
            gameIPLabel.setText("You are hosting, determining your IP...");
            new Thread(() -> {
                try {
                    String thisMachineIP =
                            new BufferedReader(
                                    new InputStreamReader(
                                            new URL("https://api.ipify.org").openStream())).readLine();
                    gameIPLabel.setText("Hosting on [CYAN]" + thisMachineIP + "[]:[ORANGE]" + client.port);
                } catch(IOException e) {
                    gameIPLabel.setText("[YELLOW]Error: could not determine your IP");
                }
            }).start();
        } else {
            gameIPLabel.setText("Connected to [CYAN]" + client.serverIP + "[]:[ORANGE]" + client.port);
        }

        inputProcessorsSetup();
    }

    @Override
    public void render(float delta) {
        HeartsGame.clearScreen();

        if(gameState.update(client)) {
            System.out.println("update");
            System.out.println(gameState.players);
            updateUIFromGameState();
        }

        stage.act(delta);
        stage.draw();
    }

    private void updateUIFromGameState() {
        float groupSpacing = viewport.getWorldWidth() / 12f;

        var pNumHeaderLabel = new Label("P#", playerLabelStyle);
        var pNameHeaderLabel = new Label("NAME", playerLabelStyle);
        var pCallRankHeaderLabel = new Label("POINTS", playerLabelStyle);
        pNameHeaderLabel.setAlignment(Align.center);

        playersListTable.clearChildren();
        playersListTable.setFillParent(false);
        playersListTable.setWidth(viewport.getWorldWidth() / 20f);
        playersListTable.defaults();

        playersListTable.row().padBottom(viewport.getWorldHeight() / 20f);
        playersListTable.add(pNumHeaderLabel).padRight(groupSpacing);
        playersListTable.add(pNameHeaderLabel);
        playersListTable.add(pCallRankHeaderLabel).padLeft(groupSpacing);

        Arrays.stream(gameState.players).filter(Objects::nonNull).forEach(p -> {
            var playerNumLabel = new Label("P" + p.getPlayerNum(), playerLabelStyle);
            var playerNameLabel = new Label(p.getName(), playerLabelStyle);
            var pointsLabel = new Label(p.getAccumulatedPoints() + "", playerLabelStyle);

            var increasePointsButton = new TextButton("+", changePointsButtonStyle);
            var decreasePointsButton = new TextButton("-", changePointsButtonStyle);
            increasePointsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    // TODO Manually changing a player's points
                }
            });
            decreasePointsButton.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {

                }
            });

            if(playerNameLabel.getText().length() > MAX_NAME_CHARS) {
                playerNameLabel.setText(playerNameLabel.getText().substring(0, MAX_NAME_CHARS) + "...");
            }

            playersListTable.row().padBottom(viewport.getWorldHeight() * 0.01f);
            playersListTable.add(playerNumLabel).padRight(groupSpacing);
            playersListTable.add(playerNameLabel);
            playersListTable.add(pointsLabel).padLeft(groupSpacing);
            if(gameState.thisPlayer != null && gameState.thisPlayer.isHost()) {
                playersListTable.add(decreasePointsButton)
                        .padLeft(viewport.getWorldWidth() * 0.05f)
                        .minWidth(viewport.getWorldWidth() * 0.05f);
                playersListTable.add(increasePointsButton)
                        .padLeft(viewport.getWorldWidth() * 0.05f)
                        .minWidth(viewport.getWorldWidth() * 0.05f);
            }

            if(p.isHost()) {
                playerNumLabel.setColor(hostColor);
                playerNameLabel.setColor(hostColor);
                pointsLabel.setColor(hostColor);
            }
            if(gameState.thisPlayer != null && p.getPlayerNum() == gameState.thisPlayer.getPlayerNum()) {
                playerNumLabel.getText().insert(0, "->");
            }
        });

        playersListTable.invalidate();

        if(gameState.thisPlayer != null && gameState.thisPlayer.isHost()) {
            startGameButton.setVisible(true);
            startGameButton.setDisabled(false);
        } else {
            startGameButton.setVisible(false);
            startGameButton.setDisabled(true);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        table.invalidate();
        playersListTable.invalidate();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
