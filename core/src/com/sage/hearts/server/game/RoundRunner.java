package com.sage.hearts.server.game;

import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.server.network.MultiplePlayersDisconnectedException;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerCode;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoundRunner {
    public static void playRound(GameState gameState)
            throws PlayerDisconnectedException, MultiplePlayersDisconnectedException {
        gameState.resetForNewRound();
        gameState.players.sendPacketToAll(new ServerPacket(ServerCode.ROUND_START));

        ServerPacket warheadMapPacket = new ServerPacket(ServerCode.WAIT_FOR_WARHEAD_MAP);
        warheadMapPacket.data.put("map", gameState.warheadMap);

        ServerPacket playerOrderPacket = new ServerPacket(ServerCode.WAIT_FOR_PLAYER_ORDER);
        playerOrderPacket.data.put("order", gameState.players.stream().mapToInt(Player::getPlayerNum).toArray());

        Deck deck = new Deck(false);
        deck.dealToPlayers(gameState.players);

        gameState.players.sendPacketToAll(warheadMapPacket);
        gameState.players.sendPacketToAll(playerOrderPacket);
        sendHands(gameState);

        tradeWarheads(gameState);

        Stream<Player> playerStream = gameState.players.stream();
        while(playerStream.allMatch(p -> p.hand.size() > 0)) {
            TrickRunner.playTrick(gameState);
        }


    }

    private static void sendHands(GameState gameState)
            throws PlayerDisconnectedException, MultiplePlayersDisconnectedException {
        gameState.players.forEach(p -> {
            ServerPacket handPacket = new ServerPacket(ServerCode.WAIT_FOR_HAND);
            handPacket.data.put("hand", p.hand.toCardNumList());
            p.sendPacket(handPacket);
        });
    }

    private static void tradeWarheads(GameState gameState)
            throws PlayerDisconnectedException, MultiplePlayersDisconnectedException {
        Map<Player, CardList<HeartsCard>> warheads = getValidWarheadsFromAll(gameState);

        // TODO
    }

    private static Map<Player, CardList<HeartsCard>> getValidWarheadsFromAll(GameState gameState)
            throws PlayerDisconnectedException, MultiplePlayersDisconnectedException {
        // If a thread detects that a player has disconnected, that thread will set the playerDisconnected flag to true
        // and notify the main thread. The main thread will then interrupt the blocking waitForPacket() calls being made
        // by the other threads. The other threads will detect the InterruptedException and exit. The main thread does
        // not continue until all threads have exited.

        gameState.players.sendPacketToAll(new ServerPacket(ServerCode.SEND_WARHEADS));

        final Object waitObject = new Object();
        final AtomicInteger numFinishedThreads = new AtomicInteger(0);
        final AtomicBoolean playerDisconnected = new AtomicBoolean(false);
        final Map<Player, CardList<HeartsCard>> allWarheads = new HashMap<>();
        final Thread[] warheadThreads = new Thread[gameState.players.size()];
        for(int i = 0, size = gameState.players.size(); i < size; i++) {
            Player p = gameState.players.get(i);
            warheadThreads[i] = new Thread(() -> {
                while(true) {
                    try {
                        ClientPacket warheadPacket = p.waitForPacket();
                        if(warheadPacket.networkCode != ClientCode.WARHEADS) {
                            continue;
                        }
                        CardList<HeartsCard> warheads =
                                Objects.requireNonNull((List<Integer>)(warheadPacket.data.get("warheads"))).stream()
                                        .map(HeartsCard::new)
                                        .collect(Collectors.toCollection(CardList::new));
                        if(gameState.areValidWarheads(p, warheads)) {
                            p.sendPacket(new ServerPacket(ServerCode.SUCCESSFUL_WARHEADS));
                            synchronized(allWarheads) {
                                allWarheads.put(p, warheads);
                            }
                            numFinishedThreads.incrementAndGet();
                            synchronized(waitObject) {
                                waitObject.notify();
                            }
                            return;
                        } else {
                            p.sendPacket(new ServerPacket(ServerCode.INVALID_WARHEADS));
                        }
                    } catch(InterruptedException e) {
                        if(playerDisconnected.get()) {
                            numFinishedThreads.incrementAndGet();
                            synchronized(waitObject) {
                                waitObject.notify();
                            }
                            return;
                        }
                    } catch(PlayerDisconnectedException e) {
                        playerDisconnected.set(true);
                        numFinishedThreads.incrementAndGet();
                        synchronized(waitObject) {
                            waitObject.notify();
                        }
                        return;
                    } catch(NullPointerException | ClassCastException e) {
                        p.sendPacket(new ServerPacket(ServerCode.INVALID_WARHEADS));
                    }
                }
            });
        }
        for(Thread t : warheadThreads) {
            t.start();
        }

        boolean alreadyInterrupted = false;
        while(numFinishedThreads.intValue() < warheadThreads.length) {
            synchronized(waitObject) {
                try {
                    waitObject.wait();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(playerDisconnected.get() && !alreadyInterrupted) {
                gameState.players.forEach(Player::interruptPacketWaiting);
                alreadyInterrupted = true;
            }
        }
        if(playerDisconnected.get()) {
            Optional<Player> firstDisconnectedPlayer =
                    gameState.players.stream().filter(p -> !p.socketIsConnected()).findFirst();
            assert firstDisconnectedPlayer.isPresent();
            gameState.players.sendPacketToAllExcluding(new ServerPacket(ServerCode.PLAYER_DISCONNECTED), firstDisconnectedPlayer.get());
            throw new PlayerDisconnectedException(firstDisconnectedPlayer.get());
        }

        return allWarheads;
    }
}
