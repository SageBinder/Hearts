package com.sage.hearts.utils.card;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class CardList<T extends Card> extends ArrayList<T> {
    public CardList() {
        super();
    }

    public CardList(Collection<? extends T> other) {
        super(other);
    }

    public CardList(CardList<T> other) {
        super(other);
    }

    public void add(Rank rank, Suit suit, CardSupplier<T> supplier) {
        add(supplier.get(rank, suit));
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

    public boolean removeAllByValue(CardList<T> remove) {
        boolean removedAny = false;
        for(T c : remove) {
            removedAny |= remove(c.getRank(), c.getSuit());
        }
        return removedAny;
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

    public boolean containsAnySuit(Suit suit) {
        for(T c : this) {
            if(c.getSuit() == suit) {
                return true;
            }
        }
        return false;
    }

    public boolean containsAnyRank(Rank rank) {
        for(T c : this) {
            if(c.getRank() == rank) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Integer> toCardNumList() {
        return stream().mapToInt(Card::getCardNum).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public ListIterator<T> reverseListIterator() {
        return listIterator(size());
    }

    public static interface CardSupplier<T> {
        T get(Rank rank, Suit suit);
    }
}
