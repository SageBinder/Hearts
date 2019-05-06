package com.sage.hearts.client.network;

import com.sage.hearts.utils.network.NetworkCode;

public enum ClientCode implements NetworkCode {
    PING("PING", 0),
    START_GAME("START_GAME", 1);

    public final String string;
    public final int codeInt;

    ClientCode(String string, int codeInt) {
        this.string = string;
        this.codeInt = codeInt;
    }
}
