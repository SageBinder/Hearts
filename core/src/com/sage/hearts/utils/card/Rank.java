package com.sage.hearts.utils.card;

public enum Rank {
    TWO("two", 2),
    THREE("three", 3),
    FOUR("four", 4),
    FIVE("five", 5),
    SIX("six", 6),
    SEVEN("seven", 7),
    EIGHT("eight", 8),
    NINE("nine", 9),
    TEN("ten", 10),
    JACK("jack", 11),
    QUEEN("queen", 12),
    KING("king", 13),
    ACE("ace", 14);

    final String stringName;
    final int rankNum;

    Rank(String name, int rankNum) {
        this.stringName = name;
        this.rankNum = rankNum;
    }
}