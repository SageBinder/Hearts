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

    // Select setters:
    public RenderableHeartsCardEntity setSelectable(boolean selectable) {
        return entity().setSelectable(selectable);
    }

    public RenderableHeartsCardEntity setSelected(boolean selected) {
        return entity().setSelected(selected);
    }

    public RenderableHeartsCardEntity select() {
        return entity().select();
    }

    public RenderableHeartsCardEntity deselect() {
        return entity().deselect();
    }

    // Select getters:
    public boolean isSelectable() {
        return entity().isSelectable();
    }

    public boolean isSelected() {
        return entity().isSelected();
    }

    public RenderableHeartsCardEntity entity() {
        return entity;
    }

    public class RenderableHeartsCardEntity extends RenderableCardEntity<RenderableHeartsCardEntity, RenderableHeartsCard> {
        // On select
        public final float defaultProportionalYChangeOnSelect = 0.9f; // Proportional to height
        public final float defaultProportionalXChangeOnSelect = 0.05f; // Proportional to width

        public final Color defaultFaceSelectedBackgroundColor = new Color(defaultFaceBackgroundColor).sub(0.5f, 0.5f, 0.5f, 0);
        public final Color defaultBackSelectedBackgroundColor = new Color(defaultBackBackgroundColor);

        private boolean selectable = true;
        private boolean isSelected = false;

        private float proportionalXChangeOnSelect = defaultProportionalXChangeOnSelect;
        private float proportionalYChangeOnSelect = defaultProportionalYChangeOnSelect;

        // On highlight:
        public final float defaultProportionalYChangeOnHighlight = 0.45f;
        public final float defaultProportionalXChangeOnHighlight = 0.05f;

        public final Color defaultFaceHighlightedBackgroundColor = new Color(1.0f, 1.0f, 0.5f, 1.0f);
        public final Color defaultBackHighlightedBackgroundColor = new Color(defaultBackBackgroundColor);

        public final float proportionalYChangeOnHighlight = defaultProportionalYChangeOnHighlight;
        public final float proportionalXChangeOnHighlight = defaultProportionalXChangeOnHighlight;

        private boolean highlightable = false;
        private boolean isHighlighted = false;

        // Other:
        public final Color defaultPointCardFaceBorderColor = new Color(0.75f, 0.75f, 0f, 1f);
        public final Color defaultPointCardBackBorderColor = new Color(defaultPointCardFaceBorderColor);

        private RenderableHeartsCardEntity(RenderableHeartsCard other) {
            super(other);
            setBorderColorIfPointCard();
        }

        @Override
        protected void cardChangedImpl() {
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

        // Position change on select methods:
        public RenderableHeartsCardEntity setProportionalYChangeOnSelect(float proportionalYChangeOnSelect) {
            this.proportionalYChangeOnSelect = proportionalYChangeOnSelect;
            if(isSelected) {
                mover.setTargetDisplayProportionalYOffset(proportionalYChangeOnSelect);
            }
            return this;
        }
        public RenderableHeartsCardEntity setAbsoluteYChangeOnSelect(float absoluteYChangeOnSelect) {
            setProportionalYChangeOnSelect(absoluteYChangeOnSelect / getHeight());
            return this;
        }

        public RenderableHeartsCardEntity setProportionalXChangeOnSelect(float proportionalXChangeOnSelect) {
            this.proportionalXChangeOnSelect = proportionalXChangeOnSelect;
            if(isSelected) {
                mover.setTargetDisplayProportionalXOffset(proportionalXChangeOnSelect);
            }
            return this;
        }

        public RenderableHeartsCardEntity setAbsoluteXChangeOnSelect(float absoluteXChangeOnSelect) {
            setProportionalXChangeOnSelect(absoluteXChangeOnSelect / getWidth());
            return this;
        }


        // Selected setters:
        public RenderableHeartsCardEntity select() {
            setSelected(true);
            return this;
        }
        public RenderableHeartsCardEntity deselect() {
            setSelected(false);
            return this;
        }

        public RenderableHeartsCardEntity toggleSelected() {
            return setSelected(!isSelected);
        }

        public RenderableHeartsCardEntity setSelected(boolean selected) {
            if(isSelected == selected || !selectable) {
                return this;
            } else {
                isSelected = selected;
                if(isSelected) {
                    setFaceBackgroundColor(defaultFaceSelectedBackgroundColor);
                    setBackBackgroundColor(defaultBackSelectedBackgroundColor);
                    mover.setTargetDisplayProportionalXOffset(proportionalXChangeOnSelect);
                    mover.setTargetDisplayProportionalYOffset(proportionalYChangeOnSelect);
                } else {
                    setFaceBackgroundColor(defaultFaceBackgroundColor);
                    setBackBackgroundColor(defaultBackBackgroundColor);
                    mover.setTargetDisplayProportionalXOffset(0);
                    mover.setTargetDisplayProportionalYOffset(0);
                }
                return this;
            }
        }

        public RenderableHeartsCardEntity setSelectable(boolean selectable) {
            this.selectable = selectable;
            return this;
        }

        // Selected getters:
        public boolean isSelected() {
            return isSelected;
        }
        public boolean isSelectable() {
            return selectable;
        }

        // Position change on highlight methods:
        public float getProportionalYChangeOnHighlight() {
            return proportionalYChangeOnHighlight;
        }

        public float getProportionalXChangeOnHighlight() {
            return proportionalXChangeOnHighlight;
        }

        // Highlight setters:
        public RenderableHeartsCardEntity setHighlightable(boolean highlightable) {
            this.highlightable = highlightable;
            return this;
        }

        public RenderableHeartsCardEntity highlight() {
            return setHighlighted(true);
        }

        public RenderableHeartsCardEntity toggleHighlighted() {
            return setHighlighted(!isHighlighted);
        }

        public RenderableHeartsCardEntity setHighlighted(boolean highlighted) {
            if(isHighlighted == highlighted || !highlightable || isSelected) {
                return this;
            } else {
                isHighlighted = highlighted;
                if(isHighlighted) {
                    setFaceBackgroundColor(defaultFaceHighlightedBackgroundColor);
                    setBackBackgroundColor(defaultBackHighlightedBackgroundColor);
                    mover.setTargetDisplayProportionalXOffset(proportionalXChangeOnHighlight);
                    mover.setTargetDisplayProportionalYOffset(proportionalYChangeOnHighlight);
                } else {
                    setFaceBackgroundColor(defaultFaceBackgroundColor);
                    setBackBackgroundColor(defaultBackBackgroundColor);
                    mover.setTargetDisplayProportionalXOffset(0);
                    mover.setTargetDisplayProportionalYOffset(0);
                }
                return this;
            }
        }

        // Highlight getters:
        public boolean isHighlightable() {
            return highlightable;
        }

        public boolean isHighlighted() {
            return isHighlighted;
        }
    }
}
