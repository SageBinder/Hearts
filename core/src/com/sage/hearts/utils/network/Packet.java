package com.sage.hearts.utils.network;

import com.badlogic.gdx.utils.SerializationException;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Hashtable;

public abstract class Packet<T extends NetworkCode> implements Serializable {
    public T networkCode;
    public final Hashtable<Serializable, Serializable> data = new Hashtable<>();

    public Packet() {
    }

    public Packet(T networkCode) {
        this.networkCode = networkCode;
    }

    public byte[] toBytes() throws SerializationException {
        return SerializationUtils.serialize(this);
    }

    public static Packet fromBytes(byte[] bytes) throws SerializationException {
        try {
            return SerializationUtils.deserialize(bytes);
        } catch(ClassCastException | IllegalArgumentException e) {
            throw new SerializationException("Could not deserialize, caused by " + e.getClass().toString());
        }
    }
}
