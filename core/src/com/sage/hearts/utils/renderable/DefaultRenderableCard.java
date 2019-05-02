package com.sage.hearts.utils.renderable;

import com.sage.hearts.utils.card.Card;
import com.sage.hearts.utils.card.InvalidCardException;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;

public class DefaultRenderableCard extends Card
        implements RenderableCard {
    public final RenderableCardEntity<RenderableCardEntity, DefaultRenderableCard> entity
            = new RenderableCardEntity<>(this);

    public DefaultRenderableCard(Rank rank, Suit suit) throws InvalidCardException {
        super(rank, suit);
    }

    public DefaultRenderableCard(int cardNum) throws InvalidCardException {
        super(cardNum);
    }

    public DefaultRenderableCard(Card other) {
        super(other);
    }

    public DefaultRenderableCard() {
        super();
    }

    @Override
    public RenderableCardEntity entity() {
        return entity;
    }
}
