package com.sage.hearts.server.network;

import com.sage.hearts.utils.network.NetworkCode;

public enum ServerCode implements NetworkCode {
    // General codes:
    PING,
    CONNECTION_ACCEPTED,
    CONNECTION_DENIED,
    PLAYER_DISCONNECTED,
    COULD_NOT_START_GAME,
    UNSUCCESSFUL_NAME_CHANGE,
    WAIT_FOR_PLAYERS,
    NEW_PLAYER_POINTS,

    // Trick codes:
    TRICK_START,
    PLAY_TWO_OF_CLUBS,
    MAKE_PLAY,
    INVALID_PLAY,
    SUCCESSFUL_PLAY,
    WAIT_FOR_TURN_PLAYER,
    WAIT_FOR_NEW_PLAY,
    WAIT_FOR_LEADING_PLAYER,
    TRICK_END,

    // Round codes:
    ROUND_START,
    WAIT_FOR_HAND,
    SEND_WARHEADS,
    INVALID_WARHEADS,
    SUCCESSFUL_WARHEADS,
    WAIT_FOR_WARHEADS,
    ROUND_END,
}
