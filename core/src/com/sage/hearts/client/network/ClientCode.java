package com.sage.hearts.client.network;

import com.sage.hearts.utils.network.NetworkCode;

public enum ClientCode implements NetworkCode {
    PING,
    START_GAME,
    PLAY,
    WARHEADS,
    NAME,
    PLAYER_POINTS_CHANGE,
    RESET_PLAYER_POINTS,
    SHUFFLE_PLAYERS
}
