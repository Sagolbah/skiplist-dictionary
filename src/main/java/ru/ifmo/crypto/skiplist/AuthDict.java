package ru.ifmo.crypto.skiplist;

/**
 * Interface for authenticated dictionaries
 *
 * @author Daniil Boger (Sagolbah)
 */
public interface AuthDict<T> {
    void insert(T elem);

    void delete(T elem);

    Proof makeProof(T elem);

    Confirmation getConfirmation();
}
