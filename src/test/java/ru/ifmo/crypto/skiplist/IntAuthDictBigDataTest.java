package ru.ifmo.crypto.skiplist;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

/**
 * Test suite for IntSkipList
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntAuthDictBigDataTest {
    private final int elementsSize = 10000;
    private final int count = 20;
    private final OutputStyle outputStyle = OutputStyle.FULL_DESCRIPTION;

    private final Random rng = new Random();
    private final Validator validator = new SkipListValidator();

    @Test
    public void test_BigData() {
        for (int i = 1; i <= count; i++) {
            tests_BigData(i * 100000);
        }
    }

    @ParameterizedTest
    private void tests_BigData(int dataSize) {
        IntAuthDict list = new IntAuthDict(IntStream.range(0, dataSize).boxed().collect(Collectors.toList()));
        Set<Integer> elements = new HashSet<>();
        while (elements.size() < elementsSize) {
            elements.add(rng.nextInt() % dataSize);
        }

        long startTime;
        long endTime;
        long deleteTime;
        long insertTime;
        long validateTime;

        startTime = System.currentTimeMillis();
        for (int elem : elements) {
            list.delete(elem);
        }
        endTime = System.currentTimeMillis();
        deleteTime = (endTime - startTime);

        startTime = System.currentTimeMillis();
        for (int elem : elements) {
            list.insert(elem);
        }
        endTime = System.currentTimeMillis();
        insertTime = (endTime - startTime);

        Confirmation conf = list.getConfirmation();
        startTime = System.currentTimeMillis();
        for (int elem : elements) {
            Proof pr = list.makeProof(elem);
            assertEquals(ValidationResult.CORRECT, validator.validate(pr, conf));
        }
        endTime = System.currentTimeMillis();
        validateTime = (endTime - startTime);

        switch (outputStyle) {
            case FULL_DESCRIPTION:
                System.out.println("Time spent deleting / inserting / validating by " + elementsSize + " elements from a skip list of size " + dataSize);
                System.out.println("Delete: " + deleteTime + " ms");
                System.out.println("Insert: " + deleteTime + " ms");
                System.out.println("Validate: " + deleteTime + " ms");
                System.out.println();
                break;
            case SHORT_DESCRIPTION:
                System.out.println(elementsSize + " : " + dataSize);
                System.out.println("D/I/V: " + deleteTime + " / " + insertTime + " / " + validateTime);
                System.out.println();
                break;
            case MINIMALISM:
                System.out.println(dataSize / 100000 + (dataSize / 100000 >= 10 ? "" : " ") + ": "
                        + deleteTime + " / " + insertTime + " / " + validateTime);
                break;
            default:
                break;
        }

    }


    private <T> T getRandomElement(final Set<T> s) {
        return s.stream().skip(rng.nextInt(s.size())).findFirst().get();
    }

    private enum OutputStyle {
        FULL_DESCRIPTION,
        SHORT_DESCRIPTION,
        MINIMALISM,
        WITHOUT_OUTPUT
    }

}
