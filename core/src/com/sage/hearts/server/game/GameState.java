package com.sage.hearts.server.game;

import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.util.Collections;
import java.util.HashMap;

public class GameState {
    public static final int NUM_PLAYERS_TO_START = 4;

    final PlayerList players = new PlayerList();

    private boolean roundRunning = false;

    final HashMap<Integer, Integer> warheadMap = new HashMap<>();
    final CardList<HeartsCard> pointCardsInTrick = new CardList<>();
    Player turnPlayer = null;
    Player leadingPlayer = null;
    Player startingPlayer = null;
    HeartsCard basePlay = null;
    int tricksPlayed = 0;
    int roundsPlayed = 0;
    boolean heartsBroke = false;

    public synchronized void addPlayer(Player p) throws RoundIsRunningException {
        if(roundRunning) {
            throw new RoundIsRunningException();
        }

        players.add(p);
    }

    public synchronized boolean removePlayer(Player p) throws RoundIsRunningException {
        if(roundRunning) {
            throw new RoundIsRunningException();
        }

        return players.remove(p);
    }

    public synchronized PlayerList getPlayers() {
        return new PlayerList(players);
    }

    public synchronized void shufflePlayers() throws RoundIsRunningException {
        if(roundRunning) {
            throw new RoundIsRunningException();
        }

        Collections.shuffle(players);
    }

    public synchronized boolean removeDisconnectedPlayers() {
        if(roundRunning) {
            return false;
        }

        boolean anyRemoved = players.removeIf(player -> !player.socketIsConnected());
        if(anyRemoved) {
            players.squashPlayerNums();
        }
        return anyRemoved;
    }

    synchronized void setRoundRunning(boolean roundRunning) {
        this.roundRunning = roundRunning;
    }

    synchronized void resetForNewRound() {
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

    void resetForNewTrick() {
        tricksPlayed++;
        pointCardsInTrick.clear();
        startingPlayer = turnPlayer = leadingPlayer;
        leadingPlayer = null;
        basePlay = null;
    }

    private void cycleWarheadMap() {
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

    PlayValidityResult isValidPlay(Player p, HeartsCard play) {
        if(play == null) {
            return new PlayValidityResult(false, "Play was null (THIS IS BAD)");
        } else if(p == null) {
            return new PlayValidityResult(false, "Player was null (THIS IS BAD)");
        } else if(!p.hand.contains(play.getRank(), play.getSuit())) {
            return new PlayValidityResult(false, "Hand did not contain card (THIS IS BAD)");
        } else if(tricksPlayed == 1 && play.getPoints() > 0 && p.hand.stream().anyMatch(c -> c.getPoints() == 0)) {
            return new PlayValidityResult(false, "You cannot play points on the first round");
        } else if(basePlay == null) {
            if(play.getSuit() != Suit.HEARTS
                    || p.hand.stream().allMatch(c -> c.getSuit() == Suit.HEARTS)
                    || heartsBroke) {
                return new PlayValidityResult(true, "");
            } else {
                return new PlayValidityResult(false, "Hearts has not been broken yet");
            }
        } else if(play.getSuit() != basePlay.getSuit()) {
            if(!p.hand.containsAnySuit(basePlay.getSuit())) {
                heartsBroke = play.getSuit() == Suit.HEARTS;
                return new PlayValidityResult(true, "");
            } else {
                return new PlayValidityResult(false,
                        "You still have " + basePlay.getSuit().toString() + " in your hand");
            }
        } else {
            return new PlayValidityResult(true, "");
        }
    }

    boolean areValidWarheads(Player p, CardList<HeartsCard> warheads) {
        p.hand.forEach(c -> System.out.print(c.toString() + " "));
        warheads.forEach(c -> System.out.print(c.toString() + " "));
        return p.hand.toCardNumList().containsAll(warheads.toCardNumList());
    }

    public synchronized boolean isRoundRunning() {
        return roundRunning;
    }
}
