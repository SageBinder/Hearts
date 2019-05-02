package com.sage.hearts.utils.card;

import java.util.ArrayList;
import java.util.ListIterator;

public class CardList<T extends Card> extends ArrayList<T> {
    public CardList() {
        super();
    }

    public CardList(CardList<T> other) {
        super(other);
    }

    public boolean remove(Rank rank, Suit suit) {
        for(T c : this) {
            if(c.getRank() == rank && c.getSuit() == suit) {
                remove(c);
                return true;
            }
        }
        return false;
    }

    public boolean contains(Rank rank, Suit suit) {
        for(T c : this) {
            if(c.getRank() == rank && c.getSuit() == suit) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAny(CardList<T> cards) {
        for(T c : cards) {
            if(contains(c)) {
                return true;
            }
        }
        return false;
    }

    public ListIterator<T> reverseListIterator() {
        return listIterator(size());
    }
}
