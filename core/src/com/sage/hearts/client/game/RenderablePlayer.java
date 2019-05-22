package com.sage.hearts.client.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.hearts.HeartsCard;
import com.sage.hearts.utils.renderable.Renderable;
import com.sage.hearts.utils.renderable.RenderableCardEntity;
import com.sage.hearts.utils.renderable.RenderableCardGroup;

import java.util.Optional;

public class RenderablePlayer implements Renderable {
    private static BitmapFont nameFont;
    private static FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans-Regular.ttf"));
    static {
        var fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = (int)(Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getWidth()) * (1f / 7f));
        fontParameter.incremental = true;
        nameFont = fontGenerator.generateFont(fontParameter);
        nameFont.getData().markupEnabled = true;
    }

    private String colorString = "";
    private boolean isExpanded = false;

    public final RenderableCardGroup<RenderableHeartsCard> collectedPointCards = new RenderableCardGroup<>();

    private RenderableHeartsCard play;
    private Vector2 pos = new Vector2();
    private int playerNum;
    private boolean isHost = false;
    private boolean isClientPlayer = false;
    private String name;
    private int accumulatedPoints = 0;

    public RenderablePlayer(int playerNum, String name) {
        this.playerNum = playerNum;
        this.name = playerNum + ": " + name;
    }

    @Override
    public void render(SpriteBatch batch, Viewport viewport) {
        nameFont.draw(batch, colorString + getName(),
                pos.x, pos.y + (nameFont.getXHeight() * 2),
                0, Align.center, false);

        float playTargetHeight = viewport.getWorldHeight() * 0.11f;
        float playTargetY = pos.y - playTargetHeight;
        if(isExpanded) {
            playTargetHeight *= 2;
        }
        float playTargetWidth = playTargetHeight * RenderableCardEntity.WIDTH_TO_HEIGHT_RATIO;
        float playTargetX = pos.x - (playTargetWidth * 0.5f);

        if(play != null) {
            play.entity.mover.sizeSpeed = 8;
            play.entity.mover.posSpeed = 8;
            play.entity.mover.setTargetHeight(playTargetHeight);
            play.entity.mover.setTargetX(playTargetX);
            play.entity.mover.setTargetY(playTargetY);
            play.render(batch, viewport);
        }

        collectedPointCards.forEach(c -> {
            c.entity.mover.sizeSpeed = 8;
            c.entity.mover.posSpeed = 8;
        });
        collectedPointCards.cardHeight = playTargetHeight * 0.5f;
        collectedPointCards.regionWidth = isExpanded ? playTargetWidth * collectedPointCards.size() : 2.2f * playTargetWidth;
        collectedPointCards.pos.x = (playTargetX + (playTargetWidth * 0.5f)) - (collectedPointCards.regionWidth * 0.5f);
        collectedPointCards.pos.y = playTargetY - (collectedPointCards.cardHeight * 1.05f);
        collectedPointCards.prefDivisionProportion = 1.1f;
        collectedPointCards.render(batch, viewport);
    }

    public void update(float delta) {
        getPlay().ifPresent(play -> play.update(delta));
        collectedPointCards.update(delta);
    }

    public void setNameColor(Color nameColor) {
        colorString = "[#"
                + Float.toHexString(nameColor.r)
                + Float.toHexString(nameColor.g)
                + Float.toHexString(nameColor.b)
                + Float.toHexString(nameColor.a)
                + "]";
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

    public void clearPlay() {
        this.play = null;
    }

    public void clearCards() {
        collectedPointCards.clear();
        play = null;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isClientPlayer() {
        return isClientPlayer;
    }

    public void setIsClientPlayer(boolean isClientPlayer) {
        this.isClientPlayer = isClientPlayer;
    }

    public void setX(float x) {
        pos.x = x;
    }

    public void setY(float y) {
        pos.y = y;
    }

    public void setPos(float x, float y) {
        setX(x);
        setY(y);
    }

    public void setPos(Vector2 pos) {
        setX(pos.x);
        setY(pos.y);
    }

    public float getX() {
        return pos.x;
    }

    public float getY() {
        return pos.y;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void toggleExpanded() {
        isExpanded = !isExpanded;
    }
}
