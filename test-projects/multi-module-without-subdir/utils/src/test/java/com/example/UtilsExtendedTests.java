package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsExtendedTests {

    @Test
    public void testUtilsDivision() {
        assertEquals(2, 8 / 4);
    }

    @Test
    public void testUtilsModulo() {
        assertEquals(1, 5 % 2);
    }
}