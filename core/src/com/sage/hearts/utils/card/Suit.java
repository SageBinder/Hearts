package com.sage.hearts.utils.card;

public enum Suit {
    HEARTS("hearts", 0),
    CLUBS("clubs", 1),
    DIAMONDS("diamonds", 2),
    SPADES("spades", 3),
    JOKER("joker", 4);

    public final String stringName;
    public final int suitNum;

    Suit(String name, int suitNum) {
        this.stringName = name;
        this.suitNum = suitNum;
    }

    public static Suit fromCardNum(int cardNum) throws InvalidCardException {
        if(cardNum > 53) throw new InvalidCardException();
        return cardNum >= 52 ? JOKER : Suit.values()[cardNum % 4];
    }

    @Override
    public String toString() {
        return stringName;
    }
}