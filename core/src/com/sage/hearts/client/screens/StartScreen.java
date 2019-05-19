package com.sage.hearts.client.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.client.game.GameState;

public class StartScreen implements Screen, InputProcessor {
    private HeartsGame game;
    private GameState gameState;

    private Viewport viewport;
    private float viewportScale = 5f;
    private float textProportion = 1f / 7f;

    private Stage stage;
    private Table table;
    private TextButton createGameButton;
    private TextButton joinGameButton;
    private TextButton optionsButton;

    private FreeTypeFontGenerator fontGenerator;
    private FreeTypeFontGenerator.FreeTypeFontParameter buttonFontParameter;

    public StartScreen(HeartsGame game) {
        this.game = game;
        this.gameState = game.getGameState();

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
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Regular.ttf"));

        buttonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        buttonFontParameter.size = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);
        buttonFontParameter.incremental = true;
    }

    private void uiSetup() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var textButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        textButtonStyle.font = fontGenerator.generateFont(buttonFontParameter);

        // Creating UI elements:
        createGameButton = new TextButton("Create game", textButtonStyle);
        createGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showCreateGameScreen();
            }
        });

        joinGameButton = new TextButton("Join game", textButtonStyle);
        joinGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showJoinGameScreen();
            }
        });

        optionsButton = new TextButton("Options", textButtonStyle);
        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showOptionsScreen();
            }
        });

        // Organizing UI elements in table:
        table = new Table().top();
        table.setFillParent(true);

        table.row().padTop(viewport.getWorldHeight() * 0.1f);
        table.add(createGameButton)
                .prefWidth(viewport.getWorldWidth() * 0.3f)
                .prefHeight(viewport.getWorldHeight()* 0.1f);

        table.row().padTop(viewport.getWorldHeight() * 0.25f);
        table.add(joinGameButton)
                .prefWidth(viewport.getWorldWidth() * 0.3f)
                .prefHeight(viewport.getWorldHeight()* 0.1f);

        table.row().padTop(viewport.getWorldHeight() * 0.25f);
        table.add(optionsButton)
                .prefWidth(viewport.getWorldWidth() * 0.3f)
                .prefHeight(viewport.getWorldHeight()* 0.1f);

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
        if(keycode == Input.Keys.P) {
            game.showPlaygroundScreen();
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
