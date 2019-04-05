package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;

import java.util.Collection;

public class RenderableHand<T extends Card & RenderableCard> extends RenderableCardGroup<T> {
    public float bottomPaddingProportion = 0.025f,
            leftPaddingProportion = 0.05f,
            rightPaddingProportion = 0.05f,
            cardHeightProportion = 1f / 7f;

    public RenderableHand() {
        super();
        super.cardHeight = Gdx.graphics.getHeight() * cardHeightProportion;
    }

    public RenderableHand(RenderableCardList<T> cards) {
        super(cards);
        super.cardHeight = Gdx.graphics.getHeight() * cardHeightProportion;
    }

    @Override
    public void render(SpriteBatch batch, Viewport viewport) {
        this.render(batch, viewport, false);
    }

    @Override
    public void render(SpriteBatch batch, Viewport viewport, boolean renderBase) {
        super.cardHeight = viewport.getWorldHeight() * cardHeightProportion;
        super.regionWidth = viewport.getWorldWidth()
                - (viewport.getWorldWidth() * leftPaddingProportion)
                - (viewport.getWorldWidth() * rightPaddingProportion);
        super.pos.x = viewport.getWorldWidth() * leftPaddingProportion;
        super.pos.y = viewport.getWorldHeight() * bottomPaddingProportion;

        super.render(batch, viewport, renderBase);
    }

    public boolean add(T c) {
        c.getEntity().setSelectable(true);
        c.getEntity().setFlippable(true);
        return super.add(c);
    }

    public void add(int index, T c) {
        c.getEntity().setSelectable(true);
        c.getEntity().setFlippable(true);
        super.add(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        c.forEach(card -> {
            card.getEntity().setSelectable(true);
            card.getEntity().setFlippable(true);
        });
        return super.addAll(index, c);
    }
}
