package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class ApiTest {

    @Test
    public void testApiAddition() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testApiSubtraction() {
        assertEquals(2, 4 - 2);
    }

    @Test
    public void testApiMultiplication() {
        assertEquals(8, 2 * 4);
    }
}