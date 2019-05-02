package com.sage.hearts.utils.hearts;

import com.sage.hearts.utils.card.Card;
import com.sage.hearts.utils.card.InvalidCardException;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;

public class HeartsCard extends Card implements Comparable<HeartsCard> {
    private int points;

    public HeartsCard(Rank rank, Suit suit) throws InvalidCardException {
        super(rank, suit);
        points = determinePointValue();
    }

    public HeartsCard(int cardNum) throws InvalidCardException {
        super(cardNum);
        points = determinePointValue();
    }

    public HeartsCard(HeartsCard other) throws InvalidCardException {
        super(other);
        this.points = other.points;
    }

    public HeartsCard() {
        super();
        points = determinePointValue();
    }

    public int getPoints() {
        return points;
    }

    private int determinePointValue() {
        return (getSuit() == Suit.HEARTS) ? 1
                : (getSuit() == Suit.SPADES && getRank() == Rank.QUEEN) ? 13
                : 0;
    }

    @Override
    protected void cardChangedImpl() {
        points = determinePointValue();
    }

    @Override
    public int compareTo(HeartsCard o) {
        if(this.getSuit() == o.getSuit()) {
            return Integer.compare(this.getRank().rankNum, o.getRank().rankNum);
        } else {
            return Integer.compare(this.getSuit().suitNum, o.getSuit().suitNum);
        }
    }
}
