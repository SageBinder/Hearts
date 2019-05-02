package com.sage.hearts.utils.renderable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sage.hearts.utils.card.Card;
import com.sage.hearts.utils.card.Rank;
import com.sage.hearts.utils.card.Suit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

// OH GOD THIS CLASS LITERALLY MAKES ME WANT TO VOMIT AAAAAAAAHHHHHHHH

@SuppressWarnings({"unchecked", "WeakerAccess", "unused", "UnusedReturnValue"})
public class RenderableCardEntity<T extends RenderableCardEntity, CardT extends Card> {
    public static final int CARD_HEIGHT_IN_PIXELS = 969;
    public static final int CARD_WIDTH_IN_PIXELS = 666;
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

    public final float defaultYChangeOnSelect = 0.9f; // Proportional to height
    public final float defaultXChangeOnSelect = 0.0f; // Proportional to width

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

    private float YChangeOnSelect = defaultYChangeOnSelect; // Proportional to height
    private float XChangeOnSelect = defaultXChangeOnSelect; // Proportional to width

    private final Color faceBorderColor = new Color(defaultFaceBorderColor);
    private final Color backBorderColor = new Color(defaultBackBorderColor);

    private final Color faceBackgroundColor = new Color(defaultFaceUnselectedBackgroundColor);
    private final Color backBackgroundColor = new Color(defaultBackUnselectedBackgroundColor);

    // baseRect represents overall rectangle before rounding corners
    private final CardPolygon baseRect = new CardPolygon(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS);
    private final CardPolygon displayRect = new CardPolygon(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS);
    
    private float displayXOffset = 0;
    private float displayYOffset = 0;
    private float displayProportion = 1f;
    private float displayRotationOffset = 0;

    private boolean selectable = true;
    private boolean flippable = true;
    private boolean faceUp = true;
    private boolean isSelected = false;

    // Render variables:
    private static FileHandle defaultSpriteFolder = Gdx.files.internal("playing_cards/");
    private static FileHandle spriteFolder = defaultSpriteFolder;

    private static HashMap<Integer, Pixmap> faceDesignPixmaps = new HashMap<>();
    private static Pixmap backPixmap = null;

    private Sprite backSprite = null;
    private Sprite faceSprite = null;

    private static String imageExtension = ".png";

    // Card accessor:
    public final CardT card;

    // Keeps track of all cards to allow the freeing up of any card's texture
    private static final List<RenderableCardEntity> allEntities = new ArrayList<>();
    private boolean isDisposed = false;

    public RenderableCardEntity(CardT card) {
        this.card = card;
        baseRect.setPosition(0, 0);
        displayRect.setPosition(0, 0);
        reallocateResources(this);
    }

    private void setDisplayRect() {
        setDisplayPos();
        setDisplayPos();
        setDisplayRotation();
    }

    private void setDisplayX() {
        displayRect.setX(baseRect.getX() + displayXOffset + (isSelected ? XChangeOnSelect * getDisplayWidth() : 0));
    }

    private void setDisplayY() {
        displayRect.setY(baseRect.getY() + displayYOffset + (isSelected ? YChangeOnSelect * getDisplayHeight() : 0));
    }

    private void setDisplayPos() {
        setDisplayX();
        setDisplayY();
    }

    private void setDisplaySize() {
        displayRect.setWidth(baseRect.getWidth() * displayProportion);
        displayRect.setHeight(baseRect.getHeight() * displayProportion);
        setDisplayPos(); // Must call this because displayPos depends on display size (for isSelected height change)
    }

    private void setDisplayRotation() {
        displayRect.setRotation(baseRect.getRotation() + displayRotationOffset);
    }

    public boolean baseRectContainsPoint(Vector2 point) {
        return baseRectContainsPoint(point.x, point.y);
    }

    public boolean baseRectContainsPoint(float x, float y) {
        return baseRect.contains(x, y);
    }

    public boolean displayRectContainsPoint(Vector2 point) {
        return displayRectContainsPoint(point.x, point.y);
    }

    public boolean displayRectContainsPoint(float x, float y) {
        return displayRect.contains(x, y);
    }

    public boolean displayRectEqualsBaseRect() {
        return Arrays.equals(baseRect.getTransformedVertices(), displayRect.getTransformedVertices());
    }

    // --- SETTERS ---
        // --- FACE SETTERS ---:
        // Face design scale:
    public T setFaceDesignScale(float scale) {
        faceDesignHeightScale = scale;
        faceDesignWidthScale = scale;
        invalidateSprites();
        return (T)this;
    }

    public T setFaceDesignHeightScale(float scale) {
        this.faceDesignHeightScale = scale;
        invalidateSprites();
        return (T)this;
    }

    public T setFaceDesignWidthScale(float scale) {
        this.faceDesignWidthScale = scale;
        invalidateSprites();
        return (T)this;
    }

    public T resetFaceDesignScale() {
        resetFaceDesignHeightScale();
        resetFaceDesignWidthScale();
        return (T)this;
    }

    public T resetFaceDesignHeightScale() {
        setFaceDesignHeightScale(defaultFaceDesignHeightScale);
        return (T)this;
    }

    public T resetFaceDesignWidthScale() {
        setFaceDesignWidthScale(defaultFaceDesignWidthScale);
        return (T)this;
    }

    // Face colors:
    public T setFaceBackgroundColor(Color faceBackgroundColor) {
        this.faceBackgroundColor.set(faceBackgroundColor);
        invalidateSprites();
        return (T)this;
    }

    public T resetFaceBackgroundColor() {
        if(isSelected) {
            setFaceBackgroundColor(defaultFaceSelectedBackgroundColor);
        } else {
            setFaceBackgroundColor(defaultFaceUnselectedBackgroundColor);
        }
        return (T)this;
    }

    public T setFaceBorderColor(Color faceBorderColor) {
        this.faceBorderColor.set(faceBorderColor);
        invalidateSprites();
        return (T)this;
    }

    public T resetFaceBorderColor() {
        setFaceBorderColor(defaultFaceBorderColor);
        return (T)this;
    }

        // Face border size:
    public T setFaceBorderThicknessRelativeToHeight(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T)this;
    }

    public T setFaceBorderThicknessRelativeToWidth(float borderScale) {
        this.faceBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T)this;
    }

    public T setFaceBorderThicknessInPixels(int faceBorderThicknessInPixels) {
        this.faceBorderThicknessInPixels = faceBorderThicknessInPixels;
        invalidateSprites();
        return (T)this;
    }

    public T resetFaceBorderThickness() {
        return setFaceBorderThicknessInPixels(defaultFaceBorderThicknessInPixels);
    }

        // --- BACK SETTERS ---:
        // Back design scale:
    public T setBackDesignScale(float scale) {
        backDesignHeightScale = scale;
        backDesignWidthScale = scale;
        invalidateSprites();
        return (T)this;
    }

    public T setBackDesignHeightScale(float backDesignHeightScale) {
        this.backDesignHeightScale = backDesignHeightScale;
        invalidateSprites();
        return (T)this;
    }

    public T setBackDesignWidthScale(float backDesignWidthScale) {
        this.backDesignWidthScale = backDesignWidthScale;
        invalidateSprites();
        return (T)this;
    }

    public T resetBackDesignScale() {
        resetBackDesignHeightScale();
        resetBackDesignWidthScale();
        return (T)this;
    }

    public T resetBackDesignHeightScale() {
        setBackDesignHeightScale(defaultBackDesignHeightScale);
        return (T)this;
    }

    public T resetBackDesignWidthScale() {
        setBackDesignWidthScale(defaultBackDesignWidthScale);
        return (T)this;
    }

    // Back colors:
    public T setBackBackgroundColor(Color backBackgroundColor) {
        this.backBackgroundColor.set(backBackgroundColor);
        invalidateSprites();
        return (T)this;
    }

    public T resetBackBackgroundColor() {
        setBackBackgroundColor(isSelected ? defaultBackSelectedBackgroundColor : defaultBackUnselectedBackgroundColor);
        return (T)this;
    }

    public T setBackBorderColor(Color backBorderColor) {
        this.backBorderColor.set(backBorderColor);
        invalidateSprites();
        return (T)this;
    }

    public T resetBackBorderColor() {
        setBackBorderColor(defaultBackBorderColor);
        return (T)this;
    }

    // Back border size:
    public T setBackBorderThicknessRelativeToHeight(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T)this;
    }

    public T setBackBorderThicknessRelativeToWidth(float borderScale) {
        this.backBorderThicknessInPixels = (int)(borderScale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T)this;
    }

    public T setBackBorderThicknessInPixels(int backBorderThicknessInPixels) {
        this.backBorderThicknessInPixels = backBorderThicknessInPixels;
        invalidateSprites();
        return (T)this;
    }

    public T resetBackBorderThickness() {
        return setBackBorderThicknessInPixels(defaultBackBorderThicknessInPixels);
    }

        // --- GENERAL SETTERS ---:
    public T resetSpriteToDefaults() {
        resetCornerRadius();
        resetBothBorderThicknesses();
        resetBothDesignScales();
        resetYChangeOnSelect();
        resetXChangeOnSelect();
        resetBothBorderColors();
        resetBothBackgroundColors();

        invalidateSprites();

        return (T)this;
    }

    public T resetBothDesignHeightScales() {
        resetFaceDesignHeightScale();
        resetBackDesignHeightScale();
        return (T)this;
    }

    public T resetBothDesignWidthScales() {
        resetFaceDesignWidthScale();
        resetBackDesignWidthScale();
        return (T)this;
    }

    public T resetBothDesignScales() {
        resetFaceDesignScale();
        resetBackDesignScale();
        return (T)this;
    }

        // Colors:
    public T setBothBackgroundColors(Color newColor) {
        setFaceBackgroundColor(newColor);
        setBackBackgroundColor(newColor);
        return (T)this;
    }

    public T setBothBorderColors(Color newColor) {
        setFaceBorderColor(newColor);
        setBackBorderColor(newColor);
        return (T)this;
    }

    public T resetBothBackgroundColors() {
        resetFaceBackgroundColor();
        resetBackBackgroundColor();
        return (T)this;
    }

    public T resetBothBorderColors() {
        resetFaceBorderColor();
        resetBackBorderColor();
        return (T)this;
    }

        // Corner radius/border:
    public T setCornerRadiusRelativeToWidth(float scale) {
        cornerRadiusInPixels = (int)(scale * CARD_WIDTH_IN_PIXELS);
        invalidateSprites();
        return (T)this;
    }

    public T setCornerRadiusRelativeToHeight(float scale) {
        cornerRadiusInPixels = (int)(scale * CARD_HEIGHT_IN_PIXELS);
        invalidateSprites();
        return (T)this;
    }

    public T setCornerRadiusInPixels(int pixels) {
        cornerRadiusInPixels = pixels;
        invalidateSprites();
        return (T)this;
    }

    public T resetCornerRadius() {
        return setCornerRadiusInPixels(defaultCornerRadiusInPixels);
    }

    public T resetBothBorderThicknesses() {
        resetFaceBorderThickness();
        resetBackBorderThickness();
        return (T)this;
    }

        // Size/position:
    public T scale(float newScale) {
        setWidth(getWidth() * newScale);
        return (T)this;
    }

    public T setPosition(Vector2 newPosition) {
        return setPosition(newPosition.x, newPosition.y);
    }

    public T setPosition(float x, float y) {
        setX(x);
        setY(y);
        return (T)this;
    }

    public T setX(float x) {
        baseRect.setX(x);
        setDisplayX();
        return (T)this;
    }

    public T setY(float y) {
        baseRect.setY(y);
        setDisplayY();
        return (T)this;
    }

    public T setWidth(float width) {
        baseRect.setWidth(width);
        baseRect.setHeight(HEIGHT_TO_WIDTH_RATIO * width);
        setDisplaySize();
        return (T)this;
    }

    public T setHeight(float height) {
        baseRect.setHeight(height);
        baseRect.setWidth(WIDTH_TO_HEIGHT_RATIO * height);
        setDisplaySize();
        return (T)this;
    }

    public T setRotationDeg(float deg) {
        baseRect.setRotation(deg);
        setDisplayRotation();
        return (T)this;
    }

    public T setRotationRad(float rad) {
        return setRotationDeg(MathUtils.radDeg * rad);
    }

    public T rotateDeg(float deg) {
        return setRotationDeg(baseRect.getRotation() + deg);
    }

    public T rotateRad(float rad) {
        return setRotationDeg(baseRect.getRotation() + (MathUtils.radDeg * rad));
    }

    public T setOriginXProportion(float originXProportion) {
        baseRect.setOriginXProportion(originXProportion);
        displayRect.setOriginXProportion(originXProportion);
        return (T)this;
    }

    public T setOriginYProportion(float originYProportion) {
        baseRect.setOriginYProportion(originYProportion);
        displayRect.setOriginYProportion(originYProportion);
        return (T)this;
    }

    public T setOriginProportion(float originProportionX, float originProportionY) {
        baseRect.setOriginProportion(originProportionX, originProportionY);
        displayRect.setOriginProportion(originProportionX, originProportionY);
        return (T)this;
    }

    public T setOriginToCenter() {
        baseRect.setOriginToCenter();
        displayRect.setOriginToCenter();
        return (T)this;
    }

    public T resetOrigin() {
        baseRect.resetOrigin();
        displayRect.resetOrigin();
        return (T)this;
    }

        // Display offsets:
    public T setDisplayOffset(float x, float y) {
        setDisplayXOffset(x);
        setDisplayYOffset(y);
        return (T)this;
    }

    public T setDisplayXOffset(float displayXOffset) {
        this.displayXOffset = displayXOffset;
        setDisplayX();
        return (T)this;
    }

    public T setDisplayYOffset(float displayYOffset) {
        this.displayYOffset = displayYOffset;
        setDisplayY();
        return (T)this;
    }

    public T setDisplayProportion(float displayProportion) {
        this.displayProportion = displayProportion;
        setDisplayPos();
        return (T)this;
    }

    public T setDisplayRotationOffsetDeg(float displayRotationOffsetDeg) {
        this.displayRotationOffset = displayRotationOffsetDeg;
        setDisplayRotation();
        return (T)this;
    }

    public T setDisplayRotationOffsetRad(float displayRotationOffsetRad) {
        setDisplayRotationOffsetDeg(MathUtils.radDeg * displayRotationOffsetRad);
        return (T)this;
    }

    public T resetDisplayOffsets() {
        displayXOffset = 0;
        displayYOffset = 0;
        displayProportion = 1f;
        displayRotationOffset = 0;
        setDisplayRect();
        return (T)this;
    }

        // Selectable/Flippable
    public T select() {
        setSelected(true);
        return (T)this;
    }

    public T deselect() {
        setSelected(false);
        return (T)this;
    }

    public T toggleSelected() {
        return setSelected(!isSelected);
    }

    public T setSelected(boolean selected) {
        if(isSelected == selected || !selectable) {
            return (T)this;
        } else {
            isSelected = selected;
            if(isSelected) {
                setFaceBackgroundColor(defaultFaceSelectedBackgroundColor);
                setBackBackgroundColor(defaultBackSelectedBackgroundColor);
            } else {
                setBackBackgroundColor(defaultBackUnselectedBackgroundColor);
                setFaceBackgroundColor(defaultFaceUnselectedBackgroundColor);
            }
            setDisplayPos();
            return (T)this;
        }
    }

    public T setSelectable(boolean selectable) {
        this.selectable = selectable;
        return (T)this;
    }

    public T setYChangeOnSelect(float YChangeOnSelect) {
        this.YChangeOnSelect = YChangeOnSelect;
        setDisplayY();
        return (T)this;
    }

    public T resetYChangeOnSelect() {
        return setYChangeOnSelect(defaultYChangeOnSelect);
    }

    public T setXChangeOnSelect(float XChangeOnSelect) {
        this.XChangeOnSelect = XChangeOnSelect;
        setDisplayX();
        return (T)this;
    }

    public T resetXChangeOnSelect() {
        return setXChangeOnSelect(defaultXChangeOnSelect);
    }

    public T setFaceUp(boolean faceUp) {
        if(flippable) this.faceUp = faceUp;
        return (T)this;
    }

    public T flip() {
        setFaceUp(!faceUp);
        return (T)this;
    }

    public T setFlippable(boolean flippable) {
        this.flippable = flippable;
        return (T)this;
    }

    // --- GETTERS ---
        // --- FACE GETTERS ---:
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

        // --- BACK GETTERS ---:
    public float getBackDesignHeightScale() {
        return backDesignHeightScale;
    }

    public float getBackDesignWidthScale() {
        return backDesignWidthScale;
    }

    public Color getBackBorderColor() {
        return new Color(backBorderColor);
    }

    public Color getBackBackgroundColor() {
        return new Color(backBackgroundColor);
    }

        // --- GENERAL GETTERS ---:
        // Corner radius:
    public int getCornerRadiusInPixels() {
        return cornerRadiusInPixels;
    }

        // Base size/position:
    public Vector2 getPosition() {
        return new Vector2(baseRect.getX(), baseRect.getY());
    }

    public float getX() {
        return baseRect.getX();
    }

    public float getY() {
        return baseRect.getY();
    }

    public float getWidth() {
        return baseRect.getWidth();
    }

    public float getHeight() {
        return baseRect.getHeight();
    }

    public float getRotation() {
        return baseRect.getRotation();
    }

    public float getOriginXProportion() {
        return baseRect.getOriginXProportion();
    }

    public float getOriginYProportion() {
        return baseRect.getOriginYProportion();
    }

    public float getYChangeOnSelect() {
        return YChangeOnSelect;
    }

    public float getXChangeOnSelect() {
        return XChangeOnSelect;
    }

        // Display size/position:
    public Vector2 getDisplayPosition() {
        return new Vector2(displayRect.getX(), displayRect.getY());
    }

    public float getDisplayX() {
        return displayRect.getX();
    }

    public float getDisplayY() {
        return displayRect.getY();
    }

    public float getDisplayWidth() {
        return displayRect.getWidth();
    }

    public float getDisplayHeight() {
        return displayRect.getHeight();
    }

    public float getDisplayRotation() {
        return displayRect.getRotation();
    }

        // Display offsets:
    public float getDisplayXOffset() {
        return displayXOffset;
    }

    public float getDisplayYOffset() {
        return displayYOffset;
    }

    public float getDisplayProportion() {
        return displayProportion;
    }

    public float getDisplayRotationOffset() {
        return displayRotationOffset;
    }

    // Selectable/flippable:
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
    // Pixmap/sprite methods:
    public final void reallocateResources() {
        reallocateResources(this);
    }

    public final void dispose() {
        disposeEntity(this);
    }

    public final boolean isDisposed() {
        return entityIsDisposed(this);
    }

    public static void setSpriteFolder(FileHandle newSpriteFolder) {
        spriteFolder = newSpriteFolder;
        resetPixmaps();
    }

    public static void useDefaultSpriteFolder() {
        setSpriteFolder(defaultSpriteFolder);
    }

    public static void reallocateResources(RenderableCardEntity entity) {
        allEntities.add(entity);
        entity.isDisposed = false;
    }

    public static void disposeEntity(RenderableCardEntity entity) {
        entity.invalidateSprites();
        allEntities.remove(entity);
        entity.isDisposed = true;
    }

    public static void disposeAllEntities() {
        allEntities.forEach(RenderableCardEntity::dispose);
    }

    public static boolean entityIsDisposed(RenderableCardEntity entity) {
        return entity.isDisposed;
    }

    public static RenderableCardEntity[] getAllEntities() {
        return (RenderableCardEntity[])(allEntities.toArray());
    }

    private static void resetPixmaps() {
        resetBackPixmaps();
        resetFacePixmaps();
        disposeAllEntities();
    }

    private static void resetBackPixmaps() {
        backPixmap.dispose();
        backPixmap = null;
    }

    private static void resetFacePixmaps() {
        faceDesignPixmaps.values().forEach(Pixmap::dispose);
        faceDesignPixmaps.clear();
    }

    private static void loadBackPixmap() {
        Pixmap originalImagePixmap = new Pixmap(spriteFolder.child("back" + imageExtension));
        backPixmap = new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalImagePixmap.getFormat());
        backPixmap.drawPixmap(originalImagePixmap,
                0, 0, originalImagePixmap.getWidth(), originalImagePixmap.getHeight(),
                0, 0, backPixmap.getWidth(), backPixmap.getHeight());
        originalImagePixmap.dispose();
    }

    private static void loadFaceDesignPixmapForCard(int cardNum) {
        String cardImageName = (Card.isJoker(cardNum)) ? Rank.fromCardNum(cardNum).toString() + imageExtension
                : Rank.fromCardNum(cardNum).toString() + "_of_" + Suit.fromCardNum(cardNum).toString() + imageExtension;
        Pixmap originalImagePixmap =
                new Pixmap(spriteFolder.child(cardImageName));
        Pixmap resizedImagePixmap =
                new Pixmap(CARD_WIDTH_IN_PIXELS, CARD_HEIGHT_IN_PIXELS, originalImagePixmap.getFormat());

        resizedImagePixmap.drawPixmap(originalImagePixmap,
                0, 0, originalImagePixmap.getWidth(), originalImagePixmap.getHeight(),
                0, 0, resizedImagePixmap.getWidth(), resizedImagePixmap.getHeight());
        faceDesignPixmaps.put(cardNum, resizedImagePixmap);
        originalImagePixmap.dispose();
    }

    private static void roundPixmapCorners(Pixmap pixmap, int radius) {
        int pixmapHeight = pixmap.getHeight();
        int pixmapWidth = pixmap.getWidth();

        pixmap.setBlending(Pixmap.Blending.None);
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

        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(color);

        // Left border:
        pixmap.fillRectangle(
                0, radius, borderThickness, pixmapHeight - (2 * radius));
        // Right border:
        pixmap.fillRectangle(
                pixmapWidth - borderThickness, radius, borderThickness, pixmapHeight - (2 * radius));
        // Top border:
        pixmap.fillRectangle(
                radius, 0, pixmapWidth - (2 * radius), borderThickness);
        // Bottom border:
        pixmap.fillRectangle(
                radius, pixmapHeight - borderThickness, pixmapWidth - (2 * radius), borderThickness);

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
                            pixmap.drawPixel(x, y);

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
        if(faceDesignPixmaps.get(card.getCardNum()) == null) {
            loadFaceDesignPixmapForCard(card.getCardNum());
        }
        faceSprite = setupSpriteFromPixmap(
                faceDesignPixmaps.get(card.getCardNum()), getFaceBackgroundColor(),
                getFaceDesignWidthScale(), getFaceDesignHeightScale(),
                getFaceBorderThicknessInPixels(), getFaceBorderColor());
    }

    private void setupThisCardBackSprite() {
        if(backPixmap == null) {
            loadBackPixmap();
        }
        backSprite = setupSpriteFromPixmap(
                backPixmap, getBackBackgroundColor(),
                getBackDesignWidthScale(), getBackDesignHeightScale(),
                getBackBorderThicknessInPixels(), getBackBorderColor());
    }

    private Sprite setupSpriteFromPixmap(Pixmap designPixmap,
                                         Color backgroundColor,
                                         float designWidthScale, float designHeightScale,
                                         int borderThicknessInPixels, Color borderColor) {
        Pixmap spritePixmap = new Pixmap(designPixmap.getWidth(), designPixmap.getHeight(), designPixmap.getFormat());
        spritePixmap.setColor(backgroundColor);
        spritePixmap.fill();

        spritePixmap.setBlending(Pixmap.Blending.SourceOver);
        spritePixmap.drawPixmap(designPixmap,
                0, 0, designPixmap.getWidth(), designPixmap.getHeight(),
                (int)(0.5f * (CARD_WIDTH_IN_PIXELS - (CARD_WIDTH_IN_PIXELS * designWidthScale))),     // dstx
                (int)(0.5f * (CARD_HEIGHT_IN_PIXELS - (CARD_HEIGHT_IN_PIXELS * designHeightScale))),  // dsty
                (int)(CARD_WIDTH_IN_PIXELS * designWidthScale),                                       // dstWidth
                (int)(CARD_HEIGHT_IN_PIXELS * designHeightScale));                                    // dstHeight
        roundPixmapCorners(spritePixmap, getCornerRadiusInPixels());
        drawCurvedBorderOnPixmap(spritePixmap,
                getCornerRadiusInPixels(),
                borderThicknessInPixels,
                borderColor);

        Sprite sprite = new Sprite(new Texture(spritePixmap));
        sprite.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sprite.setSize(getWidth(), getHeight());
        spritePixmap.dispose();
        return sprite;
    }

    protected final void invalidateSprites() {
        if(faceSprite != null) {
            faceSprite.getTexture().dispose();
        }
        faceSprite = null;

        if(backSprite != null) {
            backSprite.getTexture().dispose();
        }
        backSprite = null;
    }

    public final void cardChanged() {
        cardChangedImpl();
        invalidateSprites();
    }

    protected void cardChangedImpl() {
    }

    // Render methods:
    public void render(SpriteBatch batch, Viewport viewport) {
        renderAt(batch, viewport,
                getDisplayX(), getDisplayY(), getDisplayWidth(), getDisplayHeight(),
                getOriginXProportion(), getOriginYProportion(), getDisplayRotation());
    }

    public void renderBase(SpriteBatch batch, Viewport viewport) {
        renderAt(batch, viewport,
                getX(),  getY(), getWidth(), getHeight(),
                getOriginXProportion(), getOriginYProportion(), getRotation());
    }

    public void renderAt(SpriteBatch batch, Viewport viewport,
                         float x, float y, float width, float height,
                         float originXProportion, float originYProportion, float rotation) {
        if(isDisposed()) {
            Gdx.app.log(toString(), "Attempted to render disposed card!");
            return;
        }

        if(isFaceUp()) {
            if(faceSprite == null) {
                setupThisCardFaceSprite();
            }
            renderFace(batch, viewport, x, y, width, height, originXProportion, originYProportion, rotation);
        } else {
            if(backSprite == null) {
                setupThisCardBackSprite();
            }
            renderBack(batch, viewport, x, y, width, height, originXProportion, originYProportion, rotation);
        }
    }

    private void renderFace(SpriteBatch batch, Viewport viewport,
                            float x, float y, float width, float height,
                            float originXProportion, float originYProportion, float rotation) {
        drawSprite(batch, viewport, faceSprite,
                x, y, width, height,
                originXProportion, originYProportion, rotation);
    }

    private void renderBack(SpriteBatch batch, Viewport viewport,
                            float x, float y, float width, float height,
                            float originXProportion, float originYProportion, float rotation) {
        drawSprite(batch, viewport, backSprite,
                x, y, width, height,
                originXProportion, originYProportion, rotation);
    }

    private static void drawSprite(SpriteBatch batch, Viewport viewport, Sprite sprite,
                                   float x, float y, float width, float height,
                                   float originXProportion, float originYProportion, float rotation) {
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
        sprite.setOrigin(originXProportion * width, originYProportion * height);
        sprite.setRotation(rotation);
        sprite.draw(batch);
    }
}
