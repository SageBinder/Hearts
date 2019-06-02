package com.sage.hearts.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.client.network.ClientConnection;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.client.network.LostConnectionToServerException;
import com.sage.hearts.server.network.ServerCode;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.card.InvalidCardException;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;
import com.sage.hearts.utils.hearts.HeartsCard;
import com.sage.hearts.utils.renderable.RenderableCardList;
import com.sage.hearts.utils.renderable.RenderableHand;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class GameState {
    // Light Goldenrod
    public static final Color winningPlayColor = new Color(238f / 255f, 221f / 255f, 130f / 255f, 1f);
    public static final Color basePlayColor = Color.LIGHT_GRAY;
    public static final Color turnPlayerNameColor = Color.LIGHT_GRAY;
    public static final Color warheadReceiverNameColor = Color.FIREBRICK;

    private Updater updater = new Updater();
    private final HeartsGame game;
    public final Actions actions = new Actions();

    private final RenderableCardList<RenderableHeartsCard> lastWarheads = new RenderableCardList<>();

    public final RenderablePlayer[] players = new RenderablePlayer[4];

    public final HashMap<Integer, Integer> warheadMap = new HashMap<>();

    public RenderablePlayer turnPlayer;
    public RenderablePlayer leadingPlayer;
    public RenderablePlayer hostPlayer;

    public RenderablePlayer thisPlayer;
    public final RenderableHand<RenderableHeartsCard> thisPlayerHand = new RenderableHand<>();

    public ServerCode lastServerCode;
    public String message = "";
    public boolean heartsBroke = false;

    public GameState(HeartsGame game) {
        this.game = game;
    }

    public void clean() {
        for(int i = 0; i < players.length; i++) {
            players[i] = null;
        }
        turnPlayer = null;
        leadingPlayer = null;
        hostPlayer = null;
        thisPlayer = null;
        thisPlayerHand.clear();
        warheadMap.clear();
        lastWarheads.clear();
    }

    public boolean update(ClientConnection client) {
        if(client == null) {
            return false;
        }

        // It's important to only update at most a single packet. If more than one packet is updated and
        // some packet changes the screen, it won't give the new screen a chance to update with the latest packet.
        // The new screen won't have any idea what state it should be in. Updating from gameState in show()
        // doesn't work because gameState may change after show() is called.
        // POSSIBLE SOLUTION: New screen should update from gameState on its first render.
        try {
            Optional<ServerPacket> p = client.getPacket();
            return (p.isPresent()) && applyUpdate(p.get());
        } catch(LostConnectionToServerException e) {
            updater.lostConnectionToServer();
            return true;
        }
    }

    private boolean applyUpdate(ServerPacket updatePacket) {
        return updater.update(updatePacket.networkCode, updatePacket.data);
    }

    private Optional<RenderablePlayer> getPlayerByPlayerNum(Integer playerNum) {
        return (playerNum == null)
                ? Optional.empty()
                : Arrays.stream(players).filter(player -> player.getPlayerNum() == playerNum).findFirst();
    }

    private class Updater {
        private Map<Serializable, Serializable> data = null;
        private final CardList<RenderableHeartsCard> pointCardsInTrick = new CardList<>();

        private boolean update(ServerCode serverCode, Map<Serializable, Serializable> data) {
            if(serverCode != ServerCode.PING) {
                lastServerCode = serverCode;
            }
            this.data = data;
            try {
                switch(lastServerCode) {
                    // General codes
                case PING:
                    ping(); break;
                case CONNECTION_ACCEPTED:
                    connectionAccepted(); break;
                case CONNECTION_DENIED:
                    connectionDenied(); break;
                case PLAYER_DISCONNECTED:
                    playerDisconnected(); break;
                case COULD_NOT_START_GAME:
                    couldNotStartGame(); break;
                case UNSUCCESSFUL_NAME_CHANGE:
                    unsuccessfulNameChange(); break;
                case WAIT_FOR_PLAYERS:
                    waitForPlayers(); break;
                case NEW_PLAYER_POINTS:
                    allPlayerPoints(); break;

                    // Trick codes:
                case TRICK_START:
                    trickStart(); break;
                case PLAY_TWO_OF_CLUBS:
                    playTwoOfClubs(); break;
                case MAKE_PLAY:
                    makePlay(); break;
                case INVALID_PLAY:
                    invalidPlay(); break;
                case SUCCESSFUL_PLAY:
                    successfulPlay(); break;
                case WAIT_FOR_TURN_PLAYER:
                    waitForTurnPlayer(); break;
                case WAIT_FOR_NEW_PLAY:
                    waitForNewPlay(); break;
                case WAIT_FOR_LEADING_PLAYER:
                    waitForLeadingPlayer(); break;
                case TRICK_END:
                    trickEnd(); break;

                    // Round codes:
                case ROUND_START:
                    roundStart(); break;
                case WAIT_FOR_HAND:
                    waitForHand(); break;
                case SEND_WARHEADS:
                    sendWarheads(); break;
                case INVALID_WARHEADS:
                    invalidWarheads(); break;
                case SUCCESSFUL_WARHEADS:
                    successfulWarheads(); break;
                case WAIT_FOR_WARHEADS:
                    waitForWarheads(); break;
                case ROUND_END:
                    roundEnd(); break;
                }
            } catch(ClassCastException | NullPointerException | InvalidServerPacketException e) {
                Gdx.app.log("Updater.update()",
                        "Oh shit encountered ClassCastException/InvalidServerPacketException/NullPointerException "
                                + "in Updater.update(), this is VERY BAD\n"
                                + e.getMessage());
                e.printStackTrace();
                message = e.getClass() + ": " + e.getMessage();
                // TODO: When encountering an error here, request the full game state from the server
            }
            return serverCode != ServerCode.PING; // PING does not constitute and update as of now
        }

        // --- GENERAL CODES ---
        // ping() is just here in case anything needs to be done on ping (and to keep the switch pattern)
        private void ping() {
        }

        private void connectionAccepted() {
            message = "Joined successfully!";
        }

        private void connectionDenied() {
            message = "Error joining game: connection denied. Maybe the game is full or has already started?";
            game.showStartScreen();
        }

        private void playerDisconnected() {
            message = "[YELLOW]A player has disconnected!";
            game.showLobbyScreen();
        }

        private void couldNotStartGame() {
            message = "[YELLOW]Error: cannot start game. Either there aren't enough players or the game is already running.";
        }

        private void unsuccessfulNameChange() {
            message = "Error: invalid name.";
        }

        private void waitForPlayers() {
            var newPlayersMap = (Map<Integer, String>)data.get("players");
            var accumulatedPointsMap = (Map<Integer, Integer>)data.get("points");
            int clientPlayerNum = (Integer)(data.get("you"));
            int hostPlayerNum = (Integer)(data.get("host"));

            for(int i = 0; i < GameState.this.players.length; i++) {
                if(players[i] != null) {
                    players[i].clearCards();
                    players[i] = null;
                }
            }

            Iterator<Integer> keyIter = newPlayersMap.keySet().iterator();
            for(int i = 0; i < players.length && keyIter.hasNext(); i++) {
                Integer playerNum = keyIter.next();
                players[i] = new RenderablePlayer(playerNum, newPlayersMap.get(playerNum));
                players[i].setHost(playerNum == hostPlayerNum);
                players[i].setIsClientPlayer(playerNum == clientPlayerNum);
                players[i].setAccumulatedPoints(accumulatedPointsMap.getOrDefault(playerNum, 0));
            }
            thisPlayer = getPlayerByPlayerNum(clientPlayerNum).orElseThrow(() -> new InvalidServerPacketException(
                    "waitForPlayers() - No player found with player num "
                            + clientPlayerNum
                            + " sent by server for client player"
            ));
            hostPlayer = getPlayerByPlayerNum(hostPlayerNum).orElseThrow(() -> new InvalidServerPacketException(
                    "waitForPlayers() - No player found with player num "
                            + clientPlayerNum
                            + " sent by server for host player"
            ));
        }

        private void allPlayerPoints() {
            RenderablePlayer updatedPlayer = getPlayerByPlayerNum((Integer)data.get("player"))
                    .orElseThrow(() -> new InvalidServerPacketException(
                            "allPlayerPoints() - No player found with player num "
                                    + data.get("player")
                                    + " sent by server for updated player"));
            Integer newPoints = (Integer)data.get("points");
            updatedPlayer.setAccumulatedPoints((newPoints != null) ? newPoints : 0);
        }

        // --- TRICK CODES ---
        private void trickStart() {
            if(leadingPlayer != null && !pointCardsInTrick.isEmpty()) {
                pointCardsInTrick.forEach(c -> {
                    c.entity.defaultFaceBackgroundColor.set(Color.WHITE);
                    c.entity.resetFaceBackgroundColor();
                });
                leadingPlayer.collectedPointCards.addAll(pointCardsInTrick);
                leadingPlayer.collectedPointCards.sort(HeartsCard::compareTo);
                pointCardsInTrick.clear();
            }

            Arrays.stream(players).forEach(player -> {
                player.clearPlay();
                player.clearNameColor();
            });
        }

        private void playTwoOfClubs() {
            thisPlayer.clearPlay();
            thisPlayer.setPlay(thisPlayerHand.getAndRemove(Rank.TWO, Suit.CLUBS)
                    .orElse(new RenderableHeartsCard(Rank.TWO, Suit.CLUBS)));
            thisPlayer.getPlay().ifPresent(play -> {
                play.entity.setHighlighted(false);
                play.entity.defaultFaceBackgroundColor.set(basePlayColor);
                play.entity.resetFaceBackgroundColor();
            });
            message = "You had the two of clubs";
        }

        private void makePlay() {
            message = "It's your turn";
            turnPlayer = thisPlayer;
        }

        private void invalidPlay() {
            message = "Error: invalid play. Try again.";
            if(thisPlayer.getPlay().isPresent()) {
                thisPlayer.getPlay().get().setSelected(true).setSelectable(true);
                thisPlayerHand.add(thisPlayer.getPlay().get());
                thisPlayer.setPlay(null);
            }
        }

        private void successfulPlay() {
            thisPlayer.getPlay().ifPresent(play -> {
                // If the play was a hearts card, hearts must be broken if it wasn't before
                if(heartsBroke |= thisPlayer.getPlay().get().getSuit() == Suit.HEARTS) {
                    Gdx.graphics.setTitle("\uD83D\uDC94\uD83D\uDC94\uD83D\uDC94");
                }

                // If this player's play was the base play, set its color accordingly.
                if(Arrays.stream(players).filter(player -> player.getPlay().isPresent()).count() == 1) {
                    play.entity.defaultFaceBackgroundColor.set(basePlayColor);
                    play.entity.resetFaceBackgroundColor();
                }

                if(play.getPoints() > 0) {
                    pointCardsInTrick.add(play);
                }
            });
            message = "Play successfully made.";
        }

        private void waitForTurnPlayer() {
            if(turnPlayer != null) {
                if(turnPlayer == leadingPlayer) {
                    turnPlayer.setNameColor(winningPlayColor);
                } else {
                    turnPlayer.clearNameColor();
                }
            }

            turnPlayer = getPlayerByPlayerNum((Integer)data.get("player"))
                    .orElseThrow(() -> new InvalidServerPacketException(
                            "waitForTurnPlayer() - No player found with player num "
                                    + data.get("player")
                                    + " sent by server for turn player"));
            turnPlayer.setNameColor(turnPlayerNameColor);
            message = "It's "
                    + "P" + turnPlayer.getPlayerNum() + ": " + turnPlayer.getColoredName()
                    + (turnPlayer.getName().charAt(turnPlayer.getName().length() - 1) == 's' ? "'" : "'s") // Pluralizing
                    + " turn";
        }

        private void waitForNewPlay() {
            RenderablePlayer newPlayPlayer = getPlayerByPlayerNum((Integer)data.get("player"))
                    .orElseThrow(() -> new InvalidServerPacketException(
                            "waitForNewPlay() - No player found with player num "
                                    + data.get("player")
                                    + " sent by server"));
            RenderableHeartsCard newPlay;
            try {
                newPlay = new RenderableHeartsCard((Integer)data.get("play"));
            } catch(InvalidCardException e) {
                throw new InvalidServerPacketException("waitForNewPlay() - server sent invalid card num " + data.get("play"));
            }

            newPlayPlayer.clearPlay(); // newPlayPlayer.play should already be null but clear just in case
            boolean newPlayIsBasePlay = Arrays.stream(players).noneMatch(player -> player.getPlay().isPresent());
            newPlayPlayer.setPlay(newPlay);
            if(newPlayIsBasePlay) {
                newPlay.entity.defaultFaceBackgroundColor.set(basePlayColor);
                newPlay.entity.resetFaceBackgroundColor();
            }
            if(newPlay.getPoints() > 0) {
                pointCardsInTrick.add(newPlay);
            }
            if(heartsBroke |= newPlay.getSuit() == Suit.HEARTS) {
                Gdx.graphics.setTitle("\uD83D\uDC94\uD83D\uDC94\uD83D\uDC94");
            }
        }

        private void waitForLeadingPlayer() {
            if(leadingPlayer != null) {
                leadingPlayer.getPlay().ifPresent(c -> c.entity.resetFaceBackgroundColor());
                leadingPlayer.clearNameColor();
            }

            leadingPlayer = getPlayerByPlayerNum((Integer)data.get("player"))
                    .orElseThrow(() -> new InvalidServerPacketException(
                            "waitForLeadingPlayer() - No player found with player num "
                                    + data.get("player")
                                    + " sent by server for leading player"
                    ));
            leadingPlayer.getPlay().ifPresent(c -> c.entity.setFaceBackgroundColor(winningPlayColor));
            leadingPlayer.setNameColor(winningPlayColor);
        }

        private void trickEnd() {
            RenderablePlayer trickWinner = getPlayerByPlayerNum((Integer)data.get("winner"))
                    .orElseThrow(() -> new InvalidServerPacketException(
                            "trickEnd() - No player found with player num "
                                    + data.get("winner")
                                    + " sent by server for trick winner"
                    ));
            message = trickWinner.getName() + " won the trick.\nCollects "
                    + pointCardsInTrick.stream().mapToInt(HeartsCard::getPoints).sum()
                    + " points";
        }

        // --- ROUND CODES ---
        private void roundStart() {
            Arrays.stream(players).filter(Objects::nonNull).forEach(RenderablePlayer::clearCards);
            turnPlayer = null;
            leadingPlayer = null;
            heartsBroke = false;
            pointCardsInTrick.clear();
            message = "";

            warheadMap.clear();
            warheadMap.putAll((HashMap<Integer, Integer>)data.get("warheadmap"));

            lastWarheads.clear();

            int[] playerOrder = (int[])data.get("playerorder");
            RenderablePlayer[] newPlayerArr = new RenderablePlayer[players.length];
            for(int i = 0; i < newPlayerArr.length; i++) {
                final int ii = i;
                newPlayerArr[i] = getPlayerByPlayerNum(playerOrder[i])
                        .orElseThrow(() -> new InvalidServerPacketException(
                                "roundStart() - No player found with player num "
                                        + playerOrder[ii]
                                        + " sent by server for player order"));
            }
            System.arraycopy(newPlayerArr, 0, players, 0, newPlayerArr.length);

            game.stopTitleTimer();
            Gdx.graphics.setTitle("❤️❤️❤️");
            game.showGameScreen();
        }

        private void waitForHand() {
            RenderableCardList<RenderableHeartsCard> newHand;
            try {
                newHand = ((List<Integer>)data.get("hand")).stream()
                        .map(RenderableHeartsCard::new)
                        .collect(Collectors.toCollection(RenderableCardList::new));
            } catch(InvalidCardException e) {
                throw new InvalidServerPacketException("waitForHand() - Server sent an invalid card num for a card in hand");
            }
            thisPlayerHand.clear();
            thisPlayerHand.addAll(newHand);
        }

        private void sendWarheads() {
            var receivingPlayer = getPlayerByPlayerNum(warheadMap.get(thisPlayer.getPlayerNum()))
                    .orElseThrow(() -> new InvalidServerPacketException(
                            "sendWarheads() - No player found with player num "
                                    + warheadMap.get(thisPlayer.getPlayerNum())
                                    + ", gotten from warheadMap.get(" + thisPlayer.getPlayerNum() + ")"
                    ));
            receivingPlayer.setNameColor(warheadReceiverNameColor);
            message = "Select 3 cards to send to "
                    + receivingPlayer.getColoredName();
        }

        private void invalidWarheads() {
            message = "Error: invalid cards. Try again.";
            thisPlayerHand.addAll(lastWarheads);
            lastWarheads.clear();
        }

        private void successfulWarheads() {
            lastWarheads.clear();
            var receivingPlayer = getPlayerByPlayerNum(warheadMap.get(thisPlayer.getPlayerNum()))
                    .orElseThrow(() -> new InvalidServerPacketException(
                            "successfulWarheads() - No player found with player num "
                                    + warheadMap.get(thisPlayer.getPlayerNum())
                                    + ", gotten from warheadMap.get(" + thisPlayer.getPlayerNum() + ")"
                    ));
            message = "Cards successfully sent to "
                    + receivingPlayer.getColoredName();
        }

        private void waitForWarheads() {
            RenderableCardList<RenderableHeartsCard> warheads;
            try {
                warheads = ((List<Integer>)data.get("warheads")).stream()
                        .map(RenderableHeartsCard::new)
                        .collect(Collectors.toCollection(RenderableCardList::new));
            } catch(InvalidCardException e) {
                throw new InvalidServerPacketException("waitForWarheads() - Server sent an invalid card num for a card in warheads");
            }
            thisPlayerHand.addAll(warheads);
        }

        private void roundEnd() {
            if(leadingPlayer != null && !pointCardsInTrick.isEmpty()) {
                pointCardsInTrick.forEach(c -> {
                    c.entity.defaultFaceBackgroundColor.set(Color.WHITE);
                    c.entity.resetFaceBackgroundColor();
                });
                leadingPlayer.collectedPointCards.addAll(pointCardsInTrick);
                leadingPlayer.collectedPointCards.sort(HeartsCard::compareTo);
                pointCardsInTrick.clear();
            }

            Map<Integer, Integer> gainedPointsMap = (Map<Integer, Integer>)data.get("gainedpointsmap");
            boolean endedEarly = (boolean)data.get("endedearly");
            int shotTheMoonPlayerNum = (int)data.get("shotthemoon"); // server will send -1 if no player shot the moon
            Optional<RenderablePlayer> shotTheMoonPlayer = getPlayerByPlayerNum(shotTheMoonPlayerNum);

            message = "Round over!";
            if(endedEarly) message += " (All point cards have been played)";
            if(shotTheMoonPlayer.isPresent()) {
                message += "\n" + shotTheMoonPlayer.get().getName() + " shot the moon! Everybody else gains 26 points!";
            } else {
                Arrays.stream(players).forEach(player -> {
                    Integer gainedPoints = Optional.ofNullable(gainedPointsMap.get(player.getPlayerNum())).orElse(0);
                    player.clearNameColor();
                    player.addToAccumulatedPoints(gainedPoints);
                    message += "\n";
                    if(player == thisPlayer) message += "->";
                    message += "P" + player.getPlayerNum() + ": "
                            + player.getName() + " gained " + gainedPoints + " points";
                });
            }

            game.startTitleTimer();
        }

        private void lostConnectionToServer() {
            message = "The connection to the host has been lost.";
            game.showStartScreen();
        }
    }

    public final class Actions {
        private Actions() {

        }

        public void sendWarheads(ClientConnection client) {
            // This if serves essentially the same purpose as the corresponding guard if in sendPlay()
            if(!lastWarheads.isEmpty()) {
                message = "Your cards are being validated by the server...";
                return;
            }

            RenderableCardList<RenderableHeartsCard> selectedCards = thisPlayerHand.stream()
                    .filter(RenderableHeartsCard::isSelected)
                    .collect(Collectors.toCollection(RenderableCardList::new));
            if(selectedCards.size() == 3) {
                ClientPacket packet = new ClientPacket(ClientCode.WARHEADS);
                packet.data.put("warheads", selectedCards.toCardNumList());
                try {
                    client.sendPacket(packet);
                } catch(IOException e) {
                    message = "There was an error while trying to contact the server... try again";
                    return;
                }
                thisPlayerHand.removeAll(selectedCards);
                lastWarheads.addAll(selectedCards);
                message = "Sending cards...";
            } else {
                message = "You must select 3 cards to send";
            }
        }

        public void sendPlay(ClientConnection client) {
            // This else/if prevents the player from making two plays in quick succession,
            // which would overwrite thisPlayer.getPlay() and would send the server both plays.
            // The server might accept the first play, but on the client side the first play would have been overwritten
            // by the second play. The second play would be evaluated by the server on the next turn.
            // This would obviously cause the server and client to go out of sync and everything would break.
            if(thisPlayer != turnPlayer) {
                message = "It's not your turn.";
            } else if(thisPlayer.getPlay().isPresent()) {
                message = "Your play is being validated by the server...";
                return;
            }

            RenderableCardList<RenderableHeartsCard> selectedCards = thisPlayerHand.stream()
                    .filter(RenderableHeartsCard::isSelected)
                    .collect(Collectors.toCollection(RenderableCardList::new));
            if(selectedCards.size() == 1) {
                RenderableHeartsCard card = selectedCards.get(0);
                ClientPacket packet = new ClientPacket(ClientCode.PLAY);
                packet.data.put("play", card.getCardNum());
                try {
                    client.sendPacket(packet);
                } catch(IOException e) {
                    message = "There was an error while trying to contact the server... try again";
                    return;
                }
                card.setSelected(false).setSelectable(false);
                thisPlayerHand.remove(card);
                thisPlayer.setPlay(card);
                message = "Sending play...";
            } else {
                message = "You must select 1 card to play";
            }
        }
    }
}
