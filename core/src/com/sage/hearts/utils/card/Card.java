package com.sage.hearts.utils.card;

import java.io.Serializable;
import java.util.Random;

@SuppressWarnings("WeakerAccess")
public class Card implements Serializable {
    public final Rank rank;
    public final Suit suit;
    public final int cardNum;

    private static Random r = new Random();

    public Card(Rank rank, Suit suit) throws InvalidCardException {
        this.rank = rank;
        this.suit = suit;
        cardNum = getCardNumFromRankAndSuit(rank, suit);
    }

    public Card(int cardNum) throws InvalidCardException {
        this.cardNum = cardNum;
        suit = Suit.fromCardNum(cardNum);
        rank = Rank.fromCardNum(cardNum);
    }

    public Card(Card other) {
        this.rank = other.rank;
        this.suit = other.suit;
        this.cardNum = other.cardNum;
    }

    public Card() {
        this(r.nextInt(54));
    }

    public boolean isJoker() {
        return suit == Suit.JOKER;
    }

    public boolean isSameAs(Card other) {
        return this.cardNum == other.cardNum;
    }

    public static int getCardNumFromRankAndSuit(Rank rank, Suit suit) throws InvalidCardException {
        if(suit == Suit.JOKER && !(rank == Rank.SMALL_JOKER || rank == Rank.BIG_JOKER)) throw new InvalidCardException();
        return rank == Rank.BIG_JOKER ? 53
                : rank == Rank.SMALL_JOKER ? 52
                : ((rank.rankNum - 2) * 4) + suit.suitNum;
    }

    public static boolean isJoker(int cardNum) {
        return cardNum == 52 || cardNum == 53;
    }

    @Override
    public String toString() {
        return (isJoker()) ? rank.stringName.replace("_", " ")
                : rank.stringName + " of " + suit.stringName;
    }
}
