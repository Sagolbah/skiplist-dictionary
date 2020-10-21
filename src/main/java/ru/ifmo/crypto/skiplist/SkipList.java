package ru.ifmo.crypto.skiplist;

/**
 * Interface for Skip List
 *
 * @author Daniil Boger (Sagolbah)
 */
public interface SkipList<T> {
    boolean find(T key);

    void insert(T elem);

    void delete(T elem);
}
