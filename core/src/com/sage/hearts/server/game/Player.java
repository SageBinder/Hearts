package com.sage.hearts.server.game;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
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
    private Map<ClientCode, PacketHandler> initialPacketHandlers = new HashMap<>();

    public Player(int playerNum, Socket socket) {
        this.socket = socket;
        this.playerNum = playerNum;
        this.name = "Player " + playerNum;

        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());

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
                    dropConnection();
                    e.printStackTrace();
                    return; // I think IOException means the player disconnected, so the queue filler thread can exit
                } catch(SerializationException | IllegalArgumentException | OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
        });
        packetQueueFillerThread.start();
    }

    private synchronized boolean initialPacketHandler(final ClientPacket packet) {
        if(packet.networkCode != null) {
            PacketHandler handler = initialPacketHandlers.get(packet.networkCode);
            if(handler != null) {
                return handler.handle(packet);
            }
        }
        return true;
    }

    // PacketHandler code will be run by packetQueueFillerThread
    public synchronized void setInitialPacketHandlerForCode(ClientCode code, PacketHandler handler) {
        initialPacketHandlers.put(code, handler);
    }

    public synchronized void resetInitialPacketHandlers() {
        initialPacketHandlers.clear();
    }

    public synchronized void resetForNewRound() {
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

    public synchronized Optional<ClientPacket> getPacket() throws PlayerDisconnectedException {
        ClientPacket ret = packetQueue.poll();
        if(ret instanceof PlayerDisconnectedItem) {
            throw new PlayerDisconnectedException(this);
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
            return packetQueue.take();
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
                return Optional.ofNullable(packetQueue.poll(timeout, unit));
            }
        }
    }

    public synchronized int getPlayerNum() {
        return playerNum;
    }

    public synchronized void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name) {
        this.name = name;
    }

    public synchronized boolean isHost() {
        return isHost;
    }

    public synchronized void setHost(boolean host) {
        this.isHost = host;
    }

    public synchronized boolean socketIsConnected() {
        return socket.isConnected();
    }

    public synchronized void ping() throws SerializationException, PlayerDisconnectedException {
        sendPacket(ServerPacket.pingPacket());
    }

    public synchronized int getAccumulatedPoints() {
        return accumulatedPoints + pointsOffset;
    }

    public synchronized int getAccumulatedPointsNoOffset() {
        return accumulatedPoints;
    }

    public synchronized void setAccumulatedPoints(int accumulatedPoints) {
        this.accumulatedPoints = accumulatedPoints;
    }

    public synchronized int getPointsOffset() {
        return pointsOffset;
    }

    public synchronized void setPointsOffset(int pointsOffset) {
        this.pointsOffset = pointsOffset;
    }

    public synchronized void incrementPointsOffset(int inc) {
        pointsOffset += inc;
    }

    public synchronized void interruptPacketWaiting() {
        packetQueue.add(new InterruptItem());
    }

    public synchronized void dropConnection() {
        socket.dispose();
        packetQueue.add(new PlayerDisconnectedItem());
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