package com.wikipediafinder.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

public class BFSResultTest {
  @Test
  public void testConstructorAndGetters() {
    List<String> path = Arrays.asList("A", "B", "C");
    BFSResult result = new BFSResult(path, 3);
    assertEquals(path, result.getPath());
    assertEquals(3, result.getNodesExplored());
  }

  @Test
  public void testEmptyPath() {
    BFSResult result = new BFSResult(Collections.emptyList(), 0);
    assertTrue(result.getPath().isEmpty());
    assertEquals(0, result.getNodesExplored());
  }

  @Test
  public void testNullPath() {
    BFSResult result = new BFSResult(null, 5);
    assertNull(result.getPath());
    assertEquals(5, result.getNodesExplored());
  }

  @Test
  public void testImmutabilityOfPath() {
    List<String> path = new ArrayList<>(Arrays.asList("A", "B"));
    BFSResult result = new BFSResult(path, 2);
    path.add("C");
    assertEquals(Arrays.asList("A", "B"), result.getPath());
  }

  @Test
  public void testLargePath() {
    List<String> path = new ArrayList<>();
    for (int i = 0; i < 1000; i++) path.add("N" + i);
    BFSResult result = new BFSResult(path, 1000);
    assertEquals(1000, result.getPath().size());
    assertEquals(1000, result.getNodesExplored());
  }

  @Test
  public void testNegativeNodesExplored() {
    List<String> path = Arrays.asList("A");
    BFSResult result = new BFSResult(path, -1);
    assertEquals(-1, result.getNodesExplored());
  }
}
