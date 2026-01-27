package com.wikipediafinder.backend;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import org.junit.jupiter.api.Test;

public class BFSTest {
  // Minimal mock PageNode for fast tests
  static class MockPageNode extends PageNode {
    private final String url;
    private Map<String, PageNode> outLinks = new HashMap<>();

    public MockPageNode(String url) {
      super("https://en.wikipedia.org/wiki/" + url);
      this.url = "https://en.wikipedia.org/wiki/" + url;
    }

    public void setMockOutLinks(PageNode... nodes) {
      for (PageNode n : nodes) outLinks.put(n.getURL(), n);
    }

    @Override
    public void findOutgoingLinks() {}

    @Override
    public Set<String> getOutLinks() {
      return outLinks.keySet();
    }

    @Override
    public Set<PageNode> getOutNodes() {
      return new HashSet<>(outLinks.values());
    }

    @Override
    public String getURL() {
      return url;
    }

    @Override
    public void setOutLinks(Map<String, PageNode> outLinks) {
      this.outLinks = outLinks;
    }
  }

  @Test
  public void testPageNodeWithMockLinks() {
    MockPageNode start = new MockPageNode("Albert_Einstein");
    MockPageNode sanrio = new MockPageNode("Sanrio");
    MockPageNode helloKitty = new MockPageNode("Hello_Kitty");
    MockPageNode japaneseCulture = new MockPageNode("Japanese_culture");
    MockPageNode kawaii = new MockPageNode("Kawaii");
    start.setMockOutLinks(sanrio, helloKitty, japaneseCulture, kawaii);
    Set<String> links = start.getOutLinks();
    int i = 0;
    for (String e : links) {
      System.out.printf("Link %d: %s", i++, e);
    }
    assertTrue(links.contains("https://en.wikipedia.org/wiki/Sanrio"));
  }
}
