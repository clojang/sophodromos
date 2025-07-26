package io.github.clojang.sophodromos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Test;

/**
 * Sample test class to demonstrate SophoDromos output formatting.
 */
public class SampleTest {

  @Test
  public void testSuccess() {
    assertTrue("This test should pass", true);
    assertEquals("Values should be equal", 4, 2 + 2);
  }

  @Test
  public void testFailure() {
    // Uncomment to see failure formatting
    // fail("This is a sample failure for testing output formatting");
  }

  @Test
  public void testError() {
    // Uncomment to see error formatting
    // throw new RuntimeException("This is a sample error for testing output formatting");
  }

  @Test
  public void testSkipped() {
    Assume.assumeTrue("Skip this test for demonstration", false);
  }
}