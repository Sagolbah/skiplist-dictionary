package ru.ifmo.crypto.skiplist;

import java.util.List;

/**
 * Wrapper for skip list proofs
 *
 * @author Daniil Boger (Sagolbah)
 */
public class Proof {
    private final long timestamp;
    private final List<byte[]> sequence;

    public Proof(long timestamp, List<byte[]> sequence) {
        this.timestamp = timestamp;
        this.sequence = sequence;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<byte[]> getSequence() {
        return sequence;
    }
}
