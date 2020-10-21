package ru.ifmo.crypto.skiplist;

public enum ValidationResult {
    CORRECT,
    OUTDATED,  // For outdated queries and proofs
    WRONG
}
