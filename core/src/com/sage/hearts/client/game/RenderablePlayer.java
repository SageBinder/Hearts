package com.sage.hearts.client.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.hearts.HeartsCard;
import com.sage.hearts.utils.renderable.Renderable;
import com.sage.hearts.utils.renderable.RenderableCardGroup;

import java.util.Optional;

public class RenderablePlayer implements Renderable {
    final RenderableCardGroup<RenderableHeartsCard> collectedPointCards = new RenderableCardGroup<>();
    private RenderableHeartsCard play;
    private int playerNum;
    private boolean isHost = false;
    private boolean isClientPlayer = false;
    private String name;
    private int accumulatedPoints = 0;

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

    public int getAccumulatedPoints() {
        return accumulatedPoints;
    }

    public void setAccumulatedPoints(int accumulatedPoints) {
        this.accumulatedPoints = accumulatedPoints;
    }

    public int getRoundPoints() {
        return collectedPointCards.stream().mapToInt(HeartsCard::getPoints).sum();
    }

    public Optional<RenderableHeartsCard> getPlay() {
        return Optional.ofNullable(play);
    }

    public void setPlay(RenderableHeartsCard play) {
        this.play = play;
    }

    public void disposePlay() {
        if(this.play != null) {
            this.play.dispose();
        }
        this.play = null;
    }

    boolean isHost() {
        return isHost;
    }

    void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    boolean isClientPlayer() {
        return isClientPlayer;
    }

    void setIsClientPlayer(boolean isClientPlayer) {
        this.isClientPlayer = isClientPlayer;
    }

    public void disposeCards() {
        collectedPointCards.disposeAll();
        collectedPointCards.clear();
        play.dispose();
        play = null;
    }

    @Override
    public void render(SpriteBatch batch, Viewport viewport) {

    }
}
