package com.wikipediafinder.backend;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

public class BFSBasicTest {
  // Helper: Minimal PageNode mock for testing
  static class MockPageNode extends PageNode {
    private final String url;
    private Set<PageNode> outNodes;

    public MockPageNode(String url, Set<PageNode> outNodes) {
      super(
          url.startsWith("https://en.wikipedia.org/wiki/")
              ? url
              : "https://en.wikipedia.org/wiki/" + url);
      this.url =
          url.startsWith("https://en.wikipedia.org/wiki/")
              ? url
              : "https://en.wikipedia.org/wiki/" + url;
      this.outNodes = outNodes;
    }

    public void setOutNodes(Set<PageNode> outNodes) {
      this.outNodes = outNodes;
    }

    @Override
    public void findOutgoingLinks() {}

    @Override
    public Set<PageNode> getOutNodes() {
      return outNodes;
    }

    @Override
    public String getURL() {
      return url;
    }
  }

  @Test
  public void testClassInstantiation() {
    BFS bfs = new BFS();
    assertNotNull(bfs);
  }

  @Test
  public void testGetPathWithIdenticalNodes() {
    MockPageNode node = new MockPageNode("A", Collections.emptySet());
    List<String> path = new BFS().getPath(node, node, url -> node);
    assertNotNull(path);
    assertEquals(1, path.size());
    assertEquals("https://en.wikipedia.org/wiki/A", path.get(0));
  }

  @Test
  public void testGetPathWithStatsIdenticalNodes() {
    MockPageNode node = new MockPageNode("A", Collections.emptySet());
    BFSResult result = new BFS().getPathWithStats(node, node, url -> node);
    assertNotNull(result);
    assertEquals(1, result.getPath().size());
    assertEquals("https://en.wikipedia.org/wiki/A", result.getPath().get(0));
    assertTrue(result.getNodesExplored() >= 1);
  }

  @Test
  public void testGetPathNullInputs() {
    MockPageNode node = new MockPageNode("A", Collections.emptySet());
    assertThrows(IllegalArgumentException.class, () -> new BFS().getPath(null, node, url -> node));
    assertThrows(IllegalArgumentException.class, () -> new BFS().getPath(node, null, url -> node));
    assertThrows(
        IllegalArgumentException.class, () -> new BFS().getPathWithStats(null, node, url -> node));
    assertThrows(
        IllegalArgumentException.class, () -> new BFS().getPathWithStats(node, null, url -> node));
  }

  @Test
  public void testUnreachableNodeReturnsNullOrEmpty() {
    MockPageNode start = new MockPageNode("A", Collections.emptySet());
    MockPageNode end = new MockPageNode("B", Collections.emptySet());
    List<String> path =
        new BFS()
            .getPath(
                start,
                end,
                url -> {
                  if (url.endsWith("A")) return start;
                  if (url.endsWith("B")) return end;
                  return new MockPageNode(url, Collections.emptySet());
                });
    assertNull(path);
    BFSResult result =
        new BFS()
            .getPathWithStats(
                start,
                end,
                url -> {
                  if (url.endsWith("A")) return start;
                  if (url.endsWith("B")) return end;
                  return new MockPageNode(url, Collections.emptySet());
                });
    assertNotNull(result);
    assertTrue(result.getPath() == null || result.getPath().isEmpty());
  }

  @Test
  public void testSimplePath() {
    MockPageNode end = new MockPageNode("B", Collections.emptySet());
    Set<PageNode> out = new HashSet<>();
    out.add(end);
    MockPageNode start = new MockPageNode("A", out);
    List<String> path =
        new BFS()
            .getPath(
                start,
                end,
                url -> {
                  if (url.endsWith("A")) return start;
                  if (url.endsWith("B")) return end;
                  return new MockPageNode(url, Collections.emptySet());
                });
    assertNotNull(path);
    assertEquals(
        Arrays.asList("https://en.wikipedia.org/wiki/A", "https://en.wikipedia.org/wiki/B"), path);
    BFSResult result =
        new BFS()
            .getPathWithStats(
                start,
                end,
                url -> {
                  if (url.endsWith("A")) return start;
                  if (url.endsWith("B")) return end;
                  return new MockPageNode(url, Collections.emptySet());
                });
    assertNotNull(result);
    assertEquals(
        Arrays.asList("https://en.wikipedia.org/wiki/A", "https://en.wikipedia.org/wiki/B"),
        result.getPath());
    assertTrue(result.getNodesExplored() > 0);
  }

  @Test
  public void testNodeExplorationCount() {
    final int CHAIN_LENGTH = 30;
    Map<String, MockPageNode> nodeMap = new HashMap<>();
    for (int i = 0; i <= CHAIN_LENGTH; i++) {
      nodeMap.put(
          "https://en.wikipedia.org/wiki/N" + i, new MockPageNode("N" + i, new HashSet<>()));
    }
    for (int i = 0; i < CHAIN_LENGTH; i++) {
      Set<PageNode> out = new HashSet<>();
      out.add(nodeMap.get("https://en.wikipedia.org/wiki/N" + (i + 1)));
      nodeMap.get("https://en.wikipedia.org/wiki/N" + i).setOutNodes(out);
    }
    MockPageNode first = nodeMap.get("https://en.wikipedia.org/wiki/N0");
    MockPageNode end = nodeMap.get("https://en.wikipedia.org/wiki/N" + CHAIN_LENGTH);
    BFSResult result = new BFS().getPathWithStats(first, end, nodeMap::get);
    assertNotNull(result);
    assertEquals(CHAIN_LENGTH, result.getNodesExplored());
  }
}
