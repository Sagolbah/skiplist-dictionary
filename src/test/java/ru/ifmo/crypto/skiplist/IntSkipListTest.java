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
        list.makeProof(5);
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

    // CRYPTO SECTION

    @Test
    public void test09_commutativeSha() {
        byte[] a1 = new byte[]{1, 2, 3, 4};
        byte[] a2 = new byte[]{5, 6, 7};
        byte[] a3 = new byte[]{8, 9};
        byte[] p1 = CommutativeHashing.SHA256(a1, a2);
        byte[] p2 = CommutativeHashing.SHA256(a2, a1);
        byte[] p3 = CommutativeHashing.SHA256(p2, a3);
        byte[] p4 = CommutativeHashing.SHA256(p1, a3);
        assertArrayEquals(p1, p2);
        assertArrayEquals(p3, p4);
    }

    @Test
    public void test10_commutativeShaEmpty() {
        byte[] a1 = new byte[]{};
        byte[] a2 = new byte[]{5, 6, 7};
        byte[] p1 = CommutativeHashing.SHA256(a1, a2);
        byte[] p2 = CommutativeHashing.SHA256(a2, a1);
        assertArrayEquals(p1, p2);
    }

    @Test
    public void test10_confirmInv() {
        list = new IntSkipList();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        Confirmation conf = list.getConfirmation();
        Proof pr = list.makeProof(5);
        SkipListValidator validator = new SkipListValidator();
        assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
    }

    @Test
    public void test11_hardConfirmation() {
        list = new IntSkipList();
        Set<Integer> arg = new HashSet<>();
        for (int i = 1; i <= 1000; i++) {
            if (rng.nextBoolean()) {
                arg.add(i);
                list.insert(i);
            }
        }
        SkipListValidator validator = new SkipListValidator();
        Confirmation conf = list.getConfirmation();
        for (Integer i : arg) {
            Proof pr = list.makeProof(i);
            assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
        }
    }

}
