package com.wikipediafinder.backend;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PageNodeTest {
  @Test
  public void testValidUrlConstruction() {
    PageNode node = new PageNode("https://en.wikipedia.org/wiki/Example");
    assertEquals("https://en.wikipedia.org/wiki/Example", node.getURL());
  }

  @Test
  public void testInvalidUrlThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> new PageNode("invalid-url"));
  }
}
