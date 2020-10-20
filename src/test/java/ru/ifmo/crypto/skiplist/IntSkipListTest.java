package ru.ifmo.crypto.skiplist;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntSkipListTest {
    private IntSkipList list;
    private final Random rng = new Random();

    @Test
    public void test01_empty() {
        list = new IntSkipList();
        assertFalse(list.find(666));
    }

    @Test
    public void test02_single() {
        list = new IntSkipList(List.of(5));
        assertTrue(list.find(5));
        assertFalse(list.find(666));
    }

    @Test
    public void test03_multiple() {
        list = new IntSkipList(List.of(16, 5, 2, 8));
        assertFalse(list.find(0));
        assertFalse(list.find(7));
        assertFalse(list.find(1000));
        assertTrue(list.find(2));
        assertTrue(list.find(8));
        assertTrue(list.find(16));
    }

    @Test
    public void test04_searchRandomized() {
        Set<Integer> arg = new HashSet<>();
        for (int i = 1; i <= 10000; i++) {
            if (rng.nextBoolean()) {
                arg.add(i);
            }
        }
        list = new IntSkipList(new ArrayList<>(arg));
        for (int i = 1; i <= 10000; i++) {
            if (arg.contains(i)) {
                assertTrue(list.find(i));
            } else {
                assertFalse(list.find(i));
            }
        }
    }

    @Test
    public void test05_basicInsertion() {
        list = new IntSkipList();
        list.insert(2);
        list.insert(3);
        list.insert(5);
        assertTrue(list.find(2));
        assertTrue(list.find(3));
        assertTrue(list.find(5));
        assertFalse(list.find(4));
    }

    @Test
    public void test06_randomizedInsertion() {
        Set<Integer> arg = new HashSet<>();
        list = new IntSkipList(List.of(1));
        arg.add(1);
        for (int i = 2; i <= 10000; i++) {
            if (rng.nextBoolean()) {
                arg.add(i);
                list.insert(i);
            }
        }
        for (int i = 1; i <= 10000; i++) {
            if (arg.contains(i)) {
                assertTrue(list.find(i));
            } else {
                assertFalse(list.find(i));
            }
        }
    }

    @Test
    public void test07_basicDeletion() {
        list = new IntSkipList();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        list.delete(3);
        assertFalse(list.find(3));
    }

    @Test
    public void test08_allOperations() {
        list = new IntSkipList();
        Set<Integer> correct = new HashSet<>();
        int queries = 0;
        for (int i = 0; i < 100000; i++) {
            int arg = rng.nextInt(Integer.MAX_VALUE);
            int op = rng.nextInt(3);
            switch (op) {
                case 0:
                    assertEquals(list.find(arg), correct.contains(arg));
                    queries++;
                    break;
                case 1:
                    list.insert(arg);
                    correct.add(arg);
                    break;
                case 2:
                    list.delete(arg);
                    correct.remove(arg);
                    break;
            }
        }
        System.out.println("Total find() queries done: " + queries);
    }

}
