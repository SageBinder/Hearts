package com.sage.hearts.client.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.client.game.GameState;
import com.sage.hearts.client.game.RenderableHeartsCard;
import com.sage.hearts.utils.card.InvalidCardException;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.renderable.RenderableCardEntity;
import com.sage.hearts.utils.renderable.RenderableCardGroup;

public class PlaygroundScreen implements Screen, InputProcessor {
    private HeartsGame game;
    private GameState gameState;

    private Viewport viewport;
    private SpriteBatch batch;

    private RenderableHeartsCard test = new RenderableHeartsCard(Rank.QUEEN, Suit.SPADES);
    private RenderableCardGroup<RenderableHeartsCard> cards = new RenderableCardGroup<>();

    public PlaygroundScreen(HeartsGame game) {
        this.game = game;
        this.gameState = game.getGameState();
        viewport = new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        test.entity().setOriginToCenter().setSelectable(true);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        HeartsGame.clearScreen();

        test.entity().rotateDeg(delta * 360 / 10);
        test.update(delta);
        cards.update(delta);

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
        cards.regionWidth = Math.min(viewport.getWorldHeight(), viewport.getWorldWidth()) / 2;
        cards.cardHeight = viewport.getWorldHeight() / 15;
        cards.pos.x = (viewport.getWorldWidth() - cards.regionWidth) / 2;
        cards.pos.y = (viewport.getWorldHeight() - cards.cardHeight) / 2;
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
        if(keycode == Input.Keys.ESCAPE) {
            cards.disposeAll();
            cards.clear();
            game.showStartScreen();
        }
        return false;
    }

    private int[] keys = new int[2];

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.ENTER) {
            var mousePos = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            for(var c : cards) {
                if(c.entity.displayRectContainsPoint(mousePos)) {
                    try {
                        c.setCardNum(Integer.parseInt(keys[0] + "" + keys[1]));
                    } catch(InvalidCardException | NumberFormatException ignored) {
                    }
                    break;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        if(character == 'd') {
            cards.disposeAll();
            cards.clear();
        } else if(Character.isDigit(character)) {
            keys[0] = keys[1];
            keys[1] = Integer.parseInt(character + "");
        }
        return false;
    }

    private int counter = 1;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        var worldPos = viewport.unproject(new Vector2(screenX, screenY));
        if(button == Input.Buttons.LEFT) {
            test.setPosition(worldPos.x - (test.getWidth() / 2f), worldPos.y - (test.getHeight() / 2f));
            test.entity.mover.stop();

            counter = (counter + 1) % 54;
            var toAdd = new RenderableHeartsCard(counter);
            toAdd.entity()
                    .setWidth(viewport.getWorldWidth() / 10)
                    .setPosition(worldPos.x - (toAdd.getWidth() / 2), worldPos.y - (toAdd.getHeight() / 2))
                    .setOriginProportion(0.5f, 0.5f);
            cards.add(toAdd);
        } else if(button == Input.Buttons.RIGHT) {
            for(var i = cards.reverseListIterator(); i.hasPrevious(); ) {
                RenderableCardEntity c;
                if((c = i.previous().entity()).displayRectContainsPoint(worldPos)) {
                    c.dispose();
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
            if(test.entity.displayRectContainsPoint(worldPos)) {
                test.entity.toggleSelected();
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        var worldPos = viewport.unproject(new Vector2(screenX, screenY));
        if(button == Input.Buttons.LEFT) {
            test.entity().mover.setTargetXY(worldPos.x - (test.getWidth() / 2f), worldPos.y - (test.getHeight() / 2f));
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        var worldPos = viewport.unproject(new Vector2(screenX, screenY));
        cards.forEach(c -> c.entity.deselect());
        for(var i = cards.reverseListIterator(); i.hasPrevious(); ) {
            var entity = i.previous().entity();
            if(entity.displayRectContainsPoint(worldPos) || entity.baseRectContainsPoint(worldPos)) {
                entity.setSelected(true);
                break;
            }
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        for(int i = 0; i < Math.abs(amount); i++) {
            counter = (counter + 1) % 54;
            cards.add(new RenderableHeartsCard(counter).entity()
                    .setHeight(viewport.getScreenHeight() / 20)
                    .setPosition(viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY())))
                    .setOriginProportion(0.5f, 0.5f)
                    .card);
        }
        return false;
    }

}
