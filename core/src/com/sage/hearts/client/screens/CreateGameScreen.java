package com.sage.hearts.client.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class CreateGameScreen implements Screen, InputProcessor {
    private HeartsGame game;

    private Viewport viewport;
    private float viewportScale = 5f;
    private float textProportion = 1f / 7f;

    private Stage stage;
    private Table table;
    private Label screenHeaderLabel;
    private Label ipLabel;
    private TextField nameField;
    private TextField portField;
    private Label errorLabel;
    private TextButton createGameButton;

    private FreeTypeFontGenerator fontGenerator;
    private FreeTypeFontGenerator.FreeTypeFontParameter labelFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter textFieldFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter textButtonFontParameter;

    public CreateGameScreen(HeartsGame game) {
        this.game = game;

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

        labelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        labelFontParameter.size = textSize;
        labelFontParameter.incremental = true;

        textFieldFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        textFieldFontParameter.size = textSize;
        textFieldFontParameter.incremental = true;

        textButtonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        textButtonFontParameter.size = textSize;
        textButtonFontParameter.incremental = true;
    }

    private void uiSetup() {
        var labelFont = fontGenerator.generateFont(labelFontParameter);
        var textFieldFont = fontGenerator.generateFont(textFieldFontParameter);
        var textButtonFont = fontGenerator.generateFont(textButtonFontParameter);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var labelStyle = skin.get(Label.LabelStyle.class);
        labelStyle.font = labelFont;
        labelStyle.font.getData().markupEnabled = true;

        var textFieldStyle = skin.get(TextField.TextFieldStyle.class);
        textFieldStyle.font = textFieldFont;

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = textButtonFont;

        // Creating UI elements:
        screenHeaderLabel = new Label("Create game", labelStyle);

        ipLabel = new Label("Determining your IP...", labelStyle);
        ipLabel.setAlignment(Align.center);
        new Thread(() -> {
            try {
                String thisMachineIP =
                        new BufferedReader(
                                new InputStreamReader(
                                        new URL("https://api.ipify.org").openStream())).readLine();
                ipLabel.setText("Your IP: [CYAN]" + thisMachineIP);
            } catch(IOException e) {
                ipLabel.setText("[YELLOW]Error: could not determine your IP");
            }
        }).start();

        nameField = new TextField("", textFieldStyle);
        nameField.setMessageText("Name");
        nameField.setMaxLength(Server.MAX_PLAYER_NAME_LENGTH);
        nameField.setDisabled(false);

        portField = new TextField("", textFieldStyle);
        portField.setMessageText("Port");
        portField.setTextFieldFilter(new TextField.TextFieldFilter.DigitsOnlyFilter());
        portField.setColor(Color.ORANGE);
        portField.setDisabled(false);

        errorLabel = new Label("", labelStyle);
        errorLabel.setColor(new Color(1f, 0.2f, 0.2f, 1));

        createGameButton = new TextButton("Start game", textButtonStyle);
        createGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String name = nameField.getText();
                int port;

                if(name.length() == 0) {
                    errorLabel.setText("Please enter a name");
                    return;
                }

                try {
                    port = Integer.parseInt(portField.getText());
                } catch(NumberFormatException e) {
                    errorLabel.setText("Error: Port must be an number");
                    return;
                }

                if(port < 1 || port > 65535) {
                    errorLabel.setText("Error: Port must be between 1 and 65535 inclusive");
                    return;
                } else if(port == 1023) { // 1023 is a reserved port
                    errorLabel.setText("Error: Port 1023 is a reserved port");
                }

                game.startGameServer(port);
                try {
                    game.joinGame("127.0.0.1", port, name);
                } catch(Exception e) {
                    errorLabel.setText("Error: Opened server but could not connect to 127.0.0.1:" + port + ". Closing server.");
                    game.closeGameServer();
                    return;
                }
                game.showLobbyScreen();
            }
        });

        // Organizing UI elements in table:
        table = new Table().top().padTop(viewport.getWorldHeight() * 0.2f);
        table.setFillParent(true);

        table.row().padBottom(viewport.getWorldHeight() / 30f);
        table.add(screenHeaderLabel).align(Align.center);

        table.row();
        table.add(ipLabel).align(Align.center);

        table.row().padTop(viewport.getWorldHeight() / 35f).fillX();
        table.add(nameField).maxWidth(viewport.getWorldWidth() * 0.3f);

        table.row().padTop(viewport.getWorldHeight() / 120f).fillX();
        table.add(portField).maxWidth(viewport.getWorldWidth() * 0.3f);

        table.row().padTop(viewport.getWorldHeight() / 120f).fillX();
        table.add(errorLabel);

        table.row().padTop(viewport.getWorldHeight() * 0.05f).fillX();
        table.add(createGameButton).maxWidth(viewport.getWorldWidth() * 0.3f);

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
        inputProcessorsSetup();
    }

    @Override
    public void render(float delta) {
        HeartsGame.clearScreen();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        table.invalidate();
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
        fontGenerator.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.ESCAPE) {
            game.showStartScreen();
        }
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
