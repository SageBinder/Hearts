package com.sage.hearts.client.network;

import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.utils.network.Packet;

public class ClientPacket extends Packet<ClientCode> {
    public ClientPacket() {
        super();
    }

    public ClientPacket(ClientCode networkCode) {
        super(networkCode);
    }

    public static ClientPacket fromBytes(byte[] bytes) throws SerializationException {
        try {
            return (ClientPacket)Packet.fromBytes(bytes);
        } catch(ClassCastException e) {
            throw new SerializationException();
        }
    }

    public static ClientPacket pingPacket() {
        return new ClientPacket(ClientCode.PING);
    }
}
