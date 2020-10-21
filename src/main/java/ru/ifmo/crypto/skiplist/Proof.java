package ru.ifmo.crypto.skiplist;

import java.util.Calendar;
import java.util.List;

/**
 * Wrapper for skip list proofs
 *
 * @author Daniil Boger (Sagolbah)
 */
public class Proof {
    private final Calendar timestamp;
    private final List<byte[]> sequence;

    public Proof(List<byte[]> sequence) {
        timestamp = Calendar.getInstance();
        this.sequence = sequence;
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public List<byte[]> getSequence() {
        return sequence;
    }
}
