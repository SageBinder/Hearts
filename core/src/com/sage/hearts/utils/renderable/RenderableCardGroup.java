package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;

import java.util.Collection;

public class RenderableCardGroup<T extends Card & RenderableCard> extends RenderableCardList<T> {
    public Vector2 pos = new Vector2();

    public float cardHeight = Gdx.graphics.getHeight() / 15f,
            regionWidth = Gdx.graphics.getWidth() / 10f,
            prefDivisionProportion = 0.2f,
            rotation = 0.0f;

    private ShapeRenderer debugRenderer = new ShapeRenderer();
    private boolean inDebugMode = false;


    public RenderableCardGroup() {
        super();
    }

    public RenderableCardGroup(Collection<? extends T> other) {
        super(other);
    }

    @Override
    public void render(SpriteBatch batch, Viewport viewport) {
        render(batch, viewport, false);
    }

    @Override
    public void render(SpriteBatch batch, Viewport viewport, boolean renderBase) {
        float cardPositionRegionWidth = regionWidth - (RenderableCardEntity.WIDTH_TO_HEIGHT_RATIO * cardHeight);

        float division = Math.min(RenderableCardEntity.WIDTH_TO_HEIGHT_RATIO * cardHeight * prefDivisionProportion,
                cardPositionRegionWidth / (size() - 1));

        float offset = MathUtils.clamp((cardPositionRegionWidth * 0.5f) - (0.5f * division * (size() - 1)),
                0,
                cardPositionRegionWidth * 0.5f);
        rotation %= MathUtils.PI2;

        for(int i = 0; i < size(); i++) {
            RenderableCardEntity e = get(i).entity();
            float rotCos = MathUtils.cos(rotation);
            float rotSin = MathUtils.sin(rotation);
            float relativeX = i * division + offset;
            float displayProportionalXOffset = e.getDisplayProportionalXOffset();
            float displayProportionalYOffset = e.getDisplayProportionalYOffset();
            float transformedDisplayProportionalXOffset = (rotCos * displayProportionalXOffset) - (rotSin * displayProportionalYOffset);
            float transformedDisplayProportionalYOffset = (rotSin * displayProportionalXOffset) + (rotCos * displayProportionalYOffset);
            e.setOriginProportion(0, 0)
                    .setRotationRad(rotation)
                    .setHeight(cardHeight)
                    .setX(pos.x + (relativeX * rotCos))
                    .setY(pos.y + (relativeX * rotSin))
                    .setDisplayProportionalXOffset(transformedDisplayProportionalXOffset)
                    .setDisplayProportionalYOffset(transformedDisplayProportionalYOffset);
        }

        if(inDebugMode) {
            batch.end();
            renderDebugLines(viewport);
            batch.begin();
        }

        super.render(batch, viewport, renderBase);
    }

    private void renderDebugLines(Viewport viewport) {
        debugRenderer.setProjectionMatrix(viewport.getCamera().combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);

        debugRenderer.setColor(0.0f, 0.0f, 1.0f, 1.0f);
        debugRenderer.rect(pos.x, pos.y, regionWidth, cardHeight);

        debugRenderer.setColor(1.0f, 0.0f, 0.0f, 1.0f);
        debugRenderer.line(pos.x + (regionWidth * 0.5f), pos.y + (cardHeight * 1.5f),
                pos.x + (regionWidth * 0.5f), pos.y - (cardHeight * 0.5f));

        debugRenderer.line(viewport.getWorldWidth() / 2, 0, viewport.getWorldWidth() / 2, viewport.getWorldHeight());
        debugRenderer.line(0, viewport.getWorldHeight() / 2, viewport.getWorldWidth(), viewport.getWorldHeight() / 2);

        debugRenderer.end();
    }
}
