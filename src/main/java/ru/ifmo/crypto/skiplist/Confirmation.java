package ru.ifmo.crypto.skiplist;

import java.util.Calendar;
import java.util.List;

public class Confirmation {
    private final Calendar timestamp;
    private final byte[] hash;

    public Confirmation(byte[] hash) {
        this.hash = hash;
        timestamp = Calendar.getInstance();
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public byte[] getHash() {
        return hash;
    }
}
