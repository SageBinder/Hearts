package com.sage.hearts.client.game;

import com.badlogic.gdx.Gdx;
import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.client.network.ClientConnection;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.client.network.LostConnectionToServerException;
import com.sage.hearts.server.network.ServerCode;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.renderable.RenderableCard;
import com.sage.hearts.utils.renderable.RenderableCardList;
import com.sage.hearts.utils.renderable.RenderableHand;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameState {
    private Updater updater = new Updater();

    private final HeartsGame game;

    public final RenderablePlayer[] players = new RenderablePlayer[4];

    public final HashMap<Integer, Integer> warheadMap = new HashMap<>();

    public RenderablePlayer turnPlayer;
    public RenderablePlayer leadingPlayer;

    public RenderablePlayer thisPlayer;
    public final RenderableHand<RenderableHeartsCard> thisPlayerHand = new RenderableHand<>();

    public ServerCode lastServerCode;

    public String message = "";
    public String buttonText = "";

    public boolean heartsBroke = false;

    public GameState(HeartsGame game) {
        this.game = game;
    }

    public boolean update(ClientConnection client) {
        if(client == null) {
            return false;
        }

        boolean didUpdate = false;
        Optional<ServerPacket> p;
        try {
            while((p = client.getPacket()).isPresent()) {
                applyUpdate(p.get());
                didUpdate = true;
            }
        } catch(LostConnectionToServerException e) {
            updater.lostConnectionToServer();
        }
        return didUpdate;
    }

    private void applyUpdate(ServerPacket updatePacket) {
        updater.update(updatePacket.networkCode, updatePacket.data);
    }

    private Optional<RenderablePlayer> getPlayerByPlayerNum(int playerNum) {
        return Arrays.stream(players).filter(p -> p.getPlayerNum() == playerNum).findFirst();
    }

    private class Updater {
        private Map data = null;
        private void update(ServerCode serverCode, Map data) {
            lastServerCode = serverCode;
            this.data = data;
            try {
                switch(lastServerCode) {
                    // General codes
                case PING:
                    ping();
                    break;
                case CONNECTION_ACCEPTED:
                    connectionAccepted();
                    break;
                case CONNECTION_DENIED:
                    connectionDenied();
                    break;
                case COULD_NOT_START_GAME:
                    couldNotStartGame();
                    break;
                case WAIT_FOR_PLAYERS:
                    waitForPlayers();
                    break;

                    // Trick codes:
                case TRICK_START:
                    trickStart();
                    break;

                    // Round codes:
                case ROUND_START:
                    roundStart();
                    break;
                case PLAYER_DISCONNECTED:
                    playerDisconnected();
                    break;
                }
            } catch(ClassCastException e) {
                Gdx.app.log("Updater.update()",
                        "Oh shit encountered ClassCastException in Updater.update(), this is VERY BAD");
                e.printStackTrace();
            }
        }

        // --- GENERAL CODES ---
        // Just here in case anything needs to be done on ping (and to keep the switch pattern)
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
            message = "A player has disconnected!";
            game.showLobbyScreen();
        }

        private void couldNotStartGame() {
            message = "Error: cannot start game. Either there aren't enough players or the game is already running.";
        }

        private void waitForPlayers() {
            var newPlayersMap = (Map<Integer, String>)data.get("players");
            var accumulatedPointsMap = (Map<Integer, Integer>)data.get("points");
            int clientPlayerNum = (Integer)(data.get("you"));
            int hostNum = (Integer)(data.get("host"));

            for(int i = 0; i < GameState.this.players.length; i++) {
                if(players[i] != null) {
                    players[i].disposeCards();
                    players[i] = null;
                }
            }

            Iterator<Integer> keyIter = newPlayersMap.keySet().iterator();
            for(int i = 0; i < players.length && keyIter.hasNext(); i++) {
                Integer playerNum = keyIter.next();
                players[i] = new RenderablePlayer(playerNum, newPlayersMap.get(playerNum));
                players[i].setHost(playerNum == hostNum);
                players[i].setIsClientPlayer(playerNum == clientPlayerNum);
                players[i].setAccumulatedPoints(accumulatedPointsMap.getOrDefault(playerNum, 0));
            }
        }

        // --- TRICK CODES ---
        private void trickStart() {
            // TODO trickStart()
        }

        // --- ROUND CODES ---
        private void roundStart() {
            Arrays.stream(players).forEach(p -> {
                if(p != null) {
                    p.disposeCards();
                }
            });
            turnPlayer = null;
            leadingPlayer = null;
            heartsBroke = false;
            message = "";
            buttonText = "";

            warheadMap.clear();
            warheadMap.putAll((HashMap<Integer, Integer>)data.get("warheadmap"));

            int[] playerOrder = (int[])data.get("playerorder");
            RenderablePlayer[] newPlayerArr = new RenderablePlayer[players.length];
            for(int i = 0; i < newPlayerArr.length; i++) {
                newPlayerArr[i] = getPlayerByPlayerNum(playerOrder[i]).orElseThrow(InvalidServerPacketException::new);
            }
            for(int i = 0; i < newPlayerArr.length; i++) {
                players[i] = newPlayerArr[i];
            }

            game.showGameScreen();
        }

        private void lostConnectionToServer() {
            message = "The connection to the host has been lost.";
            game.showStartScreen();
        }
    }

    public class Actions {
        public void sendWarheads(ClientConnection client) {
            RenderableCardList<RenderableHeartsCard> selectedCards = thisPlayerHand.stream()
                    .filter(RenderableCard::isSelected)
                    .collect(Collectors.toCollection(RenderableCardList::new));
            if(selectedCards.size() == 3) {
                ClientPacket packet = new ClientPacket();
                packet.data.put("warheads", selectedCards.toCardNumList());
                try {
                    client.sendPacket(packet);
                } catch(IOException e) {
                    message = "There was an error while trying to contact the server... try again";
                    return;
                }
                thisPlayerHand.removeAll(selectedCards);
                message = "Sending warheads...";
            } else {
                message = "You must select 3 cards to send";
            }
        }

        public void sendPlay(ClientConnection client) {
            RenderableCardList<RenderableHeartsCard> selectedCards = thisPlayerHand.stream()
                    .filter(RenderableCard::isSelected)
                    .collect(Collectors.toCollection(RenderableCardList::new));
            if(selectedCards.size() == 1) {
                ClientPacket packet = new ClientPacket();
                packet.data.put("play", selectedCards.get(0).getCardNum());
                try {
                    client.sendPacket(packet);
                } catch(IOException e) {
                    message = "There was an error while trying to contact the server... try again";
                    return;
                }
                thisPlayerHand.remove(selectedCards.get(0));
                message = "Sending play...";
            } else {
                message = "You must select 1 card to play";
            }
        }
    }
}
