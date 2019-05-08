package com.sage.hearts.server.game;

import com.sage.hearts.server.network.PlayerDisconnectedException;

public class RoundRunner {
    public static void playRound(GameState gameState) throws PlayerDisconnectedException {
        for(Player p : gameState.players) {
            p.resetForNewRound();
        }

        Deck deck = new Deck(false);
        deck.dealToPlayers(gameState.players);


    }
}
