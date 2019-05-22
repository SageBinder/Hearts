package com.sage.hearts.server.game;

import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GameState {
    public final ReentrantLock lock = new ReentrantLock();
    public volatile boolean roundStarted = false;

    public final PlayerList players = new PlayerList();
    public final HashMap<Integer, Integer> warheadMap = new HashMap<>();
    public final CardList<HeartsCard> pointCardsInTrick = new CardList<>();
    public Player turnPlayer = null;
    public Player leadingPlayer = null;
    public Player startingPlayer = null;
    public HeartsCard basePlay = null;
    public int tricksPlayed = 0;
    public int roundsPlayed = 0;
    public boolean heartsBroke = false;

    public void resetForNewRound() {
        tricksPlayed = 0;
        roundsPlayed++;
        turnPlayer = null;
        leadingPlayer = null;
        startingPlayer = null;
        basePlay = null;
        pointCardsInTrick.clear();
        heartsBroke = false;
        players.forEach(Player::resetForNewRound);
        cycleWarheadMap();
    }

    public void resetForNewTrick() {
        tricksPlayed++;
        pointCardsInTrick.clear();
        startingPlayer = turnPlayer = leadingPlayer;
        leadingPlayer = null;
        basePlay = null;
    }

    public void cycleWarheadMap() {
        if(warheadMap.isEmpty()) {
            warheadMap.put(0, 1);
            warheadMap.put(1, 2);
            warheadMap.put(2, 3);
            warheadMap.put(3, 0);
        } else {
            int size = warheadMap.keySet().size();
            for(int i = 0; i < size - 1; i++) {
                warheadMap.put(i, warheadMap.put(i + 1, warheadMap.get(i)));
            }
            if(warheadMap.get(0) == 0) {
                cycleWarheadMap();
            }
        }
    }

    public boolean isValidPlay(Player p, HeartsCard play) {
        if(play == null || p == null || !p.hand.contains(play.getRank(), play.getSuit())) {
            return false;
        } else if(basePlay == null) {
            return play.getSuit() != Suit.HEARTS || heartsBroke;
        } else if(play.getSuit() != basePlay.getSuit()) {
            boolean isValid = !p.hand.containsAnySuit(basePlay.getSuit());
            if(isValid && play.getSuit() == Suit.HEARTS) {
                heartsBroke = true;
            }
            return isValid;
        } else {
            return true;
        }
    }

    public boolean areValidWarheads(Player p, CardList<HeartsCard> warheads) {
        return true;
    }
}
