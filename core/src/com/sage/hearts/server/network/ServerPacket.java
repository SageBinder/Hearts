package com.sage.hearts.server.network;

import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.utils.network.Packet;

public class ServerPacket extends Packet<ServerCode> {
    public ServerPacket() {
        super();
    }

    public ServerPacket(ServerCode networkCode) {
        super(networkCode);
    }

    public static ServerPacket fromBytes(byte[] bytes) throws SerializationException {
        try {
            return (ServerPacket)Packet.fromBytes(bytes);
        } catch(ClassCastException e) {
            throw new SerializationException();
        }
    }

    public static ServerPacket pingPacket() {
        return new ServerPacket(ServerCode.PING);
    }
}
