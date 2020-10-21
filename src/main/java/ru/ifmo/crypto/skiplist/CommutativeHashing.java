package ru.ifmo.crypto.skiplist;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class CommutativeHashing {
    private static MessageDigest sha256;

    static {
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Hashing algorithm not found");
        }
    }

    private static byte[] concat(final byte[] s1, final byte[] s2) {
        byte[] res = new byte[s1.length + s2.length];
        System.arraycopy(s1, 0, res, 0, s1.length);
        System.arraycopy(s2, 0, res, s1.length, s2.length);
        return res;
    }

    public static byte[] SHA256(final byte[] s1, final byte[] s2) {
        // SHA256(min(s1, s2) || max(s1, s2))
        int res = Arrays.compare(s1, s2);
        if (res > 0) {
            return sha256.digest(concat(s2, s1));
        }
        return sha256.digest(concat(s1, s2));
    }
}
