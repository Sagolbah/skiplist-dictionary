package ru.ifmo.crypto.skiplist;

import java.util.List;

/**
 * Wrapper for skip list proofs
 *
 * @author Daniil Boger (Sagolbah)
 */
public class Proof {
    private final long timestamp;
    private final boolean isPresent;
    private final int element;
    private final List<byte[]> sequence;

    public Proof(int element, long timestamp, List<byte[]> sequence, boolean isPresent) {
        this.timestamp = timestamp;
        this.sequence = sequence;
        this.isPresent = isPresent;
        this.element = element;
    }

    public int getElement() {
        return element;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<byte[]> getSequence() {
        return sequence;
    }
}
