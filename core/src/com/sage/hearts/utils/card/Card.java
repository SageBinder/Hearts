package com.sage.hearts.utils.card;

import com.badlogic.gdx.utils.SerializationException;
import com.sage.hearts.utils.renderable.RenderableCard;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

@SuppressWarnings("WeakerAccess")
public class Card implements Serializable {
    // These are marked transient because they can be determined from cardNum, so we only need to serialize cardNum
    private transient Rank rank;
    private transient Suit suit;
    private int cardNum;

    private static Random r = new Random();

    public Card(Rank rank, Suit suit) throws InvalidCardException {
        this.rank = rank;
        this.suit = suit;
        cardNum = getCardNumFromRankAndSuit(rank, suit);
    }

    public Card(int cardNum) throws InvalidCardException {
        setCardNum(cardNum);
    }

    public Card(Card other) {
        this.rank = other.rank;
        this.suit = other.suit;
        this.cardNum = other.cardNum;
    }

    public Card() {
        this(r.nextInt(54));
    }

    public boolean isJoker() {
        return suit == Suit.JOKER;
    }

    public boolean isSameAs(Card other) {
        return this.cardNum == other.cardNum;
    }

    public int getCardNum() {
        return cardNum;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public void setCardNum(int cardNum) throws InvalidCardException {
        if(isValidCardNum(cardNum)) {
            this.cardNum = cardNum;
            suit = Suit.fromCardNum(cardNum);
            rank = Rank.fromCardNum(cardNum);
            cardChanged();
        } else {
            throw new InvalidCardException();
        }
    }

    public void setRank(Rank rank) {
        setCardNum(getCardNumFromRankAndSuit(rank, suit));
    }

    public void setSuit(Suit suit) {
        setCardNum(getCardNumFromRankAndSuit(rank, suit));
    }

    public void setRankAndSuit(Rank rank, Suit suit) {
        setCardNum(getCardNumFromRankAndSuit(rank, suit));
    }

    private void cardChanged() {
        cardChangedImpl();
        if(this instanceof RenderableCard) {
            var entity = ((RenderableCard)this).entity();
            if(entity != null) {
                entity.cardChanged();
            }
        }
    }


    protected void cardChangedImpl() {
    }

    public static int getCardNumFromRankAndSuit(Rank rank, Suit suit) throws InvalidCardException {
        if(suit == Suit.JOKER && !(rank == Rank.SMALL_JOKER || rank == Rank.BIG_JOKER)) {
            throw new InvalidCardException("suit was JOKER but rank was neither SMALL_JOKER nor BIG_JOKER");
        }
        return rank == Rank.BIG_JOKER ? 53
                : rank == Rank.SMALL_JOKER ? 52
                : ((rank.rankNum - 2) * 4) + suit.suitNum;
    }

    public static Rank getRankFromCardNum(int cardNum) throws InvalidCardException {
        if(isValidCardNum(cardNum)) {
            return cardNum == 53 ? Rank.BIG_JOKER
                    : cardNum == 52 ? Rank.SMALL_JOKER
                    : Rank.values()[cardNum / 4];
        } else {
            throw new InvalidCardException();
        }
    }

    public static Suit getSuitFromCardNum(int cardNum) throws InvalidCardException {
        if(isValidCardNum(cardNum)) {
            return cardNum >= 52 ? Suit.JOKER : Suit.values()[cardNum % 4];
        } else {
            throw new InvalidCardException();
        }
    }

    public static boolean isJoker(int cardNum) {
        return cardNum == 52 || cardNum == 53;
    }

    public static boolean isValidCardNum(int cardNum) {
        return cardNum <= 53 && cardNum >= 0;
    }

    @Override
    public String toString() {
        return (isJoker()) ? rank.stringName.replace("_", " ")
                : rank.stringName + " of " + suit.stringName;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeInt(cardNum);
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            setCardNum(ois.readInt());
        } catch(InvalidCardException e) {
            throw new SerializationException("Encountered InvalidCardException");
        }
    }
}
