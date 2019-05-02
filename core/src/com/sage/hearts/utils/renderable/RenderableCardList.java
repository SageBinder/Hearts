package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;
import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

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

    @Override
    public boolean remove(Object o) {
        if(o instanceof RenderableCard) {
            ((RenderableCard)o).dispose();
        }
        return super.remove(o);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for(int i = fromIndex; i <= toIndex; i++) {
            get(i).dispose();
        }
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(Collection<?> list) {
        list.forEach(c -> {
            if(contains(c) && c instanceof RenderableCard) {
                ((RenderableCard)c).dispose();
            }
        });
        return super.removeAll(list);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        List<T> toRemove = new ArrayList<>();
        forEach(c -> {
            if(filter.test(c)) {
                toRemove.add(c);
                c.dispose();
            }
        });
        return super.removeAll(toRemove);
    }

    @Override
    public T remove(int index) {
        get(index).dispose();
        return super.remove(index);
    }

    @Override
    public boolean remove(Rank rank, Suit suit) {
        for(T c : this) {
            if(c.getRank() == rank && c.getSuit() == suit) {
                c.dispose();
                remove(c);
                return true;
            }
        }
        return false;
    }

    public boolean removeDisposed() {
        return super.removeIf(RenderableCard::isDisposed);
    }

    @Override
    public void clear() {
        disposeAll();
        super.clear();
    }

    public void disposeAll() {
        forEach(RenderableCard::dispose);
    }
}
