package com.sage.hearts.server.game;

import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.client.network.ClientPacket;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerPacket;
import com.sage.hearts.utils.card.CardList;
import com.sage.hearts.utils.hearts.HeartsCard;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    private int accumulatedPoints = 0;

    private final Socket socket;
    private final DataOutputStream output;
    private final DataInputStream input;
    private final BlockingQueue<ClientPacket> packetQueue = new LinkedBlockingQueue<>();

    private final Thread packetQueueFillerThread;

    public Player(int playerNum, String name, Socket socket) {
        this.socket = socket;
        this.playerNum = playerNum;
        this.name = name;

        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());

        packetQueueFillerThread = new Thread(() -> {
            while(true) {
                ClientPacket packet;
                try {
                    int packetSize = input.readInt();
                    packet = ClientPacket.fromBytes(input.readNBytes(packetSize));
                } catch(IOException e) {
                    socket.dispose();
                    packetQueue.add(new PoisonQueueItem());
                    e.printStackTrace(); // TODO: Should maybe do something else here?
                    return; // I think IOException means the player disconnected, so the queue filler thread can exit
                } catch(SerializationException e) {
                    e.printStackTrace();
                    continue;
                }
                synchronized(packetQueue) {
                    try {
                        packetQueue.add(packet);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        packetQueueFillerThread.start();
    }

    public void resetForNewRound() {
        hand.clear();
        collectedPointCards.clear();
    }

    public void sendPacket(ServerPacket packet) throws SerializationException, PlayerDisconnectedException {
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
        synchronized(packetQueue) {
            ClientPacket ret = packetQueue.poll();
            if(ret instanceof PoisonQueueItem) {
                throw new PlayerDisconnectedException(this);
            } else {
                return Optional.ofNullable(ret);
            }
        }
    }

    public ClientPacket waitForPacket() throws InterruptedException, PlayerDisconnectedException {
        synchronized(packetQueue) {
            ClientPacket ret = packetQueue.take();
            if(ret instanceof PoisonQueueItem) {
                throw new PlayerDisconnectedException(this);
            } else {
                return packetQueue.take();
            }
        }
    }

    public Optional<ClientPacket> waitForPacket(long timeout, TimeUnit unit)
            throws InterruptedException, PlayerDisconnectedException {
        synchronized(packetQueue) {
            if(timeout <= 0) {
                return Optional.of(waitForPacket());
            } else {
                ClientPacket ret = packetQueue.poll(timeout, unit);
                if(ret instanceof PoisonQueueItem) {
                    throw new PlayerDisconnectedException(this);
                } else {
                    return Optional.ofNullable(packetQueue.poll(timeout, unit));
                }
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

    private class PoisonQueueItem extends ClientPacket {

    }
}