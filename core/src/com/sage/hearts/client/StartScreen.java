package com.sage.hearts.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.renderable.RenderableCard;
import com.sage.hearts.utils.renderable.RenderableCardEntity;
import com.sage.hearts.utils.renderable.RenderableCardList;

class StartScreen implements Screen, InputProcessor {
    private Hearts game;
    private GameState gameState;

    private Viewport viewport;
    private SpriteBatch batch;

    private RenderableHeartsCard test = new RenderableHeartsCard(Rank.QUEEN, Suit.SPADES);
    private RenderableCardList<RenderableHeartsCard> cards = new RenderableCardList<>();

    StartScreen(Hearts game, GameState gameState) {
        this.game = game;
        this.gameState = gameState;

        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();

        test.entity().setOriginToCenter();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(Hearts.BACKGROUND_COLOR.r,
                Hearts.BACKGROUND_COLOR.g,
                Hearts.BACKGROUND_COLOR.b,
                Hearts.BACKGROUND_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

//        test.entity().rotateDeg(delta * 360 / 10);
//        cards.forEach(c -> c.entity().rotateDeg(delta * 720));

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        cards.render(batch, viewport);
        test.render(batch, viewport);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        test.entity()
                .setPosition(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2)
                .setHeight(viewport.getWorldHeight() / 20);
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

    int counter = 1;
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        var worldPos = viewport.unproject(new Vector2(screenX, screenY));
        if(button == Input.Buttons.LEFT) {
            test.setPosition(worldPos.x - (test.getWidth() / 2f), worldPos.y - (test.getHeight() / 2f));

            var toAdd = new RenderableHeartsCard((counter ++) % 54);
            counter %= 54;
            toAdd.entity()
                    .setWidth(viewport.getWorldHeight() / 10)
                    .setPosition(worldPos.x - (toAdd.getWidth() / 2), worldPos.y - (toAdd.getHeight() / 2))
                    .setOriginProportion(0.5f, 0.5f);
            cards.add(toAdd);
        } else if(button == Input.Buttons.RIGHT) {
            for(var i = cards.reverseListIterator(); i.hasPrevious(); ) {
                if(i.previous().entity().displayRectContainsPoint(worldPos)) {
                    i.remove();
                    break;
                }
            }
        } else if(button == Input.Buttons.MIDDLE) {
            for(var i = cards.reverseListIterator(); i.hasPrevious(); ) {
                var ce = i.previous().entity;
                if(ce.displayRectContainsPoint(worldPos)) {
                    ce.toggleSelected();
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
}
