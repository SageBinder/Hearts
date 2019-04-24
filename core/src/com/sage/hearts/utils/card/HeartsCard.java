package com.sage.hearts.utils.card;

public class HeartsCard extends Card implements Comparable<HeartsCard> {
    public final int pointValue;

    public HeartsCard(Rank rank, Suit suit) throws InvalidCardException {
        super(rank, suit);
        pointValue = determinePointValue();
    }

    public HeartsCard(int cardNum) throws InvalidCardException {
        super(cardNum);
        pointValue = determinePointValue();
    }

    public HeartsCard(HeartsCard other) throws InvalidCardException {
        super(other);
        this.pointValue = other.pointValue;
    }

    public HeartsCard() {
        super();
        pointValue = determinePointValue();
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
