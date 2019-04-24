package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;
import com.sage.hearts.utils.card.CardList;

public class RenderableCardList<T extends Card & RenderableCard> extends CardList<T> {
    public RenderableCardList() {
        super();
    }

    public RenderableCardList(CardList<T> other) {
        super(other);
    }

    public void render(SpriteBatch batch, Viewport viewport) {
        render(batch, viewport, false);
    }

    public void render(SpriteBatch batch, Viewport viewport, boolean renderBase) {
        forEach(c -> {
            c.render(batch, viewport);
            if(renderBase && !c.entity().displayRectEqualsBaseRect()) {
                c.renderBase(batch, viewport);
            }
        });
    }
}
