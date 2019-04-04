package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;

import java.util.HashMap;

@SuppressWarnings({"unchecked", "WeakerAccess", "unused", "UnusedReturnValue"})
public class RenderableCardEntity<T extends RenderableCardEntity, CT extends Card> {
    public static final int CARD_HEIGHT_IN_PIXELS = 350;
    public static final int CARD_WIDTH_IN_PIXELS = 225;
    public static final float HEIGHT_TO_WIDTH_RATIO = (float)CARD_HEIGHT_IN_PIXELS / (float)CARD_WIDTH_IN_PIXELS;
    public static final float WIDTH_TO_HEIGHT_RATIO = (float)CARD_WIDTH_IN_PIXELS / (float)CARD_HEIGHT_IN_PIXELS;

    // Default variable values:
    public final int defaultCornerRadiusInPixels = (int)(0.075f * CARD_WIDTH_IN_PIXELS);

    public final int defaultFaceBorderThicknessInPixels = (int)(0.018f * CARD_WIDTH_IN_PIXELS);
    public final int defaultBackBorderThicknessInPixels = defaultCornerRadiusInPixels;

    public final float defaultFaceDesignHeightScale = 0.95f;
    public final float defaultFaceDesignWidthScale = 0.95f;

    public final float defaultBackDesignHeightScale = ((float)CARD_HEIGHT_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_HEIGHT_IN_PIXELS;
    public final float defaultBackDesignWidthScale = ((float)CARD_WIDTH_IN_PIXELS - (2 * (float)defaultBackBorderThicknessInPixels)) / (float)CARD_WIDTH_IN_PIXELS;

    public final float defaultHeightChangeOnSelect = 0.9f; // Relative to card height

    public final Color defaultFaceBorderColor = new Color(0, 0, 0, 1);
    public final Color defaultBackBorderColor = new Color(1, 1, 1, 1);

    public final Color defaultFaceUnselectedBackgroundColor = new Color(1, 1, 1, 1);
    public final Color defaultBackUnselectedBackgroundColor = new Color(0, 0, 0, 1);

    public final Color defaultFaceSelectedBackgroundColor = new Color(defaultFaceUnselectedBackgroundColor).sub(0.5f, 0.5f, 0.5f, 0);
    public final Color defaultBackSelectedBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    public final Color defaultFaceHighlightedBackgroundColor = new Color(1.0f, 1.0f, 0.5f, 1.0f);
    public final Color defaultBackHighlightedBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    // Member variables:
    private int cornerRadiusInPixels = defaultCornerRadiusInPixels;

    private int faceBorderThicknessInPixels = defaultFaceBorderThicknessInPixels;
    private int backBorderThicknessInPixels = defaultBackBorderThicknessInPixels;

    private float faceDesignHeightScale = defaultFaceDesignHeightScale; // Proportion that the face (numbers, design etc.) is scaled with respect to the card's overall rectangle
    private float faceDesignWidthScale = defaultFaceDesignWidthScale;

    private float backDesignHeightScale = defaultBackDesignHeightScale;
    private float backDesignWidthScale = defaultBackDesignWidthScale;

    private float heightChangeOnSelect = defaultHeightChangeOnSelect;

    private final Color faceBorderColor = new Color(defaultFaceBorderColor);
    private final Color backBorderColor = new Color(defaultBackBorderColor);

    private final Color faceBackgroundColor = new Color(defaultFaceUnselectedBackgroundColor);
    private final Color backBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    public final Color faceUnselectedBackgroundColor = new Color(defaultFaceUnselectedBackgroundColor);
    public final Color backUnselectedBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    public final Color faceSelectedBackgroundColor = new Color(defaultFaceSelectedBackgroundColor);
    public final Color backSelectedBackgroundColor = new Color(defaultBackSelectedBackgroundColor);

    public final Color faceHighlightedBackgroundColor = new Color(defaultFaceHighlightedBackgroundColor);
    public final Color backHighlightedBackgroundColor = new Color(defaultBackHighlightedBackgroundColor);

    // baseCardRect represents overall rectangle before rounding corners
    private final Rectangle baseCardRect = new Rectangle(0, 0, CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS);
    private final Rectangle displayCardRect = new Rectangle(baseCardRect);

    private boolean moveDisplayWithBase = true;

    private boolean faceUp = true;
    private boolean isSelected = false;

    private boolean selectable = true;
    private boolean flippable = true;

    // Render variables:
    private static FileHandle defaultSpriteFolder = Gdx.files.internal("playing_cards/");
    private static FileHandle spriteFolder = defaultSpriteFolder;

    private static HashMap<Integer, Pixmap> faceDesignPixmaps = new HashMap<>();
    private static Pixmap backPixmap = null;

    private Sprite thisCardBackSprite = null;
    private Sprite thisCardFaceSprite = null;

    // Other:
    public final CT card;

    public RenderableCardEntity(CT other) {
        card = other;
    }

    public boolean baseRectContainsPoint(Vector2 point) {
        return baseRectContainsPoint(point.x, point.y);
    }

    public boolean baseRectContainsPoint(float x, float y) {
        return baseCardRect.contains(x, y);
    }

    public boolean displayRectContainsPoint(Vector2 point) {
        return displayRectContainsPoint(point.x, point.y);
    }

    public boolean displayRectContainsPoint(float x, float y) {
        return displayCardRect.contains(x, y);
    }

    public boolean displayRectEqualsBaseRect() {
        return displayCardRect.equals(baseCardRect);
    }

    // --- SETTERS ---
    // Face value setters:
    public T setFaceDesignScale(float scale) {
        faceDesignHeightScale = scale;
        faceDesignWidthScale = scale;
        invalidateSprites();
        return (T) this;
    }

    public T setFaceDesignHeightScale(float scale) {
        this.faceDesignHeightScale = scale;
        invalidateSprites();
        return (T) this;
    }

    public T setFaceDesignWidthScale(float scale) {
        this.faceDesignWidthScale = scale;
        invalidateSprites();
        return (T) this;
    }

    public T setFaceBackgroundColor(Color faceBackgroundColor) {
        this.faceBackgroundColor.set(faceBackgroundColor);
        invalidateSprites();
        return (T) this;
    }

    public T resetFaceBackgroundColor() {
        if(isSelected) {
            setFaceBackgroundColor(faceSelectedBackgroundColor);
        } else {
            setFaceBackgroundColor(faceUnselectedBackgroundColor);
        }
        return (T) this;
    }

    public T setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor.set(faceBorderColor);
        invalidateSprites();
        return (T) this;
    }

    public T resetFaceBorderColor() {
        setFaceBorderColor(defaultFaceBorderColor);
        return (T) this;
    }

    public T setFaceBorderThicknessRelativeToHeight(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    public T setFaceBorderThicknessRelativeToWidth(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    public T setFaceBorderThicknessInPixels(int faceBorderThicknessInPixels) {
        this.faceBorderThicknessInPixels = faceBorderThicknessInPixels;
        invalidateSprites();
        return (T) this;
    }

    // Back value setters:
    public T setBackDesignScale(float scale) {
        backDesignHeightScale = scale;
        backDesignWidthScale = scale;
        invalidateSprites();
        return (T) this;
    }

    public T setBackDesignHeightScale(float backDesignHeightScale) {
        this.backDesignHeightScale = backDesignHeightScale;
        invalidateSprites();
        return (T) this;
    }

    public T setBackDesignWidthScale(float backDesignWidthScale) {
        this.backDesignWidthScale = backDesignWidthScale;
        invalidateSprites();
        return (T) this;
    }

    public T setBackBackgroundColor(Color backBackgroundColor) {
        this.backBackgroundColor.set(backBackgroundColor);
        return (T) this;
    }

    public T resetBackBackgroundColor() {
        if(isSelected) {
            setBackBackgroundColor(backSelectedBackgroundColor);
        } else {
            setBackBackgroundColor(backUnselectedBackgroundColor);
        }
        return (T) this;
    }

    public T setBackBorderColor(Color backBorderColor) {
        this.backBorderColor.set(backBorderColor);
        invalidateSprites();
        return (T) this;
    }

    public T resetBackBorderColor() {
        setBackBorderColor(defaultBackBorderColor);
        return (T) this;
    }

    public T setBackBorderThicknessRelativeToHeight(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    public T setBackBorderThicknessRelativeToWidth(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    public T setBackBorderThicknessInPixels(int backBorderThicknessInPixels) {
        this.backBorderThicknessInPixels = backBorderThicknessInPixels;
        invalidateSprites();
        return (T) this;
    }

    // General value setters:
    public T setBothBackgroundColors(Color newColor) {
        setFaceBackgroundColor(newColor);
        setBackBackgroundColor(newColor);
        return (T) this;
    }

    public T setBothBorderColors(Color newColor) {
        setFaceBorderColor(newColor);
        setBackBorderColor(newColor);
        return (T) this;
    }

    public T resetBothBackgroundColors() {
        resetFaceBackgroundColor();
        resetBackBackgroundColor();
        return (T) this;
    }

    public T resetBothBorderColors() {
        resetFaceBorderColor();
        resetBackBorderColor();
        return (T) this;
    }

    public T setCornerRadiusRelativeToWidth(float scale) {
        cornerRadiusInPixels = (int)(scale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    public T setCornerRadiusRelativeToHeight(float scale) {
        cornerRadiusInPixels = (int)(scale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T) this;
    }

    public T scale(float newScale) {
        setWidth(getWidth() * newScale);
        return (T) this;
    }

    public T setPosition(Vector2 newPosition) {
        return setPosition(newPosition.x, newPosition.y);
    }

    public T setPosition(float x, float y) {
        setX(x);
        setY(y);
        return (T) this;
    }

    public T setX(float x) {
        float change = x - baseCardRect.x;
        baseCardRect.x += change;
        if(moveDisplayWithBase) displayCardRect.x += change;

        return (T) this;
    }

    public T setY(float y) {
        float change = y - baseCardRect.y;
        baseCardRect.y += change;
        if(moveDisplayWithBase) displayCardRect.y += change;

        return (T) this;
    }

    public T setWidth(float width) {
        float widthChangeProportion = width / baseCardRect.width;

        baseCardRect.width *= widthChangeProportion;
        baseCardRect.height = HEIGHT_TO_WIDTH_RATIO * baseCardRect.width;

        if(moveDisplayWithBase) {
            displayCardRect.width *= widthChangeProportion;
            displayCardRect.height = HEIGHT_TO_WIDTH_RATIO * displayCardRect.width;
        }

        return (T) this;
    }

    public T setHeight(float height) {
        float heightChangeProportion = height / baseCardRect.height;

        baseCardRect.height *= heightChangeProportion;
        baseCardRect.width = WIDTH_TO_HEIGHT_RATIO * baseCardRect.height;

        if(moveDisplayWithBase) {
            displayCardRect.height *= heightChangeProportion;
            displayCardRect.width = WIDTH_TO_HEIGHT_RATIO * displayCardRect.height;
        }

        return (T) this;
    }

    public T setDisplayPosition(Vector2 newPosition) {
        return setDisplayPosition(newPosition.x, newPosition.y);
    }

    public T setDisplayPosition(float x, float y) {
        displayCardRect.setPosition(x, y);
        return (T) this;
    }

    public T setDisplayX(float x) {
        displayCardRect.x = x;
        return (T) this;
    }

    public T setDisplayY(float y) {
        displayCardRect.y = y;
        return (T) this;
    }

    public T setDisplayWidth(float width) {
        displayCardRect.width = width;
        displayCardRect.height = HEIGHT_TO_WIDTH_RATIO * width;
        return (T) this;
    }

    public T setDisplayHeight(float height) {
        displayCardRect.height = height;
        displayCardRect.width = WIDTH_TO_HEIGHT_RATIO * height;
        return (T) this;
    }

    public T setMoveDisplayWithBase(boolean moveDisplayWithBase) {
        this.moveDisplayWithBase = moveDisplayWithBase;
        return (T) this;
    }

    public T resetDisplayRect() {
        displayCardRect.set(baseCardRect);
        return (T) this;
    }

    public T select() {
        setSelected(true);
        return (T) this;
    }

    public T deselect() {
        setSelected(false);
        return (T) this;
    }

    public T toggleSelected() {
        return setSelected(!isSelected);
    }

    public T setSelected(boolean selected) {
        if(isSelected == selected || !selectable) {
            return (T) this;
        }
        isSelected = selected;

        if(isSelected) {
            setFaceBackgroundColor(faceSelectedBackgroundColor);
            setBackBackgroundColor(backSelectedBackgroundColor);
            setDisplayPosition(baseCardRect.x, baseCardRect.y + (heightChangeOnSelect * baseCardRect.height));
        } else {
            setBackBackgroundColor(backUnselectedBackgroundColor);
            setFaceBackgroundColor(faceUnselectedBackgroundColor);
            resetDisplayRect();
        }

        return (T) this;
    }

    public T setSelectable(boolean selectable) {
        if(!selectable) setSelected(false);
        this.selectable = selectable;
        return (T) this;
    }

    public T setHeightChangeOnSelect(float heightChangeOnSelect) {
        this.heightChangeOnSelect = heightChangeOnSelect;
        return (T) this;
    }

    public T setFaceUp(boolean faceUp) {
        if(flippable) {
            this.faceUp = faceUp;
        }
        return (T) this;
    }

    public T flip() {
        if(flippable) {
            faceUp = !faceUp;
        }
        return (T) this;
    }

    public T setFlippable(boolean flippable) {
        if(!flippable) setFaceUp(true);
        this.flippable = flippable;
        return (T) this;
    }

    // --- GETTERS ---
    // Face value getters:
    public Color getFaceBackgroundColor() {
        return new Color(this.faceBackgroundColor);
    }

    public Color getFaceBorderColor() {
        return new Color(faceBorderColor);
    }

    public int getFaceBorderThicknessInPixels() {
        return faceBorderThicknessInPixels;
    }

    public int getBackBorderThicknessInPixels() {
        return backBorderThicknessInPixels;
    }

    public float getFaceDesignHeightScale() {
        return faceDesignHeightScale;
    }

    public float getFaceDesignWidthScale() {
        return faceDesignWidthScale;
    }

    // Back value getters:
    public float getBackDesignHeightScale() {
        return backDesignHeightScale;
    }

    public float getBackDesignWidthScale() {
        return backDesignWidthScale;
    }

    public float getHeightChangeOnSelect() {
        return heightChangeOnSelect;
    }

    public Color getBackBorderColor() {
        return new Color(backBorderColor);
    }

    public Color getBackBackgroundColor() {
        return new Color(backBackgroundColor);
    }

    // General value getters:
    public int getCornerRadiusInPixels() {
        return cornerRadiusInPixels;
    }

    public float getX() {
        return baseCardRect.x;
    }

    public float getY() {
        return baseCardRect.y;
    }

    public float getHeight() {
        return baseCardRect.height;
    }

    public float getWidth() {
        return baseCardRect.width;
    }

    public float getDisplayX() {
        return displayCardRect.x;
    }

    public float getDisplayY() {
        return displayCardRect.y;
    }

    public float getDisplayWidth() {
        return displayCardRect.width;
    }

    public float getDisplayHeight() {
        return displayCardRect.height;
    }

    public Vector2 getPosition() {
        return baseCardRect.getPosition(new Vector2());
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public boolean isFlippable() {
        return flippable;
    }

    // --- RENDER LOGIC ---
    public static void setSpriteFolder(FileHandle newSpriteFolder) {
        spriteFolder = newSpriteFolder;
        resetPixmaps();
    }

    public static void useDefaultSpriteFolder() {
        spriteFolder = defaultSpriteFolder;
        resetPixmaps();
    }

    public static void dispose() {
        resetPixmaps();
    }

    private static void resetPixmaps() {
        resetBackPixmap();
        resetFaceDesignPixmaps();
    }

    private static void resetBackPixmap() {
        backPixmap.dispose();
        backPixmap = null;
    }

    private static void resetFaceDesignPixmaps() {
        for(Pixmap p : faceDesignPixmaps.values()) {
            p.dispose();
        }
        faceDesignPixmaps.clear();
    }

    private static void loadBackPixmap() {
        Pixmap originalImagePixmap = new Pixmap(spriteFolder.child("back.png"));
        backPixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalImagePixmap.getFormat());
        backPixmap.drawPixmap(originalImagePixmap,
                0, 0, originalImagePixmap.getWidth(), originalImagePixmap.getHeight(),
                0, 0, backPixmap.getWidth(), backPixmap.getHeight());
        originalImagePixmap.dispose();
    }

    private static void loadFaceDesignPixmapForCard(int cardNum) {
        String cardImageName;

        Suit suit = Card.getSuitFromCardNum(cardNum);
        Rank rank = Card.getRankFromCardNum(cardNum);

        cardImageName = rank.toString() + "_of_" + suit.toString() + ".png";

        Pixmap originalImagePixmap = new Pixmap(spriteFolder.child(cardImageName));
        Pixmap resizedImagePixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalImagePixmap.getFormat());
        resizedImagePixmap.drawPixmap(originalImagePixmap,
                0, 0, originalImagePixmap.getWidth(), originalImagePixmap.getHeight(),
                0, 0, resizedImagePixmap.getWidth(), resizedImagePixmap.getHeight());

        faceDesignPixmaps.put(cardNum, resizedImagePixmap);

        originalImagePixmap.dispose();
    }

    private static void roundPixmapCorners(Pixmap pixmap, int radius) {
        int pixmapHeight = pixmap.getHeight();
        int pixmapWidth = pixmap.getWidth();

        Pixmap.setBlending(Pixmap.Blending.None);

        // These loops create the rounded rectangle pixmap by adding transparent pixels at the corners
        for(int x = 0; x < pixmapWidth; x++) {
            nextIter:
            for(int y = 0; y < pixmapHeight; y++) {
                // These two innermost loops check conditions for adding a transparent pixel for each of the four corners
                for(int i = 0; i < 2; i++) {
                    for(int j = 0; j < 2; j++) {
                        // Top left corner: i == 0, j == 0
                        // Bottom left corner: i == 1, j == 0
                        // Top right corner: i == 0, j == 1
                        // Bottom right corner: i == 1, j == 1
                        int circleCenter_y = i == 0 ? radius : pixmapHeight - radius;
                        int circleCenter_x = j == 0 ? radius : pixmapWidth - radius;
                        double distance = Math.sqrt(Math.pow(x - circleCenter_x, 2) + Math.pow(y - circleCenter_y, 2));

                        // Using (<= and >=) as opposed to (< and >) doesn't seem to make any visual difference
                        if(((i == 0 && y <= circleCenter_y) || (i == 1 && y >= circleCenter_y))
                                && ((j == 0 && x <= circleCenter_x) || (j == 1 && x >= circleCenter_x))
                                && distance >= radius) {
                            pixmap.drawPixel(x, y, 0);

                            // Since it was determined that pixel (x, y) should be transparent,
                            // the rest of the conditions shouldn't be checked, so exit the two innermost loops.
                            continue nextIter;
                        }
                    }
                }
            }
        }
    }

    private static void drawCurvedBorderOnPixmap(Pixmap pixmap, int radius, int borderThickness, Color color) {
        int pixmapHeight = pixmap.getHeight();
        int pixmapWidth = pixmap.getWidth();

        Pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(color);

        // Left border
        pixmap.fillRectangle(0, radius, borderThickness, pixmapHeight - (2 * radius));

        // Right border
        pixmap.fillRectangle(pixmapWidth - borderThickness, radius, borderThickness, pixmapHeight - (2 * radius));

        // Top border
        pixmap.fillRectangle(radius, 0, pixmapWidth - (2 * radius), borderThickness);

        // Bottom border
        pixmap.fillRectangle(radius, pixmapHeight - borderThickness, pixmapWidth - (2 * radius), borderThickness);

        // This code is almost the exact same as the code in roundPixmapCorners()
        for(int x = 0; x < pixmapWidth; x++) {
            nextIter:
            for(int y = 0; y < pixmapHeight; y++) {
                // These two innermost loops check conditions for adding a border pixel for each of the four corners
                for(int i = 0; i < 2; i++) {
                    for(int j = 0; j < 2; j++) {
                        // Top left corner: i == 0, j == 0
                        // Bottom left corner: i == 1, j == 0
                        // Top right corner: i == 0, j == 1
                        // Bottom right corner: i == 1, j == 1
                        int circleCenter_y = i == 0 ? radius : pixmapHeight - radius;
                        int circleCenter_x = j == 0 ? radius : pixmapWidth - radius;
                        double distance = Math.sqrt(Math.pow(x - circleCenter_x, 2) + Math.pow(y - circleCenter_y, 2));

                        // Using (<= and >=) as opposed to (< and >) doesn't seem to make any visual difference
                        if(((i == 0 && y <= circleCenter_y) || (i == 1 && y >= circleCenter_y))
                                && ((j == 0 && x <= circleCenter_x) || (j == 1 && x >= circleCenter_x))
                                && distance <= radius
                                && distance >= radius - borderThickness) {
                            pixmap.drawPixel(x, y, Color.rgba8888(color));

                            // Since it was determined that pixel (x, y) is a border pixel,
                            // the rest of the conditions shouldn't be checked, so exit the two innermost loops.
                            continue nextIter;
                        }
                    }
                }
            }
        }
    }

    private void setupThisCardFaceSprite() {
        if(faceDesignPixmaps.get(card.cardNum) == null) {
            loadFaceDesignPixmapForCard(card.cardNum);
        }
        thisCardFaceSprite = setupSpriteFromPixmap(faceDesignPixmaps.get(card.cardNum),
                getFaceBackgroundColor(),
                getFaceDesignWidthScale(), getFaceDesignHeightScale(),
                getFaceBorderThicknessInPixels(), getFaceBorderColor());
    }

    private void setupThisCardBackSprite() {
        if(backPixmap == null) {
            loadBackPixmap();
        }
        thisCardBackSprite = setupSpriteFromPixmap(backPixmap, getBackBackgroundColor(),
                getBackDesignWidthScale(), getBackDesignHeightScale(),
                getBackBorderThicknessInPixels(), getBackBorderColor());
    }

    private Sprite setupSpriteFromPixmap(Pixmap designPixmap,
                                         Color backgroundColor,
                                         float designWidthScale, float designHeightScale,
                                         int borderThicknessInPixels, Color borderColor) {
        Pixmap spritePixmap = new Pixmap(designPixmap.getWidth(), designPixmap.getHeight(), designPixmap.getFormat());

        Pixmap.setBlending(Pixmap.Blending.SourceOver);
        spritePixmap.setColor(backgroundColor);
        spritePixmap.fill();

        spritePixmap.drawPixmap(designPixmap,
                0, 0, designPixmap.getWidth(), designPixmap.getHeight(),
                (int) (0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * designWidthScale))), (int) (0.5f * (CARD_HEIGHT_IN_PIXELS - (CARD_HEIGHT_IN_PIXELS * designHeightScale))),
                (int) (CARD_WIDTH_IN_PIXELS * designWidthScale), (int) (CARD_HEIGHT_IN_PIXELS * designHeightScale));

        roundPixmapCorners(spritePixmap, getCornerRadiusInPixels());
        drawCurvedBorderOnPixmap(spritePixmap,
                getCornerRadiusInPixels(),
                borderThicknessInPixels,
                borderColor);

        Texture spriteTexture = new Texture(spritePixmap);

        Sprite sprite = new Sprite(spriteTexture);
        sprite.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sprite.setSize(getWidth(), getHeight());

        spritePixmap.dispose();
        return sprite;
    }

    public void render(SpriteBatch batch, Viewport viewport) {
        renderAt(batch, viewport, getDisplayX(), getDisplayY(), getDisplayWidth(), getDisplayHeight());
    }

    public void renderBase(SpriteBatch batch, Viewport viewport) {
        renderAt(batch, viewport, getX(),  getY(), getWidth(), getHeight());
    }

    public void renderAt(SpriteBatch batch, Viewport viewport, float x, float y, float width, float height) {
        if(isFaceUp()) {
            if(thisCardFaceSprite == null) {
                setupThisCardFaceSprite();
            }
            renderFace(batch, viewport, x, y, width, height);
        } else {
            if(thisCardBackSprite == null) {
                setupThisCardBackSprite();
            }
            renderBack(batch, viewport, x, y, width, height);
        }
    }

    private void renderFace(SpriteBatch batch, Viewport viewport,
                            float x, float y, float width, float height) {
        drawSprite(batch, viewport, thisCardFaceSprite, x, y, width, height);
    }

    private void renderBack(SpriteBatch batch, Viewport viewport,
                            float x, float y, float width, float height) {
        drawSprite(batch, viewport, thisCardBackSprite, x, y, width, height);
    }

    private void drawSprite(SpriteBatch batch, Viewport viewport, Sprite sprite,
                            float x, float y, float width, float height) {
        // TODO: Maybe this rounding should only be done when position changes, but I'm too lazy to do that right now

        // vecXY is initialized with world coordinates for card position
        Vector2 vecXY = new Vector2(x, y);

        // vecXY is then projected onto the screen, so now it represents the *screen* coordinates of the card
        viewport.project(vecXY);

        // viewport.project doesn't seem to account for the fact that with screen coordinates, y starts from the top of
        // the screen, so this line accounts for that
        vecXY.y = Gdx.graphics.getHeight() - vecXY.y;

        // Round vecXY so that the card's position isn't in between two pixels
        vecXY.x = MathUtils.round(vecXY.x);
        vecXY.y = MathUtils.round(vecXY.y);

        // Unproject vecXY back to world coordinates, so now we know the world coordinates of the card will be projected
        // to a whole pixel value, and thus the card's sprite won't have any weird subpixel stretching going on
        viewport.unproject(vecXY);

        sprite.setBounds(vecXY.x, vecXY.y, width, height);
        sprite.draw(batch);
    }

    void invalidateSprites() {
        if(thisCardFaceSprite != null) {
            thisCardFaceSprite.getTexture().dispose();
        }
        thisCardFaceSprite = null;

        if(thisCardBackSprite != null) {
            thisCardBackSprite.getTexture().dispose();
        }
        thisCardBackSprite = null;
    }
}
