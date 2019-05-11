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

    public void setPlayers(PlayerList players) {
        this.players.clear();
        this.players.addAll(players);
    }

    public void cycleWarheadMap() {
        // TODO
    }

    // TODO: This isn't correct
    public boolean isValidPlay(Player p, HeartsCard play) {
        if(play == null || !p.hand.contains(play.getRank(), play.getSuit())) {
            return false;
        } else {
            return play.getSuit() != Suit.HEARTS || heartsBroke;
        }
    }

    public boolean areValidWarheads(Player p, CardList<HeartsCard> warheads) {
        return true;
    }
}
