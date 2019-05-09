package com.sage.hearts.server.game;

import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.hearts.HeartsCard;
import org.apache.commons.collections4.iterators.PermutationIterator;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GameState {
    public final PlayerList players = new PlayerList();
    public final int[] warheadMap = {1, 2, 3, 0};
    public final CardList<HeartsCard> pointCardsInTrick = new CardList<>();
    public Player turnPlayer = null;
    public Player leadingPlayer = null;
    public Player startingPlayer = null;
    public HeartsCard basePlay = null;
    public int tricksPlayed = 0;
    public int roundsPlayed = 0;
    public boolean heartsBroke = false;

    private PermutationIterator<Integer> warheadMapPermuteIter =
            new PermutationIterator<>(Arrays.stream(warheadMap).boxed().collect(Collectors.toList()));

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
        Integer[] newWarheadMap;
        do {
            if(!warheadMapPermuteIter.hasNext()) {
                warheadMapPermuteIter =
                        new PermutationIterator<>(Arrays.stream(warheadMap).boxed().collect(Collectors.toList()));
            }
            newWarheadMap = warheadMapPermuteIter.next().toArray(new Integer[0]);
        } while(newWarheadMap[0] == 0 || newWarheadMap[1] == 1 || newWarheadMap[2] == 2 || newWarheadMap[3] == 3);
        for(int i = 0; i < newWarheadMap.length; i++) {
            warheadMap[i] = newWarheadMap[i];
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

    public boolean areValidWarheads(Player p, CardList<HeartsCard> warheads) {
        return true;
    }
}
