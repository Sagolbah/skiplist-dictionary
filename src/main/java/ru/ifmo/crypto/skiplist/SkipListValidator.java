package ru.ifmo.crypto.skiplist;

import java.util.Arrays;
import java.util.List;

public class SkipListValidator implements Validator {
    @Override
    public ValidationResult validate(Proof proof, Confirmation conf) {
        List<byte[]> seq = proof.getSequence();
        byte[] cur = CommutativeHashing.SHA256(seq.get(0), seq.get(1));
        for (int i = 2; i < seq.size(); i++) {
            cur = CommutativeHashing.SHA256(cur, seq.get(i));
        }
        cur = CommutativeHashing.SHA256(cur, new byte[]{});
        return Arrays.equals(cur, conf.getHash()) ? ValidationResult.CORRECT : ValidationResult.WRONG;
    }
}
