package com.sage.hearts.client;

import com.badlogic.gdx.graphics.Color;
import com.sage.hearts.utils.renderable.RenderableCard;
import com.sage.hearts.utils.renderable.RenderableCardEntity;
import com.sage.hearts.utils.card.HeartsCard;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;

class RenderableHeartsCard extends HeartsCard implements RenderableCard {
    private final RenderableHeartsCardEntity entity = new RenderableHeartsCardEntity(this);

    RenderableHeartsCard(Rank rank, Suit suit) {
        super(rank, suit);
    }

    RenderableHeartsCard(int cardNum) {
        super(cardNum);
    }

    RenderableHeartsCard(RenderableHeartsCard other) {
        super(other);
    }

    void setSelected(boolean selected) {
        entity.setSelected(selected);
    }

    void select() {
        entity.select();
    }

    void deselect() {
        entity.deselect();
    }

    public RenderableCardEntity<RenderableHeartsCardEntity, HeartsCard> getEntity() {
        return entity;
    }

    class RenderableHeartsCardEntity extends RenderableCardEntity<RenderableHeartsCardEntity, HeartsCard> {
        final Color defaultPointCardFaceBorderColor = new Color(0.75f, 0.75f, 0f, 1f);
        final Color defaultPointCardBackBorderColor = new Color(defaultPointCardFaceBorderColor);

        private RenderableHeartsCardEntity(HeartsCard other) {
            super(other);
            if(RenderableHeartsCard.this.pointValue > 0) {
                setFaceBorderColor(defaultPointCardFaceBorderColor);
                setBackBorderColor(defaultPointCardBackBorderColor);
            }
        }
    }
}
