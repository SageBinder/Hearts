package com.sage.hearts.server;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.NetJavaServerSocketImpl;
import com.badlogic.gdx.net.ServerSocketHints;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.sage.hearts.client.network.ClientCode;
import com.sage.hearts.server.game.GameState;
import com.sage.hearts.server.game.Player;
import com.sage.hearts.server.game.RoundRunner;
import com.sage.hearts.server.network.MultiplePlayersDisconnectedException;
import com.sage.hearts.server.network.PlayerDisconnectedException;
import com.sage.hearts.server.network.ServerCode;
import com.sage.hearts.server.network.ServerPacket;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {
    public static final int NUM_PLAYERS_TO_START = 4;
    public static final long PING_PERIOD = 1000; // In milliseconds
    public static final int MAX_PLAYER_NAME_LENGTH = 16;

    public final int port;

    private final GameState gameState = new GameState();

    private volatile boolean startRoundFlag = false; // This flag is set by the player communication thread
    private final Object startRoundObj = new Object();

    private Player host = null;
    private NetJavaServerSocketImpl serverSocket;

    private volatile boolean closed = false;

    public Server(int port) {
        this.port = port;

        ServerSocketHints hints = new ServerSocketHints();
        hints.acceptTimeout = 0;
        serverSocket = new NetJavaServerSocketImpl(Net.Protocol.TCP, port, hints);

        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public void run() {
        Thread connectionAcceptorThread = new Thread(connectionAcceptor);
        connectionAcceptorThread.start();

        Timer pingerTimer = new Timer();
        pingerTimer.scheduleAtFixedRate(pruneDisconnectedPlayersTask, PING_PERIOD, PING_PERIOD);

        outerLoop:
        while(!closed) {
            // This inner loop breaks with gameState.lock still locked when the round is being started
            while(true) {
                if(startRoundFlag) {
                    gameState.lock.lock();
                    if(gameState.players.size() == NUM_PLAYERS_TO_START) {
                        gameState.roundStarted = true;
                        break;
                    } else if(closed){
                        break outerLoop;
                    } else {
                        startRoundFlag = false;
                        host.sendPacket(new ServerPacket(ServerCode.COULD_NOT_START_GAME));
                        gameState.lock.unlock();
                    }
                }
                try {
                    synchronized(startRoundObj) {
                        startRoundObj.wait();
                    }
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                RoundRunner.playRound(gameState);
            } catch(PlayerDisconnectedException | MultiplePlayersDisconnectedException e) {
                gameState.players.removeDisconnectedPlayers();
                try {
                    gameState.players.sendPacketToAll(new ServerPacket(ServerCode.PLAYER_DISCONNECTED));
                } catch(MultiplePlayersDisconnectedException e1) {
                    gameState.players.removeDisconnectedPlayers();
                }
            } finally {
                sendPlayersToAllUntilNoDisconnections();
                startRoundFlag = false;
                gameState.roundStarted = false;
                gameState.lock.unlock();
            }
        }

        try {
            serverSocket.dispose();
        } catch(GdxRuntimeException e) {
            Gdx.app.log("Server.run()", "Encountered GdxRuntimeException when trying to dispose of socket");
        }
        pingerTimer.cancel();
    }

    private void sendPlayersToAllUntilNoDisconnections() {
        while(true) {
            try {
                gameState.players.sendPlayersToAll();
            } catch(MultiplePlayersDisconnectedException e1) {
                gameState.players.removeDisconnectedPlayers();
                continue;
            }
            break;
        }
    }

    private void setInitialPacketHandlersForPlayer(Player player) {
        player.setInitialPacketHandlerForCode(ClientCode.NAME, packet -> {
            if(packet.data.get("name") instanceof String && !gameState.roundStarted) {
                String sentName = (String)packet.data.get("name");
                player.setName(sentName.substring(0, Math.min(sentName.length(), MAX_PLAYER_NAME_LENGTH)));
                sendPlayersToAllUntilNoDisconnections();
            } else {
                player.sendPacket(new ServerPacket(ServerCode.UNSUCCESSFUL_NAME_CHANGE));
            }
            return false; // This packet does not need to be put into the player's packetQueue
        });
        player.setInitialPacketHandlerForCode(ClientCode.START_GAME, packet -> {
            if(host == player && !startRoundFlag) {
                startRoundFlag = true; // This simply requests the round runner thread to start; it does not force the round to start
                synchronized(startRoundObj) {
                    startRoundObj.notify();
                }
            } else {
                player.sendPacket(new ServerPacket(ServerCode.COULD_NOT_START_GAME));
            }
            return false; // This packet does not need to be put into the player's packetQueue
        });
        player.setInitialPacketHandlerForCode(ClientCode.PLAYER_POINTS_CHANGE, packet -> {
            if(gameState.lock.tryLock()
                    && packet.data.get("player") instanceof Integer
                    && packet.data.get("pointschange") instanceof Integer) {
                gameState.players.getByPlayerNum((Integer)packet.data.get("player")).ifPresent(p -> {
                    p.incrementPointsOffset((Integer)packet.data.get("pointschange"));
                    ServerPacket newPlayerPointsPacket = new ServerPacket(ServerCode.NEW_PLAYER_POINTS);
                    newPlayerPointsPacket.data.put("player", p.getPlayerNum());
                    newPlayerPointsPacket.data.put("points", p.getAccumulatedPoints());
                    gameState.players.sendPacketToAll(newPlayerPointsPacket);
                });
                gameState.lock.unlock();
            }
            return false;
        });
        player.setInitialPacketHandlerForCode(ClientCode.RESET_PLAYER_POINTS, packet -> {
            if(gameState.lock.tryLock() && packet.data.get("player") instanceof Integer) {
                gameState.players.getByPlayerNum((Integer)packet.data.get("player")).ifPresent(p -> {
                    p.setPointsOffset(0);
                    ServerPacket newPlayerPointsPacket = new ServerPacket(ServerCode.NEW_PLAYER_POINTS);
                    newPlayerPointsPacket.data.put("player", p.getPlayerNum());
                    newPlayerPointsPacket.data.put("points", p.getAccumulatedPoints());
                    gameState.players.sendPacketToAll(newPlayerPointsPacket);
                });
                gameState.lock.unlock();
            }
            return false;
        });
        player.setInitialPacketHandlerForCode(ClientCode.PING, packet -> false);
    }

    public void close() {
        try {
            // We drop every player connection which should (?) make RoundRunner.playRound() throw a PlayerDisconnectedException.
            // When the main server loop repeats, it will query the value of closed and will exit.
            gameState.players.forEach(Player::dropConnection);
            closed = true;

            // We need to notify startRoundObj in case the server is currently waiting for the round to start
            synchronized(startRoundObj) {
                startRoundObj.notify();
            }
        } finally {
            try { // No matter what, serverSocket should be disposed
                serverSocket.dispose();
            } catch(GdxRuntimeException e) {
                Gdx.app.log("Server.close()", "Encountered GdxRuntimeException when trying to dispose of socket");
            }
        }
    }

    Runnable connectionAcceptor = new Runnable() {
        @Override
        public void run() {
            while(!closed) {
                Player newPlayer;
                try {
                    newPlayer = new Player(0, serverSocket.accept(null));
                } catch(GdxRuntimeException e) {
                    continue;
                }
                // If round is started, we can immediately end the connection
                if(gameState.roundStarted) { // gameState.roundStarted is volatile so we don't need to lock
                    newPlayer.sendPacket(new ServerPacket(ServerCode.CONNECTION_DENIED));
                    continue;
                }

                // If round has not started, attempt to acquire the gameState lock and add the new player
                boolean acquiredLock;
                try {
                    // Acquiring the lock needs a timeout because we want this thread to continue even when
                    // the round has started (the lock will not be acquirable while the round is running)
                    acquiredLock = gameState.lock.tryLock(10, TimeUnit.SECONDS);
                } catch(InterruptedException e) {
                    newPlayer.sendPacket(new ServerPacket(ServerCode.CONNECTION_DENIED));
                    continue;
                }

                // If the lock was not acquired, the lock was probably acquired by the round runner thread. (?)
                // If the lock was acquired, the round must not be running.
                // Even if the round isn't running, new players cannot be accepted if the maximum number of players was reached
                if(!acquiredLock || gameState.players.size() == NUM_PLAYERS_TO_START) {
                    newPlayer.sendPacket(new ServerPacket(ServerCode.CONNECTION_DENIED));
                    continue;
                }

                if(host == null) {
                    host = newPlayer;
                    host.setHost(true);
                }
                newPlayer.setPlayerNum(gameState.players.size());
                setInitialPacketHandlersForPlayer(newPlayer);
                newPlayer.sendPacket(new ServerPacket(ServerCode.CONNECTION_ACCEPTED));
                gameState.players.add(newPlayer);
                sendPlayersToAllUntilNoDisconnections();

                gameState.lock.unlock();
            }
        }
    };

    private TimerTask pruneDisconnectedPlayersTask = new TimerTask() {
        @Override
        public void run() {
            // We don't need a timeout on this lock because it doesn't need to run while the round is running.
            // We need to tryLock() instead of lock() because the Timer will try to catch up with missed executions.
            if(gameState.lock.tryLock()) {
                if(gameState.players.removeDisconnectedPlayers()) {
                    sendPlayersToAllUntilNoDisconnections();
                }
                gameState.lock.unlock();
            }
        }
    };
}
