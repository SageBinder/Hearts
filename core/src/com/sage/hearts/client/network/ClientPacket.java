package com.sage.hearts.client.network;

import com.sage.hearts.utils.network.Packet;

public class ClientPacket extends Packet<ClientCode> {
    public ClientPacket() {
        super();
    }

    public ClientPacket(ClientCode networkCode) {
        super(networkCode);
    }
}
