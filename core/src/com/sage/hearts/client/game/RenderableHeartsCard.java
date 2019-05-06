package com.sage.hearts.client.game;

import com.badlogic.gdx.graphics.Color;
import com.sage.hearts.utils.card.InvalidCardException;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.hearts.HeartsCard;
import com.sage.hearts.utils.renderable.RenderableCard;
import com.sage.hearts.utils.renderable.RenderableCardEntity;

public class RenderableHeartsCard extends HeartsCard
        implements RenderableCard<RenderableHeartsCard.RenderableHeartsCardEntity> {
    public final RenderableHeartsCardEntity entity = new RenderableHeartsCardEntity(this);

    public RenderableHeartsCard(Rank rank, Suit suit) throws InvalidCardException {
        super(rank, suit);
    }

    public RenderableHeartsCard(int cardNum) throws InvalidCardException {
        super(cardNum);
    }

    public RenderableHeartsCard(RenderableHeartsCard other) throws InvalidCardException {
        super(other);
    }

    public RenderableHeartsCard() {
        super();
    }

    public RenderableHeartsCardEntity entity() {
        return entity;
    }

    public class RenderableHeartsCardEntity extends RenderableCardEntity<RenderableHeartsCardEntity, RenderableHeartsCard> {
        final Color defaultPointCardFaceBorderColor = new Color(0.75f, 0.75f, 0f, 1f);
        final Color defaultPointCardBackBorderColor = new Color(defaultPointCardFaceBorderColor);

        private RenderableHeartsCardEntity(RenderableHeartsCard other) {
            super(other);
            setBorderColorIfPointCard();
        }

        private void setBorderColorIfPointCard() {
            if(card.getPoints() > 0) {
                setFaceBorderColor(defaultPointCardFaceBorderColor);
                setBackBorderColor(defaultPointCardBackBorderColor);
            } else {
                setFaceBorderColor(defaultFaceBorderColor);
                setBackBorderColor(defaultBackBorderColor);
            }
        }

        @Override
        protected void cardChangedImpl() {
            setBorderColorIfPointCard();
        }
    }
}