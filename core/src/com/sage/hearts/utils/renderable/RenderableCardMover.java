package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.math.MathUtils;

public abstract class RenderableCardMover {
    public final RenderableCard card;
    public float posSpeed = 1;
    public float rotSpeed = MathUtils.PI2;
    public float displayPosOffsetSpeed = 1;
    public float displayRotOffsetSpeed = 1;
    public float displayProportionSpeed = 1;
    public float displayProportionalOffsetSpeed = 10;

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

                RenderableCardEntity entity = card.entity();

                float deltaX;
                float deltaY;
                float deltaRot;

                float deltaDisplayXOffset;
                float deltaDisplayYOffset;
                float deltaDisplayRotationOffset;
                float deltaDisplayProportion;

                float deltaProportionalXOffset;
                float deltaProportionalYOffset;

                if(target.targetX != null) {
                    deltaX = target.targetX - entity.getX();
                    float xChange = Math.signum(deltaX) * Math.min(
                            Math.abs(deltaX * delta * posSpeed),
                            Math.abs(deltaX));
                    entity.setX(entity.getX() + xChange);
                }
                if(target.targetY != null) {
                    deltaY = target.targetY - entity.getY();
                    float yChange = Math.signum(deltaY) * Math.min(
                            Math.abs(deltaY * delta * posSpeed),
                            Math.abs(deltaY));
                    entity.setY(entity.getY() + yChange);
                }
                if(target.targetRotation != null) {
                    deltaRot = target.targetRotation - entity.getRotationRad();
                    float rotChange = Math.signum(deltaRot) * Math.min(
                            Math.abs(deltaRot * delta * rotSpeed),
                            Math.abs(deltaRot));
                    entity.rotateRad(rotChange);
                }
                if(target.targetDisplayXOffset != null) {
                    deltaDisplayXOffset = target.targetDisplayXOffset - entity.getDisplayXOffset();
                    float displayXOffsetChange = Math.signum(deltaDisplayXOffset) * Math.min(
                            Math.abs(deltaDisplayXOffset * delta * displayPosOffsetSpeed),
                            Math.abs(deltaDisplayXOffset));
                    entity.setDisplayXOffset(entity.getDisplayXOffset() + displayXOffsetChange);
                }
                if(target.targetDisplayYOffset != null) {
                    deltaDisplayYOffset = target.targetDisplayYOffset - entity.getDisplayYOffset();
                    float displayYOffsetChange = Math.signum(deltaDisplayYOffset) * Math.min(
                            Math.abs(deltaDisplayYOffset * delta * displayPosOffsetSpeed),
                            Math.abs(deltaDisplayYOffset));
                    entity.setDisplayYOffset(entity.getDisplayYOffset() + displayYOffsetChange);
                }
                if(target.targetDisplayRotationOffset != null) {
                    deltaDisplayRotationOffset = target.targetDisplayRotationOffset - entity.getDisplayRotationOffsetRad();
                    float displayRotationOffsetChange = Math.signum(deltaDisplayRotationOffset) * Math.min(
                            Math.abs(deltaDisplayRotationOffset * delta * displayRotOffsetSpeed),
                            Math.abs(deltaDisplayRotationOffset));
                    entity.setDisplayRotationOffsetRad(entity.getDisplayRotationOffsetRad() + displayRotationOffsetChange);
                }
                if(target.targetDisplayProportion != null) {
                    deltaDisplayProportion = target.targetDisplayProportion - entity.getDisplayProportion();
                    float displayProportionChange = Math.signum(deltaDisplayProportion) * Math.min(
                            Math.abs(deltaDisplayProportion * delta * displayProportionSpeed),
                            Math.abs(deltaDisplayProportion));
                    entity.setDisplayProportion(entity.getDisplayProportion() * displayProportionChange);
                }
                if(target.targetDisplayProportionalXOffset != null) {
                    deltaProportionalXOffset = target.targetDisplayProportionalXOffset - entity.getDisplayProportionalXOffset();
                    float proportionalXOffsetChange = Math.signum(deltaProportionalXOffset) * Math.min(
                            Math.abs(deltaProportionalXOffset * delta * displayProportionalOffsetSpeed),
                            Math.abs(deltaProportionalXOffset));
                    entity.setDisplayProportionalXOffset(entity.getDisplayProportionalXOffset() + proportionalXOffsetChange);
                }
                if(target.targetDisplayProportionalYOffset != null) {
                    deltaProportionalYOffset = target.targetDisplayProportionalYOffset - entity.getDisplayProportionalYOffset();
                    float proportionalYOffsetChange = Math.signum(deltaProportionalYOffset) * Math.min(
                            Math.abs(deltaProportionalYOffset * delta * displayProportionalOffsetSpeed),
                            Math.abs(deltaProportionalYOffset));
                    entity.setDisplayProportionalYOffset(entity.getDisplayProportionalYOffset() + proportionalYOffsetChange);
                }
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
