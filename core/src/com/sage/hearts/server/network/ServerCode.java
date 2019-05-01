package com.sage.hearts.server.network;

import com.sage.hearts.utils.network.InvalidCodeException;
import com.sage.hearts.utils.network.NetworkCode;

public enum ServerCode implements NetworkCode {
    START_GAME("START_GAME", 0);

    public final String string;
    public final int codeInt;

    ServerCode(String string, int codeNum) {
        this.string = string;
        this.codeInt = codeNum;
    }

    public static ServerCode fromCodeInt(int i) throws InvalidCodeException {
        for(var code : values()) {
            if(code.codeInt == i) {
                return code;
            }
        }
        throw new InvalidCodeException();
    }
}
