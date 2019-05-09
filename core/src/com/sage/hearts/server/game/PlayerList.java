package com.sage.hearts.server.game;

import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.server.network.MultiplePlayersDisconnectedException;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

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

    public void sendPacketToAllExcluding(ServerPacket packet, Player...players) {
        Stream<Player> playerStream = Arrays.stream(players);
        for(Player p : this) {
            if(playerStream.noneMatch(p1 -> p1.getPlayerNum() == p.getPlayerNum())) {
                p.sendPacket(packet);
            }
        }
    }

    public boolean removeDisconnectedPlayers() {
        return removeIf(player -> !player.socketIsConnected());
    }

    public boolean pingAllAndRemoveDisconnected() {
        try {
            sendPacketToAll(ServerPacket.pingPacket());
        } catch(MultiplePlayersDisconnectedException e) {
            return removeAll(e.getDisconnectedPlayers());
        }
        return false;
    }
}
