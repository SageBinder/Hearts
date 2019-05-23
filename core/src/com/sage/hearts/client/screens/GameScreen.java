package com.sage.hearts.client.screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
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
import com.sage.hearts.client.game.RenderablePlayer;
import com.sage.hearts.client.network.ClientConnection;
import com.sage.hearts.server.network.ServerCode;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;

public class GameScreen implements Screen, InputProcessor {
    private HeartsGame game;
    private GameState gameState;
    private ClientConnection client;

    private SpriteBatch batch = new SpriteBatch();
    private Viewport viewport;
    private float viewportScale = 5f;
    private float textProportion = 1 / 7f;

    private Stage uiStage;
    private Table uiTable;
    private Label messageLabel;
    private TextButton actionButton;

    private FreeTypeFontGenerator fontGenerator;
    private FreeTypeFontGenerator.FreeTypeFontParameter labelFontParameter;
    private FreeTypeFontGenerator.FreeTypeFontParameter textButtonFontParameter;

    private float updateDelay = 0;
    private float delayCounter = 0;

    private boolean renderPlayers = true;

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

        labelFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        labelFontParameter.size = textSize;
        labelFontParameter.incremental = true;

        textButtonFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        textButtonFontParameter.size = textSize;
        textButtonFontParameter.incremental = true;
    }

    private void uiSetup() {
        Skin skin = new Skin(Gdx.files.internal("uiskin.json"));

        var messageLabelStyle = skin.get(Label.LabelStyle.class);
        messageLabelStyle.font = fontGenerator.generateFont(labelFontParameter);
        messageLabelStyle.font.getData().markupEnabled = true;

        var actionButtonStyle = skin.get(TextButton.TextButtonStyle.class);
        actionButtonStyle.font = fontGenerator.generateFont(textButtonFontParameter);

        // Creating UI elements:
        messageLabel = new Label("", messageLabelStyle);
        messageLabel.setAlignment(Align.center);
        messageLabel.setWrap(true);

        actionButton = new TextButton("", actionButtonStyle);
        actionButton.setProgrammaticChangeEvents(true);

        // Organizing UI elements into table:
        uiTable = new Table();
        uiTable.setFillParent(false);

        uiTable.row().padBottom(viewport.getWorldHeight() / 120f);
        uiTable.add(actionButton);

        uiTable.row();
        uiTable.add(messageLabel);

        uiStage = new Stage(viewport, batch);
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
        inputProcessorsSetup();
        updateUIFromGameState();
    }

    @Override
    public void render(float delta) {
        // Update:
        viewport.apply(true);
        batch.setProjectionMatrix(viewport.getCamera().combined);

        delayCounter += delta;
        if(delayCounter >= updateDelay && gameState.update(client)) {
            updateUIFromGameState();
            delayCounter = 0;
        }

        Arrays.stream(gameState.players).filter(Objects::nonNull).forEach(p -> p.setExpanded(false));
        if(Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)) {
            Vector2 mousePos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            outer:
            for(RenderablePlayer p : gameState.players) {
                if(p == null) {
                    continue;
                }

                if((p.getPlay().isPresent() && p.getPlay().get().entity.displayRectContainsPoint(mousePos))) {
                    p.setExpanded(true);
                    break;
                } else {
                    for(var i = p.collectedPointCards.reverseListIterator(); i.hasPrevious(); ) {
                        var entity = i.previous().entity;
                        if(entity.displayRectContainsPoint(mousePos)) {
                            p.setExpanded(true);
                            break outer;
                        }
                    }
                }
            }
        }

        float playersCenterX = viewport.getWorldWidth() * 0.5f;
        float playersCenterY = viewport.getWorldHeight() * 0.66f;
        var topPlayerIdx = (ArrayUtils.indexOf(gameState.players, gameState.thisPlayer) + (gameState.players.length / 2)) % gameState.players.length;
        var topPlayer = gameState.players[Math.max(topPlayerIdx, 0)];

        uiTable.pack();
        float uiTableX = playersCenterX - (uiTable.getWidth() * 0.5f);
        float uiTableY = (!renderPlayers) ? playersCenterY - (uiTable.getHeight() * 0.5f)
                : (topPlayer != null) ? topPlayer.collectedPointCards.pos.y - (uiTable.getHeight())
                : playersCenterY - actionButton.getHeight();
        uiTableY -= uiTable.getHeight() * 0.1f;
        uiTable.setPosition(uiTableX, uiTableY);
        uiStage.act(delta);
        updateCards(delta);

        // Render:
        HeartsGame.clearScreen();

        batch.begin();
        if(renderPlayers) {
            renderPlayers(playersCenterX, playersCenterY,
                    viewport.getWorldWidth() * 0.3f,viewport.getWorldHeight() * 0.22f);
        }
        gameState.thisPlayerHand.render(batch, viewport);
        batch.end();

        uiStage.draw();
    }

    private void renderPlayers(float centerX, float centerY, float widthRadius, float heightRadius) {
        var players = gameState.players;
        float angleIncrement = MathUtils.PI2 / players.length;
        float shift = (ArrayUtils.indexOf(players, gameState.thisPlayer) * angleIncrement) + (MathUtils.PI * 0.5f);

        for(int i = 0; i < players.length; i++) {
            RenderablePlayer toRender = players[i];
            if(toRender == null) {
                continue;
            }

            toRender.setX((MathUtils.cos((i * angleIncrement) - shift) * widthRadius) + centerX);
            toRender.setY((MathUtils.sin((i * angleIncrement) - shift) * heightRadius) + centerY);
            toRender.render(batch, viewport);
        }
    }

    private void updateCards(float delta) {
        gameState.thisPlayerHand.forEach(c -> c.entity.mover.posSpeed = 6);
        gameState.thisPlayerHand.update(delta);
        for(RenderablePlayer p : gameState.players) {
            if(p != null) {
                p.update(delta);
            }
        }
    }

    private void updateUIFromGameState() {
        updateMessageLabelFromUI();
        setActionButtonFromServerCode(gameState.lastServerCode);
    }

    private void updateMessageLabelFromUI() {
        messageLabel.setText(gameState.message);
    }

    private void setActionButtonFromServerCode(ServerCode code) {
        switch(code) {
        case MAKE_PLAY:
        case INVALID_PLAY:
            enableButton(actionButton, () -> {
                gameState.actions.sendPlay(client);
                updateMessageLabelFromUI();
            });
            actionButton.setText("Send play");
            break;

        case SEND_WARHEADS:
        case INVALID_WARHEADS:
            enableButton(actionButton, () -> {
                gameState.actions.sendWarheads(client);
                updateMessageLabelFromUI();
            });
            actionButton.setText("Send cards");
            break;

        case TRICK_END:
            disableButton(actionButton);
            updateDelay = 4;
            break;

        case ROUND_END:
            enableButton(actionButton, () -> game.showLobbyScreen());
            actionButton.setText("Exit to lobby");
            renderPlayers = false;
            break;

        case PLAY_TWO_OF_CLUBS:
        case SUCCESSFUL_PLAY:
            disableButton(actionButton);
            actionButton.setText("");
            updateDelay = 2;
            break;

        case ROUND_START:
            renderPlayers = true;
            // --- FALL THROUGH ---
        case SUCCESSFUL_WARHEADS:
        case TRICK_START:
        case WAIT_FOR_WARHEADS:
        case WAIT_FOR_LEADING_PLAYER:
        case WAIT_FOR_TURN_PLAYER:
        case WAIT_FOR_HAND:
        case WAIT_FOR_NEW_PLAY:
            disableButton(actionButton);
            actionButton.setText("");
            // --- FALL THROUGH ---
        default:
            updateDelay = 0;
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

    private void disableButton(TextButton button) {
        while(button.getListeners().size > 1) {
            button.getListeners().removeIndex(button.getListeners().size - 1);
        }
        button.setDisabled(true);
        button.setVisible(false);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
        switch(keycode) {
        case Input.Keys.ENTER:
            actionButton.toggle();
            // --- FALL THROUGH ---
        case Input.Keys.SPACE:
            updateDelay = 0;
            break;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        var clickPos = viewport.unproject(new Vector2(screenX, screenY));
        if(button == Input.Buttons.LEFT) {
            for(var i = gameState.thisPlayerHand.reverseListIterator(); i.hasPrevious(); ) {
                var entity = i.previous().entity;
                if(entity.displayRectContainsPoint(clickPos)) {
                    entity.toggleSelected();
                    break;
                }
            }
        }
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
