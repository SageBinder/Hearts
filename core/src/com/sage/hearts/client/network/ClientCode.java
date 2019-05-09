package com.sage.hearts.client.network;

import com.sage.hearts.utils.network.NetworkCode;

public enum ClientCode implements NetworkCode {
    PING(0),
    START_GAME(1),
    PLAY(2),
    WARHEADS(3);

    public final int codeInt;

    ClientCode(int codeInt) {
        this.codeInt = codeInt;
    }
}
