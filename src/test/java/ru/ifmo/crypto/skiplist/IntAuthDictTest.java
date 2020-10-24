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
    private final Random rng = new Random();
    private final Validator validator = new SkipListValidator();

    // Functionality section (no cryptography tests)

    @Test
    public void test01_empty() {
        IntAuthDict list = new IntAuthDict();
        assertFalse(list.find(666));
    }

    @Test
    public void test02_single() {
        IntAuthDict list = new IntAuthDict(List.of(5));
        assertTrue(list.find(5));
        assertFalse(list.find(666));
    }

    @Test
    public void test03_multiple() {
        IntAuthDict list = new IntAuthDict(List.of(16, 5, 2, 8));
        assertFalse(list.find(0));
        assertFalse(list.find(7));
        assertFalse(list.find(1000));
        assertTrue(list.find(2));
        assertTrue(list.find(8));
        assertTrue(list.find(16));
    }

    @Test
    public void test04_randomizedSearch() {
        Set<Integer> elements = new HashSet<>();
        for (int i = 1; i <= 10000; i++) {
            if (rng.nextBoolean()) {
                elements.add(i);
            }
        }
        IntAuthDict list = new IntAuthDict(new ArrayList<>(elements));
        for (int i = 1; i <= 10000; i++) {
            if (elements.contains(i)) {
                assertTrue(list.find(i));
            } else {
                assertFalse(list.find(i));
            }
        }
    }

    @Test
    public void test05_basicInsertion() {
        IntAuthDict list = new IntAuthDict();
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
        Set<Integer> elements = new HashSet<>();
        IntAuthDict list = new IntAuthDict(List.of(1));
        elements.add(1);
        for (int i = 2; i <= 10000; i++) {
            if (rng.nextBoolean()) {
                elements.add(i);
                list.insert(i);
            }
        }
        for (int i = 1; i <= 10000; i++) {
            if (elements.contains(i)) {
                assertTrue(list.find(i));
            } else {
                assertFalse(list.find(i));
            }
        }

    }

    @Test
    public void test07_basicDeletion() {
        IntAuthDict list = new IntAuthDict();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        assertTrue(list.find(3));

        list.delete(3);
        assertFalse(list.find(3));
    }

    @Test
    public void test08_allOperations() {
        IntAuthDict list = new IntAuthDict();
        Set<Integer> elements = new HashSet<>();
        int queries = 0;
        for (int i = 0; i < 20000; i++) {
            int arg = rng.nextInt(Integer.MAX_VALUE);
            int op = rng.nextInt(3);
            switch (op) {
                case 0:
                    assertEquals(list.find(arg), elements.contains(arg));
                    queries++;
                    break;
                case 1:
                    list.insert(arg);
                    elements.add(arg);
                    break;
                case 2:
                    if (elements.isEmpty()) {
                        continue;
                    }
                    list.delete(elements.iterator().next());
                    elements.remove(elements.iterator().next());
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
    public void test11_validation() {
        IntAuthDict list = new IntAuthDict();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        Confirmation conf = list.getConfirmation();
        Proof pr = list.makeProof(5);
        assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
    }

    @Test
    public void test12_hardValidation() {
        IntAuthDict list = new IntAuthDict();
        Set<Integer> elements = new HashSet<>();
        for (int i = 1; i <= 10000; i++) {
            int element = rng.nextInt();
            elements.add(element);
            list.insert(element);
        }
        Confirmation conf = list.getConfirmation();
        for (Integer i : elements) {
            Proof pr = list.makeProof(i);
            assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
        }
    }

    @Test
    public void test13_outdatedPostInsertValidation() {
        IntAuthDict list = new IntAuthDict();
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
    public void test14_outdatedPostDeleteValidation() {
        IntAuthDict list = new IntAuthDict();
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
    public void test15_emptyNonExist() {
        IntAuthDict list = new IntAuthDict();
        Proof pr = list.makeProof(1);
        Confirmation conf = list.getConfirmation();
        assertFalse(pr.isPresent());
        assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
    }

    @Test
    public void test16_simpleNonExist() {
        IntAuthDict list = new IntAuthDict();
        list.insert(5);
        list.insert(2);
        list.insert(3);
        list.insert(7);
        Confirmation conf = list.getConfirmation();
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(-1349), conf));
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(4), conf));
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(2), conf));
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(6), conf));
        assertEquals(ValidationResult.CORRECT, validator.validate(list.makeProof(0), conf));
    }

    @Test
    public void test17_postRandomOperationsValidation() {
        IntAuthDict list = new IntAuthDict();
        Set<Integer> elements = new HashSet<>();
        Proof pr;
        Confirmation conf = list.getConfirmation();
        for (int i = 0; i < 10000; i++) {
            int op = rng.nextInt(3);
            switch (op) {
                case 0:
                    if (elements.isEmpty()) {
                        continue;
                    }
                    int arg = getRandomElement(elements);
                    pr = list.makeProof(arg);
                    assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
                    break;
                case 1:
                    int newElem = rng.nextInt(Integer.MAX_VALUE);
                    list.insert(newElem);
                    elements.add(newElem);
                    pr = list.makeProof(newElem);
                    conf = list.getConfirmation();
                    assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
                    break;
                case 2:
                    if (elements.isEmpty()) {
                        continue;
                    }
                    int removedElem = getRandomElement(elements);
                    list.delete(removedElem);
                    elements.remove(removedElem);
                    pr = list.makeProof(removedElem);
                    conf = list.getConfirmation();
                    assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
                    break;
            }
        }
    }


    @Test
    public void test18_fakeProof() {
        IntAuthDict list = new IntAuthDict();
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

    private <T> T getRandomElement(final Set<T> s) {
        return s.stream().skip(rng.nextInt(s.size())).findFirst().get();
    }


}
