package ru.ifmo.crypto.skiplist;

import java.util.Calendar;
import java.util.List;

public class Confirmation {
    private final long timestamp;
    private final byte[] hash;

    public Confirmation(long timestamp, byte[] hash) {
        this.hash = hash;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getHash() {
        return hash;
    }
}
