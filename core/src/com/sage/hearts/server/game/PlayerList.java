package com.sage.hearts.server.game;

import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.server.network.MultiplePlayersDisconnectedException;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerCode;
import com.sage.hearts.server.network.ServerPacket;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PlayerList extends ArrayList<Player> {
    public PlayerList() {
        super();
    }

    public PlayerList(Collection<? extends Player> other) {
        super(other);
    }

    public void sendPacketToAll(ServerPacket packet) throws MultiplePlayersDisconnectedException {
        PlayerList disconnectedPlayers = null;
        for(Player p : this) {
            try {
                p.sendPacket(packet);
            } catch(PlayerDisconnectedException e) {
                if(disconnectedPlayers == null) {
                    disconnectedPlayers = new PlayerList();
                }
                disconnectedPlayers.add(p);
            } catch(SerializationException e) {
                e.printStackTrace();
            }
        }
        if(disconnectedPlayers != null) {
            throw new MultiplePlayersDisconnectedException(disconnectedPlayers);
        }
    }

    public void sendPacketToAllExcluding(ServerPacket packet, Player...excluded) throws MultiplePlayersDisconnectedException {
        PlayerList newPlayerList = new PlayerList(this);
        newPlayerList.removeAll(List.of(excluded));
        newPlayerList.sendPacketToAll(packet);
    }

    public void sendPlayersToAll() throws MultiplePlayersDisconnectedException {
        if(isEmpty()) {
            return;
        }

        HashMap<Integer, String> players =
                stream().collect(Collectors.toMap(Player::getPlayerNum, Player::getName, (a, b) -> b, HashMap::new));
        HashMap<Integer, Integer> accumulatedPoints =
                stream().collect(Collectors.toMap(Player::getPlayerNum, Player::getAccumulatedPoints, (a, b) -> b, HashMap::new));
        Integer hostNum = stream().filter(Player::isHost).findFirst().orElse(this.get(0)).getPlayerNum();

        // Pretty much copy/pasted code from sendPacketToAll()
        PlayerList disconnectedPlayers = null;
        for(Player p : this) {
            ServerPacket playersPacket = new ServerPacket(ServerCode.WAIT_FOR_PLAYERS);
            playersPacket.data.put("players", players);
            playersPacket.data.put("points", accumulatedPoints);
            playersPacket.data.put("host", hostNum);
            playersPacket.data.put("you", p.getPlayerNum());
            try {
                p.sendPacket(playersPacket);
            } catch(PlayerDisconnectedException e) {
                if(disconnectedPlayers == null) {
                    disconnectedPlayers = new PlayerList();
                }
                disconnectedPlayers.add(p);
            } catch(SerializationException e) {
                e.printStackTrace();
            }
        }
        if(disconnectedPlayers != null) {
            throw new MultiplePlayersDisconnectedException(disconnectedPlayers);
        }
    }

    public boolean removeDisconnectedPlayers() {
        boolean ret = removeIf(player -> !player.socketIsConnected());
        squashPlayerNums();
        return ret;
    }

    public boolean pingAllAndRemoveDisconnected() {
        try {
            sendPacketToAll(ServerPacket.pingPacket());
        } catch(MultiplePlayersDisconnectedException e) {
            boolean ret = removeAll(e.getDisconnectedPlayers());
            squashPlayerNums();
            return ret;
        }
        return false;
    }

    public Optional<Player> getByPlayerNum(int playerNum) {
        for(Player p : this) {
            if(p.getPlayerNum() == playerNum) {
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }

    public void squashPlayerNums() {
        IntStream.range(0, size()).forEach(i -> get(i).setPlayerNum(i));
    }
}
