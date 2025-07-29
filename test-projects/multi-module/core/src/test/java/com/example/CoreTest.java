package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreTest {

    @Test
    public void testCoreAddition() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testCoreSubtraction() {
        assertEquals(2, 4 - 2);
    }

    @Test
    public void testCoreMultiplication() {
        assertEquals(8, 2 * 4);
    }
}