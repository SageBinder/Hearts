package com.sage.hearts.server;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaServerSocketImpl;
import com.badlogic.gdx.net.ServerSocketHints;
import com.sage.hearts.server.game.Player;
import com.sage.hearts.server.game.PlayerList;

public class Server extends Thread {
    public final int port;

    public final int maxPlayers = 4;
    private final PlayerList connectedPlayers = new PlayerList();

    private Player host;
    private NetJavaServerSocketImpl serverSocket;

    public Server(int port, int numPlayers) {
        this.port = port;

        ServerSocketHints hints = new ServerSocketHints();
        hints.acceptTimeout = 0;
        serverSocket = new NetJavaServerSocketImpl(Net.Protocol.TCP, port, hints);
    }

    @Override
    public void run() {

    }
}