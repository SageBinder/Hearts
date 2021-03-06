package com.sage.hearts.server.game;

import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.util.Collections;

public class Deck extends CardList<HeartsCard> {
    public Deck(boolean jokers) {
        for(int i = 0; i < 52; i++) {
            add(new HeartsCard(i));
        }
        if(jokers) {
            add(new HeartsCard(52));
            add(new HeartsCard(53));
        }
    }

    public void shuffle() {
        Collections.shuffle(this);
    }

    public void dealToPlayers(PlayerList players) {
        for(int i = 0; i < size(); i++) {
            players.get(i % players.size()).hand.add(this.get(i));
        }
        clear();
    }
}
