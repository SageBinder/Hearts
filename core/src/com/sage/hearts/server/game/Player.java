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

    public final CardList<HeartsCard> hand = new CardList<>();
    public final CardList<HeartsCard> collectedPointCards = new CardList<>();
    public HeartsCard play;
    public int accumulatedPoints = 0;

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
            while(true) {
                ClientPacket packet;
                try {
                    int packetSize = input.readInt();
                    packet = ClientPacket.fromBytes(input.readNBytes(packetSize));
                    initialPacketHandler(packet);
                } catch(IOException e) {
                    socket.dispose();
                    packetQueue.add(new PlayerDisconnectedItem());
                    e.printStackTrace(); // TODO: Should maybe do something else here?
                    return; // I think IOException means the player disconnected, so the queue filler thread can exit
                } catch(SerializationException e) {
                    e.printStackTrace();
                    continue;
                }

                try {
                    packetQueue.add(packet);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        packetQueueFillerThread.start();
    }

    private void initialPacketHandler(final ClientPacket packet) {
        if(packet.networkCode != null) {
            PacketHandler handler = initialPacketHandlers.get(packet.networkCode);
            if(handler != null) {
                handler.handle(packet);
            }
        }
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

    public void sendPacket(final ServerPacket packet) throws SerializationException, PlayerDisconnectedException {
        if(!socketIsConnected()) {
            throw new PlayerDisconnectedException(this);
        }
        try {
            byte[] packetBytes = packet.toBytes();
            output.writeInt(packetBytes.length);
            output.write(packetBytes);
            output.flush();
        } catch(IOException e) {
            socket.dispose();
            throw new PlayerDisconnectedException(this);
        }
    }

    public Optional<ClientPacket> getPacket() throws PlayerDisconnectedException {
        ClientPacket ret = packetQueue.poll();
        if(ret instanceof PlayerDisconnectedItem) {
            throw new PlayerDisconnectedException(this);
        } else {
            return Optional.ofNullable(ret);
        }
    }

    public ClientPacket waitForPacket() throws InterruptedException, PlayerDisconnectedException {
        ClientPacket ret = packetQueue.take();
        if(ret instanceof PlayerDisconnectedItem) {
            throw new PlayerDisconnectedException(this);
        } else if(ret instanceof InterruptItem) {
            throw new InterruptedException();
        } else {
            return packetQueue.take();
        }
    }

    public Optional<ClientPacket> waitForPacket(long timeout, TimeUnit unit)
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

    public boolean socketIsConnected() {
        return socket.isConnected();
    }

    public void ping() throws SerializationException, PlayerDisconnectedException {
        sendPacket(ServerPacket.pingPacket());
    }

    public int getAccumulatedPoints() {
        return accumulatedPoints;
    }

    public void setAccumulatedPoints(int accumulatedPoints) {
        this.accumulatedPoints = accumulatedPoints;
    }

    public synchronized void interruptPacketWaiting() {
        packetQueue.add(new InterruptItem());
    }

    private abstract class PoisonQueueItem extends ClientPacket {

    }

    private class PlayerDisconnectedItem extends PoisonQueueItem {

    }

    private class InterruptItem extends PoisonQueueItem {

    }

    public interface PacketHandler {
        void handle(final ClientPacket packet);
    }
}