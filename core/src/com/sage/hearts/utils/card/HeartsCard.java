package com.sage.hearts.utils.card;

public class HeartsCard extends Card implements Comparable<HeartsCard> {
    public final int pointValue;

    public HeartsCard(Rank rank, Suit suit) {
        super(rank, suit);
        pointValue = determinePointValue();
    }

    public HeartsCard(int cardNum) {
        super(cardNum);
        pointValue = determinePointValue();
    }

    public HeartsCard(HeartsCard other) {
        super(other);
        this.pointValue = other.pointValue;
    }

    private int determinePointValue() {
        return (suit == Suit.HEARTS) ? 1
                : (suit == Suit.SPADES && rank == Rank.QUEEN) ? 13
                : 0;
    }

    @Override
    public int compareTo(HeartsCard o) {
        return Integer.compare(this.rank.rankNum, o.rank.rankNum);
    }
}
