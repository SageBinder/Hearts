package com.sage.hearts.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.client.game.GameState;
import com.sage.hearts.client.network.ClientConnection;
import com.sage.hearts.server.network.ServerCode;

public class GameScreen implements Screen, InputProcessor {
    private HeartsGame game;
    private GameState gameState;
    private ClientConnection client;

    private SpriteBatch batch;
    private Viewport viewport;
    private float viewportScale = 5f;
    private float textProportion = 1 / 7f;

    private Stage uiStage;
    private Table uiTable;
    private Label messageLabel;
    private TextButton actionButton;

    private FreeTypeFontGenerator fontGenerator;
    private FreeTypeFontGenerator.FreeTypeFontParameter messageLabelFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter actionButtonFontParameter;

    public GameScreen(HeartsGame game) {
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
        int textSize = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * textProportion);

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Regular.ttf"));

        messageLabelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        messageLabelFontParameter.size = textSize;
        messageLabelFontParameter.incremental = true;

        actionButtonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        actionButtonFontParameter.size = textSize;
        actionButtonFontParameter.incremental = true;
    }

    private void uiSetup() {
        var messageLabelFont = fontGenerator.generateFont(messageLabelFontParameter);
        var actionButtonFont = fontGenerator.generateFont(actionButtonFontParameter);

        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var messageLabelStyle = skin.get(Label.LabelStyle.class);
        messageLabelStyle.font = messageLabelFont;
        messageLabelStyle.font.getData().markupEnabled = true;

        var actionButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        actionButtonStyle.font = messageLabelFont;

        // Creating UI elements:
        messageLabel = new Label("", messageLabelStyle);

        actionButton = new TextButton("", actionButtonStyle);

        // Organizing UI elements into table:
        uiTable = new Table();
        uiTable.setFillParent(true);

        uiTable.row().padBottom(viewport.getWorldHeight() / 120f);
        uiTable.add(actionButton);

        uiTable.row();
        uiTable.add(messageLabel);

        uiStage = new Stage(viewport);
        uiStage.addActor(uiTable);
    }

    private void inputProcessorsSetup() {
        var multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(uiStage);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void show() {
        client = game.getClientConnection();
        disableButton(actionButton);
        inputProcessorsSetup();
    }

    @Override
    public void render(float delta) {
        HeartsGame.clearScreen();

        if(gameState.update(client)) {
            messageLabel.setText(gameState.message);
            setActionButtonFromServerCode(gameState.lastServerCode);
        }
    }

    private void setActionButtonFromServerCode(ServerCode code) {
        switch(code) {
        case MAKE_PLAY:
        case INVALID_PLAY:
            enableButton(actionButton, () -> gameState.actions.sendPlay(client));
            break;

        case SEND_WARHEADS:
        case INVALID_WARHEADS:
            enableButton(actionButton, () -> gameState.actions.sendWarheads(client));
            break;

        default:
            disableButton(actionButton);
        }
    }

    private static void enableButton(TextButton button, ButtonAction action) {
        while(button.getListeners().size > 1) {
            button.getListeners().removeIndex(button.getListeners().size - 1);
        }
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.onAction();
            }
        });
        button.setDisabled(false);
        button.setVisible(true);
    }

    private static void disableButton(TextButton button) {
        while(button.getListeners().size > 1) {
            button.getListeners().removeIndex(button.getListeners().size - 1);
        }
        button.setDisabled(true);
        button.setVisible(false);
    }

    @Override
    public void resize(int width, int height) {

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

    private interface ButtonAction {
        void onAction();
    }
}
