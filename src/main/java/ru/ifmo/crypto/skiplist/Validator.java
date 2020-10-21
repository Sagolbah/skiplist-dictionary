package ru.ifmo.crypto.skiplist;

public interface Validator {
    ValidationResult validate(final Proof proof, final Confirmation conf);
}
