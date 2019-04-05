package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;

public interface RenderableCard<T extends RenderableCardEntity<? extends T, ? extends Card>> {
    default void render(SpriteBatch batch, Viewport viewport) {
        getEntity().render(batch, viewport);
    }

    default void renderBase(SpriteBatch batch, Viewport viewport) {
        getEntity().renderBase(batch, viewport);
    }

    default void renderAt(SpriteBatch batch, Viewport viewport, float x, float y, float width, float height) {
        getEntity().renderAt(batch, viewport, x, y, width, height);
    }

    // Select setters:
    default T setSelectable(boolean selectable) {
        return getEntity().setSelectable(selectable);
    }

    default T setSelected(boolean selected) {
        return getEntity().setSelected(selected);
    }

    default T select() {
        return getEntity().select();
    }

    default T deselect() {
        return getEntity().deselect();
    }

    // Position setters:
    default T setPosition(Vector2 pos) {
        return getEntity().setPosition(pos);
    }

    default T setPosition(float x, float y) {
        return getEntity().setPosition(x, y);
    }

    default T setX(float x) {
        return getEntity().setX(x);
    }

    default T setY(float y) {
        return getEntity().setY(y);
    }

    // Select getters:
    default boolean isSelectable() {
        return getEntity().isSelectable();
    }

    default boolean isSelected() {
        return getEntity().isSelected();
    }

    // Position/size getters:
    default Vector2 getPosition() {
        return getEntity().getPosition();
    }

    default float getX() {
        return getEntity().getX();
    }

    default float getY() {
        return getEntity().getY();
    }

    default float getWidth() {
        return getEntity().getWidth();
    }

    default float getHeight() {
        return getEntity().getHeight();
    }

    default Vector2 getDisplayPosition() {
        return getEntity().getDisplayPosition();
    }

    default float getDisplayX() {
        return getEntity().getDisplayX();
    }

    default float getDisplayY() {
        return getEntity().getDisplayY();
    }

    default float getDisplayWidth() {
        return getEntity().getDisplayWidth();
    }

    default float getDisplayHeight() {
        return getEntity().getDisplayHeight();
    }

    T getEntity();
}
