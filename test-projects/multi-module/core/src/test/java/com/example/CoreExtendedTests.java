package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class CoreExtendedTests {

    @Test
    public void testCoreDivision() {
        assertEquals(2, 8 / 4);
    }

    @Test
    public void testCoreModulo() {
        assertEquals(1, 5 % 2);
    }
}