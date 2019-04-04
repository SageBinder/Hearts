package com.sage.hearts.utils.card;

@SuppressWarnings("WeakerAccess")
public class Card {
    public final Rank rank;
    public final Suit suit;
    public final int cardNum;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
        cardNum = getCardNumFromRankAndSuit(rank, suit);
    }

    public Card(int cardNum) {
        this.cardNum = cardNum;
        suit = getSuitFromCardNum(cardNum);
        rank = getRankFromCardNum(cardNum);
    }

    public Card(Card other) {
        this.rank = other.rank;
        this.suit = other.suit;
        this.cardNum = other.cardNum;
    }

    public static Rank getRankFromCardNum(int cardNum) {
        return Rank.values()[cardNum / 4];
    }

    public static Suit getSuitFromCardNum(int cardNum) {
        return Suit.values()[cardNum % 4];
    }

    public static int getCardNumFromRankAndSuit(Rank rank, Suit suit) {
        return ((rank.rankNum - 2) * 4) + suit.suitNum;
    }

    public boolean isSameAs(Card other) {
        return this.cardNum == other.cardNum;
    }
}
