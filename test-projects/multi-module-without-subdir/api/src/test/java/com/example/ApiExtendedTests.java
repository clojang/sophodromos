package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class ApiExtendedTests {

    @Test
    public void testApiDivision() {
        assertEquals(2, 8 / 4);
    }

    @Test
    public void testApiModulo() {
        assertEquals(1, 5 % 2);
    }
}