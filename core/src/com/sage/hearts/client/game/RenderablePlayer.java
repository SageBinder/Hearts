package com.sage.hearts.client.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.hearts.HeartsCard;
import com.sage.hearts.utils.renderable.Renderable;
import com.sage.hearts.utils.renderable.RenderableCardGroup;

public class RenderablePlayer implements Renderable {
    final RenderableCardGroup<RenderableHeartsCard> collectedPointCards = new RenderableCardGroup<>();
    RenderableHeartsCard play;
    private int playerNum;
    private boolean isHost = false;
    private boolean isClientPlayer = false;
    private String name;
    private int gamePoints = 0;

    public RenderablePlayer(int playerNum, String name) {
        this.playerNum = playerNum;
        this.name = name;
    }

    public int getPlayerNum() {
        return playerNum;
    }

    public void setPlayerNum(int playerNum) {
        this.playerNum = playerNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGamePoints() {
        return gamePoints;
    }

    public void setGamePoints(int gamePoints) {
        this.gamePoints = gamePoints;
    }

    public int getRoundPoints() {
        return collectedPointCards.stream().mapToInt(HeartsCard::getPoints).sum();
    }

    boolean isHost() {
        return isHost;
    }

    void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    boolean isThisPlayer() {
        return isClientPlayer;
    }

    void setThisPlayer(boolean isClientPlayer) {
        this.isClientPlayer = isClientPlayer;
    }

    @Override
    public void render(SpriteBatch batch, Viewport viewport) {

    }
}
