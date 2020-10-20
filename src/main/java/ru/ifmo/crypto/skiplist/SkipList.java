package ru.ifmo.crypto.skiplist;

/**
 * Interface for Skip List
 *
 * @author Daniil Boger (sagolbah@gmail.com)
 */
public interface SkipList<T> {
    boolean find(T key);

    void insert(T elem);

    void delete(T elem);
}
