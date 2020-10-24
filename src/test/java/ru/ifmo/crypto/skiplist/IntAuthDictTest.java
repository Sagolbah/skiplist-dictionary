package ru.ifmo.crypto.skiplist;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test suite for IntSkipList
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntAuthDictTest {
    private IntAuthDict list;
    private final Random rng = new Random();
    private final Validator validator = new SkipListValidator();

    // Functionality section (no cryptography tests)

    @Test
    public void test01_empty() {
        list = new IntAuthDict();
        assertFalse(list.find(666));
    }

    @Test
    public void test02_single() {
        list = new IntAuthDict(List.of(5));
        assertTrue(list.find(5));
        assertFalse(list.find(666));
    }

    @Test
    public void test03_multiple() {
        list = new IntAuthDict(List.of(16, 5, 2, 8));
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
        list = new IntAuthDict(new ArrayList<>(arg));
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
        list = new IntAuthDict();
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
        list = new IntAuthDict(List.of(1));
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
        list = new IntAuthDict();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        list.delete(3);
        assertFalse(list.find(3));
    }

    @Test
    public void test08_allOperations() {
        list = new IntAuthDict();
        Set<Integer> correct = new HashSet<>();
        int queries = 0;
        for (int i = 0; i < 20000; i++) {
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
                    if (correct.isEmpty()) {
                        continue;
                    }
                    list.delete(correct.iterator().next());
                    correct.remove(correct.iterator().next());
                    break;
            }
        }
        System.out.println("Total find() queries done: " + queries);
    }

    // Cryptography section

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
    public void test11_confirmInv() {
        list = new IntAuthDict();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        Confirmation conf = list.getConfirmation();
        Proof pr = list.makeProof(5);
        assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
    }

    @Test
    public void test12_hardConfirmation() {
        list = new IntAuthDict();
        Set<Integer> arg = new HashSet<>();
        for (int i = 1; i <= 3000; i++) {
            if (rng.nextBoolean()) {
                arg.add(i);
                list.insert(i);
            }
        }
        Confirmation conf = list.getConfirmation();
        for (Integer i : arg) {
            Proof pr = list.makeProof(i);
            assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
        }
    }

    @Test
    public void test13_fakeProof() {
        list = new IntAuthDict();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        Proof pr = list.makeProof(5);
        list = new IntAuthDict();
        list.insert(5);
        list.insert(1337);
        list.insert(1349);
        Confirmation conf = list.getConfirmation();
        assertEquals(ValidationResult.WRONG, validator.validate(pr, conf));
    }

    @Test
    public void test14_hashingInsertions() {
        list = new IntAuthDict();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        Proof pr = list.makeProof(5);
        Confirmation conf = list.getConfirmation();
        assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
        list.insert(4);
        conf = list.getConfirmation();
        Proof pr2 = list.makeProof(4);
        assertEquals(ValidationResult.CORRECT, validator.validate(pr2, conf));
        assertEquals(ValidationResult.OUTDATED, validator.validate(pr, conf));
    }

    @Test
    public void test15_hashingDeletions() {
        list = new IntAuthDict();
        list.insert(666);
        list.insert(19);
        list.insert(28);
        Proof pr = list.makeProof(28);
        Confirmation conf = list.getConfirmation();
        assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
        list.delete(666);
        conf = list.getConfirmation();
        Proof pr2 = list.makeProof(19);
        assertEquals(ValidationResult.CORRECT, validator.validate(pr2, conf));
        assertEquals(ValidationResult.OUTDATED, validator.validate(pr, conf));
    }


    @Test
    public void test16_allHashingOperations() {
        list = new IntAuthDict();
        Set<Integer> correct = new HashSet<>();
        Proof pr;
        Confirmation conf = list.getConfirmation();
        for (int i = 0; i < 10000; i++) {
            int op = rng.nextInt(4);
            switch (op) {
                case 0:
                    if (correct.isEmpty()) {
                        continue;
                    }
                    int arg = getRandomElement(correct);
                    pr = list.makeProof(arg);
                    assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
                    break;
                case 1:
                case 2:
                    int newElem = rng.nextInt(Integer.MAX_VALUE);
                    list.insert(newElem);
                    correct.add(newElem);
                    pr = list.makeProof(newElem);
                    conf = list.getConfirmation();
                    assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
                    break;
                case 3:
                    if (correct.isEmpty()) {
                        continue;
                    }
                    int removedElem = getRandomElement(correct);
                    list.delete(removedElem);
                    correct.remove(removedElem);
                    conf = list.getConfirmation();
                    break;
            }
        }
    }

    @Test
    public void test17_emptyNonexist() {
        list = new IntAuthDict();
        Proof pr = list.makeProof(1);
        Confirmation conf = list.getConfirmation();
        assertFalse(pr.isPresent());
        assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
    }

    @Test
    public void test18_simpleNonExist() {
        list = new IntAuthDict();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        list.insert(7);
        Confirmation conf = list.getConfirmation();
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(-1349), conf));
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(4), conf));
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(2), conf));
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(6), conf));
    }


    private <T> T getRandomElement(final Set<T> s) {
        return s.stream().skip(rng.nextInt(s.size())).findFirst().get();
    }


}
