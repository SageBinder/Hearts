package com.sage.hearts.server.game;

import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.hearts.HeartsCard;

public class GameState {
    public final PlayerList players = new PlayerList();
    public Player turnPlayer = null;
    public Player leadingPlayer = null;
    public HeartsCard basePlay = null;
    public int tricksPlayed = 0;
    public int roundsPlayed = 0;
    public boolean heartsBroke = false;

    public void resetForNewRound() {
        tricksPlayed = 0;
        turnPlayer = null;
        leadingPlayer = null;
        basePlay = null;
        roundsPlayed++;
        heartsBroke = false;
        for(Player p : players) {
            p.resetForNewRound();
        }
    }

    // TODO: This isn't correct
    public boolean isValidPlay(Player p, HeartsCard play) {
        if(play == null || !p.hand.contains(play.getRank(), play.getSuit())) {
            return false;
        } else {
            return play.getSuit() != Suit.HEARTS || heartsBroke;
        }
    }
}
