package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.math.MathUtils;

public abstract class RenderableCardMover {
    public final RenderableCard card;
    public float posSpeed = 1;
    public float rotSpeed = MathUtils.PI2;
    public float displayPosOffsetSpeed = 1;
    public float displayRotOffsetSpeed = 1;
    public float displayProportionSpeed = 1;
    public float onSelectProportionalChangeSpeed = 10;

    protected Target target;

    RenderableCardMover(RenderableCard card) {
        this.card = card;
        target = new Target();
    }

    public abstract void update(float delta);

    public void stop() {
        target.targetX = null;
        target.targetY = null;
        target.targetRotation = null;

        target.targetDisplayXOffset = null;
        target.targetDisplayYOffset = null;
        target.targetDisplayRotationOffset = null;
        target.targetDisplayProportion = null;

        target.targetDisplayProportionalXOffset = null;
        target.targetDisplayProportionalYOffset = null;
    }

    public RenderableCardMover setTargetX(float targetX) {
        target.targetX = targetX;
        return this;
    }

    public RenderableCardMover setTargetY(float targetY) {
        target.targetY = targetY;
        return this;
    }

    public RenderableCardMover setTargetXY(float x, float y) {
        setTargetX(x);
        setTargetY(y);
        return this;
    }

    public RenderableCardMover setTargetRotation(float targetRotation) {
        target.targetRotation = targetRotation;
        return this;
    }

    public RenderableCardMover setTargetDisplayXOffset(float targetDisplayXOffset) {
        target.targetDisplayXOffset = targetDisplayXOffset;
        return this;
    }

    public RenderableCardMover setTargetDisplayYOffset(float targetDisplayYOffset) {
        target.targetDisplayYOffset = targetDisplayYOffset;
        return this;
    }

    public RenderableCardMover setTargetDisplayXYOffset(float targetDisplayXOffset, float targetDisplayYOffset) {
        setTargetDisplayXOffset(targetDisplayXOffset);
        setTargetDisplayYOffset(targetDisplayYOffset);
        return this;
    }

    public RenderableCardMover setTargetDisplayRotationOffset(float targetDisplayRotationOffset) {
        target.targetDisplayRotationOffset = targetDisplayRotationOffset;
        return this;
    }

    public RenderableCardMover setTargetDisplayProportion(float targetDisplayProportion) {
        target.targetDisplayProportion = targetDisplayProportion;
        return this;
    }

    public RenderableCardMover setTargetDisplayProportionalXOffset(float targetDisplayProportionalXOffset) {
        target.targetDisplayProportionalXOffset = targetDisplayProportionalXOffset;
        return this;
    }

    public RenderableCardMover setTargetDisplayProportionalYOffset(float targetDisplayProportionalYOffset) {
        target.targetDisplayProportionalYOffset = targetDisplayProportionalYOffset;
        return this;
    }

    public static RenderableCardMover scaledDistanceMover(RenderableCard c) {
        return new RenderableCardMover(c) {
            @Override
            public void update(float delta) {
                if(target == null) {
                    return;
                }

                float deltaX = 0;
                float deltaY = 0;
                float deltaRot = 0;
                float deltaProportionalXChangeOnSelect = 0;
                float deltaProportionalYChangeOnSelect = 0;
                if(target.targetX != null) {
                    deltaX = target.targetX - c.getX();
                }
                if(target.targetY != null) {
                    deltaY = target.targetY - c.getY();
                }
                if(target.targetRotation != null) {
                    deltaRot = target.targetRotation - c.entity().getRotation();
                }
                if(target.targetDisplayProportionalXOffset != null) {
                    deltaProportionalXChangeOnSelect = target.targetDisplayProportionalXOffset - c.entity().getDisplayProportionalXOffset();
                }
                if(target.targetDisplayProportionalYOffset != null) {
                    deltaProportionalYChangeOnSelect = target.targetDisplayProportionalYOffset - c.entity().getDisplayProportionalYOffset();
                }

                float xChange = Math.signum(deltaX) * Math.min(Math.abs(deltaX * delta * posSpeed), Math.abs(deltaX));
                float yChange = Math.signum(deltaY) * Math.min(Math.abs(deltaY * delta * posSpeed), Math.abs(deltaY));

                card.setX(card.getX() + xChange);
                card.setY(card.getY() + yChange);

                float rotChange = Math.signum(deltaRot) * Math.min(Math.abs(delta * rotSpeed), Math.abs(deltaRot));
                card.entity().rotateRad(rotChange);

                float proportionalXChangeOnSelectChange =
                        Math.signum(deltaProportionalXChangeOnSelect) * Math.min(
                                Math.abs(deltaProportionalXChangeOnSelect * delta * onSelectProportionalChangeSpeed),
                                Math.abs(deltaProportionalXChangeOnSelect
                                ));
                float proportionalYChangeOnSelectChange =
                        Math.signum(deltaProportionalYChangeOnSelect) * Math.min(
                                Math.abs(deltaProportionalYChangeOnSelect * delta * onSelectProportionalChangeSpeed),
                                Math.abs(deltaProportionalYChangeOnSelect
                                ));
                card.entity().setDisplayProportionalXOffset(card.entity().getDisplayProportionalXOffset() + proportionalXChangeOnSelectChange);
                card.entity().setDisplayProportionalYOffset(card.entity().getDisplayProportionalYOffset() + proportionalYChangeOnSelectChange);
            }
        };
    }

    public class Target {
        public Float targetX = null;
        public Float targetY = null;
        public Float targetRotation = null;

        public Float targetDisplayXOffset = null;
        public Float targetDisplayYOffset = null;
        public Float targetDisplayRotationOffset = null;
        public Float targetDisplayProportion = null;

        public Float targetDisplayProportionalXOffset = null;
        public Float targetDisplayProportionalYOffset = null;
    }
}
