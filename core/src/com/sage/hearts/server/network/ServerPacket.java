package com.sage.hearts.server.network;

import com.sage.hearts.utils.network.Packet;

public class ServerPacket extends Packet<ServerCode> {
    public ServerPacket() {
        super();
    }

    public ServerPacket(ServerCode networkCode) {
        super(networkCode);
    }

    public static ServerPacket fromBytes(byte[] bytes) throws ClassCastException {
        return (ServerPacket)Packet.fromBytes(bytes);
    }
}
