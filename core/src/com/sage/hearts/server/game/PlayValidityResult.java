package com.sage.hearts.server.game;

class PlayValidityResult {
    final boolean isValid;
    final String message;

    PlayValidityResult(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }
}
