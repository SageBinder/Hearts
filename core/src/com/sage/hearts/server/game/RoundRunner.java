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

public class RoundRunner {
    public static void playRound(GameState gameState) {
        gameState.resetForNewRound();

        gameState.players.sendPlayersToAll();
        ServerPacket roundStartPacket = new ServerPacket(ServerCode.ROUND_START);
        roundStartPacket.data.put("warheadmap", gameState.warheadMap);
        roundStartPacket.data.put("playerorder", gameState.players.stream().mapToInt(Player::getPlayerNum).toArray());
        gameState.players.sendPacketToAll(roundStartPacket);

        Deck deck = new Deck(false);
        deck.shuffle();
        deck.dealToPlayers(gameState.players);

        sendHands(gameState);
        tradeWarheads(gameState);

        gameState.players.forEach(p -> p.setOnDisconnect(() -> {
            gameState.players.forEach(Player::interruptPacketWaiting);
        }));

        do {
            TrickRunner.playTrick(gameState);
        } while(gameState.players.stream().anyMatch(p -> p.hand.stream().anyMatch(c -> c.getPoints() > 0)));
        // There's no point in continuing play if all point cards have been played and distributed

        gameState.players.forEach(Player::resetOnDisconnect);
        gameState.players.sendPacketToAll(updatePointsAndGetRoundEndPacket(gameState));
    }

    private static ServerPacket updatePointsAndGetRoundEndPacket(GameState gameState) {
        ServerPacket roundEndPacket = new ServerPacket(ServerCode.ROUND_END);
        HashMap<Integer, Integer> gainedPointsMap = new HashMap<>();
        boolean endedEarly = gameState.players.stream().anyMatch(p -> !p.hand.isEmpty());
        int shotTheMoonPlayerNum = -1;

        PlayerList playersWithPoints = gameState.players.stream()
                .filter(p -> !p.collectedPointCards.isEmpty())
                .collect(Collectors.toCollection(PlayerList::new));
        if(playersWithPoints.size() == 1) {
            // If only one player has points, they've shot the moon. If a player shoots the moon, add 26 points to the
            // accumulatedPoints of all other players.
            Player shotTheMoon = playersWithPoints.get(0);
            gameState.players.stream().filter(p -> p != shotTheMoon).forEach(p -> {
                p.accumulatedPoints += 26;
                gainedPointsMap.put(p.getPlayerNum(), 26);
            });
            shotTheMoonPlayerNum = shotTheMoon.getPlayerNum();
            gainedPointsMap.put(shotTheMoonPlayerNum, 0);
        } else {
            // If no one shot the moon, add each player's collectedPointCards point sum to their accumulatedPoints
            playersWithPoints.forEach(p -> {
                int gainedPoints = p.collectedPointCards.stream().mapToInt(HeartsCard::getPoints).sum();
                p.accumulatedPoints += gainedPoints;
                gainedPointsMap.put(p.getPlayerNum(), gainedPoints);
            });
        }


        roundEndPacket.data.put("endedearly", endedEarly);
        roundEndPacket.data.put("shotthemoon", shotTheMoonPlayerNum);
        roundEndPacket.data.put("gainedpointsmap", gainedPointsMap);

        return roundEndPacket;
    }

    private static void sendHands(GameState gameState) {
        gameState.players.forEach(p -> {
            ServerPacket handPacket = new ServerPacket(ServerCode.WAIT_FOR_HAND);
            handPacket.data.put("hand", p.hand.toCardNumList());
            p.sendPacket(handPacket);
        });
    }

    private static void tradeWarheads(GameState gameState) {
        Map<Player, CardList<HeartsCard>> warheadsMap = getValidWarheadsFromAll(gameState);

        for(Player sender : warheadsMap.keySet()) {
            CardList<HeartsCard> warheads = warheadsMap.get(sender);
            ServerPacket warheadPacket = new ServerPacket(ServerCode.WAIT_FOR_WARHEADS);
            warheadPacket.data.put("warheads", warheads.toCardNumList());

            Optional<Player> receiver = gameState.players.getByPlayerNum(gameState.warheadMap.get(sender.getPlayerNum()));
            assert receiver.isPresent();
            receiver.get().sendPacket(warheadPacket);
            receiver.get().hand.addAll(warheads);

            sender.hand.removeAllByValue(warheads);
        }
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
                    gameState.players.stream().filter(p -> !p.socketIsConnected()).findAny();
            assert firstDisconnectedPlayer.isPresent();
            throw new PlayerDisconnectedException(firstDisconnectedPlayer.get());
        }

        return allWarheads;
    }
}
