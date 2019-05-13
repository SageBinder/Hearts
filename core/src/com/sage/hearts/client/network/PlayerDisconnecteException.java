package com.sage.hearts.client.network;

public class PlayerDisconnecteException extends RuntimeException {
    public PlayerDisconnecteException() {
        super();
    }

    public PlayerDisconnecteException(String message) {
        super(message);
    }

    public PlayerDisconnecteException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlayerDisconnecteException(Throwable cause) {
        super(cause);
    }

    protected PlayerDisconnecteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
