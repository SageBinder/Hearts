package com.sage.hearts.utils.card;

public enum Suit {
    HEARTS("hearts", 0),
    CLUBS("clubs", 1),
    DIAMONDS("diamonds", 2),
    SPADES("spades", 3);

    final String stringName;
    final int suitNum;

    Suit(String name, int suitNum) {
        this.stringName = name;
        this.suitNum = suitNum;
    }
}