package com.sage.hearts.server.game;

import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.server.network.MultiplePlayersDisconnectedException;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerCode;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.util.Objects;

// This is very bad
public class TrickRunner {
    public static void playTrick(GameState gameState)
            throws PlayerDisconnectedException, MultiplePlayersDisconnectedException {
        gameState.players.sendPacketToAll(new ServerPacket(ServerCode.TRICK_START));

        Player startingPlayer = null;
        // If turnPlayer is null then this is the first trick of the round
        if(gameState.turnPlayer == null) {
            for(Player p : gameState.players) {
                if(p.hand.contains(Rank.TWO, Suit.CLUBS)) {
                    p.sendPacket(new ServerPacket(ServerCode.PLAY_TWO_OF_CLUBS));
                    p.play = new HeartsCard(Rank.TWO, Suit.CLUBS);
                    p.hand.remove(Rank.TWO, Suit.CLUBS);
                    startingPlayer = p;
                    gameState.basePlay = p.play;
                    gameState.leadingPlayer = p;
                    gameState.turnPlayer = getNextPlayer(gameState, p);
                    break;
                }
            }
            assert gameState.turnPlayer != null && startingPlayer != null;
        } else {
            startingPlayer = gameState.turnPlayer;
        }

        do {
            gameState.turnPlayer.sendPacket(new ServerPacket(ServerCode.MAKE_PLAY));
            while(true) {
                try {
                    ClientPacket packet = gameState.turnPlayer.waitForPacket();
                    if(packet.networkCode != ClientCode.PLAY) {
                        continue;
                    }
                    gameState.turnPlayer.play = new HeartsCard(Objects.requireNonNull((Integer)packet.data.get("play")));
                } catch(InterruptedException e) {
                    throw new PlayerDisconnectedException(gameState.turnPlayer);
                } catch(NullPointerException | ClassCastException e) {
                    gameState.turnPlayer.sendPacket(new ServerPacket(ServerCode.INVALID_PLAY));
                }
                if(!(gameState.turnPlayer.play != null && gameState.isValidPlay(gameState.turnPlayer, gameState.turnPlayer.play))) {
                    gameState.turnPlayer.sendPacket(new ServerPacket(ServerCode.INVALID_PLAY));
                } else {
                    gameState.turnPlayer.sendPacket(new ServerPacket(ServerCode.SUCCESSFUL_PLAY));
                    gameState.turnPlayer.hand.remove(gameState.turnPlayer.play.getRank(), gameState.turnPlayer.play.getSuit());
                    if(gameState.basePlay == null) {
                        gameState.basePlay = gameState.turnPlayer.play;
                        gameState.leadingPlayer = gameState.turnPlayer;
                    }
                    break;
                }
            }

            ServerPacket playPacket = new ServerPacket(ServerCode.WAIT_FOR_NEW_PLAY);
            playPacket.data.put("playernum", gameState.turnPlayer.getPlayerNum());
            playPacket.data.put("play", gameState.turnPlayer.play.getCardNum());
            for(Player p : gameState.players) {
                if(p != gameState.turnPlayer) {
                    p.sendPacket(playPacket);
                }
            }

            if(gameState.turnPlayer != startingPlayer
                    && gameState.turnPlayer.play.getSuit() == gameState.basePlay.getSuit()
                    && gameState.turnPlayer.play.getRank().rankNum > gameState.basePlay.getRank().rankNum) {
                gameState.leadingPlayer = gameState.turnPlayer;
            }

            ServerPacket leadingPlayerPacket = new ServerPacket(ServerCode.WAIT_FOR_LEADING_PLAYER);
            leadingPlayerPacket.data.put("player", gameState.leadingPlayer.getPlayerNum());
            gameState.players.sendPacketToAll(leadingPlayerPacket);

            gameState.turnPlayer = getNextPlayer(gameState, gameState.turnPlayer);
        } while(gameState.turnPlayer != startingPlayer);


        gameState.turnPlayer = gameState.leadingPlayer;
        gameState.players.sendPacketToAll(new ServerPacket(ServerCode.TRICK_END));
    }

    private static Player getNextPlayer(GameState gameState, Player prev) {
        int nextPlayerIdx = 0;
        for(int i = 0; i < gameState.players.size(); i++) {
            if(gameState.players.get(i) == prev) {
                if(i + 1 != gameState.players.size()) {
                    nextPlayerIdx = i;
                }
                break;
            }
        }

        return gameState.players.get(nextPlayerIdx % gameState.players.size());
    }
}
