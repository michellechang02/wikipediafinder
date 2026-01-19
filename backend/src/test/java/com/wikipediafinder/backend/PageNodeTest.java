package com.wikipediafinder.backend;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

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
