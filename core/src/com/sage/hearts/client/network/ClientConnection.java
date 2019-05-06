package com.sage.hearts.client.network;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaSocketImpl;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.client.HeartsGame;
import com.sage.hearts.server.network.ServerPacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientConnection extends Thread {
    final int port;
    final String serverIP;
    private final HeartsGame game;
    private final String playerName;

    private final Socket socket;
    private final DataOutputStream output;
    private final DataInputStream input;

    private final Queue<ServerPacket> packetQueue = new LinkedBlockingQueue<>();

    private volatile boolean quit = false;

    ClientConnection(int port, String serverIP, String playerName, HeartsGame game) {
        this.port = port;
        this.serverIP = serverIP;
        this.game = game;
        this.playerName = playerName;

        SocketHints socketHints = new SocketHints();
        socketHints.socketTimeout = 0;
        socket = new NetJavaSocketImpl(Net.Protocol.TCP, serverIP, port, socketHints);
        output = new DataOutputStream(socket.getOutputStream());
        input = new DataInputStream(socket.getInputStream());

        Runtime.getRuntime().addShutdownHook(new Thread(socket::dispose));
    }

    @Override
    public void run() {
        while(!quit) {
            ServerPacket packet;
            try {
                int packetSize = input.readInt();
                packet = ServerPacket.fromBytes(input.readNBytes(packetSize));
            } catch(EOFException e) {
                quit();
                return;
            } catch(IOException | SerializationException e) {
                e.printStackTrace();
                continue;
            }
            synchronized(packetQueue) {
                packetQueue.add(packet);
            }
        }
    }

    public void quit() {
        quit = true;
        socket.dispose();
    }

    public void sendPacket(ClientPacket packet) throws IOException {
        byte[] packetBytes = packet.toBytes();
        output.writeInt(packetBytes.length);
        output.write(packetBytes);
        output.flush();
    }

    public Optional<ServerPacket> getPacket() {
        synchronized(packetQueue) {
            return Optional.ofNullable(packetQueue.poll());
        }
    }

    public boolean pingServer() {
        try {
            sendPacket(ClientPacket.pingPacket());
            return true;
        } catch(IOException e) {
            return false;
        }
    }
}