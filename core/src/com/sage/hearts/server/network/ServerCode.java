package com.sage.hearts.server.network;

import com.sage.hearts.utils.network.InvalidCodeException;
import com.sage.hearts.utils.network.NetworkCode;

public enum ServerCode implements NetworkCode {
    // General codes:
    PING(0),
    CONNECTION_ACCEPTED(1),
    CONNECTION_DENIED(2),
    PLAYER_DISCONNECTED(3),
    COULD_NOT_START_GAME(4),
    UNSUCCESSFUL_NAME_CHANGE(5),
    WAIT_FOR_PLAYERS(6),

    // Trick codes:
    TRICK_START(7),
    PLAY_TWO_OF_CLUBS(8),
    MAKE_PLAY(9),
    INVALID_PLAY(10),
    SUCCESSFUL_PLAY(11),
    WAIT_FOR_TURN_PLAYER(12),
    WAIT_FOR_NEW_PLAY(13),
    WAIT_FOR_LEADING_PLAYER(14),
    TRICK_END(15),

    // Round codes:
    ROUND_START(16),
    WAIT_FOR_HAND(17),
    SEND_WARHEADS(18),
    INVALID_WARHEADS(19),
    SUCCESSFUL_WARHEADS(20),
    WAIT_FOR_WARHEADS(21),
    ROUND_END(22);

    // Honestly I don't think codeInt is necessary because the enum is just being serialized anyways.
    // A custom serializer could make use of codeInt but I don't know if that's worth the effort.
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
