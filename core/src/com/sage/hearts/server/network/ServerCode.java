package com.sage.hearts.server.network;

import com.sage.hearts.utils.network.InvalidCodeException;
import com.sage.hearts.utils.network.NetworkCode;

public enum ServerCode implements NetworkCode {
    PING(0),
    START_GAME(1),
    PLAY_TWO_OF_CLUBS(2),
    MAKE_PLAY(3),
    INVALID_PLAY(4),
    SUCCESSFUL_PLAY(5),
    WAIT_FOR_NEW_PLAY(6),
    WAIT_FOR_LEADING_PLAYER(7),
    TRICK_START(8),
    TRICK_END(9),
    WAIT_FOR_TURN_PLAYER(10),
    WAIT_FOR_HAND(11),
    ROUND_START(12),
    WAIT_FOR_PLAYER_ORDER(13),
    WAIT_FOR_WARHEAD_MAP(14),
    SEND_WARHEADS(15),
    INVALID_WARHEADS(16),
    SUCCESSFUL_WARHEADS(17),
    PLAYER_DISCONNECTED(18);

    public final int codeInt;

    ServerCode(int codeNum) {
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
