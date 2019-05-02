package com.sage.hearts.server.network;

import com.sage.hearts.utils.network.Packet;

import java.io.Serializable;

public class ServerPacket extends Packet<ServerCode> implements Serializable {
    public ServerPacket() {
        super();
    }

    public ServerPacket(ServerCode networkCode) {
        super(networkCode);
    }
}
