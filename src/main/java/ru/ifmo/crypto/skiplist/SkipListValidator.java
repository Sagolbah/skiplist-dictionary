package ru.ifmo.crypto.skiplist;

import java.util.Arrays;
import java.util.List;

/**
 * Class for {@link Proof} validating
 *
 * @author Daniil Boger (Sagolbah)
 */
public class SkipListValidator implements Validator {
    /**
     * Validates given {@link Proof}
     *
     * @param proof given proof
     * @param conf  {@link Confirmation} of skip list
     * @return {@link ValidationResult} instance. CORRECT if proof is correct, OUTDATED if proof is outdated,
     * WRONG otherwise
     */
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
