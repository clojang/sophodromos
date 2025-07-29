package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class SimpleTest {

    @Test
    public void testAddition() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testSubtraction() {
        assertEquals(2, 4 - 2);
    }

    @Test
    public void testMultiplication() {
        assertEquals(8, 2 * 4);
    }
}