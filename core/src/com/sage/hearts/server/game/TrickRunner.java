package com.sage.hearts.server.game;

import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerCode;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.util.Objects;

class TrickRunner {
    static void playTrick(GameState gameState) {
        gameState.resetForNewTrick();
        gameState.players.sendPacketToAll(new ServerPacket(ServerCode.TRICK_START));

        // If gameState.turnPlayer is null then this is the first trick of the round
        if(gameState.turnPlayer == null) {
            for(Player p : gameState.players) {
                if(p.hand.contains(Rank.TWO, Suit.CLUBS)) {
                    p.sendPacket(new ServerPacket(ServerCode.PLAY_TWO_OF_CLUBS));
                    p.play = new HeartsCard(Rank.TWO, Suit.CLUBS);
                    p.hand.remove(Rank.TWO, Suit.CLUBS);

                    gameState.startingPlayer = p;
                    gameState.basePlay = p.play;
                    gameState.leadingPlayer = p;
                    gameState.turnPlayer = p;
                    sendNewPlay(gameState);

                    gameState.turnPlayer = getNextPlayer(gameState, gameState.turnPlayer);
                    break;
                }
            }
        }
        assert gameState.turnPlayer != null && gameState.startingPlayer != null;

        do {
            sendTurnPlayer(gameState);
            setTurnPlayerPlay(gameState, getValidPlayFromTurnPlayer(gameState));
            sendNewPlay(gameState);
            sendLeadingPlayer(gameState);
            gameState.turnPlayer = getNextPlayer(gameState, gameState.turnPlayer);
        } while(gameState.turnPlayer != gameState.startingPlayer);

        gameState.leadingPlayer.collectedPointCards.addAll(gameState.pointCardsInTrick);

        ServerPacket trickEndPacket = new ServerPacket(ServerCode.TRICK_END);
        trickEndPacket.data.put("winner", gameState.leadingPlayer.getPlayerNum());
        trickEndPacket.data.put("pointcards", gameState.pointCardsInTrick.toCardNumList());
        gameState.players.sendPacketToAll(trickEndPacket);
    }

    private static Player getNextPlayer(GameState gameState, final Player prev) {
        int nextIdx = gameState.players.indexOf(prev) + 1;
        return (nextIdx == gameState.players.size()) ? gameState.players.get(0) : gameState.players.get(nextIdx);
    }

    private static void sendLeadingPlayer(GameState gameState) {
        ServerPacket leadingPlayerPacket = new ServerPacket(ServerCode.WAIT_FOR_LEADING_PLAYER);
        leadingPlayerPacket.data.put("player", gameState.leadingPlayer.getPlayerNum());
        gameState.players.sendPacketToAll(leadingPlayerPacket);
    }

    private static void sendNewPlay(GameState gameState) {
        ServerPacket playPacket = new ServerPacket(ServerCode.WAIT_FOR_NEW_PLAY);
        playPacket.data.put("player", gameState.turnPlayer.getPlayerNum());
        playPacket.data.put("play", gameState.turnPlayer.play.getCardNum());
        gameState.players.sendPacketToAllExcluding(playPacket, gameState.turnPlayer);
    }

    private static void sendTurnPlayer(GameState gameState) {
        ServerPacket turnPlayerPacket = new ServerPacket(ServerCode.WAIT_FOR_TURN_PLAYER);
        turnPlayerPacket.data.put("player", gameState.turnPlayer.getPlayerNum());
        gameState.players.sendPacketToAll(turnPlayerPacket);
    }

    private static void setTurnPlayerPlay(GameState gameState, HeartsCard play) {
        gameState.turnPlayer.play = play;
        gameState.turnPlayer.hand.remove(gameState.turnPlayer.play.getRank(), gameState.turnPlayer.play.getSuit());
        if(gameState.turnPlayer.play.getPoints() > 0) {
            gameState.pointCardsInTrick.add(gameState.turnPlayer.play);
        }
        if(gameState.basePlay == null) {
            gameState.basePlay = gameState.turnPlayer.play;
            gameState.leadingPlayer = gameState.turnPlayer;
        } else if(gameState.turnPlayer.play.getSuit() == gameState.leadingPlayer.play.getSuit()
                && gameState.turnPlayer.play.getRank().rankNum > gameState.leadingPlayer.play.getRank().rankNum) {
            gameState.leadingPlayer = gameState.turnPlayer;
        }
    }

    private static HeartsCard getValidPlayFromTurnPlayer(GameState gameState) {
        HeartsCard play;
        gameState.turnPlayer.sendPacket(new ServerPacket(ServerCode.MAKE_PLAY));
        while(true) {
            try {
                ClientPacket packet = gameState.turnPlayer.waitForPacket();
                if(packet.networkCode != ClientCode.PLAY) {
                    continue;
                }
                play = new HeartsCard(Objects.requireNonNull((Integer)packet.data.get("play")));
            } catch(InterruptedException e) {
                var disconnectedPlayer = gameState.players.stream().filter(p -> !p.socketIsConnected()).findAny();
                if(disconnectedPlayer.isPresent()) {
                    gameState.players.forEach(Player::clearPacketQueue);
                    throw new PlayerDisconnectedException(disconnectedPlayer.get());
                }

                if(!gameState.turnPlayer.socketIsConnected()) {
                    gameState.players.forEach(Player::clearPacketQueue);
                    throw new PlayerDisconnectedException(gameState.turnPlayer);
                }

                continue;
            } catch(NullPointerException | ClassCastException e) {
                gameState.turnPlayer.sendPacket(new ServerPacket(ServerCode.INVALID_PLAY));
                continue;
            }

            if(gameState.isValidPlay(gameState.turnPlayer, play)) {
                gameState.turnPlayer.sendPacket(new ServerPacket(ServerCode.SUCCESSFUL_PLAY));
                return play;
            } else {
                gameState.turnPlayer.sendPacket(new ServerPacket(ServerCode.INVALID_PLAY));
            }
        }
    }
}
