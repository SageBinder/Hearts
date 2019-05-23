package com.sage.hearts.server.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Player {
    private int playerNum;
    private String name;
    private boolean isHost = false;

    public final CardList<HeartsCard> hand = new CardList<>();
    public final CardList<HeartsCard> collectedPointCards = new CardList<>();
    public HeartsCard play;
    public int accumulatedPoints = 0;
    public int pointsOffset = 0; // Offset provided by host for manual point changing

    private final Socket socket;
    private final DataOutputStream output;
    private final DataInputStream input;
    private final BlockingQueue<ClientPacket> packetQueue = new LinkedBlockingQueue<>();

    private final Thread packetQueueFillerThread;
    private final Map<ClientCode, PacketHandler> initialPacketHandlers = new ConcurrentHashMap<>();

    public Player(int playerNum, Socket socket) {
        this.socket = socket;
        this.playerNum = playerNum;
        this.name = "New Player";

        output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

        packetQueueFillerThread = new Thread(() -> {
            while(socketIsConnected()) {
                try {
                    int packetSize = input.readInt();
                    ClientPacket packet = ClientPacket.fromBytes(input.readNBytes(packetSize));
                    if(initialPacketHandler(packet)) {
                        try {
                            packetQueue.add(packet);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch(IOException e) {
                    Gdx.app.log("packQueueFillerThread for player " + name,
                            "Encountered IOException, dropping connection");
                    dropConnection();
                    return; // I think IOException means the player disconnected, so the queue filler thread can exit
                } catch(SerializationException | IllegalArgumentException | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        });
        packetQueueFillerThread.start();
    }

    private boolean initialPacketHandler(final ClientPacket packet) {
        if(packet.networkCode != null) {
            PacketHandler handler = initialPacketHandlers.get(packet.networkCode);
            if(handler != null) {
                return handler.handle(packet);
            }
        }
        return true;
    }

    // PacketHandler code will be run by packetQueueFillerThread
    public void setInitialPacketHandlerForCode(ClientCode code, PacketHandler handler) {
        initialPacketHandlers.put(code, handler);
    }

    public void resetInitialPacketHandlers() {
        initialPacketHandlers.clear();
    }

    public void resetForNewRound() {
        hand.clear();
        collectedPointCards.clear();
    }

    public synchronized void sendPacket(final ServerPacket packet) throws SerializationException, PlayerDisconnectedException {
        if(!socketIsConnected()) {
            throw new PlayerDisconnectedException(this);
        }
        try {
            byte[] packetBytes = packet.toBytes();
            output.writeInt(packetBytes.length);
            output.write(packetBytes);
            output.flush();
        } catch(IOException e) {
            dropConnection();
            throw new PlayerDisconnectedException(this);
        }
    }

    public synchronized Optional<ClientPacket> getPacket() throws InterruptedException, PlayerDisconnectedException {
        ClientPacket ret = packetQueue.poll();
        if(ret instanceof PlayerDisconnectedItem) {
            throw new PlayerDisconnectedException(this);
        } else if(ret instanceof InterruptItem) {
            throw new InterruptedException();
        } else {
            return Optional.ofNullable(ret);
        }
    }

    public synchronized ClientPacket waitForPacket() throws InterruptedException, PlayerDisconnectedException {
        ClientPacket ret = packetQueue.take();
        if(ret instanceof PlayerDisconnectedItem) {
            throw new PlayerDisconnectedException(this);
        } else if(ret instanceof InterruptItem) {
            throw new InterruptedException();
        } else {
            return ret;
        }
    }

    public synchronized Optional<ClientPacket> waitForPacket(long timeout, TimeUnit unit)
            throws InterruptedException, PlayerDisconnectedException {
        if(timeout <= 0) {
            return Optional.of(waitForPacket());
        } else {
            ClientPacket ret = packetQueue.poll(timeout, unit);
            if(ret instanceof PlayerDisconnectedItem) {
                throw new PlayerDisconnectedException(this);
            } else if(ret instanceof InterruptItem) {
                throw new InterruptedException();
            } else {
                return Optional.ofNullable(ret);
            }
        }
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        this.isHost = host;
    }

    public boolean socketIsConnected() {
        return socket.isConnected();
    }

    public int getAccumulatedPoints() {
        return accumulatedPoints + pointsOffset;
    }

    public int getAccumulatedPointsNoOffset() {
        return accumulatedPoints;
    }

    public void setAccumulatedPoints(int accumulatedPoints) {
        this.accumulatedPoints = accumulatedPoints;
    }

    public int getPointsOffset() {
        return pointsOffset;
    }

    public void setPointsOffset(int pointsOffset) {
        this.pointsOffset = pointsOffset;
    }

    public void incrementPointsOffset(int inc) {
        pointsOffset += inc;
    }

    public void interruptPacketWaiting() {
        packetQueue.add(new InterruptItem());
    }

    public void dropConnection() {
        packetQueue.add(new PlayerDisconnectedItem());
        socket.dispose();
    }

    private abstract class PoisonQueueItem extends ClientPacket {

    }

    private class PlayerDisconnectedItem extends PoisonQueueItem {

    }

    private class InterruptItem extends PoisonQueueItem {

    }

    public interface PacketHandler {
        // Returns whether or not the packet should be propagated to the packetQueue
        boolean handle(final ClientPacket packet);
    }
}