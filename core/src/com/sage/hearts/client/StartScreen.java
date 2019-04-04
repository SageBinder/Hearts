package com.sage.hearts.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;

class StartScreen implements Screen {
    private Hearts game;
    private GameState gameState;

    private Viewport viewport;
    private SpriteBatch batch;

    private RenderableHeartsCard test = new RenderableHeartsCard(Rank.QUEEN, Suit.SPADES);

    StartScreen(Hearts game, GameState gameState) {
        this.game = game;
        this.gameState = gameState;

        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        test.render(batch, viewport);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        test.getEntity()
                .setPosition(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2)
                .setHeight(viewport.getWorldHeight() / 3);
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
}
