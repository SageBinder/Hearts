package com.sage.hearts.client.network;

import com.sage.hearts.utils.network.Packet;

public class ClientPacket extends Packet<ClientCode> {
    public ClientPacket() {
        super();
    }

    public ClientPacket(ClientCode networkCode) {
        super(networkCode);
    }

    public static ClientPacket fromBytes(byte[] bytes) throws ClassCastException {
        return (ClientPacket)Packet.fromBytes(bytes);
    }
}
