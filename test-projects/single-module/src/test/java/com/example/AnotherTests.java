package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnotherTests {

    @Test
    public void testDivision() {
        assertEquals(2, 8 / 4);
    }

    @Test
    public void testModulo() {
        assertEquals(1, 5 % 2);
    }
}