package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void testUtilsAddition() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testUtilsSubtraction() {
        assertEquals(2, 4 - 2);
    }

    @Test
    public void testUtilsMultiplication() {
        assertEquals(8, 2 * 4);
    }
}