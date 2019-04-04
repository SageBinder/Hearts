package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface RenderableCard {
    default void render(SpriteBatch batch, Viewport viewport) {
        getEntity().render(batch, viewport);
    }

    default void renderBase(SpriteBatch batch, Viewport viewport) {
        getEntity().renderBase(batch, viewport);
    }

    default void renderAt(SpriteBatch batch, Viewport viewport, float x, float y, float width, float height) {
        getEntity().renderAt(batch, viewport, x, y, width, height);
    }

    RenderableCardEntity getEntity();
}
