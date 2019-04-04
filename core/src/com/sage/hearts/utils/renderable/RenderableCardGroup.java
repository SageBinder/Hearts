package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;

public class RenderableCardGroup<T extends Card & RenderableCard> extends RenderableCardList<T> {
    public Vector2 pos = new Vector2();

    public float cardHeight = Gdx.graphics.getHeight() / 15f,
            regionWidth = Gdx.graphics.getWidth() / 10f,
            prefDivisionProportion = 0.2f;

    public RenderableCardGroup() {
        super();
    }

    public RenderableCardGroup(RenderableCardList<T> other) {
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

        for(int i = 0; i < size(); i++) {
            T c = get(i);
            c.getEntity().setHeight(cardHeight).setPosition((i * division) + pos.x + offset, pos.y);
        }

//        if(inDebugMode) {
//            batch.end();
//            renderDebugLines(viewport);
//            batch.begin();
//        }

        super.render(batch, viewport, renderBase);
    }
}
